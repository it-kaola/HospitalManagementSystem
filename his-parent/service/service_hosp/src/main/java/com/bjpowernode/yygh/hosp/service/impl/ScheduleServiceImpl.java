package com.bjpowernode.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bjpowernode.yygh.common.exception.YyghException;
import com.bjpowernode.yygh.common.result.ResultCodeEnum;
import com.bjpowernode.yygh.hosp.repository.ScheduleRepository;
import com.bjpowernode.yygh.hosp.service.DepartmentService;
import com.bjpowernode.yygh.hosp.service.HospitalService;
import com.bjpowernode.yygh.hosp.service.ScheduleService;
import com.bjpowernode.yygh.model.hosp.BookingRule;
import com.bjpowernode.yygh.model.hosp.Department;
import com.bjpowernode.yygh.model.hosp.Hospital;
import com.bjpowernode.yygh.model.hosp.Schedule;
import com.bjpowernode.yygh.vo.hosp.BookingScheduleRuleVo;
import com.bjpowernode.yygh.vo.hosp.ScheduleOrderVo;
import com.bjpowernode.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private MongoTemplate mongoTemplate;


    // 根据日期获取对应的星期数
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }


    private IPage getDateList(Integer page, Integer limit, BookingRule bookingRule) {
        // 获取当天放号的时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime()); // 为什么是放号时间???
        // 获取预约周期
        Integer cycle = bookingRule.getCycle();
        // 如果当天的放号时间已经过去了，因为预约周期不变，所以要往后推迟一天
        if(releaseTime.isBeforeNow()){
            cycle += 1;
        }
        // 获取可预约的所有日期
        List<Date> dateList = new ArrayList<>();
        for(int i=0; i<cycle; i++){
            DateTime time = new DateTime().plusDays(i); // 根据预约周期的天数，往后延长相应的天数
            String timeStr = time.toString("yyyy-MM-dd");
            dateList.add(new DateTime(timeStr).toDate());
        }
        // 每页显示七天的数据
        List<Date> pageDateList = new ArrayList<>();
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;

        if(end > dateList.size()){
            // 如果显示的数据小于7，直接显示
            end = dateList.size();
        }
        for(int i=start; i<end; i++){
            pageDateList.add(dateList.get(i));
        }
        // 如果显示的数据大于7，进行分页
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7, dateList.size());
        iPage.setRecords(pageDateList);

        return iPage;

    }



    // 上传排班信息
    @Override
    public void saveSchedule(Map<String, Object> paramMap) {

        // 将paramMap转换成Department对象，方便后面操作
        String jsonString = JSONObject.toJSONString(paramMap); // 将paramMap转成json格式的字符串
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);

        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());

        if(scheduleExist != null){
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }else{
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }

    // 分页查询排班列表
    @Override
    public Page<Schedule> selectPageSchedule(int pageNo, int pageSize, ScheduleQueryVo scheduleQueryVo) {

        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setStatus(1); // 排班状态
        schedule.setIsDeleted(0);

        // 创建Pageable对象，设置当前页与每页显示的记录数
        Pageable pageable = PageRequest.of(pageNo-1, pageSize); // 注意数字0为第一页

        // 创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Example<Schedule> example = Example.of(schedule, matcher);


        Page<Schedule> page = scheduleRepository.findAll(example, pageable);

        return page;
    }



    // 删除排班信息
    @Override
    public void deleteSchedule(String hoscode, String hosScheduleId) {
        // 根据医院编号与排班编号查询科室信息
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        if(schedule != null){
            // 如果排班信息存在则进行删除操作
            scheduleRepository.deleteById(schedule.getId());
        }

    }


    // 根据医院编号(hoscode)和科室编号(depcode)，查询排班规则数据
    @Override
    public Map<String, Object> getScheduleRule(Long page, Long limit, String hoscode, String depcode) {

        // 1 根据医院编号和科室编号查询对应的排班信息
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        // 2 根据工作日workDate进行分组
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                                                             Aggregation.group("workDate")
                                                                        .first("workDate").as("workDate")
                                                                        .count().as("docCount")
                                                                        .sum("reservedNumber").as("reservedNumber")
                                                                        .sum("availableNumber").as("availableNumber"),
                                                             // 排序
                                                             Aggregation.sort(Sort.Direction.ASC, "workDate"),
                                                             // 实现分页
                                                             Aggregation.skip((page-1)*limit),
                                                             Aggregation.limit(limit)
                                                             );
        AggregationResults<BookingScheduleRuleVo> aggregateResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregateResult.getMappedResults();

        // 分组查询后总的记录数
        Aggregation totalAggregation = Aggregation.newAggregation(Aggregation.match(criteria), Aggregation.group("workDate"));
        AggregationResults<BookingScheduleRuleVo> totalAggregateResult = mongoTemplate.aggregate(totalAggregation, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalAggregateResult.getMappedResults().size();

        // 把日期转成对应的星期数
        for(BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        // 创建一个map存放其他数据信息
        Map<String, String> baseMap = new HashMap<>();
        // 获取医院名称
        String hosname = hospitalService.gethospitalNameByHoscode(hoscode);
        baseMap.put("hosname", hosname);

        // 将上面获取的所有数据封装到一个Map中
        Map<String, Object> result = new HashMap<>();
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        result.put("total", total);
        result.put("baseMap", baseMap);


        return result;
    }


    // 根据医院编号，科室编号和工作日期，查询排班的详细信息
    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        // 根据hoscode、depcode、workDate查询出schedule列表
        List<Schedule> scheduleList = scheduleRepository.getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        // 遍历list结合，添加每一个排班的其他信息，包括医院名称，科室名称，星期数，把它们封装在Schedule的param属性中
        for(Schedule schedule : scheduleList){
            // 设置医院名称
            schedule.getParam().put("hosname", hospitalService.gethospitalNameByHoscode(schedule.getHoscode()));
            // 设置科室名称
            schedule.getParam().put("depname", departmentService.getDepartmentNameByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()));
            // 设置星期数
            schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        }

        return scheduleList;
    }


    // 获取可预约排班数据
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {

        Map<String, Object> result = new HashMap<>();

        // 根据hoscode获取hospital对象，再根据hospital对象获取bookingRule
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        // 获取可预约的时间数据（分页显示）
        IPage<Date> iPage = this.getDateList(page, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();


        // 1 根据医院编号和科室编号查询对应的排班信息
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        // 2 根据工作日workDate进行分组
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
                        Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"));
        AggregationResults<BookingScheduleRuleVo> aggregateResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregateResult.getMappedResults();

        // 使用map集合封装数据，key为对应的日期，value为BookingScheduleRule对象
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for(int i=0; i<dateList.size(); i++){
            Date date = dateList.get(i);
            // 根据获取的date，查询map中对应的BookingScheduleRuleVo对象
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if(bookingScheduleRuleVo == null){
                // 程序执行到此处表示当天没有排班内容
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            // 获取当前日期对应的星期数
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if(i == dateList.size()-1 && page == iPage.getPages()) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if(i == 0 && page == 1) {
                DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(stopTime.isBeforeNow()) {
                    //停止预约
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.gethospitalNameByHoscode(hoscode));
        //科室
        Department department =departmentService.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    // 根据排班id获取排班信息
    @Override
    public Schedule getScheduleByScheduleId(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        // 设置医院名称
        schedule.getParam().put("hosname", hospitalService.gethospitalNameByHoscode(schedule.getHoscode()));
        // 设置科室名称
        schedule.getParam().put("depname", departmentService.getDepartmentNameByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()));
        // 设置星期数
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }


    // 根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {

        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        // 根据scheduleId获取排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if(schedule == null){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        // 获取预约规则信息
        Hospital hospital = hospitalService.getHospitalByHoscode(schedule.getHoscode());
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule == null){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }

        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.gethospitalNameByHoscode(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepartmentNameByHoscodeAndDepcode(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());



        return scheduleOrderVo;
    }


    // 更新排班信息，用于MQ
    @Override
    public void updateSchedule(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }


}

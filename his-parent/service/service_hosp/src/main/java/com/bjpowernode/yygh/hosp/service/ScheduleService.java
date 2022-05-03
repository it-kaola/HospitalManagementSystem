package com.bjpowernode.yygh.hosp.service;

import com.bjpowernode.yygh.model.hosp.Schedule;
import com.bjpowernode.yygh.vo.hosp.ScheduleOrderVo;
import com.bjpowernode.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {

    // 上传排班信息
    void saveSchedule(Map<String, Object> paramMap);

    // 分页查询排班列表
    Page<Schedule> selectPageSchedule(int pageNo, int pageSize, ScheduleQueryVo scheduleQueryVo);

    // 删除排班信息
    void deleteSchedule(String hoscode, String hosScheduleId);

    // 根据医院编号(hoscode)和科室编号(depcode)，查询排班规则数据
    Map<String, Object> getScheduleRule(Long page, Long limit, String hoscode, String depcode);

    // 根据医院编号，科室编号和工作日期，查询排班的详细信息
    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    // 获取可预约排班数据
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    // 根据排班id获取排班信息
    Schedule getScheduleByScheduleId(String scheduleId);

    // 根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    // 更新排班信息，用于MQ
    void updateSchedule(Schedule schedule);

}

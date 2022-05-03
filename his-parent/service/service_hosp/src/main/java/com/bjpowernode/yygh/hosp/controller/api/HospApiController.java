package com.bjpowernode.yygh.hosp.controller.api;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.hosp.service.DepartmentService;
import com.bjpowernode.yygh.hosp.service.HospitalService;
import com.bjpowernode.yygh.hosp.service.HospitalSetService;
import com.bjpowernode.yygh.hosp.service.ScheduleService;
import com.bjpowernode.yygh.model.hosp.Hospital;
import com.bjpowernode.yygh.model.hosp.Schedule;
import com.bjpowernode.yygh.vo.hosp.DepartmentVo;
import com.bjpowernode.yygh.vo.hosp.HospitalQueryVo;
import com.bjpowernode.yygh.vo.hosp.ScheduleOrderVo;
import com.bjpowernode.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "用户平台的相关接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospApiController {

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private HospitalSetService hospitalSetService;

    @ApiOperation("查询医院列表（分页+条件查询）")
    @GetMapping("/getHospitalList/{pageNo}/{limit}")
    public Result getHospitalList(@PathVariable("pageNo") Integer pageNo, @PathVariable("limit") Integer limit, HospitalQueryVo hospitalQueryVo){
        Page<Hospital> page = hospitalService.selectHospPage(pageNo, limit, hospitalQueryVo);
        return Result.ok(page);
    }

    @ApiOperation("根据医院名称查询医院（包括模糊查询）")
    @GetMapping("/getHospitalByHosname/{hosname}")
    public Result getHospitalByHosname(@PathVariable("hosname") String hosname){
        List<Hospital> hospitalList = hospitalService.getHospitalByHosname(hosname);
        return Result.ok(hospitalList);
    }

    @ApiOperation("根据医院编号获取所有科室信息")
    @GetMapping("/getDepartmentByHoscode/{hoscode}")
    public Result getDepartmentByHoscode(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> departmentVoList = departmentService.getDeptTreeStructure(hoscode);
        return Result.ok(departmentVoList);
    }


    @ApiOperation("根据医院编号获取预约挂号的详细信息")
    @GetMapping("/getHospAppointmentDetail/{hoscode}")
    public Result getHospAppointmentDetail(@PathVariable("hoscode") String hoscode){
        Map<String, Object> map = hospitalService.getHospAppointmentDetail(hoscode);
        return Result.ok(map);
    }


    @ApiOperation("获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingSchedule(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Integer page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Integer limit,
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode) {
        return Result.ok(scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode));
    }

    @ApiOperation("获取排班的详细信息")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(
            @ApiParam(name = "hoscode", value = "医院code", required = true)
            @PathVariable String hoscode,
            @ApiParam(name = "depcode", value = "科室code", required = true)
            @PathVariable String depcode,
            @ApiParam(name = "workDate", value = "排班日期", required = true)
            @PathVariable String workDate) {
        return Result.ok(scheduleService.getScheduleDetail(hoscode, depcode, workDate));
    }


    @ApiOperation("根据排班id获取排班信息")
    @GetMapping("getScheduleByScheduleId/{scheduleId}")
    public Result getScheduleByScheduleId(@PathVariable("scheduleId") String scheduleId){
        Schedule schedule = scheduleService.getScheduleByScheduleId(scheduleId);
        return Result.ok(schedule);
    }


    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(
            @ApiParam(name = "scheduleId", value = "排班id", required = true)
            @PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }


    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@ApiParam(name = "hoscode", value = "医院code", required = true) @PathVariable("hoscode") String hoscode) {
        return hospitalSetService.getSignInfoVo(hoscode);
    }




}

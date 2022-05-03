package com.bjpowernode.yygh.hosp.controller;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.hosp.service.ScheduleService;
import com.bjpowernode.yygh.model.hosp.Schedule;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Api(tags = "排班相关接口")
@RestController
@RequestMapping("/admin/hosp/schedule")
// @CrossOrigin
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @ApiOperation("根据医院编号(hoscode)和科室编号(depcode)，查询排班规则数据")
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable("page") Long page, @PathVariable("limit") Long limit,
                                  @PathVariable("hoscode") String hoscode, @PathVariable("depcode") String depcode){
        Map<String, Object> map = scheduleService.getScheduleRule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    @ApiOperation("根据医院编号，科室编号和工作日期，查询排班的详细信息")
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetail(@PathVariable("hoscode") String hoscode, @PathVariable("depcode") String depcode, @PathVariable("workDate") String workDate){
        List<Schedule> scheduleList = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return Result.ok(scheduleList);
    }

}

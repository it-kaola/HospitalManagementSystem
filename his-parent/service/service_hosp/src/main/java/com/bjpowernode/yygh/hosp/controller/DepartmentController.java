package com.bjpowernode.yygh.hosp.controller;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.hosp.service.DepartmentService;
import com.bjpowernode.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags = "科室相关接口")
@RestController
@RequestMapping("/admin/hosp/department")
// @CrossOrigin
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @ApiOperation("根据医院编号（hoscode）查询所有科室信息")
    @GetMapping("/getDeptTreeStructure/{hoscode}")
    public Result getDeptTreeStructure(@PathVariable("hoscode") String hoscode){
        List<DepartmentVo> list = departmentService.getDeptTreeStructure(hoscode);
        return Result.ok(list);
    }

}

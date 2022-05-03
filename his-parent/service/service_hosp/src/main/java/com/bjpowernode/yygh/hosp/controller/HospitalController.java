package com.bjpowernode.yygh.hosp.controller;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.hosp.service.HospitalService;
import com.bjpowernode.yygh.model.hosp.Hospital;
import com.bjpowernode.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/hospital")
// @CrossOrigin
public class HospitalController {

    @Resource
    HospitalService hospitalService;

    @ApiOperation("查询医院列表（条件查询+分页）")
    @GetMapping("/list/{pageNo}/{limit}")
    public Result getHospitalList(@PathVariable("pageNo") Integer pageNo, @PathVariable("limit") Integer limit, HospitalQueryVo hospitalQueryVo){

        Page<Hospital> page = hospitalService.selectHospPage(pageNo, limit, hospitalQueryVo);

        return Result.ok(page);
    }

    @ApiOperation("更新医院上线状态")
    @GetMapping("/updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable("id") String id, @PathVariable("status") Integer status){
        hospitalService.updateHospStatus(id, status);
        return Result.ok();
    }

    @ApiOperation("获取医院详细信息")
    @GetMapping("/getHospitalDetail/{id}")
    public Result getHospitalDetail(@PathVariable("id") String id){
        Map<String, Object> map = hospitalService.getHospitalDetail(id);
        return Result.ok(map);
    }

}

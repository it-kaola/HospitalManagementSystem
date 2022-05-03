package com.bjpowernode.yygh.hosp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.common.utils.MD5;
import com.bjpowernode.yygh.hosp.service.HospitalSetService;
import com.bjpowernode.yygh.model.hosp.HospitalSet;
import com.bjpowernode.yygh.vo.hosp.HospitalSetQueryVo;
import com.mysql.jdbc.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
// @CrossOrigin // 这个注解是为了解决跨域不能访问的问题
public class HospitalSetController {

    @Resource
    HospitalSetService hospitalSetService;

    // 查询hospital_set表中的所有数据
    @ApiOperation("获取所有医院设置信息")
    @GetMapping("/findAll")
    public Result findAll(){
        List<HospitalSet> list = hospitalSetService.list(); // 该list()方法是由ServiceImpl这个类提供的，功能是查询表格中的所有数据
        return Result.ok(list);
    }


    // 根据id进行逻辑删除
    @ApiOperation("根据id进行医院设置信息的逻辑删除")
    @DeleteMapping("/deleteHospSetById/{id}")
    public Result removeRecordById(@PathVariable("id") Integer id){
        boolean flag = hospitalSetService.removeById(id); //  该removeById()方法是由ServiceImpl这个类提供的，功能是根据id删除记录，返回boolean值
        if(flag){
            return Result.ok(flag);
        }else{
            return Result.fail();
        }
    }


    @ApiOperation("条件查询医院设置信息，带分页功能")
    @PostMapping("/findPageHospitalSet/{current}/{limit}")
    public Result findPageHospitalSet(@PathVariable("current") @ApiParam("当前页") Long current, @PathVariable("limit") @ApiParam("每页显示的记录数") Long limit,
                                      // @RequestBody注解表示，该参数信息放在请求体中，以json格式发送，required=false表示该参数非必要参数，可有可无。由于参数是放在请求体中，所以必须是post请求
                                      @RequestBody(required = false) @ApiParam("医院设置信息的VO对象") HospitalSetQueryVo hospitalSetQueryVo) {
        Page<HospitalSet> page = new Page(current, limit);
        QueryWrapper queryWrapper = new QueryWrapper();

        String hosname = hospitalSetQueryVo.getHosname(); // 医院名称
        String hoscode = hospitalSetQueryVo.getHoscode(); // 医院编号

        if(! StringUtils.isNullOrEmpty(hosname)){
            queryWrapper.like("hosname", hosname); // 模糊查询医院名称
        }
        if(! StringUtils.isNullOrEmpty(hoscode)) {
            queryWrapper.eq("hoscode", hoscode);
        }

        // 调用方法实现分页条件查询
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, queryWrapper); // 这个page()方法是由ServiceImpl这个类提供的

        return Result.ok(hospitalSetPage);

    }

    @ApiOperation("添加医院设置信息")
    @PostMapping("/saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){

        // 设置状态，1表示可用，0表示不可用
        hospitalSet.setStatus(1);

        // 设置秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000))); // 根据一定的规则生成一个秘钥

        boolean flag = hospitalSetService.save(hospitalSet); // 这个save()方法是由ServiceImpl这个类提供的，作用是向数据库中添加相应的记录，返回一个布尔值

        if(flag){return Result.ok();}

        return Result.fail();

    }


    @ApiOperation("根据id获取医院设置信息")
    @GetMapping("/getHospitalSetById/{id}")
    public Result getHospitalSetById(@PathVariable("id") Long id){

        // 手动创造一个异常，用于测试
        /*try{
            int age = 10/0;
        }catch (Exception e){
            throw new YyghException("发生异常请检查！", 900);
        }*/

        HospitalSet hospitalSet = hospitalSetService.getById(id); // 这个getById()方法是由ServiceImpl这个类提供的，作用是根据id查询对应的记录
        if(hospitalSet != null){
            return Result.ok(hospitalSet);
        }else{
            return Result.fail();
        }
    }


    @ApiOperation("修改医院设置信息")
    @PostMapping("/updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet){

        boolean flag = hospitalSetService.updateById(hospitalSet); // 这个updateById()方法是由ServiceImpl这个类提供的，作用是根据传入对象的id属性进行更新操作，返回一个布尔值

        if(flag){return Result.ok();}

        return Result.fail();
    }


    @ApiOperation("批量删除医院设置信息")
    @DeleteMapping("/BanchRemoveHospitalSet")
    public Result BanchRemoveHospitalSet(@RequestBody List<Long> ids){

        boolean flag = hospitalSetService.removeByIds(ids); // // 这个removeByIds()方法是由ServiceImpl这个类提供的，作用是根据传入对象的id列表进行批量删除操作，返回一个布尔值

        if(flag){return Result.ok();}

        return Result.fail();
    }


    @ApiOperation("医院设置信息的锁定与解锁") // 即修改对应的status属性的值
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable("id") Long id, @PathVariable("status") Integer status){
        // 先根据id获取对应的医院设置信息
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        // 修改医院设置信息中的status属性
        hospitalSet.setStatus(status);
        // 更新医院设置信息
        hospitalSetService.updateById(hospitalSet);
        return Result.ok();
    }


    @ApiOperation("发送签名秘钥") // 后面添加短信功能时需要用到
    @PutMapping("/sendSignKey/{id}")
    public Result sendSignKey(@PathVariable("id") Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        // 后面用到短信发送功能时，继续完善代码
        return Result.ok();
    }


}

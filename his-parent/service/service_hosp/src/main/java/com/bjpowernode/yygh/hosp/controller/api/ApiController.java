package com.bjpowernode.yygh.hosp.controller.api;

import com.bjpowernode.yygh.common.exception.YyghException;
import com.bjpowernode.yygh.common.helper.HttpRequestHelper;
import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.common.result.ResultCodeEnum;
import com.bjpowernode.yygh.common.utils.MD5;
import com.bjpowernode.yygh.hosp.service.DepartmentService;
import com.bjpowernode.yygh.hosp.service.HospitalService;
import com.bjpowernode.yygh.hosp.service.HospitalSetService;
import com.bjpowernode.yygh.hosp.service.ScheduleService;
import com.bjpowernode.yygh.model.hosp.Department;
import com.bjpowernode.yygh.model.hosp.Hospital;
import com.bjpowernode.yygh.model.hosp.Schedule;
import com.bjpowernode.yygh.vo.hosp.DepartmentQueryVo;
import com.bjpowernode.yygh.vo.hosp.ScheduleQueryVo;
import com.mysql.jdbc.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Api(tags = "为各自的医院系统提供统一的上传 查询等功能的接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Resource
    HospitalService hospitalService;

    @Resource
    HospitalSetService hospitalSetService;

    @Resource
    DepartmentService departmentService;

    @Resource
    ScheduleService scheduleService;


    /*---------------------------医院信息-------------------------------*/

    @ApiOperation("上传医院信息接口")
    @PostMapping("/saveHospital")
    public Result saveHosp(HttpServletRequest request){
        // 获取医院系统传递过来的信息
        Map<String, String[]> requestMap = request.getParameterMap(); // key对应的value都存放在String数组的第一个元素中
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap); // HttpRequestHelper.switchMap() 方法将 Map<String, String[]>转换成Map<String, Object>，方便后续操作

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");

        // 从各自医院系统中传来的hoscode
        String hoscode = (String) paramMap.get("hoscode");

        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);

        String signKeyMD5 = MD5.encrypt(signKey);

        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 传输过程，logoData的值中的"+"被转换成了" "，因此需要转换回来，不然无法正常显示图标
        String logoData = (String) paramMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        paramMap.put("logoData", logoData);

        // 调用service方法
        hospitalService.save(paramMap);
        return Result.ok();

    }


    @ApiOperation("查询医院信息接口")
    @PostMapping("/hospital/show")
    public Result getHospital(HttpServletRequest request){

        // 获取传递过来的信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 从各自医院系统中传来的签名
        String hospSign = (String) paramMap.get("sign");

        // 从各自医院系统中传来的hoscode
        String hoscode = (String) paramMap.get("hoscode");

        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);

        String signKeyMD5 = MD5.encrypt(signKey);

        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        // 调用service中的查询方法，查询出对应的医院信息
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);

        return Result.ok(hospital);

    }


    /*---------------------------科室信息-------------------------------*/

    @ApiOperation("上传医院科室接口")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){

        // 获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");

        // 从各自医院系统中传来的hoscode
        String hoscode = (String) paramMap.get("hoscode");

        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);

        String signKeyMD5 = MD5.encrypt(signKey);

        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR); // 签名不一致时抛出异常
        }

        // 调用相关service方法，添加科室信息
        departmentService.saveDepartment(paramMap);

        return Result.ok();

    }


    @ApiOperation("查询科室接口")
    @PostMapping("/department/list")
    public Result selectDepartment(HttpServletRequest request){
        // 获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 获取医院编号
        String hoscode = (String) paramMap.get("hoscode");
        // 获取当前页与每页的显示记录数，如果为空赋上默认值
        int pageNo = StringUtils.isNullOrEmpty((String) paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int pageSize = StringUtils.isNullOrEmpty((String) paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");
        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR); // 签名不一致时抛出异常
        }

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        // 调用service方法查询科室信息列表
        Page<Department> page = departmentService.selectPageDepartment(pageNo, pageSize, departmentQueryVo);

        return Result.ok(page);

    }


    @ApiOperation("删除科室接口")
    @PostMapping("/department/remove")
    public Result removeDepartment(HttpServletRequest request){
        // 获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 获取医院编号和科室编号
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");
        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR); // 签名不一致时抛出异常
        }

        // 调用service中的方法，删除科室信息
        departmentService.deleteDepartment(hoscode, depcode);

        return Result.ok();

    }


    /*---------------------------排班信息-------------------------------*/

    @ApiOperation("上传排班信息")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        // 获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");
        // 从各自医院系统中传来的hoscode
        String hoscode = (String) paramMap.get("hoscode");
        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR); // 签名不一致时抛出异常
        }

        scheduleService.saveSchedule(paramMap);

        return Result.ok();
    }


    @ApiOperation("排班列表接口")
    @PostMapping("/schedule/list")
    public Result selectSchedule(HttpServletRequest request){
        // 获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 获取医院编号
        String hoscode = (String) paramMap.get("hoscode");
        // 获取科室编号
        String depcode = (String) paramMap.get("depcode");
        // 获取当前页与每页的显示记录数，如果为空赋上默认值
        int pageNo = StringUtils.isNullOrEmpty((String) paramMap.get("page")) ? 1 : Integer.parseInt((String) paramMap.get("page"));
        int pageSize = StringUtils.isNullOrEmpty((String) paramMap.get("limit")) ? 1 : Integer.parseInt((String) paramMap.get("limit"));

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");
        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR); // 签名不一致时抛出异常
        }

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setHoscode(depcode);

        // 调用service方法查询排班信息列表
        Page<Schedule> page = scheduleService.selectPageSchedule(pageNo, pageSize, scheduleQueryVo);

        return Result.ok(page);
    }


    @ApiOperation("删除排班信息")
    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        // 获取传递过来的科室信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);

        // 获取医院编号和排班编号
        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");

        // 从各自医院系统中传来的签名（改签名已经过MD5加密）
        String hospSign = (String) paramMap.get("sign");
        // 根据hoscode查询预约挂号平台中保存的签名
        String signKey = hospitalSetService.getSignKeyByHoscode(hoscode);
        String signKeyMD5 = MD5.encrypt(signKey);
        // 判断两个签名是否一致
        if(! hospSign.equals(signKeyMD5)){
            throw new YyghException(ResultCodeEnum.SIGN_ERROR); // 签名不一致时抛出异常
        }

        // 调用service中的方法，删除科室信息
        scheduleService.deleteSchedule(hoscode, hosScheduleId);

        return Result.ok();
    }

}

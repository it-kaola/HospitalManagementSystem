package com.bjpowernode.yygh.msm.controller;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.msm.entity.Sms;
import com.bjpowernode.yygh.msm.service.MSMService;
import com.bjpowernode.yygh.msm.utils.RandomUtil;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Api(tags = "短信服务的相关接口")
@RestController
@RequestMapping("/api/msm")
public class MSMController {

    @Resource
    private MSMService msmService;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @ApiOperation("发送短信验证码")
    @GetMapping("/sendCode/{phone}")
    public Result sendCode(@PathVariable String phone){

        // 从redis获取验证码，如果获取得到，直接返回ok
        // redis中保存的格式为 key是手机号，value是验证码
        String code = redisTemplate.opsForValue().get(phone);
        if(! StringUtils.isEmpty(code)){
            return Result.ok();
        }
        // 如果从redis获取不到，就调用service中的方法生成验证码，并将验证码写入redis中，设置有效时间为5分钟
        code = RandomUtil.getSixBitRandom();
        // 调用service中的方法，发送验证码
        boolean isSend = msmService.sendCode(phone, code); // isSend为true表示发送成功，反之表示发送失败
        if(isSend){
            redisTemplate.opsForValue().set(phone,code, 5, TimeUnit.MINUTES); // 设置验证码的有效时间为5分钟
            return Result.ok();
        }else{
            return Result.fail().message("短信发送失败");
        }

    }

    @ApiOperation("发送短信验证码测试")
    @PostMapping("/sendCodeTest")
    public Result sendCode(@RequestBody Sms sms){
        int sdkAppId = 1400670204;
        String appKey = "2c6b1e48da32bc1f6da3293f0c5cf140";
        // 短信模板的id
        int templateId = 1385705;
        // 短信签名
        String signKey = "IT考拉个人公众号";

        try{
            String[] params = {sms.getCode(), Integer.toString(sms.getMin())};
            SmsSingleSender smsSingleSender = new SmsSingleSender(sdkAppId, appKey);
            SmsSingleSenderResult result = smsSingleSender.sendWithParam("86", sms.getPhoneNum(), templateId, params, signKey, "", "");
            System.out.println(result);
            return Result.ok(result);
        }catch (HTTPException | JSONException | IOException e){
            e.printStackTrace();
            return Result.fail().message("发送短信失败");
        }

    }

}

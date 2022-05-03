package com.bjpowernode.yygh.msm.service.impl;

import com.bjpowernode.yygh.msm.entity.Sms;
import com.bjpowernode.yygh.msm.service.MSMService;
import com.bjpowernode.yygh.vo.msm.MsmVo;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
public class MSMServiceImpl implements MSMService {

    /*@Value("${tencentcloud.sdkAppId}")
    private int sdkAppId;

    @Value("${tencentcloud.appKey}")
    private String appKey;

    @Value("${tencentcloud.templateId}")
    private int templateId;

    @Value("${tencentcloud.signKey}")
    private String signKey;*/


    @Override
    public boolean sendCode(String phone, String code) {
        int sdkAppId = 1400670204;
        String appKey = "2c6b1e48da32bc1f6da3293f0c5cf140";
        // 短信模板的id
        int templateId = 1385705;
        // 短信签名
        String signKey = "IT考拉个人公众号";

        // 判断手机号是否为空
        if(StringUtils.isEmpty(phone)){
            return false;
        }

        Sms sms = new Sms();
        sms.setPhoneNum(phone);
        sms.setCode(code);
        sms.setMin(5);

        try{
            String[] params = {sms.getCode(), Integer.toString(sms.getMin())};
            SmsSingleSender smsSingleSender = new SmsSingleSender(sdkAppId, appKey);
            SmsSingleSenderResult result = smsSingleSender.sendWithParam("86", sms.getPhoneNum(), templateId, params, signKey, "", "");
            System.out.println(result);
            return true;
        }catch (HTTPException | JSONException | IOException e){
            e.printStackTrace();
            return false;
        }
    }


    // mq发送短信的接口
    @Override
    public boolean sendMessageByMQ(MsmVo msmVo) {
        if(! StringUtils.isEmpty(msmVo.getPhone())){
            String code = "您已成功预约，请及时就诊";
            return this.sendCode(msmVo.getPhone(), code);
        }
        return false;
    }
}

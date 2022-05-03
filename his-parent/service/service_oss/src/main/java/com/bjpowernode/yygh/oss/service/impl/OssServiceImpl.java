package com.bjpowernode.yygh.oss.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.bjpowernode.yygh.oss.service.OssService;
import com.bjpowernode.yygh.oss.utils.ConstantOssPropertiesUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    // 上传文件到云端
    @Override
    public String upload(MultipartFile file) {

        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = "https://" + ConstantOssPropertiesUtils.EDNPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = ConstantOssPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantOssPropertiesUtils.SECRECT;
        // 填写Bucket名称，例如examplebucket。
        String bucketName = ConstantOssPropertiesUtils.BUCKET;


        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = file.getInputStream();
            String filename = file.getOriginalFilename();
            // 使用UUID生成唯一的文件名，例如：123123783926498image.jpg
            String uuid = UUID.randomUUID().toString().replace("-", "");
            filename = uuid + filename;
            // 按照当前日期创建文件夹，上传到创建的文件夹中，例如：2022_4_30/image.jpg
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");
            String dateStr = simpleDateFormat.format(new Date());
            filename = dateStr + "/" + filename;

            // 调用阿里云提供的方法实现文件上传
            ossClient.putObject(bucketName, filename, inputStream);

            // 返回上传后的文件路径
            String url = "https://" + bucketName + "." +endpoint + "/" + filename;

            return url;


        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return null;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (ossClient != null) {
                // 关闭
                ossClient.shutdown();
            }
        }

    }
}

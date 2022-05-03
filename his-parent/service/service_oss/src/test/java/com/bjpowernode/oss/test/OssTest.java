package com.bjpowernode.oss.test;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

public class OssTest {

    public static void main(String[] args) throws Exception {
        // Endpoint�Ի���1�����ݣ�Ϊ��������Region�밴ʵ�������д��
        String endpoint = "https://oss-cn-hangzhou.aliyuncs.com";
        // �������˺�AccessKeyӵ������API�ķ���Ȩ�ޣ����պܸߡ�ǿ�ҽ�����������ʹ��RAM�û�����API���ʻ��ճ���ά�����¼RAM����̨����RAM�û���
        String accessKeyId = "LTAI5tLwFCh2GR8QLfjmKowE";
        String accessKeySecret = "a7JGG08xBznOu5VUuGFJYgAcv8ptGl";
        // ��дBucket���ƣ�����examplebucket��
        String bucketName = "testbucketbjpowernode";

        // ����OSSClientʵ����
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // �����洢�ռ䡣
            ossClient.createBucket(bucketName);

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}

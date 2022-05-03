package com.bjpowernode.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {

    // 上传文件到云端
    String upload(MultipartFile file);
}

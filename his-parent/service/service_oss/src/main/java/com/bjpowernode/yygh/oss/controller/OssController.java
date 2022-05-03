package com.bjpowernode.yygh.oss.controller;

import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.oss.service.OssService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Api(tags = "上传文件到阿里云的相关接口")
@RestController
@RequestMapping("/api/oss/file")
public class OssController {

    @Resource
    OssService ossService;

    @ApiOperation("上传文件到云端")
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file){ // MultipartFile获取前端上传的文件
        String url = ossService.upload(file); // 返回文件上传的路径
        return Result.ok(url);
    }

}

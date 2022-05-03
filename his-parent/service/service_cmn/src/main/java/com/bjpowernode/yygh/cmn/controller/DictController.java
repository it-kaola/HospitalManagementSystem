package com.bjpowernode.yygh.cmn.controller;

import com.bjpowernode.yygh.cmn.service.DictService;
import com.bjpowernode.yygh.common.result.Result;
import com.bjpowernode.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
// @CrossOrigin
public class DictController {

    @Resource
    private DictService dictService;

    @ApiOperation("根据id查询子数据列表")
    @GetMapping("/findChildDataById/{id}")
    public Result findChildDataById(@PathVariable("id") Long id){
        List<Dict> dictList = dictService.findChildDataById(id);
        return Result.ok(dictList);
    }

    @ApiOperation("导出数据字典")
    @GetMapping("/exportData")
    public void exportDict(HttpServletResponse response){ // response参数用于下载文件使用
        dictService.exportDictData(response);
    }


    @ApiOperation("导入数据字典")
    @PostMapping("/importData")
    public void importDict(MultipartFile file){ // MultipartFile对象用于文件上传
        dictService.importDictData(file);
    }


    @ApiOperation("根据dictCode和value查询对应的值")
    @GetMapping("/getDictName/{dictCode}/{value}")
    public String getDictName(@PathVariable("dictCode") String dictCode, @PathVariable("value") String value){
        String dictName = dictService.getDictName(dictCode, value);
        return dictName;
    }

    @ApiOperation("根据value查询对应的值")
    @GetMapping("/getDictName/{value}")
    public String getDictName(@PathVariable("value") String value){
        String dictName = dictService.getDictName("", value);
        return dictName;
    }

    @ApiOperation("根据dictCode获取下级节点")
    @GetMapping("/getChildListByDictCode/{dictCode}")
    public Result getChildListByDictCode(@PathVariable("dictCode") String dictCode){
        List<Dict> dictList = dictService.getChildListByDictCode(dictCode);
        return Result.ok(dictList);
    }

}

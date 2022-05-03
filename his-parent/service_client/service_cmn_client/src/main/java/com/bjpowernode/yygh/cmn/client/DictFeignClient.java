package com.bjpowernode.yygh.cmn.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-cmn")
public interface DictFeignClient {

    // 根据dictCode和value查询对应的值
    @GetMapping("/admin/cmn/dict/getDictName/{dictCode}/{value}")
    String getDictName(@PathVariable("dictCode") String dictCode, @PathVariable("value") String value);


    // 根据value查询对应的值
    @GetMapping("/admin/cmn/dict/getDictName/{value}")
    String getDictName(@PathVariable("value") String value);
}

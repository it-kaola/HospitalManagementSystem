package com.bjpowernode.yygh.cmn;

import com.bjpowernode.yygh.cmn.service.impl.DictServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class Test01 {

    @Test
    public void test01(){
        DictServiceImpl dictService = new DictServiceImpl();
        String hostype = dictService.getDictName("Hostype", "1");
        System.out.println(hostype);
    }
}

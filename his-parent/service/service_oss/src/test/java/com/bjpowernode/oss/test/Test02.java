package com.bjpowernode.oss.test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test02 {
    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        System.out.println(simpleDateFormat.format(date));

    }
}

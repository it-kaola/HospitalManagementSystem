package com.bjpowernode.easyexcel;

import com.alibaba.excel.EasyExcel;

public class TestRead {
    public static void main(String[] args) {
        // 读取文件的路径
        String path = "H:\\WKCTO-Java\\22-HospitalManagementSystem\\code\\01.xlsx";
        // 调用方法实现读取操作
        EasyExcel.read(path, UserData.class, new ExcelListener()).sheet().doRead();
    }
}

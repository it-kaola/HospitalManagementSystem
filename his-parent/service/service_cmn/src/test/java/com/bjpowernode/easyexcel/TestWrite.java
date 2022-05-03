package com.bjpowernode.easyexcel;


import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

public class TestWrite {
    public static void main(String[] args) {
        // 设置excel文件路径和文件名称
        String path = "H:\\WKCTO-Java\\22-HospitalManagementSystem\\code\\01.xlsx";

        // 构建一个数据的List集合
        List<UserData> userDataList = new ArrayList<>();
        for(int i=0; i<10; i++){
            UserData userData = new UserData();
            userData.setUid(i);
            userData.setUsername("lucy" + i);
            userDataList.add(userData);
        }

        // 调用方法实现写操作
        EasyExcel.write(path, UserData.class).sheet("用户信息").doWrite(userDataList);
    }
}

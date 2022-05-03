package com.bjpowernode.easyexcel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserData {

    @ExcelProperty(value = "用户编号", index = 0) // @ExcelProperty设置表头信息，index属性为第几列（从0开始）
    private int uid;

    @ExcelProperty(value = "用户名称", index = 1)
    private String username;

}

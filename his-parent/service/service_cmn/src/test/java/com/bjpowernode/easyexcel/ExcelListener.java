package com.bjpowernode.easyexcel;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

// 该监听器用于读取excel表格中的数据
public class ExcelListener extends AnalysisEventListener<UserData> {

    // 该方法读取的是表头信息，即excel表格第一行的数据
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息：" + headMap); // 其中参数headMap中封装的就是表头信息
    }

    // 该方法用于一行一行的读取excel中的数据。注意：从表格的第二行开始读取数据
    @Override
    public void invoke(UserData userData, AnalysisContext analysisContext) {
        System.out.println(userData);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}

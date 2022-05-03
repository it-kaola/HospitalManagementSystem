package com.bjpowernode.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.bjpowernode.yygh.cmn.dao.DictDao;
import com.bjpowernode.yygh.model.cmn.Dict;
import com.bjpowernode.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictDao dictDao;

    public DictListener(DictDao dictDao) {
        this.dictDao = dictDao;
    }

    // 一行一行的读取
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        dictDao.insert(dict); // 将读取到的dictEeVo对象插入到数据库中
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}

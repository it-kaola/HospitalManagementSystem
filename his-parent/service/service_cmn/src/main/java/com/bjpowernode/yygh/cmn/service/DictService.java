package com.bjpowernode.yygh.cmn.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.bjpowernode.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {

    // 根据id查询子数据列表
    List<Dict> findChildDataById(Long id);

    // 导出数据字典
    void exportDictData(HttpServletResponse response);

    // 导入数据字典
    void importDictData(MultipartFile file);

    // 根据dictCode和value查询对应的值
    String getDictName(String dictCode, String value);

    // 根据dictCode获取下级节点
    List<Dict> getChildListByDictCode(String dictCode);
}

package com.bjpowernode.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bjpowernode.yygh.cmn.dao.DictDao;
import com.bjpowernode.yygh.cmn.listener.DictListener;
import com.bjpowernode.yygh.cmn.service.DictService;
import com.bjpowernode.yygh.model.cmn.Dict;
import com.bjpowernode.yygh.vo.cmn.DictEeVo;
import com.mysql.jdbc.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
* 说明：
*   com.baomidou.mybatisplus.extension.service.impl.ServiceImpl类已经默认实现了单表的CRUD，分页查询也有默认实现，能够更加灵活和代码简洁把分页查询功能实现。
* */

@Service
public class DictServiceImpl extends ServiceImpl<DictDao, Dict> implements DictService {

    // 根据Dict对象的id值设置判断其是否有子节点，返回布尔值
    private boolean isChildren(Long id){
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(queryWrapper); // selectCount()方法是ServiceImpl提供的，作用是返回查询到的记录条数
        return count>0;
    }


    // 根据id查询子数据列表
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator") // 将方法的返回值写入缓存中，下一次请求时，直接从缓存中取出对应数据，value属性为指定缓存存在哪块命名空间
    public List<Dict> findChildDataById(Long id) {

        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        List<Dict> dictList = baseMapper.selectList(queryWrapper); // 其中baseMapper是ServiceImpl类中已经自动注入好的一个对象，在这里直接使用即可，因为DictServiceImpl继承了ServiceImpl

        for(Dict dict : dictList){
            boolean isChild = this.isChildren(dict.getId());
            dict.setHasChildren(isChild); // 设置每个Dict对象的hasChildren属性
        }
        return dictList;
    }


    // 导出数据字典中的数据
    @Override
    public void exportDictData(HttpServletResponse response) {
        // 设置下载的相关信息
        response.setContentType("application/vnd.ms-excel"); // 以excel的形式下载下来
        response.setCharacterEncoding("utf-8");
        String fileName = "dictData";
        response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
        // 查询数据库
        List<Dict> dictList = baseMapper.selectList(null);// 查询所有数据
        // 将Dict对象转换为DictEeVo对象
        List<DictEeVo> dictEeVoList = new ArrayList<>();
        for(Dict dict : dictList){
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict, dictEeVo); // 将dict对象中与dictEeVo对象同名的属性进行赋值操作
            dictEeVoList.add(dictEeVo);
        }
        // 调用EasyExcel中的方法进行写操作
        try {
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // 上传文件，导入数据字典
    @Override
    @CacheEvict(value = "dict", allEntries=true) // 当缓存中的数据发生变更时，清空缓存中的对应数据
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 根据dictCode和value查询对应的值
    @Override
    public String getDictName(String dictCode, String value) {
        if(StringUtils.isNullOrEmpty(dictCode)){
            // dictCode为空，根据value查询dictName
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("value", value);
            Dict dict = baseMapper.selectOne(queryWrapper);
            return dict.getName();
        }else{
            // dictCode不为空，根据value查询dictName

            // 先根据dictCode查询对应得到parentId
            QueryWrapper<Dict> wrapper1 = new QueryWrapper<>();
            wrapper1.eq("dict_code", dictCode);
            Dict parentDict = baseMapper.selectOne(wrapper1);
            Long parentId = parentDict.getId();
            // 在根据parentId+value查询对应的Dict
            QueryWrapper<Dict> wrapper2 = new QueryWrapper<>();
            wrapper2.eq("parent_id", parentId);
            wrapper2.eq("value", value);
            Dict finalDict = baseMapper.selectOne(wrapper2);
            return finalDict.getName();
        }
    }


    // 根据dictCode获取下级节点
    @Override
    public List<Dict> getChildListByDictCode(String dictCode) {
        // 先根据dictCode查找到父节点的id值
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictCode);
        Dict parentDict = baseMapper.selectOne(wrapper);
        Long parentId = parentDict.getId();

        // 再根据父节点的id值查询所有子节点的列表
        List<Dict> childDataById = this.findChildDataById(parentId);
        return childDataById;
    }
}

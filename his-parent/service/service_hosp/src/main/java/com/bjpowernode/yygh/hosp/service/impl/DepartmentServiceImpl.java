package com.bjpowernode.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bjpowernode.yygh.hosp.repository.DepartmentRepository;
import com.bjpowernode.yygh.hosp.service.DepartmentService;
import com.bjpowernode.yygh.model.hosp.Department;
import com.bjpowernode.yygh.vo.hosp.DepartmentQueryVo;
import com.bjpowernode.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    DepartmentRepository departmentRepository;


    @Override
    public void saveDepartment(Map<String, Object> paramMap) {
        // 将paramMap转换成Department对象，方便后面操作
        String jsonString = JSONObject.toJSONString(paramMap); // 将paramMap转成json格式的字符串
        Department department = JSONObject.parseObject(jsonString, Department.class);

        // 根据医院编号+科室编号获取相应的科室信息
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        if(departmentExist != null){
            // 如果科室信息不为空，表示MongoDB中已经存在该记录，进行修改操作
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }else{
            // 如果科室信息为空，表示MongoDB中没有该记录，进行插入操作
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }

    }


    @Override
    public Page<Department> selectPageDepartment(int pageNo, int pageSize, DepartmentQueryVo departmentQueryVo) {

        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo, department);
        department.setIsDeleted(0);

        // 创建Pageable对象，设置当前页与每页显示的记录数
        Pageable pageable = PageRequest.of(pageNo-1, pageSize); // 注意数字0为第一页

        // 创建Example对象
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Example<Department> example = Example.of(department, matcher);


        Page<Department> page = departmentRepository.findAll(example, pageable);

        return page;
    }


    @Override
    public void deleteDepartment(String hoscode, String depcode) {
        // 根据医院编号与科室编号查询科室信息
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        if(department != null){
            // 如果科室信息存在则进行删除操作
            departmentRepository.deleteById(department.getId());
        }
    }

    
    @Override
    public List<DepartmentVo> getDeptTreeStructure(String hoscode) {

        List<DepartmentVo> result = new ArrayList<>();

        // 根据医院编号（hoscode）查询所有科室信息
        Department departmentCondition = new Department();
        departmentCondition.setHoscode(hoscode);
        Example<Department> example = Example.of(departmentCondition);
        // 所有科室列表信息
        List<Department> allDepartmentList = departmentRepository.findAll(example);

        // 根据科室信息的bigCode拆分成每个大科室，例如内科、妇产科等，存在一个Map<String, List<Department>>中，其中key为bigCode的值，value为bigCode对应的所有大科室的信息
        Map<String, List<Department>> map = new HashMap<>();
        for(Department department : allDepartmentList){
            String bigCode = department.getBigcode();
            if(! map.containsKey(bigCode)){
                List<Department> departments = new ArrayList<>();
                departments.add(department);
                map.put(department.getBigcode(), departments);
            }else{
                List<Department> departments = map.get(bigCode);
                departments.add(department);
                map.put(bigCode, departments);
            }
        }

        // 遍历map集合

        for(Map.Entry<String, List<Department>> entry : map.entrySet()){
            String bigCode = entry.getKey();
            List<Department> smallDepartmentList = entry.getValue();

            DepartmentVo departmentVo = new DepartmentVo();
            departmentVo.setDepcode(bigCode);
            departmentVo.setDepname(smallDepartmentList.get(0).getBigname());

            // 封装大科室下的小科室，例如内科下的消化内科，神经内科等
            List<DepartmentVo> children = new ArrayList<>();
            for(Department department : smallDepartmentList){
                DepartmentVo childDepartmentVo = new DepartmentVo();
                childDepartmentVo.setDepcode(department.getDepcode());
                childDepartmentVo.setDepname(department.getDepname());
                children.add(childDepartmentVo);
            }
            departmentVo.setChildren(children);
            result.add(departmentVo);
        }

        return result;
    }


    @Override
    public String getDepartmentNameByHoscodeAndDepcode(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(department != null){
            return department.getDepname();
        }
        return null;
    }


    // 根据医院编号和科室编号查询科室对象
    @Override
    public Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}

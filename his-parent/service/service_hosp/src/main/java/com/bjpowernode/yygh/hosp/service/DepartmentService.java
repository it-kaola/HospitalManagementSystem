package com.bjpowernode.yygh.hosp.service;

import com.bjpowernode.yygh.model.hosp.Department;
import com.bjpowernode.yygh.vo.hosp.DepartmentQueryVo;
import com.bjpowernode.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    // 保存科室信息
    void saveDepartment(Map<String, Object> paramMap);

    // 查询可是信息列表
    Page<Department> selectPageDepartment(int pageNo, int pageSize, DepartmentQueryVo departmentQueryVo);

    // 删除科室信息
    void deleteDepartment(String hoscode, String depcode);

    // 根据医院编号（hoscode）查询所有科室信息
    List<DepartmentVo> getDeptTreeStructure(String hoscode);

    // 根据医院编号和科室编号查询科室名
    String getDepartmentNameByHoscodeAndDepcode(String hoscode, String depcode);

    // 根据医院编号和科室编号查询科室对象
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}

package com.bjpowernode.yygh.hosp.repository;

import com.bjpowernode.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
* 在继承MongoRepository接口的Repository接口中，根据制定规则制定方法名称，MongoDB会自动根据方法名称实现对应的操作，无需开发人员手动编写具体的方法执行细节
* */

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {

    Hospital getHospitalByHoscode(String hoscode);

    List<Hospital> getHospitalByHosnameLike(String hosname);
}

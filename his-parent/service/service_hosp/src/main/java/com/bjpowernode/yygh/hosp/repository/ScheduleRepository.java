package com.bjpowernode.yygh.hosp.repository;

import com.bjpowernode.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {

    // 根据医院编号与排班编号查询排班信息
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    // 根据hoscode、depcode、workDate查询出schedule
    List<Schedule> getScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date toDate);
}

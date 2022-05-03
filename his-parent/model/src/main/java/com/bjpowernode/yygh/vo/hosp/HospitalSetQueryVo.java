package com.bjpowernode.yygh.vo.hosp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/*
* 用于条件查询的医院设置信息VO对象
* */

@Data
public class HospitalSetQueryVo {

    @ApiModelProperty(value = "医院名称")
    private String hosname;

    @ApiModelProperty(value = "医院编号")
    private String hoscode;
}

package com.bjpowernode.yygh.common.exception;

import com.bjpowernode.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {


    // 当遇到YyghException异常时的处理方法
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result doYyghException(Exception e){
        e.printStackTrace();
        return Result.fail();
    }


    // 全局的异常处理方法
    @ExceptionHandler // @ExceptionHandler注解的value属性为空时，表示当遇到其他未知的异常就进入到该方法执行异常处理
    @ResponseBody
    public Result globalError(Exception e){
        e.printStackTrace();
        return Result.fail();
    }

}

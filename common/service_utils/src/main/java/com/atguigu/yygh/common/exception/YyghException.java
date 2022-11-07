package com.atguigu.yygh.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Wei
 * @date: 2022/10/18,13:59
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YyghException extends RuntimeException{
    private Integer code; // 异常状态码信息
    private String msg; // 异常具体描述
}

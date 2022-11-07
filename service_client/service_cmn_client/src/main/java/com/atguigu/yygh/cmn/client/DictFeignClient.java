package com.atguigu.yygh.cmn.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author: Wei
 * @date: 2022/10/26,11:20
 * @description:
 */
@FeignClient("service-cmn")
public interface DictFeignClient {
    
    @GetMapping(value = "/cmn/dict/getName/{parentDictCode}/{value}")
    public String getName(
            @PathVariable("parentDictCode") String parentDictCode,
            @PathVariable("value") String value);
    
    @GetMapping(value = "/cmn/dict/getName/{value}")
    public String getName(
            @PathVariable("value") String value);
}

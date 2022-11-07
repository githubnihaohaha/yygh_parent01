package com.atguigu.yygh.hosp.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/24,14:37
 * @description:
 */
public interface UserRepository extends MongoRepository<User,String> {
    
    // 按照 SpringData 命名规范创建的方法
    List<User> findByNameAndAge(String name,Integer age);
    
    List<User> findByNameLike(String name);
    
}

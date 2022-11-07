package com.atguigu.yygh.hosp.mongo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author: Wei
 * @date: 2022/10/24,11:22
 * @description:
 */
@SpringBootTest
public class TemplateTest {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Test
    public void save() {
        User user = new User();
        user.setAge(20);
        user.setName("test");
        user.setEmail("4932200@qq.com");
        mongoTemplate.save(user); //insert是以前版本的,也可以使用
        System.out.println("user = " + user); //添加到 MongoDB后它会自动回填这个属性,里面会多出一个主键值
    }
    
    
    @Test
    public void testFindAll() {
        List<User> all = mongoTemplate.findAll(User.class);
        System.out.println("all = " + all);
        
    }
    
    @Test
    public void testFindById() {
        User byId = mongoTemplate.findById("635606296b94373bd8844271", User.class);
        
    }
    
    
    // ==========================================
    
    /*
     * 条件查询
     * */
    @Test
    public void findQuery() {
        Query query = new Query(Criteria.where("name").is("test")
                .and("age").is(20));
        List<User> users = mongoTemplate.find(query, User.class);
        System.out.println("users = " + users);
    }
    
    
    @Test
    public void findQueryLike(){
    
        // 封装模糊查询
        Pattern pattern = Pattern.compile("^.*test*.$", Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(pattern));
    
        // 模糊+分页查询
        List<User> users = mongoTemplate.find(query.skip(0).limit(2)
                , User.class);
        System.out.println("users = " + users);
    }
  
}

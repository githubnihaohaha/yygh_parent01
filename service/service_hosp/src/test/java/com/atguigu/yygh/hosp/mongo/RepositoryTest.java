package com.atguigu.yygh.hosp.mongo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author: Wei
 * @date: 2022/10/24,14:39
 * @description:
 */

@SpringBootTest
public class RepositoryTest {
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testSave(){
        User user = new User();
        user.setAge(21);
        user.setName("wei");
        user.setEmail("256510@qq.com");
        User user1 = userRepository.save(user);
        
    }
    
    
    @Test
    public void findAll(){
        List<User> all = userRepository.findAll();
        System.out.println("all = " + all);
    
    }
    
    
    @Test
    public void findById(){
        Optional<User> user = userRepository.findById("63567fba12c81946e4cc295c");
        System.out.println("user = " + user);
    
    }
    
    @Test
    public void findQuery(){
        List<User> jiajia = userRepository.findByNameAndAge("jiajia", 20);
        System.out.println("Result==="+jiajia);
    
    }
    
    @Test
    public void findLike(){
        List<User> ji = userRepository.findByNameLike("ji");
        System.out.println("Result==>"+ji);
    
    }
    
    // 分页查询
    @Test
    public void findPage(){
    
        Sort sort = Sort.by(Sort.Direction.ASC, "age");
    
        Pageable page = PageRequest.of(0,2,sort);
    
        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true); // 匹配器规则:包含,忽略大小写
    
        User user = new User();
        user.setName("i");
        Example<User> example = Example.of(user, matcher);
    
        Page<User> all = userRepository.findAll(example,page);
        List<User> userList = all.getContent();
        for (User user1 : userList) {
            System.out.println("user1 = " + user1);
        }
    
    
    }
}

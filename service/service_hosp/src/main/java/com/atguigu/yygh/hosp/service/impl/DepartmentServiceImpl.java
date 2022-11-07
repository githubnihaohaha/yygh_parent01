package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Wei
 * @date: 2022/10/25,15:21
 * @description:
 */

@Service
public class DepartmentServiceImpl implements DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    
    /**
     * 分页获取医院科室信息
     *
     * @param page
     * @param limit
     * @param queryVo
     * @return
     */
    @Override
    public Page<Department> getPage(int page, int limit, DepartmentQueryVo queryVo) {
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        PageRequest pageRequest = PageRequest.of(page - 1, limit, sort);
        
        
        Department department = new Department();
        BeanUtils.copyProperties(queryVo, department);
        
        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example<Department> example = Example.of(department, matcher);
        
        return departmentRepository.findAll(example, pageRequest);
    }
    
    
    /**
     * 添加或修改科室信息
     *
     * @param objectMap
     */
    @Override
    public void saveDept(Map<String, Object> objectMap) {
        
        String jsonString = JSONObject.toJSONString(objectMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        
        /*
         * 根据医院code和部门code确认需要更改还是添加
         *
         * */
        Department existsDepartment = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        if (existsDepartment != null) {
            department.setId(existsDepartment.getId());
            department.setCreateTime(existsDepartment.getCreateTime());
            department.setUpdateTime(new Date());
        } else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
        }
        departmentRepository.save(department);
    }
    
    
    /**
     * 通过医院的唯一标识获取科室信息,一个大科室中包含多个小科室(至少一个)
     *
     * @param hoscode 医院唯一标识
     * @return
     */
    @Override
    public List<DepartmentVo> getDepartmentTreeByHoscode(String hoscode) {
        
        //创建用于最终封装数据的List集合,这个对象只是用来展示数据的对象
        List<DepartmentVo> resultList = new ArrayList<>();
        
        // 1.根据医院编号查询所有科室
        Department department = new Department();
        department.setHoscode(hoscode);
        Example<Department> example = Example.of(department);
        List<Department> departmentList = departmentRepository.findAll(example);
        
        
        /*
         *  2.根据大科室 bigcode 进行分组,获取每个大科室下的子科室
         * String = big code   大科室编号
         * List<Department> =  这个大科室下的所有小科室
         *
         * */
        Map<String, List<Department>> departmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        
        /*
         *  3.遍历这个map,它的key就是大科室的编号,value就是这一科室下的所有小科室,将他们分别封装
         *    这个 map 每遍历一次,就封装完毕一个大科室,这个大科室对象中有小科室的集合
         *
         * */
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            
            /*
             *  3.1
             *   封装大科室
             *     每个小科室都会包含一个big name属性,这个属性的值是大科室的名称,
             *     获得第0个的目是去掉不必要的空指针,每个大科室下至少是有一个科室的,所以取第[0]的值
             * */
            String bigCode = entry.getKey();
            
            // 这个大科室下的所有小科室的集合
            List<Department> partOfBigDepartment = entry.getValue();
            
            // 封装大科室数据的 Vo 对象
            DepartmentVo bigDepartment = new DepartmentVo();
            bigDepartment.setDepcode(bigCode);
            bigDepartment.setDepname(partOfBigDepartment.get(0).getDepname());
            
            
            /*
             * 3.2
             *   封装小科室
             *       遍历这个大科室下的所有小科室集合,将科室名称与科室编码赋值给 Vo 值对象
             *       将所有小科室的信息封装到集合中,这个集合最终最为大科室的一个属性值
             *
             * */
            ArrayList<DepartmentVo> childDepartment = new ArrayList<>();
            for (Department tempDept : partOfBigDepartment) {
                DepartmentVo departmentVo = new DepartmentVo();
                
                departmentVo.setDepcode(tempDept.getDepcode());
                departmentVo.setDepname(tempDept.getDepname());
                
                childDepartment.add(departmentVo);
            }
            
            //封装完毕的所有子节点科室存入大科室集合,此时一个大科室封装完毕
            bigDepartment.setChildren(childDepartment);
            
            //每次大科室封装完毕都将他存入最终要返回的集合中
            resultList.add(bigDepartment);
        }
        
        return resultList;
    }
    
    /**
     * 通过医院编号与科室编号获得科室信息
     *
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }
}















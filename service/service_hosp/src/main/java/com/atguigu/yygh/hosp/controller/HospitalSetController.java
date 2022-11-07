package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author wei
 * @since 2022-10-17
 */
@Api(tags = "医院设置管理接口")
@RestController
@RequestMapping("/admin/hosp/hospital-set")
public class HospitalSetController {
    
    @Autowired
    private HospitalSetService hospitalSetService;
    
    @ApiOperation("测试异常处理")
    @GetMapping("/test")
    public R testException() {
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new YyghException(999, "自定义异常处理");
        }
        return R.ok();
    }
    
    
    /**
     * 锁定/解锁医院
     *
     * @param id
     * @param status
     * @return
     */
    @ApiOperation("医院锁定和解锁")
    @PutMapping("/lockHospSet/{id}/{status}")
    public R lockHospSet(@PathVariable Long id,
                         @PathVariable Integer status) {
        System.out.println("id =+++++++++++++++++++++++ " + id);
        System.out.println("status =+++++++++++++++++++++ " + status);
        HospitalSet hosSet = hospitalSetService.getById(id);
        hosSet.setStatus(status);
        hospitalSetService.updateById(hosSet);
        return R.ok();
    }
    
    
    /**
     * 批量删除
     *
     * @param ids 前端传入的json数组(选中删除的id)
     * @return
     */
    @ApiOperation("批量删除")
    @DeleteMapping("/batchDelete")
    public R batchDelete(@RequestBody List<Long> ids) {
        hospitalSetService.removeByIds(ids);
        
        return R.ok();
    }
    
    
    /**
     * 修改
     *
     * @param hospitalSet
     * @return
     */
    @ApiOperation("修改")
    @PutMapping("/updateHospSet")
    public R updateHospSet(@RequestBody HospitalSet hospitalSet) {
        boolean is_success = hospitalSetService.updateById(hospitalSet);
        if (is_success) {
            return R.ok();
        } else {
            return R.error();
        }
    }
    
    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @ApiOperation("根据id查询")
    @GetMapping("/getHospSet/{id}")
    public R getHospSet(@PathVariable Long id) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("hospitalSet", hospitalSet);
    }
    
    
    /**
     * 添加
     *
     * @param hospitalSet
     * @return
     */
    @ApiOperation("添加")
    @PostMapping("/saveHospSet")
    public R saveHospSet(@RequestBody HospitalSet hospitalSet) {
        
        // 为每一个医院生成唯signKey
        String signKey = System.currentTimeMillis() + "" + new Random().nextInt(1000);
        hospitalSet.setSignKey(signKey);
    
        boolean is_success = hospitalSetService.save(hospitalSet);
    
        if (is_success) {
            
            // 调用医院模拟系统模拟系统同步接口,将signKey在医院系统也保留一份
            Map<String, Object> map = new HashMap<>();
            map.put("sign",signKey);
            map.put("hoscode",hospitalSet.getHoscode());
            
            //使用httpClient工具类,调用医院模拟系统接口
            HttpRequestHelper.sendRequest(map,"http://localhost:9998/hospSet/updateSignKey");
            return R.ok();
        } else {
            return R.error();
        }
    }
    
    
    /**
     * 分页查询[条件
     *
     * @param current 当前页
     * @param limit   每页显示数
     * @param queryVo 查询条件
     *                还可以使用@RequestBody注解来封装这个对象
     * @return
     */
    @ApiOperation("条件分页查询")
    @PostMapping("/findPageQuery/{current}/{limit}")
    public R findPageQuery(@PathVariable Long current,
                           @PathVariable Long limit,
                           @RequestBody HospitalSetQueryVo queryVo) {
        
        Page<HospitalSet> page = new Page<>(current, limit);
        
        // 排序条件
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        
        if (queryVo == null) {
            // 条件为空,查询全部
            hospitalSetService.page(page,wrapper);
        } else {
            // 条件不为空,封装条件进行查询
            
            String hoscode = queryVo.getHoscode();
            String hosname = queryVo.getHosname();
            
            if (!StringUtils.isEmpty(hoscode)) {
                wrapper.eq("hoscode", hoscode);
            }
            if (!StringUtils.isEmpty(hosname)) {
                wrapper.like("hosname", hosname);
            }
            hospitalSetService.page(page, wrapper);
            
        }
        List<HospitalSet> list = page.getRecords();
        long total = page.getTotal();
        
        return R.ok().data("total", total).data("list", list);
    }
    
    
    /**
     * 分页查询方法
     *
     * @param current 当前页
     * @param limit   每页记录数
     * @return
     */
    @ApiOperation("分页查询")
    @GetMapping("/findPage/{current}/{limit}")
    public R findPage(@PathVariable Long current,
                      @PathVariable Long limit) {
        // 创建page对象
        Page<HospitalSet> pageParam = new Page<>(current, limit);
        hospitalSetService.page(pageParam);
        List<HospitalSet> list = pageParam.getRecords();
        long total = pageParam.getTotal();
        
        return R.ok().data("total", total).data("list", list);
        
        
    }
    
    @ApiOperation("查询所有数据")
    @GetMapping("/getAll")
    public R getAll() {
        List<HospitalSet> list = hospitalSetService.list();
        return R.ok().data("list", list);
    }
    
    @ApiOperation("根据id删除")
    @DeleteMapping("/remove/{id}")
    public R deleteById(@PathVariable Long id) {
        boolean is_success = hospitalSetService.removeById(id);
        if (is_success) {
            return R.ok();
        } else {
            return R.error();
        }
    }
}


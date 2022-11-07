package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListenerNew;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/21,11:48
 * @description:
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    
    
    /**
     * 导入数据字典
     *
     * @param file
     * @param dictService
     */
    @CacheEvict(value = "dict", allEntries = true) // 当添加内容后,清空之前的缓存重新加载,保证缓存中的数据与数据库中即时同步
    @Override
    public void importData(MultipartFile file, DictService dictService) {
        try {
            EasyExcel.read(file.getInputStream(),
                    DictEeVo.class,
                    new DictListenerNew(dictService)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * 数据字典导出方法
     *
     * @param response
     */
    @Override
    public void exportDictData(HttpServletResponse response) {
        
        try {
            // 设置件格式和编码
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            
            // 1.设置响应头信息,下载必须为 Content-disposition
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            
            // 2.查询所有数据字典数据 Dict => DictEeVo
            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictEeVoList = new ArrayList<>();
            for (Dict dict : dictList) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo); // 将一个对象的值复制到另一个对象中去,它会复制属性相同的值,不同的不管
                dictEeVoList.add(dictEeVo);
            }
            
            // 3.使用EasyExcel写操作
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictEeVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    
    /**
     * 通过传入的id查询它的子节点
     *
     * @param id 传入的 parent_id
     * @return select * from dict where parent_id = id
     */
    @Cacheable("dict") // 将查询到的数据添加到缓存
    @Override
    public List<Dict> getDataById(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        
        List<Dict> dictList = baseMapper.selectList(wrapper);
        
        /*
         * Dict的属性hasChildren,如果有数据设置为true,否则为false
         * 1.遍历dictList,得到dict对象,判断这个对象下一层是否有数据,如果有设置为true
         *
         * */
        for (Dict dict : dictList) {
            // 查询parent_id = 当前dict的id
            Long dictId = dict.getId();
            
            boolean flag = isChildren(dictId);
            dict.setHasChildren(flag);
        }
        
        return dictList;
    }
    
    /**
     * 判断传入id是否有子节点
     *
     * @param dictId
     * @return true or false
     */
    private boolean isChildren(Long dictId) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", dictId);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
    
    
    /**
     * 通过value查询对应的名称
     * 如果value不唯一,需要传入dict_code做精确定位
     *
     * @param dictCode 非必要,在value有重复时传入
     * @param value
     * @return name字段,可能为null
     */
    @Override
    public String getNameByValue(String dictCode, String value) {
        
        if (StringUtils.isEmpty(dictCode)) { //value唯一
            
            LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Dict::getValue, value);
            
            Dict dict = baseMapper.selectOne(wrapper);
            
            if (dict != null) {
                return dict.getName();
            }
            
        } else {
            
            Dict dictParent = getDictByDictCode(dictCode);
            
            if (dictParent != null) {
                
                LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Dict::getParentId, dictParent.getId());
                wrapper.eq(Dict::getValue, value);
                
                Dict dict = baseMapper.selectOne(wrapper);
                
                if (dict != null) {
                    return dict.getName();
                }
            }
        }
        return null;
    }
    
    /**
     * 通过dict_code查询它的下层节点
     *
     * @param dictCode
     * @return
     */
    @Override
    public List<Dict> findChildrenByDictCode(String dictCode) {
        Dict dict = getDictByDictCode(dictCode);
        if (dict != null) {
            return getDataById(dict.getId());
        }
        return Collections.emptyList();
    }
    
    /**
     * 通过dict_code获取dict
     *
     * @param dictCode
     * @return
     */
    private Dict getDictByDictCode(String dictCode) {
        
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getDictCode, dictCode);
    
        return baseMapper.selectOne(wrapper);
    }
    
    /**
     * 通过parent_id获取dict
     *
     * @param id
     * @return 传入id下的所有子节点dict
     */
    @Override
    public List<Dict> findByParentId(String id) {
        
        Dict dict = baseMapper.selectOne(new LambdaQueryWrapper<Dict>().eq(Dict::getId,id));
        
        if (dict != null) {
            return getDataById(dict.getId());
        }
        return Collections.emptyList();
    }
}






















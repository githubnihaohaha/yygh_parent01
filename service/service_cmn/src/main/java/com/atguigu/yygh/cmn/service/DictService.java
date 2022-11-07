package com.atguigu.yygh.cmn.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * @author: Wei
 * @date: 2022/10/21,11:46
 * @description:
 */
public interface DictService extends IService<Dict> {
    
    /**
     * 根据传入的id查询所有子节点并检查它的字节点下时候还有子节点
     *
     * @param id
     * @return 当前id下的所有子节点集合
     */
    List<Dict> getDataById(Long id);
    
    /**
     * 导出数据字典
     *
     * @param response
     */
    void exportDictData(HttpServletResponse response);
    
    /**
     * 导入数据字典
     *
     * @param file
     * @param dictService
     */
    void importData(MultipartFile file, DictService dictService);
    
    /**
     * 通过value查询对应的名称
     * 如果value不唯一,需要传入dict_code做精确定位
     *
     * @param dictCode 非必要,在value有重复时传入
     * @param value
     * @return name字段, 可能为null
     */
    String getNameByValue(String dictCode, String value);
    
    /**
     * 通过dict_code查询它的下层节点
     *
     * @param dictCode
     * @return 所有parent_id为该dict的集合, 这个集合可以为空
     */
    List<Dict> findChildrenByDictCode(String dictCode);
    
    /**
     * 通过parent_id获取dict
     *
     * @param id
     * @return 传入id下的所有子节点dict
     */
    List<Dict> findByParentId(String id);
}

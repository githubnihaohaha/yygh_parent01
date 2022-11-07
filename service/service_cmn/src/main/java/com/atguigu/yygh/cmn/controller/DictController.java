package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/21,12:04
 * @description:
 */
@RestController
@Api(tags = "数组字典接口")
@RequestMapping("/cmn/dict")
public class DictController {
    
    @Autowired
    private DictService dictService;
    
    /**
     * 通过传入的id查询
     * @param id
     * @return
     */
    @GetMapping("/findChildData/{id}")
    public R findByParentId(@PathVariable String id){
        List<Dict> list = dictService.findByParentId(id);
        return R.ok().data("list",list);
    }
    
    
    /**
     * 根据dictCode获取下级节点
     *
     * @param dictCode
     * @return
     */
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findChildrenByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findChildrenByDictCode(dictCode);
        return R.ok().data("list", list);
    }
    
    
    /**
     * 数据字典导出
     *
     * @param file
     * @return
     */
    @ApiOperation("导入")
    @PostMapping("/importData")
    public R importDictData(MultipartFile file) { // 这个类的名字要与前端<input type='file' name='file'/>的name相同
        dictService.importData(file, dictService);
        return R.ok();
    }
    
    
    /**
     * 前台懒加载的方式,所以后台每次只查询一层数据
     * select * from dict where parent_id = ?
     *
     * @param id
     * @return
     */
    @ApiOperation("数据字典列表接口")
    @GetMapping("/findDataById/{id}")
    public R findDataById(@PathVariable Long id) {
        List<Dict> list = dictService.getDataById(id);
        return R.ok().data("list", list);
    }
    
    
    /**
     * 数字字典导出
     *
     * @param response
     */
    @ApiOperation(value = "导出")
    @GetMapping(value = "/exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportDictData(response);
    }
    
    
    /**
     * 根据上传节点和value值定位字典名称
     *
     * @param parentDictCode
     * @param value
     * @return
     */
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @ApiParam(name = "parentDictCode", value = "上级编码", required = true)
            @PathVariable("parentDictCode") String parentDictCode,
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByValue(parentDictCode, value);
    }
    
    
    /**
     * 根据dict的value字段获取数据字典名称
     *
     * @param value
     * @return
     */
    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @ApiParam(name = "value", value = "值", required = true)
            @PathVariable("value") String value) {
        return dictService.getNameByValue("", value);
    }
}































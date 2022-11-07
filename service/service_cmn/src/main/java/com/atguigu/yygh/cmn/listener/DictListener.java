package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Wei
 * @date: 2022/10/22,9:16
 * @description:
 */
@Component
public class DictListener extends AnalysisEventListener<DictEeVo> {
    
    @Autowired
    private DictMapper dictMapper;
    
    //一行一行的数据,读取到后要执行的业务处理
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        //将dictEeVo的数据存放到数据库中,调用Mapper方法,但是Mapper的泛型为Dict,所以复制一下它的值即可
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        
        //将表格的数据添加到数据库中
        dictMapper.insert(dict);
    
    }
    
    /*
    * 所有操作执行完毕后执行
    * */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    
    }
}

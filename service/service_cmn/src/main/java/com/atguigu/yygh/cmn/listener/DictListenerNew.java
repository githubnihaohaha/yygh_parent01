package com.atguigu.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @author: Wei
 * @date: 2022/10/22,10:20
 * @description: 不用Spring管理的监听器
 */

public class DictListenerNew extends AnalysisEventListener<DictEeVo> {
    
    //创建一个有参构造,通过这个有参构造进行Mapper传递
    private DictService dictService;
    
    //设置多少条记录提交
    private static final int BATCH_COUNT = 100;
    
    //创建缓存集合
    private List<Dict> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    
    
    public DictListenerNew(DictService dictService) {
        this.dictService = dictService;
    }
    
    //一行一行的数据,读取到后要执行的业务处理
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
    
        //将dictEeVo的数据存放到数据库中,调用Mapper方法,但是Mapper的泛型为Dict,所以复制一下它的值即可
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
    
        //将这行数据存入缓存集合中
        cachedDataList.add(dict);
        
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            
            //储存完毕后清理List
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
        
        
    }
    
    private void saveData() {
        dictService.saveBatch(cachedDataList);
    }
    
    /*
     * 所有操作执行完毕后执行
     * */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
    
        //确保所有数据都存储成功
        saveData();
    }
}

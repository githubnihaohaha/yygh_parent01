package com.atguigu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Wei
 * @date: 2022/11/1,21:49
 * @description:
 */
public interface FileService {
    
    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    String upload(MultipartFile file);
}

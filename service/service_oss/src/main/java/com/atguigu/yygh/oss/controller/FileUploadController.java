package com.atguigu.yygh.oss.controller;

import com.atguigu.yygh.common.result.R;
import com.atguigu.yygh.oss.service.FileService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: Wei
 * @date: 2022/11/1,21:51
 * @description:
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/admin/oss/file")
public class FileUploadController {
    
    @Autowired
    private FileService fileService;
    
    @PostMapping("/upload")
    public R upload(MultipartFile file){
        String url = fileService.upload(file);
        return R.ok().data("url",url);
    }
}

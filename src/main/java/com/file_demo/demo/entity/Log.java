package com.file_demo.demo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Log {
    private Integer id;
    //文件名称
    private String name;
    //文件类型
    private String type;
    // 文件大小
    private Integer size;
    //上传时间
    private Date date;
    // 映射
    // private String mapping;

}

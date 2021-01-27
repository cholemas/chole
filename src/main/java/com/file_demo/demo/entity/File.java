package com.file_demo.demo.entity;

import lombok.Data;

import java.util.Date;
/**
 * @author baichengwei
 * @date 2020/7/11 15:19
 */
@Data
public class File {

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

    private transient byte[] body;

    private String logId;
}

package com.file_demo.demo.service;

import com.file_demo.demo.entity.File;
import org.springframework.stereotype.Repository;
@Repository
public interface FileService {

    /**
     * 保存文件
     * @param file
     * @return
     */
    int save(File file);

    /**
     * 根据name获取文件
     * @param name
     * @return
     */
    File findByName(String name);


    /**
     * 根据name 删除
     * @param name
     * @return
     */
    int deleteByName(String name);

}
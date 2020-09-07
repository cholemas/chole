package com.file_demo.demo.service;

import com.file_demo.demo.entity.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FileServiceImpl implements FileService {

    @Autowired
    FileService fileService;

    public int save(File file) {
           return fileService.save(file);
    }

    public File findByName(String name) {
          return fileService.findByName(name);
    }

    public int deleteByName(String name) {
        return fileService.deleteByName(name);
    }

}

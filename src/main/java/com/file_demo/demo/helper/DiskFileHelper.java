package com.file_demo.demo.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author baichengwei
 * @date 2020/7/13 15:20
 */
@Component("DiskFileHelper")
public class DiskFileHelper implements FileHelper {

    @Value("${disk.file.path}")
    private String path;

    @Override
    public boolean save(String key, byte[] body) throws IOException {
        Files.write(Paths.get(path + "/" + key), body);
        return true;
    }

    @Override
    public byte[] findByKey(String key) throws IOException {
        return Files.readAllBytes(Paths.get(path + "/" + key));
    }

    @Override
    public int deleteAllByKeys(String[] keys) throws IOException {
        for(String key : keys) {
            Files.deleteIfExists(Paths.get(key));
        }
        return keys.length;
    }
}

package com.file_demo.demo.helper;


import java.io.IOException;

public interface FileHelper {

    boolean save(String key, byte[] body) throws IOException;

    byte[] findByKey(String key) throws IOException;

    int deleteAllByKeys(String[] keys) throws IOException;

}
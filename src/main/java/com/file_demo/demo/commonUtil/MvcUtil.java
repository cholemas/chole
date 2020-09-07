package com.file_demo.demo.commonUtil;

import org.springframework.http.HttpHeaders;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
/**
 * @author baichengwei
 * @date 2020/7/15 15:18
 */
public class MvcUtil {

    /**
     * 下载数据
     */
    public static void downloadData(HttpServletResponse response, byte[] data, String fileName) {
        try{
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; fileName=" + new String(fileName.getBytes("utf-8"), "ISO-8859-1"));
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            handleData(response, data);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 查看数据
     */
    public static void viewData(HttpServletResponse response, byte[] data, String fileName) {
        try{
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "fileName=" + new String(fileName.getBytes("utf-8"), "ISO-8859-1"));
            handleData(response, data);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /** 处理数据 */
    private static void handleData(HttpServletResponse response, byte[] data) throws IOException{
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(HttpHeaders.CONTENT_LENGTH, data.length + "");
        response.setHeader(HttpHeaders.CONNECTION, "close");
        try(ServletOutputStream outputStream = response.getOutputStream()) {
            // 使用jdk1.7 try resource自动关闭流
            outputStream.write(data);
            outputStream.flush();
        }
    }

}

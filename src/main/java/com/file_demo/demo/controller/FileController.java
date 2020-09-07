package com.file_demo.demo.controller;

import com.file_demo.demo.commonUtil.MvcUtil;
import com.file_demo.demo.entity.File;
import com.file_demo.demo.helper.FileHolder;
import com.file_demo.demo.rout.Result;
import com.file_demo.demo.service.FileServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author baichengwei
 * @date 2020/7/11 09:21
 */
@Controller
@Api(tags = "文件操作相关接口")
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileServiceImpl fileServiceImpl;
    @Autowired
    private FileHolder fileHolder;

    @ApiIgnore
    @GetMapping("/index")
    @ResponseBody
    public String fileDemo(){
        return "success!";
    }

    /**
     * 查询文件
     */
    @GetMapping("/selectInfo/{name}")
    @ApiOperation("查询文件的接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "要查询的文件名(支持模糊查询)")
    })
    @ResponseBody
    public Result selectInfo( @PathVariable String name) {
        File page =  fileServiceImpl.findByName(name);
        if(page == null){
            return  Result.error("文件不存在", null);
        }
        return Result.success("请求成功", page);
    }

    /**
     * 在线显示文件
     */
    @GetMapping("/view/{name}")
    @ApiOperation("在线显示文件接口")
    @ApiImplicitParam(name = "name",value = "文件名")
    @ResponseBody
    public void view(@PathVariable String name, HttpServletResponse response)  {
        File file = fileServiceImpl.findByName(name);
        if(file != null) {
            byte[] bytes = fileHolder.findByKey(file.getName());
            MvcUtil.viewData(response, bytes, file.getName());
        }
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{name}")
    @ApiOperation("根据文件名下载文件的接口")
    @ApiImplicitParam(name = "name",value = "文件名")
    @ResponseBody
    public void download(@PathVariable String name, HttpServletResponse response) throws IOException{
         File file = fileServiceImpl.findByName(name);
        if(file != null) {
            byte[] bytes = fileHolder.findByKey(file.getName());
            MvcUtil.downloadData(response, bytes, file.getName());
        }
    }

    /**
     * 删除,根据名称删除
     */
    @DeleteMapping("/delete/{name}")
    @ApiOperation("根据文件名删除文件的接口")
    @ApiImplicitParam(name = "name",value = "请输入完整的文件名")
    @ResponseBody
    public Result delete(@PathVariable String name)  {
        File file = fileServiceImpl.findByName(name);
        if(file == null) {
            return Result.error("文件不存在", null);
        }
        fileHolder.deleteAllByKeys(new String[]{ name });
         fileServiceImpl.deleteByName(file.getName());
        return Result.SUCCESS;
    }
    /**
     * 上传
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传的接口")
    @ResponseBody
    public Result upload(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) throws IOException {
        // 获取文件名
        String filename = multipartFile.getOriginalFilename();
        if(fileServiceImpl.findByName(filename) != null) {
            return Result.error("文件名重复，请更名再上传", null);
        }
        byte[] bytes = multipartFile.getBytes();
        fileHolder.save(filename, bytes);
        File file = new File();
        file.setName(filename);
        file.setType(filename.substring(filename.lastIndexOf(".") + 1));
        file.setSize(bytes.length);
        file.setDate(new Date());
        fileServiceImpl.save(file);
        Map<String, String> dataMap = new HashMap<>(2);
        return Result.success("操作成功", dataMap);
    }
    @GetMapping("/")
    public Result test(){
        return Result.success("查询成功",null);
    }
}

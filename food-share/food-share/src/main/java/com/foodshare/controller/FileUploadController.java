package com.foodshare.controller;

import com.foodshare.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 统一文件上传控制器
 * 解决 Base64 导致数据库卡死的问题，将图片真实存在服务器本地
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    // 动态获取当前项目的根目录，并在下面建一个 uploads 文件夹存图片
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传失败，请选择文件");
        }

        try {
            // 1. 确保上传目录存在
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. 生成极其安全、不会重复的随机文件名 (例如: 8a9b2c..._原名.jpg)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString().replaceAll("-", "") + extension;

            // 3. 将前端传来的文件，真实保存到服务器本地硬盘
            File dest = new File(UPLOAD_DIR + newFileName);
            file.transferTo(dest);

            // 4. 返回给前端一个可以直接访问的图片 URL 地址
            String imgUrl = "http://localhost:8080/uploads/" + newFileName;

            return Result.success(imgUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}

package com.foodshare.controller;

import com.foodshare.common.Result;
import org.apache.ibatis.annotations.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.foodshare.mapper.CategoryMapper;
import java.util.Map;

@RestController
@RequestMapping("/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping("/list")
    public Result list() {
        return Result.success(categoryMapper.findAll());
    }

    @PostMapping("/add")
    public Result add(@RequestBody Map<String, String> params) {
        categoryMapper.insert(params.get("name"));
        return Result.success("添加成功");
    }

    @PostMapping("/delete")
    public Result delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // 修改分类名称
    @PostMapping("/update")
    public Result update(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        String name = params.get("name").toString();
        categoryMapper.updateById(id, name);
        return Result.success("修改成功");
    }
}
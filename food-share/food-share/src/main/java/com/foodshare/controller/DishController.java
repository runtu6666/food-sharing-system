package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.Dish;
import com.foodshare.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品控制器
 * 职责: 处理商家菜品管理的HTTP请求
 */
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 获取指定店铺的菜品列表
     * @param shopId 店铺主键ID
     * @return 包含菜品集合的统一返回结果
     */
    @GetMapping("/list")
    public Result getDishList(@RequestParam Integer shopId) {
        List<Dish> list = dishService.listByShopId(shopId);
        return Result.success(list);
    }

    /**
     * 保存或更新菜品数据
     * @param dish 菜品实体对象
     * @return 操作成功提示
     */
    @PostMapping("/save")
    public Result saveDish(@RequestBody Dish dish) {
        dishService.save(dish);
        return Result.success("保存成功");
    }

    /**
     * 根据主键ID删除菜品
     * @param id 菜品主键ID
     * @return 操作成功提示
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteDish(@PathVariable Integer id) {
        dishService.removeById(id);
        return Result.success("删除成功");
    }
}
package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.Dish;
import com.foodshare.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishMapper dishMapper;

    @GetMapping("/list")
    public Result getDishList(@RequestParam Integer shopId) {
        List<Dish> list = dishMapper.findByShopId(shopId);
        return Result.success(list);
    }

    @PostMapping("/save")
    public Result saveDish(@RequestBody Dish dish) {
        if (dish.getId() == null) {
            if (dish.getStatus() == null) {
                dish.setStatus(1);
            }
            dishMapper.insert(dish);
        } else {
            dishMapper.update(dish);
        }
        return Result.success("保存成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteDish(@PathVariable Integer id) {
        dishMapper.deleteById(id);
        return Result.success("删除成功");
    }
}

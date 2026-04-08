package com.foodshare.service;

import com.foodshare.entity.Dish;
import com.foodshare.mapper.DishMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品业务逻辑处理类
 */
@Service
public class DishService {

    @Autowired
    private DishMapper dishMapper;

    /**
     * 获取指定店铺的所有菜品
     * @param shopId 店铺ID
     * @return 菜品列表
     */
    public List<Dish> listByShopId(Integer shopId) {
        return dishMapper.findByShopId(shopId);
    }

    /**
     * 保存菜品 (如果ID为空则执行插入，否则执行更新)
     * @param dish 菜品实体对象
     */
    public void save(Dish dish) {
        if (dish.getId() == null) {
            // 默认设置为上架状态
            if (dish.getStatus() == null) {
                dish.setStatus(1);
            }
            dishMapper.insert(dish);
        } else {
            dishMapper.update(dish);
        }
    }

    /**
     * 根据ID删除菜品
     * @param id 菜品主键ID
     */
    public void removeById(Integer id) {
        dishMapper.deleteById(id);
    }
}
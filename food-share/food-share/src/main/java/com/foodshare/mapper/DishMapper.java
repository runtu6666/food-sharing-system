package com.foodshare.mapper;

import com.foodshare.entity.Dish;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 菜品数据访问接口
 * 职责: 负责菜品记录的增删改查
 */
@Mapper
public interface DishMapper {

    /**
     * 根据店铺ID查询该店铺下的所有菜品
     * @param shopId 店铺ID
     * @return 菜品列表
     */
    @Select("SELECT *, discount_price AS discountPrice FROM dish WHERE shop_id = #{shopId} ORDER BY create_time DESC")
    List<Dish> findByShopId(@Param("shopId") Integer shopId);

    /**
     * 新增菜品
     * @param dish 菜品实体对象
     */
    @Insert("INSERT INTO dish(shop_id, name, price, discount_price, image, description, status, create_time) " +
            "VALUES(#{shopId}, #{name}, #{price}, #{discountPrice}, #{image}, #{description}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);

    /**
     * 更新菜品信息
     * @param dish 菜品实体对象
     */
    @Update("UPDATE dish SET name = #{name}, price = #{price}, discount_price = #{discountPrice}, image = #{image}, " +
            "description = #{description}, status = #{status} WHERE id = #{id}")
    void update(Dish dish);

    /**
     * 删除指定菜品
     * @param id 菜品ID
     */
    @Delete("DELETE FROM dish WHERE id = #{id}")
    void deleteById(@Param("id") Integer id);
}
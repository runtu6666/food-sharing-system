package com.foodshare.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category ORDER BY id DESC")
    List<Map<String, Object>> findAll();

    // 新增分类时，sort设为当前最大sort+1，保证排在最后
    @Insert("INSERT INTO category(name, sort) SELECT #{name}, IFNULL(MAX(sort),0)+1 FROM category")
    void insert(String name);

    @Delete("DELETE FROM category WHERE id=#{id}")
    void deleteById(Long id);

    // 修改分类名称
    @Update("UPDATE category SET name=#{name} WHERE id=#{id}")
    void updateById(@Param("id") Long id, @Param("name") String name);
}

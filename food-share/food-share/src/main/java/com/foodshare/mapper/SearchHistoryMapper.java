package com.foodshare.mapper;

import com.foodshare.entity.SearchHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 搜索历史Mapper接口
 * 负责用户搜索历史的数据库操作
 */
@Mapper
public interface SearchHistoryMapper {

    /**
     * 插入一条搜索历史记录
     * @param userId     用户ID
     * @param keyword    搜索关键词
     * @param searchType 搜索类型（1-笔记 2-店铺）
     */
    @Insert("INSERT INTO search_history (user_id, keyword, search_type, create_time) " +
            "VALUES (#{userId}, #{keyword}, #{searchType}, NOW())")
    void insert(@Param("userId") Long userId,
                @Param("keyword") String keyword,
                @Param("searchType") Integer searchType);

    /**
     * 查询某用户的搜索历史（最近10条，去重）
     * @param userId 用户ID
     * @return 搜索历史列表
     */
    @Select("SELECT keyword FROM search_history " +
            "WHERE user_id = #{userId} " +
            "GROUP BY keyword " +
            "ORDER BY MAX(create_time) DESC LIMIT 10")
    List<String> findByUserId(@Param("userId") Long userId);

    /**
     * 删除某用户的全部搜索历史
     * @param userId 用户ID
     */
    @Delete("DELETE FROM search_history WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}
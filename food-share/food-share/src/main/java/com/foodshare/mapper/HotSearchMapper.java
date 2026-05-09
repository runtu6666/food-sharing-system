package com.foodshare.mapper;

import com.foodshare.entity.HotSearch;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 热搜词Mapper接口
 * 负责热搜词统计的数据库操作
 */
@Mapper
public interface HotSearchMapper {

    /**
     * 新增或更新热搜词计数
     * 关键词存在则搜索次数+1，不存在则新插入一条
     * @param keyword 搜索关键词
     */
    @Insert("INSERT INTO hot_search (keyword, search_count, update_time) VALUES (#{keyword}, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE search_count = search_count + 1, update_time = NOW()")
    void upsert(@Param("keyword") String keyword);

    /**
     * 查询热搜词TOP10
     * @return 热搜词列表（按搜索次数降序）
     */
    @Select("SELECT keyword, search_count FROM hot_search ORDER BY search_count DESC LIMIT 10")
    List<HotSearch> findTop10();
}

package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.mapper.HotSearchMapper;
import com.foodshare.mapper.SearchHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索历史和热搜词控制器
 * 提供用户搜索历史查询/删除、热搜TOP10接口
 */
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SearchHistoryController {

    @Autowired
    private SearchHistoryMapper searchHistoryMapper;

    @Autowired
    private HotSearchMapper hotSearchMapper;

    /**
     * 获取当前用户的搜索历史（最近10条去重）
     * GET /search/history?userId=1
     */
    @GetMapping("/history")
    public Result history(@RequestParam Long userId) {
        List<String> list = searchHistoryMapper.findByUserId(userId);
        return Result.success(list);
    }

    /**
     * 删除当前用户全部搜索历史
     * DELETE /search/history/clear?userId=1
     */
    @DeleteMapping("/history/clear")
    public Result clearHistory(@RequestParam Long userId) {
        searchHistoryMapper.deleteByUserId(userId);
        return Result.success(null, "搜索历史已清空");
    }

    /**
     * 获取热搜词TOP10
     * GET /search/hot
     */
    @GetMapping("/hot")
    public Result hotList() {
        return Result.success(hotSearchMapper.findTop10());
    }
}

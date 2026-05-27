package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.SensitiveWord;
import com.foodshare.mapper.SensitiveWordMapper;
import com.foodshare.utils.DFAFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 后台敏感词管理控制器
 * 职责：提供敏感词的 CRUD 接口，并在数据变动时，实时触发 DFA 算法的热更新
 */
@RestController
@RequestMapping("/admin/sensitive-word")
@CrossOrigin
public class SensitiveWordController {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private DFAFilterUtil dfaFilterUtil;

    /**
     * 查询所有敏感词列表
     */
    @GetMapping("/list")
    public Result list() {
        List<SensitiveWord> list = sensitiveWordMapper.findAll();
        return Result.success(list);
    }

    /**
     * 添加新的敏感词
     * 请求参数: {"word": "发票"}
     */
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, String> params) {
        String word = params.get("word");
        if (word == null || word.trim().isEmpty()) {
            return Result.error("词条不能为空");
        }

        // type 默认 0（敏感词），传 "1" 则为否定词
        int type = "1".equals(params.get("type")) ? 1 : 0;

        sensitiveWordMapper.insert(word.trim(), type);
        dfaFilterUtil.reload();

        String label = type == 1 ? "否定词" : "敏感词";
        return Result.success("添加" + label + "成功，DFA 词库已热更新！");
    }

    /**
     * 删除敏感词
     * 请求参数: {"id": 1}
     */
    @PostMapping("/delete")
    public Result delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());

        // 1. 从数据库中删除
        sensitiveWordMapper.deleteById(id);

        // 2. 🔥 核心联动：删除后同样需要热更新内存字典树，释放被误杀的词汇
        dfaFilterUtil.reload();

        return Result.success("删除成功，DFA 词库已热更新！");
    }
}
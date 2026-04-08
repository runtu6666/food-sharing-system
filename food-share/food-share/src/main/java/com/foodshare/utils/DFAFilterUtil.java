package com.foodshare.utils;

import com.foodshare.mapper.SensitiveWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 顶级敏感词过滤工具类 (基于 DFA 确定有穷自动机 + 数据库动态热更新)
 * 核心架构优势：
 * 1. O(N) 极速匹配：查询时间仅与被检测文本长度有关，不受千万级庞大词库影响。
 * 2. 无缝热更新：在不阻塞用户正常请求的前提下，平滑替换内存中的 Trie 树。
 */
@Component
public class DFAFilterUtil {

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    // 核心 DFA 状态机容器 (Trie字典树的根节点)
    private Map<Object, Object> sensitiveWordMap;

    /**
     * @PostConstruct 确保 Spring 容器在实例化当前组件后，立刻执行该方法。
     * 即：系统一启动，就自动从数据库拉取词库构建状态机。
     */
    @PostConstruct
    public void init() {
        reload(); // 复用热更新逻辑进行首次初始化
    }

    /**
     * 🔥 核心大招：热更新内存字典树
     * 每次管理员在后台增删敏感词后调用此方法，系统会瞬间重新构建 Trie 树。
     */
    public void reload() {
        // 1. 从 MySQL 数据库中拉取最新的全量敏感词列表
        List<String> words = sensitiveWordMapper.findAllWords();

        // 2. 在【局部变量】中构建新的字典树。
        // 为什么要用局部变量？为了防止在构建(耗时操作)的过程中，影响前台用户的正常发帖！
        Map<Object, Object> newMap = new HashMap<>(Math.max(words.size(), 16));

        for (String word : words) {
            Map<Object, Object> nowMap = newMap;
            // 逐个字符拆解敏感词，构建 Trie 树的各个层级节点
            for (int i = 0; i < word.length(); i++) {
                char keyChar = word.charAt(i);
                Object tempMap = nowMap.get(keyChar);

                if (tempMap != null) {
                    // 节点已存在，指针向下推移
                    nowMap = (Map<Object, Object>) tempMap;
                } else {
                    // 节点不存在，构建新的 HashMap 分支
                    Map<Object, Object> newCharMap = new HashMap<>();
                    newCharMap.put("isEnd", "0"); // 默认 0，表示不是词汇的末尾
                    nowMap.put(keyChar, newCharMap);
                    nowMap = newCharMap; // 指针推移到新节点
                }

                // 如果当前字符是该敏感词的最后一个字，打上完结标记
                if (i == word.length() - 1) {
                    nowMap.put("isEnd", "1"); // 1 表示触达敏感词末尾
                }
            }
        }

        // 3. 【原子操作】：将旧的字典树指针直接指向刚刚构建好的新树！
        // 这一步极其平滑，瞬间完成旧词库到新词库的切换，完全不需要重启服务器。
        this.sensitiveWordMap = newMap;
        System.out.println("🔥 DFA 敏感词库热更新完成，当前词汇量：" + words.size());
    }

    /**
     * 🤖 机器初审：检查文本中是否包含敏感词 (用于拦截非法发布)
     * 特性：只要命中任何一个敏感词，立刻中断扫描返回 true，极致节省 CPU。
     * * @param text 待检测的用户文本
     * @return true(包含违规内容) / false(纯净安全)
     */
    public boolean containsSensitiveWord(String text) {
        // 健壮性校验：文本为空，或词库加载失败时，直接放行
        if (text == null || text.trim().isEmpty() || sensitiveWordMap == null || sensitiveWordMap.isEmpty()) {
            return false;
        }

        // 外层循环：确定文本扫描的起始位置
        for (int i = 0; i < text.length(); i++) {
            Map<Object, Object> nowMap = sensitiveWordMap;
            // 内层循环：从当前位置开始，向后探测字典树
            for (int j = i; j < text.length(); j++) {
                char word = text.charAt(j);
                nowMap = (Map<Object, Object>) nowMap.get(word);

                if (nowMap != null) {
                    // 发现字符在字典树中！检查是否到达叶子节点（敏感词结尾）
                    if ("1".equals(nowMap.get("isEnd"))) {
                        return true; // 命中敏感词！立刻拦截！
                    }
                } else {
                    // 状态链断裂，说明以当前字符开头的词不是敏感词，跳出内层探测
                    break;
                }
            }
        }
        return false;
    }

    /**
     * 🛡️ 过滤净化：将文本中的敏感词全部替换为星号 (用于评论打码)
     * * @param text 待处理的原始文本
     * @return 替换敏感词后的安全文本 (如：这玩意真***)
     */
    public String filter(String text) {
        if (text == null || text.trim().isEmpty() || sensitiveWordMap == null || sensitiveWordMap.isEmpty()) {
            return text;
        }
        String resultTxt = text;

        for (int i = 0; i < text.length(); i++) {
            int matchFlag = 0; // 记录当前匹配到的敏感词长度
            boolean flag = false; // 是否完整命中了一个敏感词
            Map<Object, Object> nowMap = sensitiveWordMap;

            for (int j = i; j < text.length(); j++) {
                char word = text.charAt(j);
                nowMap = (Map<Object, Object>) nowMap.get(word);

                if (nowMap != null) {
                    matchFlag++;
                    if ("1".equals(nowMap.get("isEnd"))) {
                        flag = true; // 完整命中！
                    }
                } else {
                    break; // 匹配中断
                }
            }

            // 如果完整命中敏感词，执行打码替换操作
            if (flag) {
                StringBuilder replaceStr = new StringBuilder();
                for (int k = 0; k < matchFlag; k++) {
                    replaceStr.append("*"); // 根据敏感词长度生成对应数量的星号
                }
                // 使用星号替换原文中的敏感片段
                resultTxt = resultTxt.replaceAll(text.substring(i, i + matchFlag), replaceStr.toString());
                // 将外层扫描指针直接跳过已打码的区域，避免重复检测，提升性能
                i = i + matchFlag - 1;
            }
        }
        return resultTxt;
    }
}
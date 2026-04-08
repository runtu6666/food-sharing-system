package com.foodshare.entity;

import java.time.LocalDateTime;

/**
 * 敏感词实体类
 * 对应数据库中的 sensitive_word 表，用于数据传输与映射
 */
public class SensitiveWord {
    private Long id;                // 敏感词唯一ID
    private String word;            // 敏感词内容
    private LocalDateTime createTime; // 添加到词库的时间

    // 标准的 Getter 和 Setter 方法，供 MyBatis 自动封装数据使用
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
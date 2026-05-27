package com.foodshare.entity;

import java.time.LocalDateTime;

/**
 * 敏感词实体类
 * 对应数据库中的 sensitive_word 表，用于数据传输与映射
 */
public class SensitiveWord {
    private Long id;
    private String word;
    private Integer type;             // 0=敏感词  1=否定词
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
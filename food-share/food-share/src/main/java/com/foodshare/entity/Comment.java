package com.foodshare.entity;

import java.time.LocalDateTime;

/**
 * 笔记评论实体类
 * 对应数据库表: comment
 * 作用: 封装用户对笔记的评论信息,支持多人回复
 */
public class Comment {

    /**
     * 评论ID - 主键,自增
     */
    private Long id;

    /**
     * 笔记ID - 外键关联note表
     * 说明: 该评论属于哪篇笔记
     */
    private Long noteId;

    /**
     * 评论用户ID - 外键关联user表
     * 说明: 是哪个用户发表的评论
     */
    private Long userId;

    /**
     * 评论内容
     * 长度限制: 500字符
     * 示例: "太好吃了,下次还来!", "请问营业时间是?"
     */
    private String content;

    /**
     * 回复内容(多人回复)
     * 存储格式: 按行分隔的回复列表
     * 示例:
     * "张三: 确实好吃
     *  李四: 我也想去
     *  商家: 欢迎光临"
     *
     * 说明:
     * - 每次新增回复时,在原内容后追加一行
     * - 前端展示时按行分割显示
     */
    private String reply;

    /**
     * 评论时间
     */
    private LocalDateTime createTime;

    // ==================== 关联查询字段(不存数据库) ====================

    /**
     * 评论用户昵称 - 从user表关联查询
     * 用途: 前端展示"张三 评论了:"
     */
    private String nickname;

    /**
     * 评论用户头像 - 从user表关联查询
     * 用途: 前端展示评论者头像
     */
    private String avatar;

    /**
     * 笔记标题 - 从note表关联查询
     * 用途: 在"收到的评论"列表中显示是哪篇笔记的评论
     */
    private String noteTitle;

    // ==================== 构造方法 ====================

    /**
     * 无参构造方法
     * MyBatis查询时需要
     */
    public Comment() {
    }

    /**
     * 常用字段构造方法
     * 用途: 快速创建评论对象
     */
    public Comment(Long noteId, Long userId, String content) {
        this.noteId = noteId;
        this.userId = userId;
        this.content = content;
    }

    // ==================== Getter和Setter方法 ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", noteId=" + noteId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
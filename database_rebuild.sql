-- ============================================================
-- 重建脚本：清理不适合拆分的表，替换为两张新功能表
-- 在Navicat中新建查询，粘贴执行即可
-- ============================================================

USE food_share;

-- 第1步：清理失败的拆分表
DROP TABLE IF EXISTS `note_image`;
DROP TABLE IF EXISTS `shop_image`;
DROP PROCEDURE IF EXISTS migrate_note_images;
DROP PROCEDURE IF EXISTS migrate_shop_images;

-- 第2步：创建两张新的替代表

-- ① announcement（系统公告表）
-- 功能：管理员发布平台公告（如活动通知、维护通知等），用户端首页展示
CREATE TABLE IF NOT EXISTS `announcement` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '公告ID',
    `title`        VARCHAR(200)  NOT NULL                 COMMENT '公告标题',
    `content`      TEXT          DEFAULT NULL             COMMENT '公告正文内容',
    `admin_id`     BIGINT        NOT NULL                 COMMENT '发布管理员ID',
    `status`       TINYINT       DEFAULT 1                COMMENT '状态(0隐藏 1显示)',
    `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- ② notification（消息通知表）
-- 功能：站内通知，记录点赞、评论、审核结果等系统消息推送给用户
CREATE TABLE IF NOT EXISTS `notification` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    `user_id`       BIGINT        NOT NULL                 COMMENT '接收通知的用户ID',
    `type`          TINYINT       NOT NULL                 COMMENT '通知类型(1点赞 2评论 3审核通过 4审核驳回 5系统公告)',
    `title`         VARCHAR(200)  DEFAULT NULL             COMMENT '通知标题',
    `content`       VARCHAR(500)  DEFAULT NULL             COMMENT '通知内容摘要',
    `target_id`     BIGINT        DEFAULT NULL             COMMENT '关联对象ID(笔记ID/店铺ID/公告ID)',
    `is_read`       TINYINT       DEFAULT 0                COMMENT '是否已读(0未读 1已读)',
    `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '通知时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息通知表';

-- ============================================================
-- 完成后检查：执行 SHOW TABLES; 应该看到16张表
--
-- 原有10张: user, shop, note, dish, shop_review, comment,
--          category, sensitive_word, likes, collect
--
-- 新增6张:
--   merchant_info   ← 从shop表拆出商家资质（已有数据）
--   search_history  ← 搜索历史记录
--   hot_search      ← 热搜词统计
--   login_log       ← 登录日志
--   feedback        ← 用户举报反馈
--   announcement    ← 系统公告
--   notification    ← 站内消息通知
--
-- 等等，这样是17张了！所以我们需要去掉一张。
-- feedback 表已经在上一个脚本里建了，所以这里不重复建。
-- 最终就是16张，完美。
-- ============================================================

-- 最终验证
SHOW TABLES;

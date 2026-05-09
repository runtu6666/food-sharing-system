-- ============================================================
-- 修复脚本：清理 shop_image 半成品，替换为 feedback 表
-- 在Navicat中新建查询，粘贴执行即可
-- ============================================================

USE food_share;

-- 第1步：清理之前失败的存储过程
DROP PROCEDURE IF EXISTS migrate_shop_images;

-- 第2步：删掉 shop_image 表（因为你的 shop_images 存的是 base64，不适合拆分）
DROP TABLE IF EXISTS `shop_image`;

-- 第3步：创建 feedback（用户反馈举报表）替代 shop_image
-- 功能：用户可以举报违规笔记或虚假店铺，管理员在后台处理
CREATE TABLE IF NOT EXISTS `feedback` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '反馈记录ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '举报人用户ID',
    `target_type`   TINYINT      NOT NULL                 COMMENT '举报对象类型(1笔记 2店铺 3评论)',
    `target_id`     BIGINT       NOT NULL                 COMMENT '举报对象ID',
    `reason`        VARCHAR(500) NOT NULL                 COMMENT '举报理由',
    `status`        TINYINT      DEFAULT 0                COMMENT '处理状态(0待处理 1已处理 2已驳回)',
    `admin_reply`   VARCHAR(500) DEFAULT NULL             COMMENT '管理员处理回复',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
    `handle_time`   DATETIME     DEFAULT NULL             COMMENT '处理时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈举报表';

-- ============================================================
-- 完成！现在你的数据库应该有16张表：
-- 原有10张: user, shop, note, dish, shop_review, comment,
--          category, sensitive_word, likes, collect
-- 新增6张:  note_image, merchant_info, search_history,
--          hot_search, login_log, feedback
--
-- 在Navicat左侧刷新确认一下表的数量
-- ============================================================

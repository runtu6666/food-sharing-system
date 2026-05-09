-- ============================================================
-- 数据库升级脚本 —— 从10张表扩展到16张表
-- 数据库: food_share
-- 日期: 2026-05-08
-- 说明: 请在Navicat中打开此文件，全选执行即可
--       执行前请先备份数据库！(右键数据库 → 转储SQL文件)
-- ============================================================

USE food_share;

-- ============================================================
-- 第一部分：拆分表（从现有大表中拆出独立子表）
-- ============================================================

-- ------------------------------------------------------------
-- 1. note_image（笔记图片表）—— 从 note 表的 images 字段拆出
--    原来: note.images 存的是 "url1,url2,url3" 逗号分隔字符串
--    现在: 每张图片单独一行，支持排序
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `note_image` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '图片记录ID',
    `note_id`     BIGINT       NOT NULL                 COMMENT '所属笔记ID',
    `image_url`   VARCHAR(500) NOT NULL                 COMMENT '图片访问URL',
    `sort_order`  INT          DEFAULT 0                COMMENT '图片排序序号(从0开始)',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    INDEX `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记图片表（从note表拆分）';

-- 数据迁移：把 note.images 中已有的逗号分隔URL拆成多行插入 note_image
-- 说明：这段存储过程会自动处理迁移，执行完后旧数据不会丢失
DELIMITER //
CREATE PROCEDURE migrate_note_images()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_note_id BIGINT;
    DECLARE v_images LONGTEXT;
    DECLARE v_image VARCHAR(500);
    DECLARE v_pos INT;
    DECLARE v_sort INT;

    DECLARE cur CURSOR FOR
        SELECT id, images FROM note WHERE images IS NOT NULL AND images != '';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_note_id, v_images;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET v_sort = 0;
        -- 循环按逗号拆分
        WHILE v_images != '' DO
            SET v_pos = LOCATE(',', v_images);
            IF v_pos > 0 THEN
                SET v_image = TRIM(LEFT(v_images, v_pos - 1));
                SET v_images = SUBSTRING(v_images, v_pos + 1);
            ELSE
                SET v_image = TRIM(v_images);
                SET v_images = '';
            END IF;

            -- 只插入非空URL
            IF v_image != '' THEN
                INSERT INTO note_image(note_id, image_url, sort_order)
                VALUES(v_note_id, v_image, v_sort);
                SET v_sort = v_sort + 1;
            END IF;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

-- 执行迁移
CALL migrate_note_images();
-- 清理存储过程
DROP PROCEDURE IF EXISTS migrate_note_images;

-- ------------------------------------------------------------
-- 2. shop_image（店铺图片表）—— 从 shop 表的 shop_images 字段拆出
--    原来: shop.shop_images 存的是 "url1,url2,url3" 逗号分隔字符串
--    现在: 每张图片单独一行
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `shop_image` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '图片记录ID',
    `shop_id`     BIGINT       NOT NULL                 COMMENT '所属店铺ID',
    `image_url`   VARCHAR(500) NOT NULL                 COMMENT '图片访问URL',
    `sort_order`  INT          DEFAULT 0                COMMENT '图片排序序号',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺图片表（从shop表拆分）';

-- 数据迁移：把 shop.shop_images 中已有的数据拆分插入 shop_image
DELIMITER //
CREATE PROCEDURE migrate_shop_images()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_shop_id BIGINT;
    DECLARE v_images LONGTEXT;
    DECLARE v_image VARCHAR(500);
    DECLARE v_pos INT;
    DECLARE v_sort INT;

    DECLARE cur CURSOR FOR
        SELECT id, shop_images FROM shop WHERE shop_images IS NOT NULL AND shop_images != '';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO v_shop_id, v_images;
        IF done THEN
            LEAVE read_loop;
        END IF;

        SET v_sort = 0;
        WHILE v_images != '' DO
            SET v_pos = LOCATE(',', v_images);
            IF v_pos > 0 THEN
                SET v_image = TRIM(LEFT(v_images, v_pos - 1));
                SET v_images = SUBSTRING(v_images, v_pos + 1);
            ELSE
                SET v_image = TRIM(v_images);
                SET v_images = '';
            END IF;

            IF v_image != '' THEN
                INSERT INTO shop_image(shop_id, image_url, sort_order)
                VALUES(v_shop_id, v_image, v_sort);
                SET v_sort = v_sort + 1;
            END IF;
        END WHILE;
    END LOOP;
    CLOSE cur;
END //
DELIMITER ;

CALL migrate_shop_images();
DROP PROCEDURE IF EXISTS migrate_shop_images;

-- ------------------------------------------------------------
-- 3. merchant_info（商家资质信息表）—— 从 shop 表拆出商家资质相关字段
--    原来: shop.legal_name 混在店铺表里
--    现在: 独立成表，并扩展营业执照号、身份证号等资质字段
--    关联: 一个商家用户(user_id)对应一条资质记录
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `merchant_info` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '资质记录ID',
    `user_id`         BIGINT       NOT NULL                 COMMENT '关联商家用户ID',
    `legal_name`      VARCHAR(50)  DEFAULT NULL             COMMENT '法人姓名',
    `id_card`         VARCHAR(20)  DEFAULT NULL             COMMENT '法人身份证号',
    `license_number`  VARCHAR(50)  DEFAULT NULL             COMMENT '营业执照编号',
    `license_image`   VARCHAR(500) DEFAULT NULL             COMMENT '营业执照照片URL',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '资质提交时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近修改时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家资质信息表（从shop表拆分）';

-- 数据迁移：把 shop 表中已有的 legal_name 迁移到 merchant_info
-- 注意：同一个 user_id 可能有多个店铺，这里用 INSERT IGNORE 去重
INSERT IGNORE INTO merchant_info(user_id, legal_name)
SELECT DISTINCT user_id, legal_name
FROM shop
WHERE user_id IS NOT NULL AND legal_name IS NOT NULL AND legal_name != '';

-- ============================================================
-- 第二部分：新增功能表
-- ============================================================

-- ------------------------------------------------------------
-- 4. search_history（搜索历史表）—— "搜索与热搜"功能
--    记录每个用户的搜索关键词，支持搜索历史回显和清空
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `search_history` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '搜索记录ID',
    `user_id`     BIGINT       DEFAULT NULL             COMMENT '搜索用户ID(未登录为NULL)',
    `keyword`     VARCHAR(100) NOT NULL                 COMMENT '搜索关键词',
    `search_type` TINYINT      DEFAULT 1                COMMENT '搜索类型(1笔记搜索 2店铺搜索)',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户搜索历史记录表';

-- ------------------------------------------------------------
-- 5. hot_search（热搜词统计表）—— "搜索与热搜"功能
--    统计各关键词的搜索频次，用于首页热搜展示
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `hot_search` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '热搜记录ID',
    `keyword`      VARCHAR(100) NOT NULL                 COMMENT '搜索关键词',
    `search_count` INT          DEFAULT 1                COMMENT '被搜索次数',
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最近搜索时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门搜索词统计表';

-- ------------------------------------------------------------
-- 6. login_log（登录日志表）—— 并入"登录注册"功能
--    记录每次登录的时间、IP、设备信息，用于安全审计
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `user_id`     BIGINT       NOT NULL                 COMMENT '登录用户ID',
    `login_ip`    VARCHAR(50)  DEFAULT NULL             COMMENT '登录IP地址',
    `device`      VARCHAR(200) DEFAULT NULL             COMMENT '设备/浏览器信息(User-Agent)',
    `login_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `status`      TINYINT      DEFAULT 1                COMMENT '登录结果(1成功 0失败)',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录日志表';

-- ============================================================
-- 第三部分：清理旧表冗余字段（可选，建议数据迁移确认无误后再执行）
-- 如果你想保守一点，可以先不执行这部分，等代码改完测试通过后再执行
-- ============================================================

-- 注意：以下三行是删除旧字段的操作，建议确认迁移数据正确后再取消注释执行
-- ALTER TABLE `note` DROP COLUMN `images`;
-- ALTER TABLE `shop` DROP COLUMN `shop_images`;
-- ALTER TABLE `shop` DROP COLUMN `legal_name`;

-- ============================================================
-- 完成！执行后请检查：
-- 1. 在Navicat左侧刷新，确认6张新表已创建
-- 2. 检查 note_image 表是否有数据（应该和原来note.images的数据一致）
-- 3. 检查 shop_image 表是否有数据
-- 4. 检查 merchant_info 表是否有数据
-- 5. search_history、hot_search、login_log 是空表，这是正常的
-- ============================================================

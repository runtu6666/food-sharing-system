-- ============================================================
-- 检查脚本：查看目前数据库里所有表，以及新表的数据情况
-- 在Navicat中新建查询，粘贴执行即可
-- ============================================================

USE food_share;

-- 1. 查看当前所有表
SHOW TABLES;

-- 2. 检查 note_image 表是否存在且有数据
SELECT 'note_image表' AS 检查项, COUNT(*) AS 记录数 FROM note_image;

-- 3. 检查 merchant_info 表是否存在且有数据
SELECT 'merchant_info表' AS 检查项, COUNT(*) AS 记录数 FROM merchant_info;

-- 4. 检查 search_history 表是否存在
SELECT 'search_history表' AS 检查项, COUNT(*) AS 记录数 FROM search_history;

-- 5. 检查 hot_search 表是否存在
SELECT 'hot_search表' AS 检查项, COUNT(*) AS 记录数 FROM hot_search;

-- 6. 检查 login_log 表是否存在
SELECT 'login_log表' AS 检查项, COUNT(*) AS 记录数 FROM login_log;

-- 7. 检查 note_image 的迁移是否正确：对比原始数据
-- 这里看一下note表中有images数据的笔记数量，和note_image表中有几个不同的note_id
SELECT 'note表有图片的笔记数' AS 检查项, COUNT(*) AS 数量 FROM note WHERE images IS NOT NULL AND images != '';
SELECT 'note_image表涉及的笔记数' AS 检查项, COUNT(DISTINCT note_id) AS 数量 FROM note_image;

-- 8. 随机抽查一条：看原始images字段 vs note_image表的数据是否一致
SELECT n.id AS 笔记ID, n.images AS 原始images字段,
       GROUP_CONCAT(ni.image_url ORDER BY ni.sort_order) AS 迁移后的图片
FROM note n
LEFT JOIN note_image ni ON n.id = ni.note_id
WHERE n.images IS NOT NULL AND n.images != ''
GROUP BY n.id
LIMIT 3;

package com.foodshare.mapper;

import com.foodshare.entity.Note;
import com.foodshare.entity.Comment;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 笔记数据访问接口
 * 职责: 负责笔记相关的数据库操作,包括笔记CRUD、点赞、收藏、评论等
 */
@Mapper
public interface NoteMapper {

    // ==================== 笔记查询功能 ====================

    /**
     * 查询审核通过的笔记列表
     * 支持: 分类筛选、关键词搜索、排序、分页
     *
     * 参数说明:
     * @param categoryId 分类ID(可选,不传则查询所有分类)
     * @param keyword 搜索关键词(可选,模糊匹配标题和内容)
     * @param sort 排序方式(latest-最新, hot-热门)
     * @param shopId 店铺ID(可选,查询某店铺的所有笔记)
     * @param offset 分页偏移量(第1页offset=0, 第2页offset=12)
     * @param userId 当前用户ID(用于判断是否已点赞/收藏)
     * @return 笔记列表(每页12条)
     *
     * 返回字段包含:
     * - 笔记基本信息(标题、内容、图片、评分等)
     * - 发布者信息(nickname、avatar)
     * - 店铺信息(shopName)
     * - 分类信息(categoryName)
     * - 点赞/收藏状态(liked、collected)
     */
    List<Map<String, Object>> findNotes(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("shopId") Long shopId,
            @Param("offset") int offset,
            @Param("userId") Long userId
    );

    /**
     * 查询笔记详情(包含点赞/收藏状态)
     *
     * 说明:
     * 1. LEFT JOIN user表获取发布者昵称和头像
     * 2. 子查询判断当前用户是否已点赞(返回0或1)
     * 3. 子查询判断当前用户是否已收藏(返回0或1)
     *
     * @param id 笔记ID
     * @param userId 当前用户ID
     * @return 笔记详情(如果不存在返回null)
     */
    @Select("SELECT n.*, u.nickname, u.avatar, " +
            "(SELECT COUNT(*) FROM likes WHERE note_id = n.id AND user_id = #{userId}) AS liked, " +
            "(SELECT COUNT(*) FROM collect WHERE note_id = n.id AND user_id = #{userId}) AS collected " +
            "FROM note n LEFT JOIN user u ON n.user_id = u.id " +
            "WHERE n.id = #{id}")
    Map<String, Object> findById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询某用户发布的所有笔记(包含所有审核状态)
     * 使用场景: 个人中心"我的笔记"列表
     * @param userId 用户ID
     * @return 该用户发布的所有笔记(按时间倒序)
     */
    @Select("SELECT * FROM note WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Map<String, Object>> findByUserId(@Param("userId") Long userId);

    /**
     * 查询某用户收藏的所有笔记
     * 使用场景: 个人中心"我的收藏"列表
     *
     * 说明:
     * - 从collect表出发,关联note表和user表
     * - 只查询审核通过的笔记(status=1)
     * - 按收藏时间倒序排列
     *
     * @param userId 用户ID
     * @return 该用户收藏的所有笔记列表
     */
    @Select("SELECT n.*, u.nickname " +
            "FROM collect c " +
            "LEFT JOIN note n ON c.note_id = n.id " +
            "LEFT JOIN user u ON n.user_id = u.id " +
            "WHERE c.user_id = #{userId} AND n.status = 1 " +
            "ORDER BY c.create_time DESC")
    List<Map<String, Object>> findCollectsByUserId(@Param("userId") Long userId);

    /**
     * 根据店铺ID查询关联的所有笔记
     * 使用场景: 商家中心查看"哪些笔记提到了我的店铺"
     * @param shopId 店铺ID
     * @return 关联该店铺的所有笔记(仅审核通过的)
     */
    @Select("SELECT n.*, u.nickname " +
            "FROM note n " +
            "LEFT JOIN user u ON n.user_id = u.id " +
            "WHERE n.shop_id = #{shopId} AND n.status = 1 " +
            "ORDER BY n.create_time DESC")
    List<Map<String, Object>> findByShopId(@Param("shopId") Long shopId);

    // ==================== 笔记管理功能 ====================

    /**
     * 发布探店笔记
     *
     * 说明:
     * - status默认为0(待审核)
     * - 需要管理员审核通过后才能显示给其他用户
     *
     * @param note 笔记对象(包含标题、内容、图片、评分等)
     */
    @Insert("INSERT INTO note(user_id, shop_id, title, content, images, score, category_id, status, create_time) " +
            "VALUES(#{userId}, #{shopId}, #{title}, #{content}, #{images}, #{score}, #{categoryId}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Note note);

    /**
     * 修改笔记(只能修改自己的笔记)
     *
     * 说明:
     * - 修改后status会重置为0(待审核)
     * - WHERE条件同时校验id和user_id,防止修改别人的笔记
     *
     * @param note 笔记对象(必须包含id和userId字段)
     */
    @Update("UPDATE note SET title = #{title}, content = #{content}, score = #{score}, " +
            "images = #{images}, category_id = #{categoryId}, shop_id = #{shopId}, status = 0 " +
            "WHERE id = #{id} AND user_id = #{userId}")
    void updateById(Note note);

    /**
     * 删除笔记(只能删除自己的笔记)
     *
     * @param id 笔记ID
     * @param userId 用户ID
     */
    @Delete("DELETE FROM note WHERE id = #{id} AND user_id = #{userId}")
    void deleteById(@Param("id") Long id, @Param("userId") Long userId);

    // ==================== 评论功能 ====================

    /**
     * 查询笔记的所有评论(按时间升序)
     *
     * 说明: LEFT JOIN user表获取评论者的昵称和头像
     *
     * @param noteId 笔记ID
     * @return 评论列表(包含评论者昵称、头像、评论内容、回复内容)
     */
    @Select("SELECT c.*, u.nickname, u.avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.note_id = #{noteId} " +
            "ORDER BY c.create_time ASC")
    List<Map<String, Object>> findComments(@Param("noteId") Long noteId);

    /**
     * 发表评论
     * @param comment 评论对象(包含noteId、userId、content)
     */
    @Insert("INSERT INTO comment(note_id, user_id, content, create_time) " +
            "VALUES(#{noteId}, #{userId}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertComment(Comment comment);

    /**
     * 追加评论回复(支持多人回复)
     *
     * 业务逻辑:
     * 1. 如果reply字段为空,直接写入第一条回复
     * 2. 如果reply字段不为空,在原内容后追加新回复(用换行符分隔)
     * 3. 这样可以实现"一条评论多次回复"的效果
     *
     * 回复格式: "昵称: 内容"
     *
     * @param commentId 评论ID
     * @param replyLine 回复内容(格式: "张三: 我也觉得很好吃")
     */
    @Update("UPDATE comment SET reply = CASE " +
            "WHEN reply IS NULL OR reply = '' THEN #{replyLine} " +
            "ELSE CONCAT(reply, '\n', #{replyLine}) END " +
            "WHERE id = #{commentId}")
    void appendCommentReply(@Param("commentId") Long commentId, @Param("replyLine") String replyLine);

    /**
     * 根据用户ID查询昵称
     * 使用场景: 评论回复时拼接"昵称: 内容"
     * @param userId 用户ID
     * @return 用户昵称
     */
    @Select("SELECT nickname FROM user WHERE id = #{userId}")
    String findUserNickname(@Param("userId") Long userId);

    // ==================== 点赞功能 ====================

    /**
     * 查询用户是否已点赞某笔记
     * @param noteId 笔记ID
     * @param userId 用户ID
     * @return 点赞数量(0-未点赞, 1-已点赞)
     */
    @Select("SELECT COUNT(*) FROM likes WHERE note_id = #{noteId} AND user_id = #{userId}")
    int checkLike(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 添加点赞记录
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    @Insert("INSERT INTO likes(note_id, user_id, create_time) VALUES(#{noteId}, #{userId}, NOW())")
    void insertLike(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 删除点赞记录(取消点赞)
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    @Delete("DELETE FROM likes WHERE note_id = #{noteId} AND user_id = #{userId}")
    void deleteLike(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 更新笔记点赞数
     *
     * 说明: delta为+1表示点赞数+1, delta为-1表示点赞数-1
     *
     * @param noteId 笔记ID
     * @param delta 变化量(+1或-1)
     */
    @Update("UPDATE note SET like_count = like_count + #{delta} WHERE id = #{noteId}")
    void updateLikeCount(@Param("noteId") Long noteId, @Param("delta") int delta);

    /**
     * 查询作者收到的点赞记录
     * 使用场景: 个人中心"收到的赞"列表
     *
     * 业务逻辑:
     * 1. 从likes表出发,找到所有点赞记录
     * 2. 关联note表,筛选出"我发布的笔记"(n.user_id = #{userId})
     * 3. 关联user表,获取"谁点赞了"(点赞人昵称)
     *
     * @param userId 作者用户ID
     * @return 点赞记录列表(包含笔记标题、点赞人昵称)
     */
    @Select("SELECT l.id, n.id AS noteId, n.title, u.id AS likerId, u.nickname AS likerNickname " +
            "FROM likes l " +
            "LEFT JOIN note n ON l.note_id = n.id " +
            "LEFT JOIN user u ON l.user_id = u.id " +
            "WHERE n.user_id = #{userId} " +
            "ORDER BY l.id DESC")
    List<Map<String, Object>> findLikeUsersByAuthorId(@Param("userId") Long userId);

    // ==================== 收藏功能 ====================

    /**
     * 查询用户是否已收藏某笔记
     * @param noteId 笔记ID
     * @param userId 用户ID
     * @return 收藏数量(0-未收藏, 1-已收藏)
     */
    @Select("SELECT COUNT(*) FROM collect WHERE note_id = #{noteId} AND user_id = #{userId}")
    int checkCollect(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 添加收藏记录
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    @Insert("INSERT INTO collect(note_id, user_id, create_time) VALUES(#{noteId}, #{userId}, NOW())")
    void insertCollect(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 删除收藏记录(取消收藏)
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    @Delete("DELETE FROM collect WHERE note_id = #{noteId} AND user_id = #{userId}")
    void deleteCollect(@Param("noteId") Long noteId, @Param("userId") Long userId);

    /**
     * 更新笔记收藏数
     * @param noteId 笔记ID
     * @param delta 变化量(+1或-1)
     */
    @Update("UPDATE note SET collect_count = collect_count + #{delta} WHERE id = #{noteId}")
    void updateCollectCount(@Param("noteId") Long noteId, @Param("delta") int delta);

    // ==================== 商家管理功能 ====================

    /**
     * 商家回复探店笔记
     * * 安全逻辑:
     * WHERE 条件中强制校验了 id 和 shop_id，确保只有这篇笔记所关联的店铺主人，才能修改这条数据，彻底杜绝越权回复。
     * * @param noteId 笔记ID
     * @param shopId 店铺ID
     * @param reply 商家回复内容
     * @return 更新的行数(0表示笔记不存在或无权限)
     */
    @Update("UPDATE note SET reply = #{reply} WHERE id = #{noteId} AND shop_id = #{shopId}")
    int updateMerchantReply(@Param("noteId") Long noteId, @Param("shopId") Long shopId, @Param("reply") String reply);

    // ==================== 管理员功能 ====================

    /**
     * 查询作者收到的所有互动消息记录（包含普通用户评论与商家官方回复）
     * 使用场景: 个人中心 "收到评论" 列表展示
     *
     * 业务逻辑 (UNION ALL 联合查询):
     * 1. 上半部分: 查询 comment 表，获取其他普通用户对当前作者笔记的真实评论数据。
     * 2. 下半部分: 查询 note 表，提取商家直接回复在 note.reply 字段中的官方回应数据。
     * 3. 字段映射: 为保证前端列表渲染结构一致，将商家回复伪装成评论格式（指定 commenterNickname 为 '商家'）。
     * 4. 冲突处理: 将 note 表的 id 乘以 -1 作为虚拟主键，避免与 comment 表的真实主键冲突，防止 Vue 渲染报错。
     * 5. 排序规则: 将两部分结果合并后，统一按照创建/回复时间倒序排列。
     *
     * @param userId 笔记作者的用户ID
     * @return 混合后的评论与回复消息列表
     */
    @Select("(SELECT c.id, c.note_id AS noteId, c.content, c.create_time AS createTime, " +
            "n.title, u.id AS commenterId, u.nickname AS commenterNickname " +
            "FROM comment c " +
            "LEFT JOIN note n ON c.note_id = n.id " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE n.user_id = #{userId} AND c.user_id <> #{userId}) " +
            "UNION ALL " +
            "(SELECT (n.id * -1) AS id, n.id AS noteId, n.reply AS content, n.create_time AS createTime, " +
            "n.title, 0 AS commenterId, '商家' AS commenterNickname " +
            "FROM note n " +
            "WHERE n.user_id = #{userId} AND n.reply IS NOT NULL AND n.reply != '') " +
            "ORDER BY createTime DESC")
    List<Map<String, Object>> findReceivedCommentsByAuthorId(@Param("userId") Long userId);

    /**
     * 管理员查询所有已审核笔记(包含正常和违规屏蔽)，支持按标题或内容模糊搜索
     * 使用场景: 管理后台"笔记管理"列表
     * @param keyword 搜索关键词
     * @return 所有已审核笔记列表(status=1或3)
     */
    @Select("<script>" +
            "SELECT n.id, n.title, n.content, n.images, n.score, n.status, n.create_time, " +
            "n.like_count AS likeCount, n.collect_count AS collectCount, u.nickname " +
            "FROM note n " +
            "LEFT JOIN user u ON n.user_id = u.id " +
            "WHERE n.status IN (1, 3) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (n.title LIKE CONCAT('%', #{keyword}, '%') OR n.content LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY n.create_time DESC" +
            "</script>")
    List<Map<String, Object>> findAllPublished(@Param("keyword") String keyword);


}
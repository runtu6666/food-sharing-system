package com.foodshare.mapper;

import com.foodshare.entity.User;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT status FROM user WHERE id=#{id}")
    Integer findStatusById(Long id);

    // 按昵称查询用户，用于资料修改时做昵称唯一性校验
    @Select("SELECT * FROM user WHERE nickname = #{nickname}")
    User findByNickname(String nickname);

    // 插入用户时不再写死 role='user'，而是使用传入的role字段（user / shop）
    @Insert("INSERT INTO user(username, password, nickname, role) " +
            "VALUES(#{username}, #{password}, #{nickname}, #{role})")
    int insert(User user);

    // 统计注册用户总数（包含普通用户、商家和管理员）
    @Select("SELECT COUNT(*) FROM user")
    int countUsers();

    // 统计已入驻商家数量（status 1=正常，2=封禁，不含待审核和已拒绝）
    @Select("SELECT COUNT(*) FROM shop WHERE status IN (1,2)")
    int countShops();

    // 统计已发布探店笔记数量（status 1=正常，3=违规屏蔽，不含待审核和驳回）
    @Select("SELECT COUNT(*) FROM note WHERE status IN (1,3)")
    int countNotes();

    @Select("SELECT COUNT(*) FROM note WHERE status=0")
    int countPendingNotes();

    @Select("SELECT * FROM note WHERE status=0")
    List<Map<String, Object>> findPendingNotes();

    @Update("UPDATE note SET status=#{status}, reject_reason=#{reason} WHERE id=#{id}")
    void updateNoteStatus(Long id, Integer status, String reason);

    // 后台用户管理：展示所有账号（普通用户、商家和管理员），方便统一查看角色分布，支持按用户名或昵称模糊搜索
    @Select("<script>" +
            "SELECT * FROM user " +
            "<where>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    username LIKE CONCAT('%', #{keyword}, '%') OR nickname LIKE CONCAT('%', #{keyword}, '%')" +
            "  </if>" +
            "</where>" +
            "ORDER BY id DESC" +
            "</script>")
    List<User> findAllUsers(@Param("keyword") String keyword);

    @Update("UPDATE user SET status=#{status} WHERE id=#{id}")
    void updateUserStatus(Long id, Integer status);

    // 删除用户前先删除其关联数据，避免残留脏数据
    @Delete("DELETE FROM likes WHERE note_id IN (SELECT id FROM note WHERE user_id=#{userId})")
    void deleteLikesByAuthorId(Long userId);

    @Delete("DELETE FROM collect WHERE note_id IN (SELECT id FROM note WHERE user_id=#{userId})")
    void deleteCollectsByAuthorId(Long userId);

    @Delete("DELETE FROM comment WHERE note_id IN (SELECT id FROM note WHERE user_id=#{userId})")
    void deleteCommentsByAuthorId(Long userId);

    @Delete("DELETE FROM likes WHERE user_id=#{userId}")
    void deleteLikesByUserId(Long userId);

    @Delete("DELETE FROM collect WHERE user_id=#{userId}")
    void deleteCollectsByUserId(Long userId);

    @Delete("DELETE FROM comment WHERE user_id=#{userId}")
    void deleteCommentsByUserId(Long userId);

    @Delete("DELETE FROM note WHERE user_id=#{userId}")
    void deleteNotesByUserId(Long userId);

    @Delete("DELETE FROM shop WHERE user_id=#{userId}")
    void deleteShopsByUserId(Long userId);

    @Delete("DELETE FROM user WHERE id=#{userId}")
    void deleteUserById(Long userId);

    // 用户修改个人资料：只允许更新用户名和昵称
    @Update("UPDATE user SET username=#{username}, nickname=#{nickname} WHERE id=#{id}")
    void updateProfile(@Param("id") Long id, @Param("username") String username, @Param("nickname") String nickname);

    @Select("SELECT * FROM shop WHERE status=0")
    List<Map<String, Object>> findPendingShops();

    // 加上 reject_reason 字段，并给参数加上 @Param 注解防止 MyBatis 报错
    @Update("UPDATE shop SET status=#{status}, reject_reason=#{rejectReason} WHERE id=#{id}")
    void updateShopStatus(@Param("id") Long id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);

    // 查询所有已审批的商家（包含正常和封禁状态，不包含待审核和已拒绝），支持按店名或地址模糊搜索
    @Select("<script>" +
            "SELECT s.*, u.nickname FROM shop s LEFT JOIN user u ON s.user_id=u.id " +
            "WHERE s.status IN (1,2) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (s.name LIKE CONCAT('%', #{keyword}, '%') OR s.address LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY s.create_time DESC" +
            "</script>")
    List<Map<String, Object>> findAllShops(@Param("keyword") String keyword);

    // 封禁/解封商家（status=2封禁，status=1正常）
    @Update("UPDATE shop SET status=#{status} WHERE id=#{id}")
    void updateShopBanStatus(@Param("id") Long id, @Param("status") Integer status);

    // 管理员屏蔽/解除屏蔽笔记（status=3违规屏蔽，status=1恢复正常）
    @Update("UPDATE note SET status=#{status} WHERE id=#{id}")
    void adminUpdateNoteStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 根据用户ID查询用户信息
     *
     * 用途: JWT验证后,根据token中的userId查询完整的用户信息
     *
     * @param id 用户ID
     * @return 用户对象,如果不存在返回null
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 更新用户资料
     *
     * 用途: 用户修改昵称、头像等个人信息
     *
     * 注意: 只更新nickname和avatar字段,不会更新密码等敏感信息
     *
     * @param user 用户对象,必须包含id、nickname、avatar
     */
    @Update("UPDATE user SET nickname = #{nickname}, avatar = #{avatar} WHERE id = #{id}")
    void update(User user);
}

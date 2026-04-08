package com.foodshare.mapper;

import com.foodshare.entity.Shop;
import com.foodshare.entity.ShopReview;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 店铺数据访问接口
 * 职责: 负责店铺相关的数据库操作,包括店铺CRUD、评价管理等
 */
@Mapper
public interface ShopMapper {

    // ==================== 店铺查询功能 ====================

    /**
     * 按关键词模糊搜索店铺(仅搜索已审批通过的店铺)
     * 使用场景: 用户发布笔记时搜索关联店铺、首页搜索店铺
     * @param keyword 搜索关键词
     * @return 店铺列表(最多10条)
     */
    @Select("SELECT * FROM shop WHERE name LIKE CONCAT('%', #{keyword}, '%') AND status = 1 LIMIT 10")
    List<Shop> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 查询所有审批通过的店铺
     * 使用场景: 首页展示店铺列表、地图模式展示店铺标记点
     * @return 所有审批通过的店铺列表(status=1)
     */
    @Select("SELECT * FROM shop WHERE status = 1 ORDER BY create_time DESC")
    List<Shop> findAll();

    /**
     * 根据店铺ID查询店铺详情
     * @param shopId 店铺ID
     * @return 店铺对象(如果不存在返回null)
     */
    @Select("SELECT * FROM shop WHERE id = #{shopId}")
    Shop findById(@Param("shopId") Long shopId);

    /**
     * 查询某商家的所有店铺(支持一个商家管理多个店铺)
     * 使用场景: 商家中心"我的店铺"列表
     * @param userId 商家用户ID
     * @return 该商家的所有店铺列表(按创建时间倒序)
     */
    @Select("SELECT * FROM shop WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Shop> findAllByUserId(@Param("userId") Long userId);

    // ==================== 店铺管理功能 ====================

    /**
     * 商家提交店铺入驻申请
     * 说明: status默认为0(待审核),需要管理员审批后才能正常显示
     * @param shop 店铺对象(包含商家ID、店铺名称、地址等信息)
     */
    @Insert("INSERT INTO shop(user_id, name, address, phone, business_hours, category_id, " +
            "legal_name, shop_images, latitude, longitude, status, create_time) " +
            "VALUES(#{userId}, #{name}, #{address}, #{phone}, #{businessHours}, #{categoryId}, " +
            "#{legalName}, #{shopImages}, #{latitude}, #{longitude}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Shop shop);

    /**
     * 更新店铺基本信息
     * 说明: 商家修改店铺信息后，同步更新状态为待审核
     * @param shop 店铺对象(必须包含id字段)
     */
    @Update("UPDATE shop SET name = #{name}, address = #{address}, phone = #{phone}, " +
            "business_hours = #{businessHours}, latitude = #{latitude}, longitude = #{longitude}, " +
            "shop_images = #{shopImages}, status = #{status} WHERE id = #{id}")
    void update(Shop shop);

    /**     * 删除店铺(必须同时校验商家ID,防止删除别人的店铺)
     * 说明: 删除前需要先清除笔记关联(调用clearShopBinding)
     * @param shopId 店铺ID
     * @param userId 商家用户ID
     * @return 删除的行数(0表示删除失败,可能是无权限)
     */
    @Delete("DELETE FROM shop WHERE id = #{shopId} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("shopId") Long shopId, @Param("userId") Long userId);

    /**
     * 校验店铺所有权
     * 使用场景: 回复评价前校验是否是店铺主人
     * @param shopId 店铺ID
     * @param userId 商家用户ID
     * @return 匹配数量(1表示是店主,0表示不是)
     */
    @Select("SELECT COUNT(*) FROM shop WHERE id = #{shopId} AND user_id = #{userId}")
    int checkOwner(@Param("shopId") Long shopId, @Param("userId") Long userId);

    /**
     * 清除笔记与店铺的关联
     * 使用场景: 删除店铺前,将所有关联该店铺的笔记的shop_id设为NULL
     * @param shopId 店铺ID
     */
    @Update("UPDATE note SET shop_id = NULL WHERE shop_id = #{shopId}")
    void clearShopBinding(@Param("shopId") Long shopId);

    // ==================== 店铺评价功能 ====================

    /**
     * 查询店铺的所有评价(关联用户信息)
     * 说明: LEFT JOIN user表获取评价者的昵称和头像
     * @param shopId 店铺ID
     * @return 评价列表(按时间倒序,越新的越靠前)
     */
    @Select("SELECT r.*, u.nickname AS username, u.avatar " +
            "FROM shop_review r " +
            "LEFT JOIN user u ON r.user_id = u.id " +
            "WHERE r.shop_id = #{shopId} " +
            "ORDER BY r.create_time DESC")
    List<ShopReview> findReviewsByShopId(@Param("shopId") Long shopId);

    /**
     * 查询用户对某店铺的评价ID(用于判断是新增还是修改评价)
     * 说明: 一个用户对一个店铺只能有一条评价,重复评价会覆盖
     * @param shopId 店铺ID
     * @param userId 用户ID
     * @return 评价ID(如果不存在返回null)
     */
    @Select("SELECT id FROM shop_review WHERE shop_id = #{shopId} AND user_id = #{userId} LIMIT 1")
    Long findReviewId(@Param("shopId") Long shopId, @Param("userId") Long userId);

    /**
     * 新增店铺评价
     * @param review 评价对象(包含shopId、userId、rating、content)
     */
    @Insert("INSERT INTO shop_review(shop_id, user_id, rating, content, create_time) " +
            "VALUES(#{shopId}, #{userId}, #{rating}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertReview(ShopReview review);

    /**
     * 更新店铺评价(用户修改自己的评价)
     * @param review 评价对象(必须包含id字段)
     */
    @Update("UPDATE shop_review SET rating = #{rating}, content = #{content}, " +
            "create_time = NOW() WHERE id = #{id}")
    void updateReview(ShopReview review);

    /**
     * 商家回复评价
     * 说明: 商家看到评价后可以回复用户
     * @param id 评价ID
     * @param reply 回复内容
     * @return 更新的行数(0表示评价不存在)
     */
    @Update("UPDATE shop_review SET merchant_reply = #{reply} WHERE id = #{id}")
    int replyReview(@Param("id") Long id, @Param("reply") String reply);

    /**
     * 删除店铺的所有评价
     * 使用场景: 删除店铺时自动清理关联的评价数据
     * @param shopId 店铺ID
     */
    @Delete("DELETE FROM shop_review WHERE shop_id = #{shopId}")
    void deleteReviewsByShopId(@Param("shopId") Long shopId);

    // ==================== 店铺评价功能 ====================

    /**
     * 【答辩注】：
     * 这是一个基于 LBS 的自定义查询方法。
     * 我没有把距离计算放到 Java 业务层，而是直接将用户当前的经度(userLng)和纬度(userLat)
     * 传给底层 MySQL，利用数据库的空间索引和函数进行计算和排序
     * * @param userLng 用户的当前经度
     * @param userLat 用户的当前纬度
     * @param radius  搜索半径(单位：米)，可选
     * @return 排序好的附近店铺列表
     */
    List<Shop> selectNearbyShops(@Param("userLng") BigDecimal userLng,
                                 @Param("userLat") BigDecimal userLat,
                                 @Param("radius") Double radius,
                                 @Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId);
}
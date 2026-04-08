package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.Note;
import com.foodshare.entity.Shop;
import com.foodshare.entity.ShopReview;
import com.foodshare.mapper.ShopMapper;
import com.foodshare.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.util.List;

/**
 * 店铺控制器
 * 职责: 处理店铺相关的业务逻辑,包括店铺管理、评价管理等
 */
@RestController
@RequestMapping("/shop")
@CrossOrigin
public class ShopController {

    @Autowired
    private ShopMapper shopMapper;

    @Autowired
    private UserMapper userMapper;


    // ==================== 店铺查询接口 ====================

    /**
     * 搜索店铺(支持关键词模糊搜索)
     * 接口地址: GET /shop/search?keyword=烧烤
     * 使用场景:
     * 1. 用户发布笔记时搜索关联店铺
     * 2. 首页搜索栏搜索店铺
     * 3. 百度地图搜索店铺标记点
     * @param keyword 搜索关键词(店铺名称)
     * @return 店铺列表(最多10条)
     */
    @GetMapping("/search")
    public Result search(@RequestParam String keyword) {
        List<Shop> shops = shopMapper.searchByKeyword(keyword);
        return Result.success(shops);
    }

    /**
     * 查询店铺详情
     * 接口地址: GET /shop/detail?id=1
     * 使用场景: 用户点击店铺卡片查看详情
     * @param id 店铺ID
     * @return 店铺详细信息
     */
    @GetMapping("/detail")
    public Result detail(@RequestParam Long id) {
        Shop shop = shopMapper.findById(id);
        if (shop == null) {
            return Result.error("店铺不存在");
        }
        return Result.success(shop);
    }

    /**
     * 获取所有审批通过的店铺列表
     * 接口地址: GET /shop/list
     * 使用场景:
     * 1. 首页展示店铺列表
     * 2. 百度地图展示所有店铺标记点
     * @return 所有审批通过的店铺列表(status=1)
     */
    @GetMapping("/list")
    public Result list() {
        List<Shop> shops = shopMapper.findAll();
        return Result.success(shops);
    }

    /**
     * 获取附近店铺接口 (LBS核心功能)
     * 【答辩注】：
     * 前端 Vue 通过百度地图定位组件拿到用户的坐标后，调用此接口。
     * 我在这里做了一个容错处理，如果前端没有传范围参数(radius)，我默认搜索周边 5000 米（5公里）的店铺。
     */
    @GetMapping("/nearby")
    public Result getNearbyShops(
            @RequestParam("lng") BigDecimal lng,
            @RequestParam("lat") BigDecimal lat,
            @RequestParam(value = "radius", required = false, defaultValue = "5000") Double radius,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {

        // 参数校验，防止前端传空数据导致底层 SQL 报错
        if (lng == null || lat == null) {
            return Result.error("无法获取当前定位坐标，请检查授权");
        }

        // 执行 LBS 距离计算、店名模糊匹配、分类精准过滤的联合查询
        List<Shop> shopList = shopMapper.selectNearbyShops(lng, lat, radius, keyword, categoryId);

        // 3. 返回成功结果给前端渲染列表
        return Result.success(shopList);
    }

    // ==================== 商家店铺管理接口 ====================

    /**
     * 商家提交店铺入驻申请
     * 接口地址: POST /shop/register
     * 请求参数示例:
     * {
     *   "userId": 1,
     *   "name": "老李烧烤",
     *   "address": "北京市朝阳区三里屯路19号",
     *   "phone": "13800138000",
     *   "businessHours": "10:00-22:00",
     *   "categoryId": 2,
     *   "legalName": "李四",
     *   "shopImages": "url1,url2,url3",
     *   "latitude": 39.9042,
     *   "longitude": 116.4074
     * }
     * 业务流程:
     * 1. 校验用户是否被封禁
     * 2. 插入店铺数据,status默认为0(待审核)
     * 3. 等待管理员审批
     * @param shop 店铺对象
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result register(@RequestBody Shop shop) {
        // 校验用户是否被封禁
        if (isBanned(shop.getUserId())) {
            return Result.error("此账号已被封禁,请联系管理员");
        }

        // 插入店铺数据(status默认为0,在Mapper的INSERT语句中设置)
        shopMapper.insert(shop);

        return Result.success("提交成功,等待管理员审批");
    }

    /**
     * 商家更新自己的店铺信息
     * 接口地址: POST /shop/update
     * 请求参数: Shop对象(必须包含id字段)
     * 说明: 只能修改自己管理的店铺
     * @param shop 店铺对象(包含修改后的信息)
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result update(@RequestBody Shop shop) {
        // 校验用户是否被封禁
        if (isBanned(shop.getUserId())) {
            return Result.error("此账号已被封禁,请联系管理员");
        }

        // 强行重置审核状态：只要修改了店铺信息，一律打回待审核状态 (0: 待审核)
        shop.setStatus(0);

        // 更新店铺信息
        shopMapper.update(shop);

        return Result.success("更新成功");
    }

    /**
     * 查询当前商家的所有店铺
     * 接口地址: GET /shop/my?userId=1
     * 使用场景: 商家中心"我的店铺"列表
     * 功能说明: 一个商家可以管理多个店铺
     * @param userId 商家用户ID
     * @return 该商家的所有店铺列表
     */
    @GetMapping("/my")
    public Result my(@RequestParam Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return Result.error("此账号已被封禁,请联系管理员");
        }

        // 查询该商家的所有店铺
        List<Shop> shops = shopMapper.findAllByUserId(userId);

        return Result.success(shops);
    }

    /**
     * 删除店铺
     * 接口地址: POST /shop/delete
     * 请求参数:
     * {
     *   "shopId": 1,
     *   "userId": 1
     * }
     * 业务流程:
     * 1. 校验用户是否被封禁
     * 2. 清除笔记关联(将关联该店铺的笔记的shop_id设为NULL)
     * 3. 删除店铺(必须是店铺主人才能删除)
     * @param shopId 店铺ID
     * @param userId 商家用户ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result delete(@RequestParam Long shopId, @RequestParam Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return Result.error("此账号已被封禁,请联系管理员");
        }

        // 清除笔记与店铺的关联
        shopMapper.clearShopBinding(shopId);

        // 删除店铺(同时校验userId,防止删除别人的店铺)
        int count = shopMapper.deleteByIdAndUser(shopId, userId);
        if (count == 0) {
            return Result.error("删除失败,店铺不存在或无权限");
        }

        return Result.success("删除成功");
    }

    // ==================== 店铺评价接口 ====================

    /**
     * 查询店铺的所有评价
     * 接口地址: GET /shop/reviews/list?shopId=1
     * 使用场景: 店铺详情页展示用户评价列表
     * @param shopId 店铺ID
     * @return 评价列表(包含评价者昵称、头像、评分、内容、商家回复等)
     */
    @GetMapping("/reviews/list")
    public Result reviews(@RequestParam Long shopId) {
        List<ShopReview> reviews = shopMapper.findReviewsByShopId(shopId);
        return Result.success(reviews);
    }

    /**
     * 用户发表/修改店铺评价
     * 接口地址: POST /shop/reviews/add
     * 请求参数:
     * {
     *   "shopId": 1,
     *   "userId": 1,
     *   "rating": 5,
     *   "content": "味道很好,服务态度也不错"
     * }
     * 业务逻辑:
     * 1. 校验用户是否被封禁
     * 2. 校验评分范围(1-5分)
     * 3. 校验店铺是否可评价(status=1)
     * 4. 如果已评价过则修改,否则新增
     * @param review 评价对象
     * @return 操作结果
     */
    @PostMapping("/reviews/add")
    public Result addReview(@RequestBody ShopReview review) {
        // 校验用户是否被封禁
        if (isBanned(review.getUserId())) {
            return Result.error("此账号已被封禁,请联系管理员");
        }

        // 校验评价内容
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            return Result.error("评价内容不能为空");
        }

        // 校验评分范围
        if (review.getRating() < 1 || review.getRating() > 5) {
            return Result.error("评分范围必须是1到5");
        }

        // 校验店铺是否存在且已审批通过
        Shop shop = shopMapper.findById(review.getShopId());
        if (shop == null || shop.getStatus() != 1) {
            return Result.error("店铺不可评价");
        }

        // 查询用户是否已评价过该店铺
        Long reviewId = shopMapper.findReviewId(review.getShopId(), review.getUserId());

        if (reviewId == null) {
            // 首次评价 → 新增
            shopMapper.insertReview(review);
        } else {
            // 已评价过 → 修改(覆盖原评价)
            review.setId(reviewId);
            shopMapper.updateReview(review);
        }

        return Result.success("评价成功");
    }

    /**
     * 商家回复用户评价
     * 接口地址: POST /shop/reviews/reply
     * 请求参数:
     * {
     *   "reviewId": 1,
     *   "shopId": 1,
     *   "userId": 1,
     *   "reply": "感谢您的光临,欢迎下次再来!"
     * }
     * 业务流程:
     * 1. 校验商家是否被封禁
     * 2. 校验回复内容非空
     * 3. 校验是否是店铺主人(只有店主才能回复)
     * 4. 更新评价的merchant_reply字段
     * @param reviewId 评价ID
     * @param shopId 店铺ID
     * @param userId 商家用户ID
     * @param reply 回复内容
     * @return 操作结果
     */
    @PostMapping("/reviews/reply")
    public Result replyReview(@RequestParam Long reviewId,
                              @RequestParam Long shopId,
                              @RequestParam Long userId,
                              @RequestParam String reply) {
        // 校验商家是否被封禁
        if (isBanned(userId)) {
            return Result.error("此账号已被封禁,请联系管理员");
        }

        // 校验回复内容
        if (reply == null || reply.trim().isEmpty()) {
            return Result.error("回复内容不能为空");
        }

        // 校验是否是店铺主人
        if (shopMapper.checkOwner(shopId, userId) == 0) {
            return Result.error("无权限回复该店铺评价");
        }

        // 更新评价的回复内容
        int updated = shopMapper.replyReview(reviewId, reply.trim());
        if (updated == 0) {
            return Result.error("评价不存在");
        }

        return Result.success("回复成功");
    }

    // ==================== 工具方法 ====================4
    /**
     * 校验用户是否被封禁
     * @param userId 用户ID
     * @return true-已封禁, false-正常
     */
    private boolean isBanned(Long userId) {
        if (userId == null) {
            return false;
        }
        Integer status = userMapper.findStatusById(userId);
        // status=1表示已封禁
        return status != null && status == 1;
    }
}
package com.foodshare.controller;

import com.foodshare.common.Result;
import com.foodshare.entity.Note;
import com.foodshare.entity.Comment;
import com.foodshare.entity.Shop;
import com.foodshare.mapper.NoteMapper;
import com.foodshare.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 笔记控制器
 * 职责: 处理笔记相关的业务逻辑,包括笔记CRUD、点赞、收藏、评论等
 */
@RestController
@RequestMapping("/note")
@CrossOrigin
public class NoteController {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.foodshare.utils.DFAFilterUtil dfaFilterUtil;

    // ==================== 笔记查询接口 ====================

    /**
     * 获取笔记列表(支持筛选、搜索、排序、分页)
     *
     * 接口地址: GET /note/list
     *
     * 请求参数:
     * - categoryId: 分类ID(可选,不传则查询所有分类)
     * - keyword: 搜索关键词(可选)
     * - sort: 排序方式(latest-最新, hot-热门,默认latest)
     * - shopId: 店铺ID(可选,查询某店铺的笔记)
     * - page: 页码(默认第1页)
     * - userId: 当前用户ID(可选,用于判断点赞/收藏状态)
     *
     * 示例:
     * GET /note/list?categoryId=1&sort=hot&page=1&userId=1
     *
     * @return 笔记列表(每页12条)
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long shopId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) Long userId
    ) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 计算分页偏移量(第1页offset=0, 第2页offset=12)
        int offset = (page - 1) * 12;

        // 查询笔记列表
        List<Map<String, Object>> notes = noteMapper.findNotes(categoryId, keyword, sort, shopId, offset, userId);

        return Result.success(notes);
    }

    /**
     * 获取笔记详情
     *
     * 接口地址: GET /note/detail/{id}
     *
     * 示例: GET /note/detail/1?userId=1
     *
     * @param id 笔记ID
     * @param userId 当前用户ID(可选)
     * @return 笔记详情(包含评论列表)
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 查询笔记详情
        Map<String, Object> note = noteMapper.findById(id, userId);
        if (note == null) {
            return Result.error("笔记不存在");
        }

        // 查询笔记的评论列表
        List<Map<String, Object>> comments = noteMapper.findComments(id);
        note.put("comments", comments);

        return Result.success(note);
    }

    /**
     * 查询我发布的所有笔记
     *
     * 接口地址: GET /note/my?userId=1
     * 使用场景: 个人中心"我的笔记"列表
     *
     * @param userId 用户ID
     * @return 该用户发布的所有笔记(包含所有审核状态)
     */
    @GetMapping("/my")
    public Result myNotes(@RequestParam Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        List<Map<String, Object>> notes = noteMapper.findByUserId(userId);
        return Result.success(notes);
    }

    /**
     * 查询我收藏的所有笔记
     *
     * 接口地址: GET /note/collects?userId=1
     * 使用场景: 个人中心"我的收藏"列表
     *
     * @param userId 用户ID
     * @return 该用户收藏的所有笔记
     */
    @GetMapping("/collects")
    public Result myCollects(@RequestParam Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        List<Map<String, Object>> notes = noteMapper.findCollectsByUserId(userId);
        return Result.success(notes);
    }

    /**
     * 根据店铺ID查询关联笔记
     *
     * 接口地址: GET /note/byShop?shopId=1
     * 使用场景: 商家中心查看"哪些笔记提到了我的店铺"
     *
     * @param shopId 店铺ID
     * @return 关联该店铺的所有笔记
     */
    @GetMapping("/byShop")
    public Result byShop(@RequestParam Long shopId) {
        List<Map<String, Object>> notes = noteMapper.findByShopId(shopId);
        return Result.success(notes);
    }

    // ==================== 笔记管理接口 ====================

    /**
     * 发布探店笔记
     *
     * 接口地址: POST /note/publish
     *
     * 请求参数示例:
     * {
     *   "userId": 1,
     *   "shopId": 1,
     *   "title": "这家烧烤太好吃了",
     *   "content": "强烈推荐他家的羊肉串...",
     *   "images": "url1,url2,url3",
     *   "score": 5,
     *   "categoryId": 2
     * }
     *
     * 业务流程:
     * 1. 校验用户是否被封禁
     * 2. 校验必填字段(标题、内容)
     * 3. 校验评分范围(1-5)
     * 4. 插入笔记数据(status默认为0,待审核)
     *
     * @param note 笔记对象
     * @return 操作结果
     */
    @PostMapping("/publish")
    public Result publish(@RequestBody Note note) {
        // 校验用户是否被封禁
        if (isBanned(note.getUserId())) {
            return bannedResult();
        }

        // 校验必填字段
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        // 校验评分范围
        if (note.getScore() == null || note.getScore() < 1 || note.getScore() > 5) {
            return Result.error("评分范围必须是1到5");
        }

        // 🤖 机器初审 (DFA 算法)：对标题和正文进行极速扫描
        boolean hasBadWord = dfaFilterUtil.containsSensitiveWord(note.getTitle())
                || dfaFilterUtil.containsSensitiveWord(note.getContent());
        if (hasBadWord) {
            // 触发拦截机制，直接打回，不进数据库
            return Result.error("提交失败！内容包含违规敏感词，已被系统机器初审拦截。");
        }

        // 机器初审通过，插入笔记 (此时status默认为0，流转至审核工作台等待【人工复核】)
        noteMapper.insert(note);

        return Result.success("机器初审通过，已提交人工复核");
    }

    /**
     * 修改笔记
     *
     * 接口地址: POST /note/update
     *
     * 说明:
     * - 只能修改自己的笔记
     * - 修改后status会重置为0(待审核)
     *
     * @param note 笔记对象(必须包含id和userId字段)
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result updateNote(@RequestBody Note note) {
        // 校验用户是否被封禁
        if (isBanned(note.getUserId())) {
            return bannedResult();
        }

        // 校验必填字段
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        // 校验评分范围
        if (note.getScore() == null || note.getScore() < 1 || note.getScore() > 5) {
            return Result.error("评分范围必须是1到5");
        }

        // 🤖 机器初审 (DFA 算法)：防止修改时混入敏感词
        boolean hasBadWord = dfaFilterUtil.containsSensitiveWord(note.getTitle())
                || dfaFilterUtil.containsSensitiveWord(note.getContent());
        if (hasBadWord) {
            return Result.error("修改失败！内容包含违规敏感词，已被机器初审拦截。");
        }

        // 机器初审通过，更新笔记状态为等待人工复核
        noteMapper.updateById(note);

        return Result.success("修改已提交，等待人工复核");
    }

    /**
     * 删除笔记
     *
     * 接口地址: POST /note/delete
     * 请求参数: {"id": 1, "userId": 1}
     *
     * 说明: 只能删除自己的笔记
     *
     * @param params 包含 id 和 userId 的JSON对象
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result deleteNote(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 删除笔记(同时校验userId,防止删除别人的笔记)
        noteMapper.deleteById(id, userId);

        return Result.success("删除成功");
    }

    // ==================== 点赞功能 ====================

    /**
     * 点赞/取消点赞
     *
     * 接口地址: POST /note/like
     * 请求参数: {"noteId": 1, "userId": 1}
     *
     * 业务逻辑:
     * 1. 查询用户是否已点赞
     * 2. 如果已点赞 → 取消点赞,笔记点赞数-1
     * 3. 如果未点赞 → 添加点赞,笔记点赞数+1
     *
     * @param params 包含 noteId 和 userId 的JSON对象
     * @return 操作结果
     */
    @PostMapping("/like")
    public Result like(@RequestBody Map<String, Object> params) {
        Long noteId = Long.valueOf(params.get("noteId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 查询是否已点赞
        int count = noteMapper.checkLike(noteId, userId);

        if (count > 0) {
            // 已点赞 → 取消点赞
            noteMapper.deleteLike(noteId, userId);
            noteMapper.updateLikeCount(noteId, -1);
        } else {
            // 未点赞 → 添加点赞
            noteMapper.insertLike(noteId, userId);
            noteMapper.updateLikeCount(noteId, 1);
        }

        return Result.success("操作成功");
    }

    /**
     * 查询我收到的点赞记录
     *
     * 接口地址: GET /note/likes/received?userId=1
     * 使用场景: 个人中心"收到的赞"列表
     *
     * @param userId 用户ID
     * @return 点赞记录列表(包含笔记标题、点赞人昵称)
     */
    @GetMapping("/likes/received")
    public Result receivedLikes(@RequestParam Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        List<Map<String, Object>> likes = noteMapper.findLikeUsersByAuthorId(userId);
        return Result.success(likes);
    }

    // ==================== 收藏功能 ====================

    /**
     * 收藏/取消收藏
     *
     * 接口地址: POST /note/collect
     * 请求参数: {"noteId": 1, "userId": 1}
     *
     * 业务逻辑:
     * 1. 查询用户是否已收藏
     * 2. 如果已收藏 → 取消收藏,笔记收藏数-1
     * 3. 如果未收藏 → 添加收藏,笔记收藏数+1
     *
     * @param params 包含 noteId 和 userId 的JSON对象
     * @return 操作结果
     */
    @PostMapping("/collect")
    public Result collect(@RequestBody Map<String, Object> params) {
        Long noteId = Long.valueOf(params.get("noteId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 查询是否已收藏
        int count = noteMapper.checkCollect(noteId, userId);

        if (count > 0) {
            // 已收藏 → 取消收藏
            noteMapper.deleteCollect(noteId, userId);
            noteMapper.updateCollectCount(noteId, -1);
        } else {
            // 未收藏 → 添加收藏
            noteMapper.insertCollect(noteId, userId);
            noteMapper.updateCollectCount(noteId, 1);
        }

        return Result.success("操作成功");
    }

    // ==================== 评论功能 ====================

    /**
     * 发表评论
     *
     * 接口地址: POST /note/comment
     * 请求参数: {"noteId": 1, "userId": 1, "content": "太好吃了"}
     *
     * @param comment 评论对象
     * @return 操作结果
     */
    @PostMapping("/comment")
    public Result comment(@RequestBody Comment comment) {
        // 校验用户是否被封禁
        if (isBanned(comment.getUserId())) {
            return bannedResult();
        }

        // 校验评论内容
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        // 🛡️ DFA 敏感词过滤：对评论内容进行净化
        comment.setContent(dfaFilterUtil.filter(comment.getContent()));

        // 插入评论
        noteMapper.insertComment(comment);

        return Result.success("评论成功");
    }

    /**
     * 回复评论(支持多人回复)
     *
     * 接口地址: POST /note/comment/reply
     * 请求参数: {"commentId": 1, "userId": 1, "content": "谢谢推荐"}
     *
     * 业务逻辑:
     * 1. 根据userId查询用户昵称
     * 2. 拼接回复内容格式: "昵称: 内容"
     * 3. 追加到评论的reply字段(用换行符分隔)
     *
     * @param params 包含 commentId, userId 和 content 的JSON对象
     * @return 操作结果
     */
    @PostMapping("/comment/reply")
    public Result replyComment(@RequestBody Map<String, Object> params) {
        Long commentId = Long.valueOf(params.get("commentId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());
        String content = params.get("content").toString();
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 校验回复内容
        if (content == null || content.trim().isEmpty()) {
            return Result.error("回复内容不能为空");
        }

        // 查询用户昵称
        String nickname = noteMapper.findUserNickname(userId);
        if (nickname == null || nickname.isEmpty()) {
            nickname = "用户";
        }

        // 🛡️ DFA 敏感词过滤：对回复内容进行净化
        String safeContent = dfaFilterUtil.filter(content.trim());

        // 拼接回复内容: "昵称: 内容"
        String replyLine = nickname + ": " + safeContent;

        // 追加回复
        noteMapper.appendCommentReply(commentId, replyLine);

        return Result.success("回复成功");
    }

    /**
     * 查询我收到的评论记录
     *
     * 接口地址: GET /note/comments/received?userId=1
     * 使用场景: 个人中心"收到的评论"列表
     *
     * @param userId 用户ID
     * @return 评论记录列表
     */
    @GetMapping("/comments/received")
    public Result receivedComments(@RequestParam Long userId) {
        // 校验用户是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        List<Map<String, Object>> comments = noteMapper.findReceivedCommentsByAuthorId(userId);
        return Result.success(comments);
    }

    /**
     * 商家回复顾客探店笔记
     *
     * 接口地址: POST /note/merchant/reply
     * 业务逻辑:
     * 1. 校验商家是否被封禁
     * 2. 校验回复内容非空
     * 3. 校验并更新笔记的商家回复字段
     */
    @PostMapping("/merchant/reply")
    public Result replyMerchantNote(@RequestParam Long noteId,
                                    @RequestParam Long shopId,
                                    @RequestParam Long userId,
                                    @RequestParam String reply) {
        // 校验商家是否被封禁
        if (isBanned(userId)) {
            return bannedResult();
        }

        // 校验回复内容
        if (reply == null || reply.trim().isEmpty()) {
            return Result.error("回复内容不能为空");
        }

        // 调用 Mapper 更新笔记的商家回复字段
        int updated = noteMapper.updateMerchantReply(noteId, shopId, reply.trim());
        if (updated == 0) {
            return Result.error("笔记不存在或您无权限回复此笔记");
        }

        return Result.success("回复成功");
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员查询所有已审核笔记
     *
     * 接口地址: GET /note/admin/all
     * 使用场景: 管理后台"笔记管理"列表
     *
     * @return 所有已审核笔记列表(status=1或3)
     */
    @GetMapping("/admin/all")
    public Result adminAllNotes(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> notes = noteMapper.findAllPublished(keyword);
        return Result.success(notes);
    }

    // ==================== 工具方法 ====================

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

    /**
     * 返回"账号已封禁"的错误信息
     * @return Result对象
     */
    private Result bannedResult() {
        return Result.error("此账号已被封禁,请联系管理员");
    }


}
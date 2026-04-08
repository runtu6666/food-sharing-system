<template>
  <div class="detail-page">

    <!-- 顶部导航栏 -->
    <div class="navbar">
      <!-- 如果是从管理后台或个人中心跳转过来的，返回来源页；否则返回首页 -->
      <div class="nav-back" @click="goBack">← 返回</div>
      <div class="nav-title">探店笔记详情</div>
      <div style="width:80px"></div>
    </div>

    <!-- 加载中状态 -->
    <div v-if="loading" class="loading-wrap">
      <p>🍜 加载中...</p>
    </div>

    <!-- 主体内容 -->
    <div v-else class="detail-body">

      <!-- 左侧：笔记主内容 -->
      <div class="detail-main">

        <!-- 图片轮播区域 -->
        <div class="image-swiper" v-if="note.images">
          <div
              class="swiper-blur-bg"
              :style="{ backgroundImage: 'url(' + imageList[currentImg] + ')' }"
          ></div>
          <!-- 当前显示的图片 -->
          <img
              :src="imageList[currentImg]"
              class="swiper-img"
              alt="笔记图片"
              style="cursor: zoom-in;"
              @click="openPreview(currentImg)"
          />
          <!-- 左右切换箭头（多图时才显示） -->
          <div class="swiper-arrow left" v-if="imageList.length > 1" @click="prevImg">❮</div>
          <div class="swiper-arrow right" v-if="imageList.length > 1" @click="nextImg">❯</div>
          <!-- 图片计数 -->
          <div class="swiper-count" v-if="imageList.length > 1">
            {{ currentImg + 1 }} / {{ imageList.length }}
          </div>
          <!-- 底部小圆点 -->
          <div class="swiper-dots" v-if="imageList.length > 1">
            <span
                v-for="(img, idx) in imageList"
                :key="idx"
                :class="['dot', currentImg === idx ? 'active' : '']"
                @click="currentImg = idx"
            ></span>
          </div>
        </div>
        <!-- 无图片时的默认封面 -->
        <div v-else class="no-image-banner">
          <span>🍜</span>
        </div>

        <!-- 笔记信息卡片 -->
        <div class="note-info-card">
          <!-- 作者信息行 -->
          <div class="author-row">
            <div class="author-left">
              <div class="author-avatar">{{ note.nickname ? note.nickname[0] : 'U' }}</div>
              <div>
                <div class="author-name">{{ note.nickname }}</div>
                <div class="author-time">{{ note.createTime }}</div>
              </div>
            </div>
            <!-- 收藏按钮 -->
            <button
                :class="['collect-btn', note.collected ? 'collected' : '']"
                @click="toggleCollect"
            >
              {{ note.collected ? '⭐ 已收藏' : '☆ 收藏' }}
            </button>
          </div>

          <!-- 笔记标题 -->
          <h1 class="note-title">{{ note.title }}</h1>

          <!-- 评分 -->
          <div class="note-score">
            <span v-for="n in (note.score || 5)" :key="n" class="star-filled">★</span>
            <span v-for="n in (5 - (note.score || 5))" :key="'e'+n" class="star-empty">★</span>
            <span class="score-label">{{ note.score }}分</span>
          </div>

          <!-- 正文内容 -->
          <div class="note-content">{{ note.content }}</div>

          <div class="merchant-reply-box" v-if="note.reply">
            <div class="reply-header">
              <span class="shop-badge">🏪 商家回应</span>
            </div>
            <div class="reply-content">{{ note.reply }}</div>
          </div>

          <!-- 点赞按钮 -->
          <div class="like-row">
            <button
                :class="['like-btn', note.liked ? 'liked' : '']"
                @click="toggleLike"
            >
              {{ note.liked ? '❤️ 已点赞' : '🤍 点赞' }}
              <span class="like-num">{{ note.likeCount || note.like_count || 0 }}</span>
            </button>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section">
          <div class="comment-title">💬 评论 ({{ comments.length }})</div>

          <!-- 发表评论输入框 -->
          <div class="comment-input-wrap">
            <div class="comment-avatar">{{ currentUser.nickname ? currentUser.nickname[0] : 'U' }}</div>
            <input
                v-model="commentText"
                class="comment-input"
                placeholder="说点什么吧..."
                @keyup.enter="doComment"
            />
            <button class="comment-submit" @click="doComment">发送</button>
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div v-if="comments.length === 0" class="no-comment">暂无评论，快来抢沙发吧 🛋️</div>
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <!-- 评论者头像 -->
              <div class="comment-avatar-small">
                {{ comment.nickname ? comment.nickname[0] : 'U' }}
              </div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-name">{{ comment.nickname }}</span>
                  <span class="comment-time">{{ comment.createTime }}</span>
                </div>
                <!-- 评论内容 -->
                <div class="comment-text">{{ comment.content }}</div>
                <!-- 已有回复 -->
                <div class="comment-reply" v-if="comment.reply">
                  <span class="reply-label">回复：</span>
                  <div
                      v-for="(line, idx) in getReplyLines(comment.reply)"
                      :key="idx"
                      class="reply-line"
                  >
                    {{ line }}
                  </div>
                </div>
                <!--
                  回复入口：作者和其他用户都可以回复。
                  这里只负责打开输入框，不直接发请求，避免误触导致空回复提交。
                -->
                <div class="comment-actions">
                  <span class="reply-action" @click="startReply(comment)">回复</span>
                </div>
                <!-- 回复输入框 -->
                <div v-if="replyingCommentId === comment.id" class="reply-input-wrap">
                  <input
                      v-model="replyText"
                      class="reply-input"
                      placeholder="输入回复内容..."
                      @keyup.enter="doReply(comment.id)"
                  />
                  <button class="reply-submit" @click="doReply(comment.id)">发送</button>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>

      <!-- 右侧：店铺信息卡片 -->
      <div class="detail-sidebar">
        <div class="shop-card" v-if="note.shopId">
          <div class="shop-card-title">🏪 探店店铺</div>
          <div class="shop-name">{{ note.shopName || '未知店铺' }}</div>
          <div class="shop-info-item" v-if="note.shopAddress">
            📍 {{ note.shopAddress }}
          </div>
          <div class="shop-info-item" v-if="note.shopPhone">
            📞 {{ note.shopPhone }}
          </div>
          <div class="shop-info-item" v-if="note.businessHours">
            🕐 {{ note.businessHours }}
          </div>
        </div>

        <!-- 相关推荐（同分类其他笔记） -->
        <div class="recommend-card">
          <div class="shop-card-title">🔥 相关推荐</div>
          <div
              v-for="rec in recommends"
              :key="rec.id"
              class="recommend-item"
              @click="goNote(rec.id)"
          >
            <img
                v-if="rec.images"
                :src="rec.images.split('|||')[0]"
                class="rec-img"
                alt=""
            />
            <div v-else class="rec-img-empty">🍜</div>
            <div class="rec-info">
              <div class="rec-title">{{ rec.title }}</div>
              <div class="rec-author">{{ rec.nickname }}</div>
            </div>
          </div>
          <div v-if="recommends.length === 0" style="color:#bbb;font-size:13px;text-align:center;padding:16px">
            暂无推荐
          </div>
        </div>
      </div>

    </div>
  </div>
  <div v-if="showPreview" class="image-preview-overlay" @click.self="closePreview">
    <span class="preview-close" @click="closePreview">✕</span>

    <img :src="imageList[previewIndex]" class="preview-img" alt="全屏预览" />

    <div class="preview-arrow left" v-if="imageList.length > 1" @click.stop="prevPreview">❮</div>

    <div class="preview-arrow right" v-if="imageList.length > 1" @click.stop="nextPreview">❯</div>

    <div class="preview-count" v-if="imageList.length > 1">
      {{ previewIndex + 1 }} / {{ imageList.length }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'NoteDetailView',
  data() {
    return {
      // 当前登录用户
      currentUser: JSON.parse(localStorage.getItem('user') || '{}'),
      loading: true,        // 是否加载中
      note: {},             // 笔记详情数据
      comments: [],         // 评论列表
      recommends: [],       // 相关推荐笔记
      commentText: '',      // 评论输入框内容
      replyingCommentId: null, // 当前正在回复的评论ID
      replyText: '',        // 回复输入框内容
      currentImg: 0,        // 当前内嵌轮播显示第几张图片
      showPreview: false,   // 是否展示全屏图片预览遮罩
      previewIndex: 0,      // 全屏预览模式下当前显示的图片索引
    }
  },
  computed: {
    // 把图片字符串按分隔符拆成数组
    imageList() {
      if (!this.note.images) return []
      return this.note.images.split('|||')
    }
  },
  watch: {
    // 监听路由参数 id 的变化：
    // 解决在详情页内部点击“相关推荐”时，Vue Router 复用组件导致生命周期函数不执行、数据不刷新的问题
    '$route.params.id': function(newId) {
      if (newId) {
        // 发现 ID 改变，立刻重新拉取新笔记的详情数据
        this.loadDetail();
        // 切换笔记后，强制将页面平滑滚动回最顶部，提升阅读体验
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }
  },
  mounted() {
    // 检查登录状态
    if (!this.currentUser || !this.currentUser.id) {
      this.$router.push('/login')
      return
    }
    this.loadDetail()
  },
  methods: {
    // 加载笔记详情（包含评论）
    async loadDetail() {
      this.loading = true
      const id = this.$route.params.id  // 从路由参数获取笔记ID
      try {
        const res = await this.$axios.get('/note/detail/' + id, {
          params: { userId: this.currentUser.id }
        })
        if (res.data.code === 200) {
          this.note = res.data.data
          this.comments = res.data.data.comments || []
          // 加载相关推荐
          this.loadRecommends()
        }
      } catch (e) {
        this.$message.error('加载失败')
      }
      this.loading = false
    },

    // 加载相关推荐（同分类的其他笔记）
    async loadRecommends() {
      const res = await this.$axios.get('/note/list', {
        params: {
          categoryId: this.note.categoryId,
          page: 1,
          userId: this.currentUser.id
        }
      })
      if (res.data.code === 200) {
        // 过滤掉当前笔记，最多显示4条
        this.recommends = res.data.data
            .filter(n => n.id !== this.note.id)
            .slice(0, 4)
      }
    },

    // 切换到上一张图片
    prevImg() {
      this.currentImg = this.currentImg > 0
          ? this.currentImg - 1
          : this.imageList.length - 1
    },

    // 切换到下一张图片
    nextImg() {
      this.currentImg = this.currentImg < this.imageList.length - 1
          ? this.currentImg + 1
          : 0
    },

    // 开启全屏图片预览模式，同步当前点击的图片索引
    openPreview(index) {
      this.previewIndex = index
      this.showPreview = true
    },

    // 关闭全屏图片预览模式
    closePreview() {
      this.showPreview = false
    },

    // 预览模式：切换至上一张大图（支持循环轮播）
    prevPreview() {
      this.previewIndex = this.previewIndex > 0
          ? this.previewIndex - 1
          : this.imageList.length - 1
    },

    // 预览模式：切换至下一张大图（支持循环轮播）
    nextPreview() {
      this.previewIndex = this.previewIndex < this.imageList.length - 1
          ? this.previewIndex + 1
          : 0
    },

    // 点赞/取消点赞
    async toggleLike() {
      const res = await this.$axios.post('/note/like', {
        noteId: this.note.id,
        userId: this.currentUser.id
      })
      if (res.data.code === 200) {
        this.note.liked = !this.note.liked
        const current = this.note.likeCount || this.note.like_count || 0
        this.note.likeCount = this.note.liked ? current + 1 : current - 1
        this.note.like_count = this.note.likeCount
      }
    },

    // 收藏/取消收藏
    async toggleCollect() {
      const res = await this.$axios.post('/note/collect', {
        noteId: this.note.id,
        userId: this.currentUser.id
      })
      if (res.data.code === 200) {
        this.note.collected = !this.note.collected
        // 同步更新收藏数字
        this.note.collectCount = this.note.collected
            ? (this.note.collectCount || 0) + 1
            : (this.note.collectCount || 0) - 1
        this.$message.success(this.note.collected ? '已收藏' : '已取消收藏')
      }
    },

    // 发表评论
    async doComment() {
      if (!this.commentText.trim()) {
        this.$message.warning('请输入评论内容')
        return
      }
      const res = await this.$axios.post('/note/comment', {
        noteId: this.note.id,
        userId: this.currentUser.id,
        content: this.commentText
      })
      if (res.data.code === 200) {
        this.$message.success('评论成功')
        this.commentText = ''
        // 重新加载评论列表
        this.loadDetail()
      }
    },

    // 把回复内容按换行拆分，支持展示多人连续回复
    // 后端把多次回复拼接在comment.reply里，这里按行拆分后逐行渲染。
    getReplyLines(replyText) {
      if (!replyText) return []
      return replyText.split('\n')
    },

    // 打开某条评论的回复输入框
    // 每次切换目标评论时清空输入内容，避免把上一条草稿误发到下一条评论。
    startReply(comment) {
      this.replyingCommentId = comment.id
      this.replyText = ''
    },

    // 回复评论：作者和其他用户都走同一接口
    // 统一走 /note/comment/reply，后端按 userId 查昵称并追加到该评论回复中。
    async doReply(commentId) {
      if (!this.replyText.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      const res = await this.$axios.post('/note/comment/reply', {
        commentId,
        userId: this.currentUser.id,
        content: this.replyText
      })
      if (res.data.code === 200) {
        this.$message.success('回复成功')
        this.replyingCommentId = null
        this.replyText = ''
        this.loadDetail()
      }
    },

    // 返回上一页：根据来源页决定返回到哪里，保证用户上下文不丢失
    goBack() {
      if (this.$route.query.from === 'admin') {
        const tab = this.$route.query.tab || 'dashboard'
        this.$router.push('/admin?tab=' + tab)
      } else if (this.$route.query.from === 'profile') {
        const tab = this.$route.query.tab || 'notes'
        this.$router.push('/profile?tab=' + tab)
      } else {
        this.$router.push('/home')
      }
    },

    // 跳转到其他笔记详情
    goNote(id) {
      this.$router.push('/note/' + id)
    }
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.detail-page { min-height: 100vh; background: #f5f5f5; }

/* ===== 顶部导航 ===== */
.navbar {
  position: sticky; top: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 32px; height: 60px;
  background: white; box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.nav-back { color: #666; cursor: pointer; font-size: 14px; padding: 6px 12px; border-radius: 8px; }
.nav-back:hover { background: #f5f5f5; }
.nav-title { font-size: 18px; font-weight: 700; color: #333; }

/* 加载中 */
.loading-wrap { text-align: center; padding: 100px 0; color: #999; font-size: 18px; }

/* ===== 主体：左右布局 ===== */
.detail-body {
  display: flex; gap: 24px;
  max-width: 1100px; margin: 24px auto; padding: 0 24px;
}

/* ===== 左侧主内容 ===== */
.detail-main { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 16px; }

/* 图片轮播容器整体样式：
   固定最小高度保证页面排版稳定，设置浅色兜底底色，
   利用 flex 布局使内部的主体图片始终垂直水平居中显示 */
.image-swiper {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  background: #f8f8f8;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

/* 动态高斯模糊底层样式：
   1. 尺寸溢出(120%)：绝对定位铺满并略微溢出容器，强制消除高斯模糊在容器边缘产生的漏光/白边现象。
   2. 深度模糊：使用 filter: blur(25px) 实现深度的毛玻璃效果，填补不同尺寸图片留下的空白。
   3. 亮度压制：使用 brightness(0.85) 适度压暗背景亮度，确保不喧宾夺主，凸显前方主体图片。
   4. 层级控制：z-index 设为 0，确保其始终处于最底层。 */
.swiper-blur-bg {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 120%;
  height: 120%;
  background-size: cover;
  background-position: center;
  filter: blur(25px) brightness(0.85);
  z-index: 0;
}

/* 主体图片样式：
   限制最大高度防止竖向长图撑爆页面，使用 object-fit: contain 保证图片完整展示绝对不被裁剪。
   z-index 设为 1，确保始终悬浮于高斯模糊背景层之上。 */
.swiper-img {
  width: 100%;
  max-height: 480px;
  object-fit: contain;
  display: block;
  position: relative;
  z-index: 1;
}

.no-image-banner {
  height: 200px; background: linear-gradient(135deg, #fff3ee, #ffe0cc);
  display: flex; align-items: center; justify-content: center;
  font-size: 60px; border-radius: 16px;
}
/* 左右切换箭头基础样式，绝对定位至垂直居中，默认半透明黑底白字并置于最顶层 */
.swiper-arrow {
  position: absolute; top: 50%; transform: translateY(-50%);
  width: 40px; height: 40px; border-radius: 50%;
  background: rgba(0,0,0,0.4); color: white;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 18px; transition: background 0.2s;
}
/* 鼠标悬停翻页箭头时加深背景透明度 */
.swiper-arrow:hover { background: rgba(0,0,0,0.7); }
/* 左箭头贴紧左侧边缘12px处 */
.swiper-arrow.left { left: 12px; }
/* 右箭头贴紧右侧边缘12px处 */
.swiper-arrow.right { right: 12px; }
/* 图片计数 */
.swiper-count {
  position: absolute; top: 12px; right: 12px;
  background: rgba(0,0,0,0.5); color: white;
  font-size: 13px; padding: 4px 10px; border-radius: 12px;
}
/* 底部小圆点 */
.swiper-dots {
  position: absolute; bottom: 12px; left: 50%; transform: translateX(-50%);
  display: flex; gap: 6px;
}
/* 导航小圆点基础样式，默认半透明白色以便透出底层图片颜色 */
.dot {
  width: 8px; height: 8px; border-radius: 50%;
  background: rgba(255,255,255,0.5); cursor: pointer; transition: background 0.2s;
}
/* 当前处于激活状态的导航小圆点，呈现纯白色高亮 */
.dot.active { background: white; }

/* 笔记信息卡片 */
.note-info-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }

/* 作者信息行 */
.author-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.author-left { display: flex; align-items: center; gap: 12px; }
.author-avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ffd93d);
  color: white; font-size: 18px; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}
.author-name { font-size: 15px; font-weight: 600; color: #333; }
.author-time { font-size: 12px; color: #aaa; margin-top: 2px; }

/* 收藏按钮 */
.collect-btn {
  padding: 8px 20px; border-radius: 20px; border: 2px solid #eee;
  background: white; color: #666; cursor: pointer; font-size: 14px; transition: all 0.3s;
}
.collect-btn.collected { background: #fff3ee; border-color: #ff6b35; color: #ff6b35; font-weight: 600; }

/* 笔记标题 */
.note-title { font-size: 24px; font-weight: 800; color: #222; margin-bottom: 12px; line-height: 1.4; }

/* 评分 */
.note-score { display: flex; align-items: center; gap: 4px; margin-bottom: 16px; }
.star-filled { color: #ffd93d; font-size: 20px; }
.star-empty { color: #ddd; font-size: 20px; }
.score-label { font-size: 14px; color: #ff6b35; font-weight: 600; margin-left: 8px; }

/* 正文 */
.note-content { font-size: 15px; color: #444; line-height: 1.8; margin-bottom: 20px; white-space: pre-wrap; }

/* 商家回复气泡整体容器样式，采用暖色背景以区分普通结构 */
.merchant-reply-box {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #fff8f5;
  border: 1px solid #ffe4d7;
  border-radius: 12px;
  position: relative;
}

/* 商家回复气泡左上角的小三角指示器，增强气泡视觉边界 */
.merchant-reply-box::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 20px;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid #ffe4d7;
}

/* 商家回应专属徽标样式 */
.shop-badge {
  font-size: 13px;
  color: #ff6b35;
  font-weight: bold;
  margin-bottom: 8px;
  display: inline-block;
}

/* 商家回复的正文文本样式 */
.reply-content {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

/* 点赞按钮 */
.like-row { display: flex; justify-content: center; }
.like-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 32px; border-radius: 24px; border: 2px solid #eee;
  background: white; color: #666; cursor: pointer; font-size: 15px;
  transition: all 0.3s; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.like-btn.liked { background: #fff0f0; border-color: #ff6b6b; color: #ff6b6b; font-weight: 600; }
.like-num { font-size: 14px; }

/* ===== 评论区 ===== */
.comment-section { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.comment-title { font-size: 18px; font-weight: 700; color: #333; margin-bottom: 20px; }

/* 评论输入区 */
.comment-input-wrap { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.comment-avatar {
  width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #ff6b35, #ffd93d);
  color: white; font-weight: bold; font-size: 14px;
  display: flex; align-items: center; justify-content: center;
}
.comment-input {
  flex: 1; border: 2px solid #eee; border-radius: 24px;
  padding: 10px 20px; font-size: 14px; outline: none; transition: border-color 0.3s;
}
.comment-input:focus { border-color: #ff6b35; }
.comment-submit {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white; border: none; border-radius: 20px;
  padding: 10px 20px; cursor: pointer; font-size: 14px; white-space: nowrap;
}

/* 评论列表 */
.no-comment { text-align: center; padding: 32px 0; color: #bbb; font-size: 14px; }
.comment-item { display: flex; gap: 12px; padding: 16px 0; border-bottom: 1px solid #f5f5f5; }
.comment-avatar-small {
  width: 32px; height: 32px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; font-size: 12px; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}
.comment-body { flex: 1; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.comment-name { font-size: 14px; font-weight: 600; color: #333; }
.comment-time { font-size: 12px; color: #bbb; }
.comment-text { font-size: 14px; color: #555; line-height: 1.6; }
.comment-reply {
  margin-top: 8px; background: #f8f8f8; border-radius: 8px;
  padding: 8px 12px; font-size: 13px; color: #666;
  border-left: 3px solid #ff6b35;
}
.reply-label { color: #ff6b35; font-weight: 600; }
.reply-line { margin-top: 4px; line-height: 1.5; }
.comment-actions { margin-top: 8px; }
.reply-action { font-size: 13px; color: #ff6b35; cursor: pointer; }
.reply-input-wrap { display: flex; align-items: center; gap: 8px; margin-top: 10px; }
.reply-input {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 18px;
  padding: 8px 12px;
  font-size: 13px;
  outline: none;
}
.reply-input:focus { border-color: #ff6b35; }
.reply-submit {
  border: none;
  border-radius: 16px;
  padding: 8px 12px;
  background: #ff6b35;
  color: white;
  cursor: pointer;
  font-size: 12px;
}

/* ===== 右侧侧边栏 ===== */
.detail-sidebar { width: 280px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; position: sticky; top: 80px; height: fit-content; }

/* 店铺信息卡片 */
.shop-card { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.shop-card-title { font-size: 15px; font-weight: 700; color: #333; margin-bottom: 12px; }
.shop-name { font-size: 18px; font-weight: 700; color: #222; margin-bottom: 10px; }
.shop-info-item { font-size: 13px; color: #666; padding: 4px 0; line-height: 1.6; }

/* 相关推荐卡片 */
.recommend-card { background: white; border-radius: 16px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.recommend-item {
  display: flex; gap: 10px; padding: 10px 0;
  border-bottom: 1px solid #f5f5f5; cursor: pointer; transition: opacity 0.2s;
}
.recommend-item:hover { opacity: 0.8; }
.rec-img { width: 60px; height: 60px; border-radius: 8px; object-fit: cover; flex-shrink: 0; }
.rec-img-empty {
  width: 60px; height: 60px; border-radius: 8px; flex-shrink: 0;
  background: #fff3ee; display: flex; align-items: center; justify-content: center; font-size: 24px;
}
.rec-info { flex: 1; min-width: 0; }
.rec-title { font-size: 13px; font-weight: 600; color: #333; margin-bottom: 4px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.rec-author { font-size: 12px; color: #aaa; }
/* 图片全屏预览遮罩层基础样式：深色半透明背景，强制最顶层显示 */
.image-preview-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.92); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}

/* 预览大图样式：限制最大宽高防溢出，注入带有弹簧阻尼感的弹性放大动画 */
.preview-img {
  max-width: 90vw; max-height: 90vh; object-fit: contain;
  animation: popZoom 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
@keyframes popZoom {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

/* 全屏关闭按钮 */
.preview-close {
  position: absolute; top: 32px; right: 40px; color: white;
  font-size: 32px; cursor: pointer; opacity: 0.6; transition: opacity 0.2s;
}
.preview-close:hover { opacity: 1; }

/* 全屏左右切换箭头 */
.preview-arrow {
  position: absolute; top: 50%; transform: translateY(-50%);
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,0.1); color: white;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 24px; transition: background 0.2s;
}
.preview-arrow:hover { background: rgba(255,255,255,0.25); }
.preview-arrow.left { left: 40px; }
.preview-arrow.right { right: 40px; }

/* 全屏底部页码指示器 */
.preview-count {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  color: white; font-size: 16px; letter-spacing: 2px; opacity: 0.8;
}
</style>

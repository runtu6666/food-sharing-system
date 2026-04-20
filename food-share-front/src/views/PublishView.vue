<template>
  <!-- 发布笔记页面容器 -->
  <div class="publish-page">

    <!-- 顶部导航栏 -->
    <div class="navbar">
      <!-- 左侧：两个返回按钮并排 -->
      <div class="nav-left">
        <div class="nav-back" @click="$router.push('/home')">← 首页</div>
        <div class="nav-divider">/</div>
        <div class="nav-back" @click="$router.push('/profile')">个人中心</div>
      </div>
      <div class="nav-title">✏️ 发布探店笔记</div>
      <!-- 右侧提交按钮 -->
      <button class="submit-btn" :class="{loading}" @click="doPublish">
        {{ loading ? '发布中...' : '发布笔记' }}
      </button>
    </div>

    <!-- 主体内容区：左侧表单 + 右侧预览 -->
    <div class="publish-body">

      <!-- 左侧：填写表单 -->
      <div class="form-area">

        <!-- 笔记标题 -->
        <div class="form-section">
          <div class="section-label">📝 笔记标题 <span class="required">*</span></div>
          <input
              v-model="form.title"
              class="title-input"
              placeholder="写一个吸引人的标题，比如：探店｜这家火锅让我念念不忘..."
              maxlength="50"
          />
          <!-- 字数统计 -->
          <div class="word-count">{{ form.title.length }}/50</div>
        </div>

        <!-- 正文内容 -->
        <div class="form-section">
          <div class="section-label">📖 探店内容 <span class="required">*</span></div>
          <textarea
              v-model="form.content"
              class="content-input"
              placeholder="分享你的探店经历吧！可以写：店铺环境、特色菜品、服务态度、消费感受、适合人群..."
              maxlength="2000"
          ></textarea>
          <div class="word-count">{{ form.content.length }}/2000</div>
        </div>

        <!-- 图片上传区域 -->
        <div class="form-section">
          <div class="section-label">🖼️ 添加图片</div>
          <div class="section-tip">支持上传多张图片，第一张将作为封面展示</div>
          <div class="image-upload-area">
            <!-- 已上传的图片预览 -->
            <div
                v-for="(img, index) in form.images"
                :key="index"
                class="image-preview-item"
                :class="{ 'is-cover': index === coverIndex }"
            >
              <img :src="img" alt="预览图"/>
              <!-- 删除按钮 -->
              <div class="delete-img" @click="removeImage(index)">✕</div>
              <!-- 封面标签：当前封面显示已选，其他显示设为封面 -->
              <div
                  class="cover-badge"
                  :class="{ selected: index === coverIndex }"
                  @click="setCover(index)"
              >
                {{ index === coverIndex ? '✅ 封面' : '设为封面' }}
              </div>
            </div>
            <!-- 上传按钮（最多9张） -->
            <div class="upload-btn" v-if="form.images.length < 9" @click="triggerUpload">
              <span class="upload-icon">📷</span>
              <span>添加图片</span>
            </div>
          </div>
          <!-- 隐藏的文件选择input -->
          <input
              ref="fileInput"
              type="file"
              accept="image/*"
              multiple
              style="display:none"
              @change="handleImageUpload"
          />
        </div>

        <!-- 第二行：评分 + 分类 -->
        <div class="form-row">
          <!-- 综合评分 -->
          <div class="form-section half">
            <div class="section-label">⭐ 综合评分</div>
            <div class="star-rating">
              <span
                  v-for="n in 5"
                  :key="n"
                  class="star"
                  :class="{ active: n <= form.score }"
                  @click="form.score = n"
              >★</span>
              <span class="score-text">{{ scoreText }}</span>
            </div>
          </div>

          <!-- 美食分类 -->
          <div class="form-section half">
            <div class="section-label">🏷️ 美食分类</div>
            <div class="category-select">
              <div
                  v-for="cat in categories"
                  :key="cat.id"
                  :class="['cat-tag', form.categoryId === cat.id ? 'active' : '']"
                  @click="form.categoryId = cat.id"
              >{{ cat.name }}</div>
            </div>
          </div>
        </div>

        <!-- 关联店铺（选填） -->
        <div class="form-section">
          <div class="section-label">🏪 关联店铺 <span class="optional">（选填）</span></div>
          <div class="shop-select">
            <input
                v-model="shopKeyword"
                placeholder="搜索店铺名称..."
                @input="searchShops"
                class="shop-search"
            />
            <!-- 搜索结果下拉列表 -->
            <div class="shop-dropdown" v-if="shopList.length > 0">
              <div
                  v-for="shop in shopList"
                  :key="shop.id"
                  class="shop-option"
                  @click="selectShop(shop)"
              >
                <span class="shop-name">{{ shop.name }}</span>
                <span class="shop-addr">{{ shop.address }}</span>
              </div>
            </div>
            <!-- 已选店铺展示 -->
            <div class="selected-shop" v-if="form.shopId">
              <span>已选：{{ selectedShopName }}</span>
              <span class="clear-shop" @click="clearShop">✕</span>
            </div>
          </div>
        </div>

      </div>

      <!-- 右侧：实时预览 -->
      <div class="preview-area">
        <div class="preview-title">📱 预览效果</div>
        <!-- 模拟手机卡片预览 -->
        <div class="preview-phone">
          <div class="preview-card">
            <!-- 预览封面图 -->
            <div class="preview-cover">
              <img v-if="form.images.length > 0" :src="form.images[0]" alt="封面"/>
              <div v-else class="preview-cover-empty">
                <span>🍜</span>
                <p>图片将在此展示</p>
              </div>
            </div>
            <!-- 预览文字内容 -->
            <div class="preview-content">
              <div class="preview-note-title">
                {{ form.title || '你的笔记标题' }}
              </div>
              <div class="preview-note-content">
                {{ form.content || '你的探店内容将在这里展示...' }}
              </div>
              <!-- 预览底部信息 -->
              <div class="preview-footer">
                <div class="preview-author">
                  <div class="preview-avatar">{{ user.nickname ? user.nickname[0] : 'U' }}</div>
                  <span>{{ user.nickname }}</span>
                </div>
                <div class="preview-score">
                  <span v-for="n in form.score" :key="n">⭐</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 温馨提示 -->
        <div class="publish-tips">
          <div class="tip-title">📌 发布须知</div>
          <div class="tip-item">✅ 内容需经过审核后才会公开展示</div>
          <div class="tip-item">✅ 请确保内容真实，禁止虚假宣传</div>
          <div class="tip-item">✅ 图片请使用原创或有版权的图片</div>
          <div class="tip-item">✅ 审核通常在24小时内完成</div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'PublishView',
  data() {
    return {
      // 从本地存储获取当前登录用户
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      loading: false,       // 发布按钮加载状态
      shopKeyword: '',      // 店铺搜索关键词
      shopList: [],         // 搜索到的店铺列表
      selectedShopName: '', // 已选店铺名称（用于显示）
      categories: [],       // 分类列表
      // 表单数据
      coverIndex: 0,  // 封面图片的下标，默认第一张
      form: {
        id: null,           // 记录笔记ID，区分是发布还是修改
        title: '',          // 笔记标题
        content: '',        // 正文内容
        images: [],         // 图片列表（base64或URL）
        score: 5,           // 评分，默认5星
        categoryId: null,   // 分类ID
        shopId: null        // 关联的店铺ID
      }
    }
  },
  computed: {
    // 根据评分返回对应文字
    scoreText() {
      const texts = ['', '很差', '较差', '一般', '不错', '非常好']
      return texts[this.form.score]
    }
  },
  mounted() {
    // 检查登录状态
    if (!this.user || !this.user.id) {
      this.$router.push('/login')
      return
    }
    this.loadCategories()

    // 如果是编辑模式，获取ID并拉取旧数据回显
    const editId = this.$route.query.id;
    if (editId) {
      this.loadEditNote(editId);
    }
  },
  methods: {
    // 加载美食分类列表
    async loadCategories() {
      const res = await this.$axios.get('/category/list')
      if (res.data.code === 200) {
        this.categories = res.data.data
      }
    },

    // 拉取要编辑的笔记详情
    async loadEditNote(id) {
      const res = await this.$axios.get(`/note/detail/${id}`);
      if (res.data.code === 200) {
        const data = res.data.data;
        // 将旧数据填入表单
        this.form = {
          id: data.id,
          title: data.title,
          content: data.content,
          images: data.images ? data.images.split('|||') : [],
          score: data.score,
          categoryId: data.categoryId,
          shopId: data.shopId
        };
        this.selectedShopName = data.shopName;
      }
    },

    // 触发文件选择框
    triggerUpload() {
      this.$refs.fileInput.click()
    },

    // 图片上传：通过 FormData 传给后端，只保存极其轻量的 URL
    async handleImageUpload(e) {
      const files = Array.from(e.target.files)
      // 限制最多9张
      const remaining = 9 - this.form.images.length
      const toAdd = files.slice(0, remaining)

      // 遍历选中的文件，挨个上传给后端
      for (let file of toAdd) {
        // 创建浏览器原生的表单数据对象
        const formData = new FormData()
        formData.append('file', file)

        try {
          const res = await this.$axios.post('/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
          })

          if (res.data.code === 200) {
            // 后端返回的是短短的网络URL (例如 http://localhost:8080/uploads/xxx.jpg)
            // 直接推入数组，极大节省内存和数据库空间
            this.form.images.push(res.data.message)
            this.$message.success('图片上传成功')
          } else {
            this.$message.error(res.data.message || '图片上传失败')
          }
        } catch (error) {
          this.$message.error('上传接口请求出错')
        }
      }

      // 清空input值，允许下次重复选同一张图
      e.target.value = ''
    },

    // 设置封面：把选中图片移到数组第一位
    setCover(index) {
      if (index === this.coverIndex) return
      // 把选中图片取出来放到数组最前面
      const selected = this.form.images.splice(index, 1)[0]
      this.form.images.unshift(selected)
      this.coverIndex = 0
      this.$message.success('封面已设置')
    },

    // 删除已选图片
    removeImage(index) {
      this.form.images.splice(index, 1)
      // 删除后封面始终是第一张
      this.coverIndex = 0
    },

    // 搜索关联店铺
    async searchShops() {
      if (!this.shopKeyword) {
        this.shopList = []
        return
      }
      const res = await this.$axios.get('/shop/search', {
        params: { keyword: this.shopKeyword }
      })
      if (res.data.code === 200) {
        this.shopList = res.data.data
      }
    },

    // 选择店铺
    selectShop(shop) {
      this.form.shopId = shop.id
      this.selectedShopName = shop.name
      this.shopKeyword = ''
      this.shopList = []
    },

    // 清除已选店铺
    clearShop() {
      this.form.shopId = null
      this.selectedShopName = ''
    },

    // 发布笔记：校验 → 提交到后端
    async doPublish() {
      // 基础校验
      if (!this.form.title.trim()) {
        this.$message.warning('请填写笔记标题')
        return
      }
      if (!this.form.content.trim()) {
        this.$message.warning('请填写探店内容')
        return
      }
      this.loading = true
      try {
        // 根据表单中是否包含笔记 ID，来判断当前是“修改”还是“全新发布”
        const requestUrl = this.form.id ? '/note/update' : '/note/publish';

        const res = await this.$axios.post(requestUrl, {
          id: this.form.id, // 把笔记ID传给后端，如果是发布新笔记这里就是null，后端会自动忽略
          userId: this.user.id,
          title: this.form.title,
          content: this.form.content,
          images: this.form.images.join('|||'),
          score: this.form.score,
          categoryId: this.form.categoryId,
          shopId: this.form.shopId
        })

        if (res.data.code === 200) {
          // 动态读取后端 Result.success() 传过来的 message 字段
          this.$message.success(res.data.message)
          // 发布成功后跳回首页
          setTimeout(() => this.$router.push('/home'), 1500)
        } else {
          this.$message.error(res.data.message)
        }
      } catch (e) {
        this.$message.error('发布失败，请重试')
      }
      this.loading = false
    }
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.publish-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航 ===== */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  height: 60px;
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.nav-back {
  color: #666;
  cursor: pointer;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}
/* 左侧导航区域：面包屑风格 */
.nav-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 面包屑分隔符 */
.nav-divider {
  color: #ccc;
  font-size: 14px;
}
.nav-back:hover { background: #f5f5f5; }
.nav-title { font-size: 18px; font-weight: 700; color: #333; }
.submit-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 10px 28px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255,107,53,0.3);
  transition: all 0.3s;
}
.submit-btn:hover { transform: translateY(-2px); }
.submit-btn.loading { opacity: 0.7; cursor: not-allowed; }

/* ===== 主体：左右布局 ===== */
.publish-body {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 24px auto;
  padding: 0 24px;
  width: 100%;
}

/* ===== 左侧表单区 ===== */
.form-area { flex: 1; display: flex; flex-direction: column; gap: 16px; }

/* 表单分区 */
.form-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.form-section.half { flex: 1; }
.form-row { display: flex; gap: 16px; }

/* 分区标签 */
.section-label {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
}
.required { color: #ff4444; }
.optional { color: #aaa; font-weight: normal; font-size: 13px; }
.section-tip { font-size: 12px; color: #aaa; margin-bottom: 12px; }

/* 标题输入框 */
.title-input {
  width: 100%;
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 12px 16px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.3s;
  box-sizing: border-box;
}
.title-input:focus { border-color: #ff6b35; }

/* 内容文本域 */
.content-input {
  width: 100%;
  height: 180px;
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 12px 16px;
  font-size: 14px;
  outline: none;
  resize: none;
  line-height: 1.7;
  transition: border-color 0.3s;
  box-sizing: border-box;
}
.content-input:focus { border-color: #ff6b35; }

/* 字数统计 */
.word-count {
  text-align: right;
  font-size: 12px;
  color: #bbb;
  margin-top: 6px;
}

/* 图片上传区域 */
.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.image-preview-item {
  width: 90px;
  height: 90px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}
.image-preview-item img {
  width: 100%; height: 100%;
  object-fit: cover;
}
/* 已选为封面的图片加橙色边框 */
.image-preview-item.is-cover {
  border: 3px solid #ff6b35;
  border-radius: 10px;
}
/* 封面标签样式 */
.cover-badge {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  background: rgba(0,0,0,0.5);
  color: white;
  font-size: 10px;
  text-align: center;
  padding: 4px 0;
  cursor: pointer;
  transition: background 0.2s;
}
.cover-badge.selected {
  background: rgba(255,107,53,0.9);
}
.cover-badge:hover { background: rgba(255,107,53,0.8); }
/* 删除图片按钮 */
.delete-img {
  position: absolute;
  top: 4px; right: 4px;
  width: 20px; height: 20px;
  background: rgba(0,0,0,0.6);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  cursor: pointer;
}
/* 封面标签 */
.cover-badge {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  background: rgba(255,107,53,0.85);
  color: white;
  font-size: 11px;
  text-align: center;
  padding: 3px 0;
}
/* 上传按钮 */
.upload-btn {
  width: 90px; height: 90px;
  border: 2px dashed #ddd;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #bbb;
  font-size: 12px;
  gap: 6px;
  transition: all 0.2s;
}
.upload-btn:hover { border-color: #ff6b35; color: #ff6b35; }
.upload-icon { font-size: 24px; }

/* 评分星星 */
.star-rating { display: flex; align-items: center; gap: 8px; }
.star {
  font-size: 28px;
  color: #ddd;
  cursor: pointer;
  transition: color 0.2s, transform 0.2s;
}
.star.active { color: #ffd93d; }
.star:hover { transform: scale(1.2); }
.score-text { font-size: 14px; color: #ff6b35; font-weight: 600; margin-left: 8px; }

/* 分类标签 */
.category-select { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-tag {
  padding: 6px 14px;
  border-radius: 20px;
  background: #f5f5f5;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}
.cat-tag:hover { background: #fff3ee; color: #ff6b35; }
.cat-tag.active {
  background: #ff6b35;
  color: white;
  border-color: #ff6b35;
}

/* 店铺搜索 */
.shop-select { position: relative; }
.shop-search {
  width: 100%;
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.3s;
}
.shop-search:focus { border-color: #ff6b35; }
.shop-dropdown {
  position: absolute;
  top: 100%;
  left: 0; right: 0;
  background: white;
  border: 1px solid #eee;
  border-radius: 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
  z-index: 10;
  max-height: 200px;
  overflow-y: auto;
}
.shop-option {
  padding: 10px 16px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background 0.2s;
}
.shop-option:hover { background: #fff3ee; }
.shop-name { font-size: 14px; color: #333; font-weight: 600; }
.shop-addr { font-size: 12px; color: #999; }
.selected-shop {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff3ee;
  color: #ff6b35;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
}
.clear-shop { cursor: pointer; font-weight: bold; }

/* ===== 右侧预览区 ===== */
.preview-area {
  width: 280px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  height: fit-content;
}
.preview-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 16px;
}
/* 手机样式的卡片预览 */
.preview-phone {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
  margin-bottom: 16px;
}
.preview-card { overflow: hidden; }
.preview-cover {
  height: 180px;
  background: #f5f5f5;
  overflow: hidden;
}
.preview-cover img { width: 100%; height: 100%; object-fit: cover; }
.preview-cover-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 14px;
  gap: 8px;
}
.preview-cover-empty span { font-size: 40px; }
.preview-content { padding: 14px; }
.preview-note-title {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.preview-note-content {
  font-size: 12px;
  color: #888;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.6;
}
.preview-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.preview-author { display: flex; align-items: center; gap: 6px; }
.preview-avatar {
  width: 24px; height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ffd93d);
  color: white;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}
.preview-author span { font-size: 12px; color: #999; }
.preview-score { font-size: 12px; }

/* 发布须知 */
.publish-tips {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.tip-title {
  font-size: 14px;
  font-weight: 700;
  color: #333;
  margin-bottom: 10px;
}
.tip-item {
  font-size: 12px;
  color: #666;
  padding: 4px 0;
  line-height: 1.6;
}
</style>
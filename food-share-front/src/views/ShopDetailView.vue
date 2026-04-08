<template>
  <div class="shop-detail-page">
    <div class="navbar">
      <div class="nav-back" @click="$router.back()">❮ 返回</div>
      <div class="nav-title">店铺详情</div>
      <div style="width: 60px;"></div> </div>

    <div class="content-container">
      <div v-if="loadingShop" class="loading-box">正在加载店铺信息...</div>

      <div v-else-if="shop" class="shop-header-card">
        <div class="shop-images-gallery" v-if="shopImages.length > 0">
          <img
              v-for="(img, idx) in shopImages"
              :key="idx"
              :src="img"
              class="gallery-img clickable-img"
              @click="openPreview(img)"
              alt="环境图"
          />
        </div>
        <div v-else class="gallery-placeholder">暂无店铺环境图</div>

        <div class="shop-info-main">
          <div class="shop-name-row">
            <h1 class="shop-name">{{ shop.name }}</h1>
            <div class="shop-status-tag" v-if="shop.status === 1">营业中</div>
          </div>
          <div class="shop-meta-list">
            <div class="meta-item">📍 地址：{{ shop.address }}</div>
            <div class="meta-item">⏰ 营业时间：{{ shop.businessHours }}</div>
            <div class="meta-item">📞 联系电话：{{ shop.phone || '暂无' }}</div>
          </div>
        </div>
      </div>

      <div class="shop-content-section" v-if="shop">
        <div class="tab-nav">
          <div :class="['tab-item', activeTab === 'dishes' ? 'active' : '']" @click="activeTab = 'dishes'">
            🍽️ 招牌菜品
          </div>
          <div :class="['tab-item', activeTab === 'reviews' ? 'active' : '']" @click="activeTab = 'reviews'">
            💬 顾客评价
          </div>
        </div>

        <div v-show="activeTab === 'dishes'" class="tab-panel">
          <div v-if="loadingDishes" class="loading-box">正在加载菜单...</div>
          <div v-else-if="dishes.length === 0" class="empty-box">这家店还没有上传菜单哦</div>
          <div v-else class="dish-grid">
            <div v-for="dish in dishes" :key="dish.id" class="dish-card">
              <div class="dish-img-wrap">
                <img v-if="dish.image" :src="dish.image" class="dish-img clickable-img" @click="openPreview(dish.image)"/>
                <div v-else class="dish-no-img">暂无图片</div>

                <div class="sold-out-mask" v-if="dish.status === 0">已售罄</div>
              </div>
              <div class="dish-info">
                <div class="dish-name">{{ dish.name }}</div>
                <div class="dish-price-wrap">
                  <span v-if="dish.discountPrice && dish.discountPrice > 0" class="dish-discount">
                    ¥ {{ dish.discountPrice }}
                  </span>
                  <span :class="['dish-price', (dish.discountPrice && dish.discountPrice > 0) ? 'old-price' : '']">
                    ¥ {{ dish.price }}
                  </span>
                </div>
                <div class="dish-desc">{{ dish.description || '暂无描述' }}</div>
              </div>
            </div>
          </div>
        </div>

        <div v-show="activeTab === 'reviews'" class="tab-panel">
          <div v-if="loadingReviews" class="loading-box">正在加载评价...</div>
          <div v-else-if="reviews.length === 0" class="empty-box">暂时还没有人评价，快去抢沙发！</div>
          <div v-else class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-card">
              <div class="review-header">
                <div class="reviewer-info">
                  <div class="avatar">{{ review.username ? review.username[0] : 'U' }}</div>
                  <span class="name">{{ review.username }}</span>
                </div>
                <div class="rating">{'⭐'.repeat(review.rating)}</div>
              </div>
              <div class="review-content">{{ review.content }}</div>
              <div class="review-time">{{ review.createTime }}</div>

              <div class="merchant-reply-box" v-if="review.merchantReply">
                <span class="reply-label">商家回复：</span>
                <span class="reply-text">{{ review.merchantReply }}</span>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <div v-if="showPreview" class="image-preview-overlay" @click.self="closePreview">
      <span class="preview-close" @click="closePreview">✕</span>
      <img :src="previewImageUrl" class="preview-img" alt="全屏大图" />
    </div>

  </div>
</template>

<script>
export default {
  name: 'ShopDetailView',
  data() {
    return {
      shopId: this.$route.params.id, // 从路由 URL 中捕获店铺 ID
      shop: null,                    // 店铺基础数据
      shopImages: [],                // 解析后的店铺图片数组
      loadingShop: true,

      activeTab: 'dishes',           // 默认展开菜品面板

      dishes: [],                    // 菜品列表
      loadingDishes: false,

      reviews: [],                   // 评价列表
      loadingReviews: false,

      // 图片预览相关状态
      showPreview: false,
      previewImageUrl: ''
    }
  },
  mounted() {
    // 页面加载完毕后，立刻基于拿到的 shopId 请求数据
    this.fetchShopDetail();
    this.fetchShopDishes();
    this.fetchShopReviews();
  },
  methods: {
    // 拉取店铺基础信息并解析图片外链
    async fetchShopDetail() {
      try {
        const res = await this.$axios.get(`/shop/detail?id=${this.shopId}`);
        if (res.data.code === 200) {
          this.shop = res.data.data;
          // 解析图片字符串为数组，兼容 '|||' 分隔符和普通单图格式
          if (this.shop.shopImages) {
            this.shopImages = this.shop.shopImages.includes('|||')
                ? this.shop.shopImages.split('|||').filter(Boolean)
                : [this.shop.shopImages];
          }
        } else {
          this.$message.error('店铺信息加载失败');
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.loadingShop = false;
      }
    },

    // 拉取该店铺关联的菜品列表，包含所有状态的菜品，交由模板层渲染不同视觉效果
    async fetchShopDishes() {
      this.loadingDishes = true;
      try {
        const res = await this.$axios.get(`/dish/list?shopId=${this.shopId}`);
        if (res.data.code === 200) {
          this.dishes = res.data.data || [];
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.loadingDishes = false;
      }
    },

    // 拉取该店铺的顾客评价数据
    async fetchShopReviews() {
      this.loadingReviews = true;
      try {
        const res = await this.$axios.get(`/shop/reviews/list?shopId=${this.shopId}`);
        if (res.data.code === 200) {
          this.reviews = res.data.data || [];
        }
      } catch (e) {
        console.error(e);
      } finally {
        this.loadingReviews = false;
      }
    },

    // 触发图片全屏预览
    openPreview(url) {
      if (!url) return;
      this.previewImageUrl = url;
      this.showPreview = true;
    },

    // 关闭图片全屏预览
    closePreview() {
      this.showPreview = false;
      this.previewImageUrl = '';
    }
  }
}
</script>

<style scoped>
/* ===== 页面基础布局 ===== */
.shop-detail-page {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: 40px;
}

/* ===== 顶部导航栏 ===== */
.navbar {
  position: sticky; top: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 32px; height: 60px;
  background: white; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.nav-back { font-weight: 600; color: #666; cursor: pointer; transition: color 0.2s; }
.nav-back:hover { color: #ff6b35; }
.nav-title { font-size: 18px; font-weight: bold; color: #333; }

/* ===== 内容区容器 ===== */
.content-container { max-width: 1000px; margin: 24px auto; padding: 0 16px; }

/* 状态提示块 */
.loading-box, .empty-box {
  text-align: center; padding: 60px 0; color: #999; font-size: 15px;
}

/* ===== 1. 店铺基本信息卡片 ===== */
.shop-header-card {
  background: white; border-radius: 16px; padding: 24px;
  margin-bottom: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

/* 图片横向滚动区 */
.shop-images-gallery {
  display: flex; gap: 16px; overflow-x: auto; padding-bottom: 12px; margin-bottom: 20px;
}
/* 隐藏横向滚动条以保持美观 */
.shop-images-gallery::-webkit-scrollbar { display: none; }
.gallery-img {
  width: 240px; height: 160px; object-fit: cover; border-radius: 12px; flex-shrink: 0;
}
.gallery-placeholder {
  height: 160px; background: #f0f2f5; border-radius: 12px; margin-bottom: 20px;
  display: flex; align-items: center; justify-content: center; color: #aaa;
}

/* 核心文字信息 */
.shop-name-row { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.shop-name { margin: 0; font-size: 24px; font-weight: 900; color: #222; }
.shop-status-tag {
  background: #e1f3d8; color: #67c23a; padding: 4px 10px; border-radius: 6px;
  font-size: 12px; font-weight: bold;
}
.shop-meta-list { display: flex; flex-direction: column; gap: 8px; }
.meta-item { font-size: 14px; color: #555; }

/* ===== 2. 核心内容区 (Tab + 面板) ===== */
.shop-content-section {
  background: white; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); overflow: hidden;
}

/* 选项卡导航 */
.tab-nav {
  display: flex; border-bottom: 1px solid #eee; background: #fafafa;
}
.tab-item {
  padding: 16px 32px; font-size: 16px; font-weight: 600; color: #888; cursor: pointer;
  transition: all 0.2s; border-bottom: 3px solid transparent; margin-bottom: -1px;
}
.tab-item.active { color: #ff6b35; border-bottom-color: #ff6b35; background: white; }

.tab-panel { padding: 24px; }

/* ----- 2.1 菜品网格布局 ----- */
.dish-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 20px;
}
.dish-card {
  border: 1px solid #f0f0f0; border-radius: 12px; overflow: hidden; transition: transform 0.2s;
}
.dish-card:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(0,0,0,0.06); }
.dish-img-wrap { width: 100%; height: 150px; background: #fafafa; }
.dish-img { width: 100%; height: 100%; object-fit: cover; }
.dish-no-img { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; color: #bbb; font-size: 13px; }
.dish-info { padding: 12px; }
.dish-name { font-size: 15px; font-weight: bold; color: #333; margin-bottom: 6px; }
.dish-price { font-size: 16px; font-weight: 800; color: #ff6b35; margin-bottom: 6px; }
.dish-desc { font-size: 12px; color: #999; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* ----- 2.2 评价列表布局 ----- */
.review-list { display: flex; flex-direction: column; gap: 20px; }
.review-card { border-bottom: 1px solid #f5f5f5; padding-bottom: 20px; }
.review-card:last-child { border-bottom: none; padding-bottom: 0; }
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.reviewer-info { display: flex; align-items: center; gap: 8px; }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: #ffd93d; color: white; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.name { font-size: 14px; font-weight: bold; color: #444; }
.rating { font-size: 14px; letter-spacing: 2px; }
.review-content { font-size: 14px; color: #333; line-height: 1.6; margin-bottom: 10px; }
.review-time { font-size: 12px; color: #aaa; margin-bottom: 12px; }

/* 商家回复专属气泡 */
.merchant-reply-box {
  background: #f8f9fa; padding: 12px 16px; border-radius: 8px; border-left: 3px solid #ff6b35;
}
.reply-label { font-size: 13px; font-weight: bold; color: #ff6b35; margin-right: 6px; }
.reply-text { font-size: 13px; color: #555; line-height: 1.5; }

/* ===== 全局交互辅助样式 ===== */
.clickable-img { cursor: pointer; transition: opacity 0.2s; }
.clickable-img:hover { opacity: 0.9; }

/* 图片全屏预览弹窗 */
.image-preview-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.92); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.preview-img {
  max-width: 90vw; max-height: 90vh; object-fit: contain;
  animation: popZoom 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
@keyframes popZoom { 0% { transform: scale(0.8); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
.preview-close { position: absolute; top: 32px; right: 40px; color: white; font-size: 32px; cursor: pointer; opacity: 0.6; }
.preview-close:hover { opacity: 1; }

/* 图片容器定位参照，确保遮罩层能绝对定位在图片正上方 */
.dish-img-wrap { position: relative; }

/* 售罄半透明遮罩层样式 */
.sold-out-mask {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); color: white; display: flex;
  align-items: center; justify-content: center; font-size: 16px;
  font-weight: bold; letter-spacing: 2px;
}

/* 售罄卡片整体视觉降级处理，透明度降低并置灰，同时禁用点击事件交互 */
.sold-out { opacity: 0.6; filter: grayscale(80%); pointer-events: none; }
.dish-price-wrap { display: flex; align-items: baseline; gap: 8px; margin-bottom: 6px; }
.dish-discount { font-size: 18px; font-weight: 900; color: #e53935; }
.old-price { text-decoration: line-through; color: #999; font-size: 13px; font-weight: normal; }
</style>
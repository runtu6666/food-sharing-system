<template>
  <div class="merchant-page">

    <!-- 顶部导航 -->
    <div class="navbar">
      <div class="nav-back" @click="handleBack">
        ← {{ (!myShop && myShops.length > 0) ? '返回店铺管理' : '返回首页' }}
      </div>
      <div class="nav-title">🏪 商家中心</div>
      <div style="width:80px"></div>
    </div>

    <div class="merchant-body">

      <!-- 如果还没有店铺：显示入驻申请 -->
      <div v-if="!myShop" class="register-page-new">

        <div class="register-container">
          <div class="register-header">
            <h2>🏪 店铺入驻申请</h2>
            <p>填写以下信息,提交后管理员将在24小时内完成审核</p>
          </div>

          <div class="register-form">

            <div class="form-item">
              <label>店铺名称 <span class="required">*</span></label>
              <div class="input-wrap">
                <span class="input-icon">🏪</span>
                <input v-model="form.name" placeholder="请输入店铺名称" class="nice-input"/>
              </div>
            </div>

            <div class="form-item">
              <label>店铺地址 <span class="required">*</span></label>
              <div class="input-wrap">
                <span class="input-icon">📍</span>
                <input v-model="form.address" placeholder="请输入详细地址" class="nice-input"/>
              </div>
            </div>

            <!-- 地图选点 - 放大版 -->
            <div class="form-item">
              <label>地图位置 <span class="required">*</span></label>
              <div class="map-description">
                在地图上标记店铺位置,可拖动标记调整。使用下方搜索框快速定位到您的城市区域。
              </div>

              <!-- 地点搜索 -->
              <div class="map-search-bar">
                <input
                    v-model="searchKeyword"
                    @keyup.enter="searchLocation"
                    placeholder="🔍 输入省市区快速定位,如:山东省济南市长清区"
                    class="map-search-input-large"
                />
                <button @click="searchLocation" class="map-search-btn-large">搜索定位</button>
              </div>

              <div id="map-container" class="map-container-large"></div>

              <div class="location-display" v-if="form.latitude && form.longitude">
                <div class="location-item">
                  <span class="location-label">经度:</span>
                  <span class="location-value">{{ form.longitude.toFixed(6) }}</span>
                </div>
                <div class="location-item">
                  <span class="location-label">纬度:</span>
                  <span class="location-value">{{ form.latitude.toFixed(6) }}</span>
                </div>
                <div class="location-item">
                  <span class="location-label">地址:</span>
                  <span class="location-value">{{ form.address }}</span>
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="form-item">
                <label>联系电话 <span class="required">*</span></label>
                <div class="input-wrap">
                  <span class="input-icon">📞</span>
                  <input v-model="form.phone" placeholder="请输入联系电话" class="nice-input"/>
                </div>
              </div>

              <div class="form-item">
                <label>营业时间</label>
                <div class="input-wrap">
                  <span class="input-icon">🕐</span>
                  <input v-model="form.businessHours" placeholder="例如：09:00-22:00" class="nice-input"/>
                </div>
              </div>
            </div>

            <div class="form-item">
              <label>美食分类 <span class="required">*</span></label>
              <div class="category-select">
                <div
                    v-for="cat in categories"
                    :key="cat.id"
                    :class="['cat-tag', form.categoryId === cat.id ? 'active' : '']"
                    @click="form.categoryId = cat.id"
                >{{ cat.name }}</div>
              </div>
            </div>

            <div class="form-item">
              <label>法人姓名 <span class="required">*</span></label>
              <div class="input-wrap">
                <span class="input-icon">👤</span>
                <input v-model="form.legalName" placeholder="请输入法人/负责人真实姓名" class="nice-input"/>
              </div>
            </div>

            <div class="form-item">
              <label>店铺实景照片 <span class="required">*</span></label>
              <div class="upload-tip">上传店铺真实照片,帮助管理员核实资质(最多3张)</div>
              <div class="shop-image-upload">
                <div
                    v-for="(img, index) in form.shopImages"
                    :key="index"
                    class="shop-img-preview"
                >
                  <img :src="img" alt="店铺照片"/>
                  <div class="del-img" @click="form.shopImages.splice(index, 1)">✕</div>
                </div>
                <div class="upload-box" v-if="form.shopImages.length < 3" @click="$refs.shopImgInput.click()">
                  <span style="font-size:32px">📷</span>
                  <span style="font-size:13px;color:#aaa;margin-top:8px">添加照片</span>
                </div>
              </div>
              <input
                  ref="shopImgInput"
                  type="file"
                  accept="image/*"
                  multiple
                  style="display:none"
                  @change="handleShopImages"
              />
            </div>

            <div class="form-item">
              <label>店铺简介</label>
              <textarea v-model="form.description" placeholder="介绍一下你的店铺特色..." class="nice-textarea"></textarea>
            </div>

            <div class="form-actions">
              <button class="submit-btn-large" :class="{loading}" @click="doRegister">
                {{ loading ? '提交中...' : '🚀 提交申请' }}
              </button>
            </div>
          </div>
        </div>

      </div>

      <!-- 如果已有店铺：显示店铺管理页面 -->
      <div v-else class="manage-wrap">

        <!-- 多店铺切换栏 -->
        <div class="sidebar-shop-list" v-if="myShops.length > 0">
          <div class="sidebar-header">
            <h3>店铺列表</h3>
            <button class="add-shop-btn" @click="myShop = null">+ 新增</button>
          </div>
          <div class="shop-card-list">
            <div
                v-for="shop in myShops"
                :key="shop.id"
                :class="['shop-card-item', { active: myShop && myShop.id === shop.id }]"
                @click="switchShop(shop)"
            >
              <div class="card-main">
                <span class="shop-name-text">{{ shop.name }}</span>
                <span :class="['status-dot', shop.status === 1 ? 'approved' : (shop.status === 2 || shop.status === -1 ? 'rejected' : 'pending')]"></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 左侧：店铺信息 -->
        <div class="shop-info-card">
          <div :class="['shop-status-banner', myShop.status === 1 ? 'approved' : (myShop.status === 2 ? 'banned' : (myShop.status === -1 ? 'rejected' : 'pending'))]">
            {{ myShop.status === 1 ? '✅ 已通过审核，正常营业中' :
              (myShop.status === 2 ? ('🚫 该店铺已被封禁！原因：' + (myShop.rejectReason || myShop.reject_reason || '严重违规') + '。请联系管理员申诉: 15154797270@163.com') :
                  (myShop.status === -1 ? ('❌ 审核已拒绝：' + (myShop.rejectReason || myShop.reject_reason || '资料不符') + '。请修改后重新提交') : '⏳ 审核中，请耐心等待')) }}
          </div>

          <div class="shop-detail">
            <h2 class="shop-name">{{ myShop.name }}</h2>
            <div class="shop-info-item">📍 {{ myShop.address }}</div>
            <div class="shop-info-item">📞 {{ myShop.phone }}</div>
            <div class="shop-info-item" v-if="myShop.businessHours">🕐 {{ myShop.businessHours }}</div>
            <div class="shop-info-item">
              👤 {{ myShop.legalName || myShop.legal_name || '未填写法人姓名' }}
            </div>
            <div v-if="myShop.shopImages || myShop.shop_images" style="margin-top:12px">
              <div style="font-size:13px;color:#aaa;margin-bottom:8px">店铺实景照片</div>
              <div style="display:flex;gap:8px;flex-wrap:wrap">
                <img
                    v-for="(img, idx) in (myShop.shopImages || myShop.shop_images).split('|||')"
                    :key="idx"
                    :src="img"
                    style="width:80px;height:60px;border-radius:8px;object-fit:cover"
                    alt="店铺照片"
                />
              </div>
            </div>
          </div>

          <div class="edit-section" v-if="myShop.status !== 2">
            <h3>修改店铺信息</h3>
            <div class="form-item">
              <label>联系电话</label>
              <input v-model="editForm.phone" :placeholder="myShop.phone" class="form-input"/>
            </div>
            <div class="form-item">
              <label>营业时间</label>
              <input v-model="editForm.businessHours" :placeholder="myShop.businessHours || '请输入营业时间'" class="form-input"/>
            </div>
            <div class="form-item">
              <label>店铺地址</label>
              <div class="edit-address-wrap">
                <input v-model="editForm.address" :placeholder="myShop.address" class="form-input" readonly />
                <button class="map-btn" @click="openMapDialog">📍 选点</button>
              </div>
            </div>

            <div class="form-item shop-image-upload-item">
              <label>管理店铺实景图 (最多3张)</label>

              <div class="shop-image-upload" v-if="editForm.shopImages && editForm.shopImages.length > 0" style="margin-bottom: 12px;">
                <div v-for="(img, index) in editForm.shopImages" :key="index" class="shop-img-preview">
                  <img :src="img" alt="图片预览"/>
                  <div class="del-img" @click="editForm.shopImages.splice(index, 1)">✕</div>
                </div>
              </div>

              <div class="upload-btn-wrap" v-if="!editForm.shopImages || editForm.shopImages.length < 3">
                <button class="upload-trigger-btn" @click="triggerImageUpload">
                  📸 添加新图片
                </button>
                <span class="upload-tip">
                  已选 <b>{{ editForm.shopImages ? editForm.shopImages.length : 0 }}</b> / 3 张
                </span>
              </div>

              <input
                  type="file"
                  ref="imageUpload"
                  multiple
                  accept="image/*"
                  class="hidden-file-input"
                  @change="handleImageUpload"
              />
            </div>

            <button class="save-btn" @click="doUpdate">保存修改</button>
          </div>
        </div>

        <!-- 右侧：Tab切换 -->
        <div class="right-panel" v-if="myShop.status === 2" style="display: flex; flex-direction: column; align-items: center; justify-content: center; background: #fffafb; border: 1px solid #ffccc7;">
          <div style="font-size: 80px; margin-bottom: 20px;">🚫</div>
          <h2 style="color: #ff4d4f; margin-bottom: 12px;">店铺已被封禁，功能已全面冻结</h2>
          <p style="color: #888; font-size: 15px;">您的店铺由于违规已被封禁，暂无法进行菜品管理、修改信息及回复评论等任何操作。</p>
          <div style="margin-top: 24px; padding: 12px 24px; background: #fff1f0; color: #cf1322; border-radius: 8px; font-size: 14px; max-width: 80%;">
            <b>封禁原因：</b>{{ myShop.rejectReason || myShop.reject_reason || '严重违规' }}
          </div>
          <button class="save-btn" style="margin-top: 30px; width: 200px; background: #ff4d4f;" @click="$message.error('当前状态已锁定，点击任何功能均无效')">
            操作已冻结
          </button>
        </div>

        <div class="right-panel" v-if="myShop.status !== 2">
          <div class="tab-nav">
            <div :class="['tab-item', activeTab === 'dishes' ? 'active' : '']" @click="activeTab = 'dishes'">
              🍽️ 菜品管理
            </div>
            <div :class="['tab-item', activeTab === 'reviews' ? 'active' : '']" @click="activeTab = 'reviews'">
              💬 用户评价
            </div>
            <div :class="['tab-item', activeTab === 'notes' ? 'active' : '']" @click="activeTab = 'notes'">
              📝 顾客探店笔记
            </div>
          </div>

          <div class="tab-content">

            <!-- Tab1: 探店笔记 -->
            <div v-show="activeTab === 'notes'" class="notes-panel">
              <div class="panel-header">
                <h3>顾客探店笔记</h3>
                <span class="count">共 {{ shopNotes.length }} 篇</span>
              </div>

              <div v-if="shopNotes.length === 0" class="no-data">
                <div style="font-size:40px;margin-bottom:12px">📝</div>
                <p>还没有顾客发布探店笔记</p>
              </div>

              <div class="note-list">
                <div v-for="note in shopNotes" :key="note.id" class="note-item">
                  <div class="note-header">
                    <div class="note-author">
                      <div class="note-avatar">{{ note.nickname ? note.nickname[0] : 'U' }}</div>
                      <div>
                        <div class="note-name">{{ note.nickname }}</div>
                        <div class="note-time">{{ note.createTime }}</div>
                      </div>
                    </div>
                    <div class="note-score">
                      <span v-for="n in (note.score || 5)" :key="n">⭐</span>
                    </div>
                  </div>

                  <div class="note-title">{{ note.title }}</div>
                  <div class="note-content">{{ note.content }}</div>

                  <div class="note-images" v-if="note.images">
                    <img
                        v-for="(img, idx) in note.images.split('|||').slice(0,3)"
                        :key="idx"
                        :src="img"
                        class="note-img"
                        alt=""
                    />
                  </div>

                  <div class="reply-section">
                    <div v-if="note.reply" class="existing-reply">
                      <span class="reply-label">商家回复：</span>{{ note.reply }}
                      <span class="edit-reply" @click="startReply(note)">修改回复</span>
                    </div>
                    <div v-if="replyingNoteId === note.id" class="reply-input-wrap">
                      <input v-model="replyText" placeholder="输入商家回复..." class="reply-input"/>
                      <button class="reply-btn" @click="doReply(note.id)">发送</button>
                      <button class="reply-cancel" @click="replyingNoteId = null">取消</button>
                    </div>
                    <button v-if="!note.reply && replyingNoteId !== note.id" class="start-reply-btn" @click="startReply(note)">
                      💬 回复顾客
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Tab2: 用户评价 -->
            <div v-show="activeTab === 'reviews'" class="reviews-panel">
              <div class="panel-header">
                <h3>用户评价</h3>
                <span class="count">共 {{ shopReviews.length }} 条</span>
              </div>

              <div v-if="shopReviews.length === 0" class="no-data">
                <div style="font-size:40px;margin-bottom:12px">💬</div>
                <p>还没有用户评价</p>
              </div>

              <div class="review-list">
                <div v-for="review in shopReviews" :key="review.id" class="review-item">
                  <div class="review-header">
                    <div class="review-author">
                      <div class="review-avatar">{{ review.username ? review.username[0] : 'U' }}</div>
                      <div>
                        <div class="review-name">{{ review.username }}</div>
                        <div class="review-time">{{ formatTime(review.createTime) }}</div>
                      </div>
                    </div>
                    <div class="review-score">
                      <span v-for="n in review.rating" :key="n">⭐</span>
                    </div>
                  </div>

                  <div class="review-content">{{ review.content }}</div>

                  <div class="reply-section">
                    <div v-if="review.merchantReply" class="existing-reply">
                      <span class="reply-label">商家回复：</span>{{ review.merchantReply }}
                    </div>
                    <div v-if="replyingReviewId === review.id" class="reply-input-wrap">
                      <input v-model="reviewReplyText" placeholder="输入商家回复..." class="reply-input"/>
                      <button class="reply-btn" @click="doReplyReview(review.id)">发送</button>
                      <button class="reply-cancel" @click="replyingReviewId = null">取消</button>
                    </div>
                    <button v-if="!review.merchantReply && replyingReviewId !== review.id" class="start-reply-btn" @click="startReplyReview(review)">
                      💬 回复顾客
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-show="activeTab === 'dishes'" class="dishes-panel">
              <div class="panel-header">
                <h3>店铺菜品管理</h3>
                <button class="add-shop-btn" @click="openDishDialog(null)">+ 添加菜品</button>
              </div>

              <div v-if="shopDishes.length === 0" class="no-data">
                <div style="font-size:40px;margin-bottom:12px">🍽️</div>
                <p>还没有添加任何菜品，赶快丰富你的菜单吧</p>
              </div>

              <div class="dish-grid">
                <div v-for="dish in shopDishes" :key="dish.id" class="dish-card">
                  <div class="dish-img-wrap">
                    <img v-if="dish.image" :src="dish.image" class="dish-img clickable-img" alt="菜品图片" @click="handlePreview(dish.image)"/>
                    <div v-else class="dish-no-img">暂无图片</div>
                    <div :class="['dish-status-tag', dish.status === 1 ? 'on-sale' : 'off-sale']">
                      {{ dish.status === 1 ? '售卖中' : '已售罄' }}
                    </div>
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
                    <div class="dish-actions">
                      <button class="dish-btn edit" @click="openDishDialog(dish)">编辑</button>
                      <button class="dish-btn toggle" @click="toggleDishStatus(dish)">
                        {{ dish.status === 1 ? '设为售罄' : '重新上架' }}
                      </button>
                      <button class="dish-btn delete" @click="deleteDish(dish.id)">删除</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="map-dialog-overlay" v-if="showDishDialog">
              <div class="map-dialog" style="width: 500px;">
                <div class="dialog-header">
                  <h3>{{ dishForm.id ? '编辑菜品' : '新增菜品' }}</h3>
                  <span class="close-btn" @click="showDishDialog = false">✕</span>
                </div>
                <div class="dialog-body">
                  <div class="form-item">
                    <label>菜品名称 <span class="required">*</span></label>
                    <input v-model="dishForm.name" class="form-input" placeholder="输入菜品名称"/>
                  </div>
                  <div class="form-item">
                    <label>菜品价格 (元) <span class="required">*</span></label>
                    <input type="number" v-model="dishForm.price" class="form-input" placeholder="输入价格"/>
                  </div>
                  <div class="form-item">
                    <label>特价/优惠价 (元) - 可选</label>
                    <input type="number" v-model="dishForm.discountPrice" class="nice-input" placeholder="不填则按原价售卖" step="0.01"/>
                  </div>
                  <div class="form-item">
                    <label>菜品图片</label>
                    <div class="shop-image-upload">
                      <div v-if="dishForm.image" class="shop-img-preview">
                        <img :src="dishForm.image" alt="预览" class="clickable-img" @click="handlePreview(dishForm.image)"/>
                        <div class="del-img" @click="dishForm.image = ''">✕</div>
                      </div>
                      <div v-else class="upload-box" @click="$refs.dishImgInput.click()">
                        <span style="font-size:24px">📷</span>
                        <span style="font-size:12px;color:#aaa;margin-top:4px">上传图片</span>
                      </div>
                      <input ref="dishImgInput" type="file" accept="image/*" style="display:none" @change="handleDishImageUpload"/>
                    </div>
                  </div>
                  <div class="form-item">
                    <label>菜品描述</label>
                    <textarea v-model="dishForm.description" class="nice-textarea" placeholder="一句话介绍这个菜品..."></textarea>
                  </div>
                  <div class="form-item">
                    <label>售卖状态</label>
                    <div style="display: flex; gap: 16px; margin-top: 8px;">
                      <label style="cursor: pointer;">
                        <input type="radio" v-model="dishForm.status" :value="1" /> 售卖中
                      </label>
                      <label style="cursor: pointer;">
                        <input type="radio" v-model="dishForm.status" :value="0" /> 已售罄 / 下架
                      </label>
                    </div>
                  </div>
                </div>
                <div class="dialog-footer">
                  <button class="cancel-btn" @click="showDishDialog = false">取消</button>
                  <button class="confirm-btn" @click="saveDish">保存菜品</button>
                </div>
              </div>
            </div>

          </div>
        </div>

      </div>
    </div>
    <div class="map-dialog-overlay" v-if="showMapDialog">
      <div class="map-dialog">
        <div class="dialog-header">
          <h3>📍 修改店铺位置</h3>
          <span class="close-btn" @click="showMapDialog = false">✕</span>
        </div>
        <div class="dialog-body">
          <div class="map-search-bar">
            <input
                v-model="editSearchKeyword"
                @keyup.enter="searchEditLocation"
                placeholder="🔍 输入省市区快速定位"
                class="map-search-input-large"
            />
            <button @click="searchEditLocation" class="map-search-btn-large">搜索定位</button>
          </div>
          <div id="edit-map-container" class="map-container-large"></div>
          <div class="location-display" v-if="editForm.latitude && editForm.longitude">
            <div class="location-item">
              <span class="location-label">当前选中地址:</span>
              <span class="location-value">{{ editForm.address }}</span>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="cancel-btn" @click="showMapDialog = false">取消</button>
          <button class="confirm-btn" @click="confirmLocation">确定保存位置</button>
        </div>
      </div>
    </div>
    <el-dialog
        title="图片预览"
        v-model="dialogVisible"
        width="600px"
        custom-class="image-preview-dialog"
        destroy-on-close>
      <img width="100%" :src="dialogImageUrl" alt="放大预览图片" style="border-radius: 8px; object-fit: contain;"/>
    </el-dialog>
  </div>
</template>

<script>
/* global BMap */  // 告诉ESLint BMap是全局变量，就不会报错了
export default {
  name: 'MerchantView',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      loading: false,

      // 百度地图相关
      map: null,           // 地图实例
      marker: null,        // 地图标记
      searchKeyword: '',   // 地点搜索关键词

      // 店铺相关
      myShops: [],
      myShop: null,
      shopNotes: [],
      shopReviews: [],
      shopDishes: [],        // 当前店铺的菜品列表
      showDishDialog: false, // 控制菜品编辑弹窗显示
      dishForm: {            // 菜品表单数据模型
        id: null,
        name: '',
        price: '',
        image: '',
        description: '',
        status: 1
      },
      dialogImageUrl: '',  // 存放当前正在预览的大图的网络 URL 地址
      dialogVisible: false, // 标识位：true 代表图片预览弹窗处于可见状态，false 代表已关闭
      categories: [],

      // Tab切换：默认打开菜品管理
      activeTab: 'dishes',

      // 回复相关
      replyingNoteId: null,
      replyText: '',
      replyingReviewId: null,
      reviewReplyText: '',

      // 入驻申请表单
      form: {
        name: '',
        address: '',
        phone: '',
        businessHours: '',
        categoryId: null,
        description: '',
        legalName: '',
        shopImages: [],
        latitude: null,   // 纬度
        longitude: null   // 经度
      },

      // 编辑店铺表单
      editForm: {
        phone: '',
        businessHours: '',
        address: '',
        latitude: null,
        longitude: null,
        shopImages: [] // 存放商家选择的新图片 Base64 字符串
      },
      showMapDialog: false,  // 控制弹窗显示
      editSearchKeyword: '', // 弹窗里的搜索词
      editMap: null,         // 弹窗地图实例
      editMarker: null       // 弹窗地图上的标记点
    }
  },

  mounted() {
    // 权限校验
    if (!this.user || !this.user.id) {
      this.$router.push('/login')
      return
    }
    if (this.user.role !== 'shop') {
      this.$message.warning('当前账号不是商家，请使用商家账号登录')
      this.$router.push('/home')
      return
    }

    this.loadCategories()
    this.loadMyShop()
  },

  watch: {
    // 监听myShop变化,当变为null时初始化地图
    myShop(newVal) {
      if (newVal === null) {
        // 延迟初始化,确保DOM已渲染
        this.$nextTick(() => {
          setTimeout(() => {
            this.initMap()
          }, 100)
        })
      }
    }
  },

  methods: {

    // 处理导航栏返回点击事件
    handleBack() {
      // 如果当前在“新增店铺”页面（myShop为空），且账号下已有其他店铺，则切换回已有店铺的管理视图
      if (!this.myShop && this.myShops.length > 0) {
        this.myShop = this.myShops[0];
      } else {
        // 如果是首次入驻（毫无店铺），或者已经在店铺管理视图中，则直接返回系统首页
        this.$router.push('/home');
      }
    },

    /**
     * 动态加载百度地图API
     */
    loadBaiduMapScript() {
      return new Promise((resolve, reject) => {
        // 如果已经加载过,直接返回
        if (window.BMap) {
          resolve()
          return
        }

        // 创建script标签
        const script = document.createElement('script')
        script.type = 'text/javascript'
        script.src = 'https://api.map.baidu.com/api?v=3.0&ak=uvkALDXOlA5wZxZacGNK6NlwweuadRUV&callback=initBMap'
        script.onerror = reject

        // 全局回调函数
        window.initBMap = () => {
          console.log('百度地图API加载成功')
          resolve()
        }

        document.head.appendChild(script)
      })
    },

    /**
     * 初始化百度地图(在入驻表单显示时调用)
     */
    async initMap() {
      try {
        // 先加载百度地图API
        await this.loadBaiduMapScript()

        // 检查容器是否存在
        const container = document.getElementById('map-container')
        if (!container) {
          console.error('地图容器不存在')
          return
        }

        console.log('开始初始化地图...')

        // 创建地图实例,默认中心点为北京
        this.map = new BMap.Map('map-container')
        const point = new BMap.Point(116.404, 39.915)
        this.map.centerAndZoom(point, 15)
        this.map.enableScrollWheelZoom(true)

        // 添加地图点击事件
        this.map.addEventListener('click', (e) => {
          this.addMarker(e.point)
        })

        // 添加定位控件
        const geolocationControl = new BMap.GeolocationControl()
        geolocationControl.addEventListener('locationSuccess', (e) => {
          this.addMarker(e.point)
        })
        this.map.addControl(geolocationControl)

        console.log('地图初始化完成')
      } catch (error) {
        console.error('地图初始化失败:', error)
        this.$message.error('地图加载失败,请刷新页面重试')
      }
    },

    /**
     * 搜索地点并定位
     */
    searchLocation() {
      if (!this.searchKeyword.trim()) {
        this.$message.warning('请输入地点关键词')
        return
      }

      if (!this.map) {
        this.$message.error('地图未初始化')
        return
      }

      // 创建地址解析器
      const myGeo = new BMap.Geocoder()

      // 将地址解析为经纬度
      myGeo.getPoint(this.searchKeyword, (point) => {
        if (point) {
          // 定位到该点
          this.map.centerAndZoom(point, 15)
          // 添加标记
          this.addMarker(point)
          this.$message.success('定位成功')
        } else {
          this.$message.error('未找到该地点,请输入更详细的地址')
        }
      })
    },

    /**
     * 在地图上添加标记
     */
    addMarker(point) {
      // 清除旧标记
      if (this.marker) {
        this.map.removeOverlay(this.marker)
      }

      // 创建新标记
      this.marker = new BMap.Marker(point)
      this.marker.enableDragging() // 允许拖拽
      this.map.addOverlay(this.marker)

      // 保存经纬度
      this.form.longitude = point.lng
      this.form.latitude = point.lat

      // 逆地理编码(经纬度转地址)
      const geocoder = new BMap.Geocoder()
      geocoder.getLocation(point, (result) => {
        if (result) {
          this.form.address = result.address
        }
      })

      // 标记拖拽事件
      this.marker.addEventListener('dragend', (e) => {
        this.form.longitude = e.point.lng
        this.form.latitude = e.point.lat
        geocoder.getLocation(e.point, (result) => {
          if (result) {
            this.form.address = result.address
          }
        })
      })
    },

    async loadCategories() {
      const res = await this.$axios.get('/category/list')
      if (res.data.code === 200) {
        this.categories = res.data.data
      }
    },

    async loadMyShop() {
      try {
        const res = await this.$axios.get('/shop/my', {
          params: { userId: this.user.id }
        })
        if (res.data.code === 200) {
          this.myShops = res.data.data || []
          this.myShop = this.myShops.length > 0 ? this.myShops[0] : null

          if (this.myShop) {
            // 初始化时，把数据库里现有的图片解析到编辑表单中
            this.editForm.shopImages = (this.myShop.shopImages || this.myShop.shop_images) ?
                (this.myShop.shopImages || this.myShop.shop_images).split('|||') : [];

            this.loadShopData()
          }
        }
      } catch (error) {
        console.error('加载店铺失败:', error)
        this.$message.error('加载店铺列表失败')
      }
    },

    switchShop(shop) {
      this.myShop = shop
      // 左侧切换店铺时，也要同步把对应店铺的图片解析进来
      this.editForm.shopImages = (shop.shopImages || shop.shop_images) ?
          (shop.shopImages || shop.shop_images).split('|||') : [];

      this.loadShopData()
    },

    loadShopData() {
      this.loadShopNotes()
      this.loadShopReviews()
      this.loadShopDishes() // 触发加载菜品列表
    },

    // 实时探测店铺状态（前端热更新）
    async checkShopStatusHot() {
      if (!this.myShop) return true;

      // 极速拉取当前店铺的最新详情
      const res = await this.$axios.get('/shop/detail', { params: { id: this.myShop.id } });
      if (res.data.code === 200) {
        const latestStatus = res.data.data.status;

        // 如果后端状态已经是封禁(2)，但前端内存里还不是 2
        if (latestStatus === 2 && this.myShop.status !== 2) {
          // 瞬间触发热更新：强制修改内存状态！
          // 瞬间隐藏所有表单并弹出红牌拦截面板
          this.myShop.status = 2;
          this.myShop.rejectReason = res.data.data.rejectReason;
          this.$message.error('系统检测到您的店铺刚刚已被管理员封禁，操作已强制阻断！');
          return false; // 返回 false，直接掐死接下来的写入操作
        }
      }
      return true; // 状态正常，放行
    },

    async loadShopNotes() {
      const res = await this.$axios.get('/note/byShop', {
        params: { shopId: this.myShop.id }
      })
      if (res.data.code === 200) {
        this.shopNotes = res.data.data
      }
    },

    async loadShopReviews() {
      const res = await this.$axios.get('/shop/reviews/list', {
        params: { shopId: this.myShop.id }
      })
      if (res.data.code === 200) {
        this.shopReviews = res.data.data
      }
    },

    async doRegister() {
      // 校验必填项
      if (!this.form.name || !this.form.address || !this.form.phone) {
        this.$message.warning('请填写店铺名称、地址和联系电话')
        return
      }
      if (!this.form.categoryId) {
        this.$message.warning('请选择美食分类')
        return
      }
      if (!this.form.legalName) {
        this.$message.warning('请填写法人姓名')
        return
      }
      if (this.form.shopImages.length === 0) {
        this.$message.warning('请上传至少一张店铺实景照片')
        return
      }
      if (!this.form.latitude || !this.form.longitude) {
        this.$message.warning('请在地图上标记店铺位置')
        return
      }

      this.loading = true
      const res = await this.$axios.post('/shop/register', {
        ...this.form,
        userId: this.user.id,
        shopImages: this.form.shopImages.join('|||')
      })
      if (res.data.code === 200) {
        this.$message.success('申请提交成功！等待管理员审核')
        this.loadMyShop()
      }
      this.loading = false
    },

    // 触发隐藏的文件选择框点击
    triggerImageUpload() {
      this.$refs.imageUpload.click();
    },

    // 处理商家选择的多个图片文件
    handleImageUpload(event) {
      const files = event.target.files;
      if (!files || files.length === 0) return;

      // 计算还能上传几张
      const remaining = 3 - (this.editForm.shopImages ? this.editForm.shopImages.length : 0);
      if (remaining <= 0) {
        this.$message.warning('最多只能保存 3 张图片！');
        return;
      }

      // 截取安全数量的文件
      const filesToProcess = Array.from(files).slice(0, remaining);
      this.$message.info(`正在读取 ${filesToProcess.length} 张图片...`);

      // 允许多图上传，用 Promise.all 确保所有图片都读取完毕
      const readFilePromises = [];
      for (let i = 0; i < files.length; i++) {
        readFilePromises.push(new Promise((resolve) => {
          const reader = new FileReader();
          reader.onload = (e) => {
            // e.target.result 就是完整的 data:image/...) 字符串
            resolve(e.target.result);
          };
          reader.readAsDataURL(files[i]);
        }));
      }

      // 所有图片异步读取完毕后，进行去重处理并追加到表单数组
      Promise.all(readFilePromises).then((base64Arr) => {
        // 过滤掉已经存在于待上传列表中的重复图片，防止冗余数据提交
        const uniqueImages = base64Arr.filter(img => !this.editForm.shopImages.includes(img));

        // 如果比对发现重复图片，向商家弹出自动拦截提示
        if (uniqueImages.length < base64Arr.length) {
          this.$message.warning(`已自动拦截 ${base64Arr.length - uniqueImages.length} 张重复图片`);
        }

        // 将去重后的安全图片追加到现有列表中
        this.editForm.shopImages = this.editForm.shopImages.concat(uniqueImages);

        // 清空 input 的底层值，确保下次选择曾被删除的同一张图时，仍能正常触发 change 事件
        event.target.value = '';
      });
    },

    // 打开地图弹窗，并自动定位到当前店铺旧址
    openMapDialog() {
      this.showMapDialog = true;
      // 预先回显原店铺的经纬度和地址
      if (!this.editForm.address) {
        this.editForm.address = this.myShop.address;
        this.editForm.longitude = this.myShop.longitude;
        this.editForm.latitude = this.myShop.latitude;
      }
      // 等待DOM渲染后初始化弹窗里的地图
      this.$nextTick(() => {
        setTimeout(() => {
          this.initEditMap();
        }, 100);
      });
    },

    // 初始化弹窗内的编辑地图
    async initEditMap() {
      await this.loadBaiduMapScript();
      const container = document.getElementById('edit-map-container');
      if (!container) return;

      this.editMap = new BMap.Map('edit-map-container');

      // 取出旧坐标，如果没有则默认定位到北京
      let point = new BMap.Point(116.404, 39.915);
      if (this.editForm.longitude && this.editForm.latitude) {
        point = new BMap.Point(this.editForm.longitude, this.editForm.latitude);
      } else if (this.myShop.longitude && this.myShop.latitude) {
        point = new BMap.Point(this.myShop.longitude, this.myShop.latitude);
      }

      this.editMap.centerAndZoom(point, 15);
      this.editMap.enableScrollWheelZoom(true);

      // 初始化就打上一个标记
      this.addEditMarker(point);

      this.editMap.addEventListener('click', (e) => {
        this.addEditMarker(e.point);
      });
    },

    searchEditLocation() {
      if (!this.editSearchKeyword.trim()) return;
      const myGeo = new BMap.Geocoder();
      myGeo.getPoint(this.editSearchKeyword, (point) => {
        if (point) {
          this.editMap.centerAndZoom(point, 15);
          this.addEditMarker(point);
        } else {
          this.$message.error('未找到该地点');
        }
      });
    },

    addEditMarker(point) {
      if (this.editMarker) {
        this.editMap.removeOverlay(this.editMarker);
      }
      this.editMarker = new BMap.Marker(point);
      this.editMarker.enableDragging();
      this.editMap.addOverlay(this.editMarker);

      // 更新变量
      this.editForm.longitude = point.lng;
      this.editForm.latitude = point.lat;

      const geocoder = new BMap.Geocoder();
      geocoder.getLocation(point, (result) => {
        if (result) {
          this.editForm.address = result.address;
        }
      });

      this.editMarker.addEventListener('dragend', (e) => {
        this.editForm.longitude = e.point.lng;
        this.editForm.latitude = e.point.lat;
        geocoder.getLocation(e.point, (result) => {
          if (result) {
            this.editForm.address = result.address;
          }
        });
      });
    },

    confirmLocation() {
      if (!this.editForm.longitude || !this.editForm.latitude) {
        this.$message.warning('请在地图上选择一个位置');
        return;
      }
      this.showMapDialog = false;
    },

    // 真正发送更新请求给后端
    async doUpdate() {
      // 热更新拦截
      if (!(await this.checkShopStatusHot())) return;
      // 校验：不能把图片全删光
      if (!this.editForm.shopImages || this.editForm.shopImages.length === 0) {
        this.$message.warning('请至少保留一张店铺照片！');
        return;
      }

      const finalImages = this.editForm.shopImages.join('|||');

      const res = await this.$axios.post('/shop/update', {
        id: this.myShop.id,
        userId: this.user.id,
        phone: this.editForm.phone || this.myShop.phone,
        businessHours: this.editForm.businessHours || this.myShop.businessHours,
        address: this.editForm.address || this.myShop.address,
        // 把最新拿到的经纬度发给后端！
        longitude: this.editForm.longitude || this.myShop.longitude,
        latitude: this.editForm.latitude || this.myShop.latitude,
        name: this.myShop.name,
        // 【逻辑更新】：不再区分单独的新旧图片，统一提交经过动态增删管理的图片数组字符串
        shopImages: finalImages
      })
      if (res.data.code === 200) {
        this.$message.success('修改已提交，需等待管理员重新审核后生效！')
        // 修改完毕后清空表单，防止串台（注：这里保留了当前的图片数组，防止再次点开修改时图片闪烁）
        this.editForm = { phone: '', businessHours: '', address: '', latitude: null, longitude: null, shopImages: finalImages.split('|||') };
        this.loadMyShop()
      }
    },

    handleShopImages(e) {
      const files = Array.from(e.target.files)
      const remaining = 3 - this.form.shopImages.length
      files.slice(0, remaining).forEach(file => {
        const reader = new FileReader()
        reader.onload = (ev) => {
          this.form.shopImages.push(ev.target.result)
        }
        reader.readAsDataURL(file)
      })
      e.target.value = ''
    },

    startReply(note) {
      this.replyingNoteId = note.id
      this.replyText = note.reply || ''
    },

    async doReply(noteId) {
      // 热更新拦截
      if (!(await this.checkShopStatusHot())) return;
      // 校验回复内容不能为空
      if (!this.replyText.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }

      try {
        // 统一采用params传参，并严谨地带上商家身份和店铺ID校验
        const res = await this.$axios.post('/note/merchant/reply', null, {
          params: {
            noteId: noteId,
            shopId: this.myShop.id,
            userId: this.user.id,
            reply: this.replyText.trim()
          }
        })

        if (res.data.code === 200) {
          this.$message.success('回复成功')
          // 清空回复框
          this.replyingNoteId = null
          this.replyText = ''
          // 重新加载笔记列表,显示最新的回复
          this.loadShopNotes()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (error) {
        console.error('回复失败:', error)
        this.$message.error('回复失败,请稍后重试')
      }
    },

    startReplyReview(review) {
      this.replyingReviewId = review.id
      this.reviewReplyText = ''
    },

    async doReplyReview(reviewId) {
      if (!this.reviewReplyText.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      const res = await this.$axios.post('/shop/reviews/reply', null, {
        params: {
          reviewId: reviewId,
          shopId: this.myShop.id,
          userId: this.user.id,
          reply: this.reviewReplyText.trim()
        }
      })
      if (res.data.code === 200) {
        this.$message.success('回复成功')
        this.replyingReviewId = null
        this.reviewReplyText = ''
        this.loadShopReviews()
      }
    },

    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString('zh-CN')
    },

    // 请求服务端获取当前店铺的菜品数据
    async loadShopDishes() {
      if (!this.myShop) return;
      const res = await this.$axios.get('/dish/list', {
        params: { shopId: this.myShop.id }
      })
      if (res.data.code === 200) {
        this.shopDishes = res.data.data || [];
      }
    },

    // 打开菜品表单弹窗，如果传入了dish说明是编辑操作，否则为新增
    openDishDialog(dish) {
      if (dish) {
        this.dishForm = { ...dish };
      } else {
        this.dishForm = { id: null, name: '', price: '', image: '', description: '', status: 1, discountPrice: null };
      }
      this.showDishDialog = true;
    },

    // 调用后端文件上传接口，将图片存入服务器本地
    async handleDishImageUpload(event) {
      // 获取用户在文件选择框中选中的首个文件对象
      const file = event.target.files[0];
      if (!file) return;

      // 构造基于 multipart/form-data 的表单数据对象，用于承载二进制文件流
      const formData = new FormData();
      formData.append('file', file);

      try {
        // 发起异步 POST 请求，将文件流传输至后端独立的文件处理路由
        const res = await this.$axios.post('/upload', formData, {
          headers: {
            // 强制指定请求头类型，确保后端 MultipartFile 能够正确解析文件边界数据
            'Content-Type': 'multipart/form-data'
          }
        });

        // 解析统一返回体 (Result类)，根据业务状态码判定上传链路是否完整
        if (res.data.code === 200) {
          // 提取后端响应中的图片外链地址，并将其绑定至当前表单模型以供预览或提交入库
          this.dishForm.image = res.data.message;
          this.$message.success('图片上传成功');
        } else {
          // 捕获并展示后端抛出的自定义业务异常信息（如文件格式错误、大小超限等）
          this.$message.error(res.data.message || '图片上传失败');
        }
      } catch (error) {
        console.error('上传异常:', error);
        // 兜底异常处理：拦截网络中断或服务端宕机等引发的致命系统错误
        this.$message.error('服务异常，图片上传失败');
      } finally {
        // 强制重置底层 DOM 元素的 value 属性，确保用户删除图片后重新上传同一文件时仍能触发 change 事件
        event.target.value = '';
      }
    },

    // 提交菜品表单数据至服务端保存
    async saveDish() {
      //热更新拦截
      if (!(await this.checkShopStatusHot())) return;
      if (!this.dishForm.name || !this.dishForm.price) {
        this.$message.warning('菜品名称和价格不能为空');
        return;
      }
      const res = await this.$axios.post('/dish/save', {
        ...this.dishForm,
        shopId: this.myShop.id
      });
      if (res.data.code === 200) {
        this.$message.success('保存成功');
        this.showDishDialog = false;
        this.loadShopDishes();
      }
    },

    // 切换菜品的上下架状态并即时同步至数据库
    async toggleDishStatus(dish) {
      // 提交前实时探测店铺状态，若已被封禁则直接阻断操作
      if (this.checkShopStatusHot && !(await this.checkShopStatusHot())) return;

      // 状态反转：1为售卖中，0为已售罄/下架
      const newStatus = dish.status === 1 ? 0 : 1;

      // 发起状态更新请求
      const res = await this.$axios.post('/dish/save', {
        ...dish,                // 【终极修复】：通过展开语法，把菜品原有的名称、价格、图片全部带上！
        shopId: this.myShop.id, // 强绑定店铺ID
        status: newStatus       // 覆盖最新状态
      });

      // 更新成功后，同步修改前端列表里的状态
      if (res.data.code === 200) {
        dish.status = newStatus;
        this.$message.success(newStatus === 1 ? '菜品已上架' : '菜品已售罄');
      } else {
        this.$message.error(res.data.message || '操作失败');
      }
    },

    // 提交菜品主键ID至服务端执行物理删除
    async deleteDish(id) {
      // 热更新拦截
      if (!(await this.checkShopStatusHot())) return;
      if (!confirm('确定要删除这个菜品吗？')) return;
      const res = await this.$axios.delete(`/dish/delete/${id}`);
      if (res.data.code === 200) {
        this.$message.success('删除成功');
        this.loadShopDishes();
      }
    },
    /**
     * 打开菜品图片的放大预览弹窗
     * @param imageUrl 被点击图片的完整网络访问 URL
     */
    handlePreview(imageUrl) {
      // 1. 将被点击图片的 URL 赋值给预览专用的数据模型
      this.dialogImageUrl = imageUrl;
      // 2. 将弹窗的状态标识设置为 true，触发 DOM 元素的渲染和显示
      this.dialogVisible = true;
    },
  }
}
</script>

<style scoped>
.merchant-page { min-height: 100vh; background: #f5f5f5; }

/* 导航栏 */
.navbar {
  position: sticky; top: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 32px; height: 60px;
  background: white; box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}
.nav-back { color: #666; cursor: pointer; font-size: 14px; padding: 6px 12px; border-radius: 8px; }
.nav-back:hover { background: #f5f5f5; }
.nav-title { font-size: 18px; font-weight: 700; color: #333; }

/* 入驻申请页面 - 新版单栏布局 */
.register-page-new {
  min-height: calc(100vh - 60px);
  background: #f5f5f5;
  padding: 24px;
}

.register-container {
  max-width: 1000px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  overflow: hidden;
}

.register-header {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  padding: 32px 40px;
  text-align: center;
}

.register-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 700;
}

.register-header p {
  margin: 0;
  font-size: 15px;
  opacity: 0.9;
}

.register-form {
  padding: 40px;
}

/* 表单行布局(两列) */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* 地图相关样式 */
.map-description {
  font-size: 13px;
  color: #666;
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  line-height: 1.6;
  border-left: 3px solid #ff6b35;
}

.map-search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.map-search-input-large {
  flex: 1;
  border: 2px solid #eee;
  border-radius: 12px;
  padding: 14px 20px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.3s;
}

.map-search-input-large:focus {
  border-color: #ff6b35;
}

.map-search-btn-large {
  padding: 14px 32px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(255,107,53,0.3);
}

.map-search-btn-large:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255,107,53,0.4);
}

.map-container-large {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #eee;
  margin-bottom: 16px;
}

.location-display {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  background: #f8f9fa;
  padding: 16px;
  border-radius: 10px;
}

.location-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.location-label {
  font-size: 13px;
  color: #999;
  font-weight: 600;
}

.location-value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.form-actions {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.submit-btn-large {
  width: 100%;
  padding: 18px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255,107,53,0.4);
  transition: all 0.3s;
  letter-spacing: 2px;
}

.submit-btn-large:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255,107,53,0.5);
}

.submit-btn-large.loading {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 表单项 */
.form-item { margin-bottom: 20px; }
.form-item label { display: block; font-size: 14px; font-weight: 600; color: #333; margin-bottom: 8px; }
.required { color: #ff4444; }

/* 带图标的输入框 */
.input-wrap {
  display: flex;
  align-items: center;
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 0 14px;
  transition: border-color 0.3s;
}
.input-wrap:focus-within { border-color: #ff6b35; }
.input-icon { font-size: 16px; margin-right: 8px; }
.nice-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px 0;
  font-size: 14px;
  color: #333;
  background: transparent;
}
.nice-textarea {
  width: 100%;
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 12px 16px;
  font-size: 14px;
  outline: none;
  resize: none;
  height: 80px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}
.nice-textarea:focus { border-color: #ff6b35; }

.form-input {
  width: 100%; border: 2px solid #eee; border-radius: 10px;
  padding: 12px 16px; font-size: 14px; outline: none;
  box-sizing: border-box; transition: border-color 0.3s;
}
.form-input:focus { border-color: #ff6b35; }

/* 地图容器 */
.map-tip {
  font-size: 12px;
  color: #666;
  margin-bottom: 12px;
  background: #fff3ee;
  padding: 8px 12px;
  border-radius: 6px;
  border-left: 3px solid #ff6b35;
}
.map-search {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.map-search-input {
  flex: 1;
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
}
.map-search-input:focus {
  border-color: #ff6b35;
}
.map-search-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: #ff6b35;
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}
.map-search-btn:hover {
  background: #f7931e;
}
.map-container {
  width: 100%;
  height: 300px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #eee;
  margin-bottom: 8px;
}
.location-info {
  margin-top: 8px;
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 6px;
}

/* 分类标签 */
.category-select { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-tag {
  padding: 6px 16px; border-radius: 20px; background: #f5f5f5;
  color: #666; font-size: 13px; cursor: pointer; transition: all 0.2s;
}
.cat-tag:hover { background: #fff3ee; color: #ff6b35; }
.cat-tag.active { background: #ff6b35; color: white; }

/* 店铺图片上传 */
.shop-image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.shop-img-preview {
  width: 90px; height: 90px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}
.shop-img-preview img { width: 100%; height: 100%; object-fit: cover; }
.del-img {
  position: absolute;
  top: 4px; right: 4px;
  width: 20px; height: 20px;
  background: rgba(0,0,0,0.6);
  color: white; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; cursor: pointer;
}
.upload-box {
  width: 90px; height: 90px;
  border: 2px dashed #ddd;
  border-radius: 10px;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  cursor: pointer; gap: 6px;
  transition: all 0.2s;
}
.upload-box:hover { border-color: #ff6b35; }

/* 提交按钮 */
.submit-btn {
  width: 100%; padding: 16px; border: none; border-radius: 12px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white; font-size: 16px; font-weight: 700; cursor: pointer;
  box-shadow: 0 4px 16px rgba(255,107,53,0.4); transition: all 0.3s; letter-spacing: 2px;
}
.submit-btn:hover { transform: translateY(-2px); }
.submit-btn.loading { opacity: 0.7; cursor: not-allowed; }

/* 商家管理页 */
.merchant-body { max-width: 1200px; margin: 24px auto; padding: 0 24px; }
.manage-wrap { display: flex; flex-direction: column; gap: 16px; }

/* ==================== 侧边栏卡片样式 ==================== */
.sidebar-shop-list {
  width: 240px; /* 侧边栏固定宽度 */
  flex-shrink: 0;
  background: white;
  border-radius: 16px;
  padding: 20px 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  height: fit-content;
  max-height: calc(100vh - 100px); /* 防止屏幕过小时无限拉伸 */
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.sidebar-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin: 0;
}
.add-shop-btn {
  padding: 6px 12px;
  background-color: #fff3ee;
  color: #ff6b35;
  border: none;
  border-radius: 14px;
  font-size: 12px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}
.add-shop-btn:hover { background-color: #ffe4d7; }
.shop-card-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
/* 隐藏滚动条美化 */
.shop-card-list::-webkit-scrollbar { width: 4px; }
.shop-card-list::-webkit-scrollbar-thumb { background: #ddd; border-radius: 4px; }

.shop-card-item {
  padding: 14px 16px;
  background-color: #fafafa;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}
.shop-card-item:hover {
  background-color: #fef8f0;
  border-color: #ffe4d7;
  transform: translateY(-2px);
}
.shop-card-item.active {
  background-color: #fff8f5;
  border-color: #ffbfa5;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.1);
}
.card-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.shop-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #555;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 12px;
}
.shop-card-item.active .shop-name-text { color: #ff6b35; font-weight: 700; }

/* 状态小圆灯 */
.status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.status-dot.approved { background-color: #52c41a; box-shadow: 0 0 4px rgba(82, 196, 26, 0.4); } /* 绿灯：营业中 */
.status-dot.pending  { background-color: #faad14; box-shadow: 0 0 4px rgba(250, 173, 20, 0.4); } /* 黄灯：审核中 */
.status-dot.rejected { background-color: #f5222d; box-shadow: 0 0 4px rgba(245, 34, 45, 0.4); } /* 红灯：被驳回 */

/* 左右布局 */
.manage-wrap { display: flex; flex-direction: row; gap: 24px; }

/* 左侧店铺信息 */
.shop-info-card { width: 320px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; }
.shop-status-banner {
  border-radius: 12px; padding: 14px 20px;
  font-size: 14px; font-weight: 600; text-align: center;
}
.approved { background: #f6ffed; color: #52c41a; border: 1px solid #b7eb8f; }
.pending { background: #fff7e6; color: #fa8c16; border: 1px solid #ffd591; }

.shop-detail {
  background: white; border-radius: 16px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.shop-name { font-size: 20px; font-weight: 700; color: #222; margin-bottom: 12px; }
.shop-info-item { font-size: 14px; color: #666; padding: 4px 0; }

.edit-section {
  background: white; border-radius: 16px; padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.edit-section h3 { font-size: 15px; color: #333; margin-bottom: 16px; }
.save-btn {
  width: 100%; padding: 12px; border: none; border-radius: 10px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white; font-size: 14px; font-weight: 600; cursor: pointer;
}

/* 右侧面板 */
.right-panel {
  flex: 1;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

/* Tab导航 */
.tab-nav {
  display: flex;
  border-bottom: 2px solid #f5f5f5;
  padding: 0 24px;
  background: white;
}
.tab-item {
  padding: 16px 24px;
  font-size: 15px;
  font-weight: 600;
  color: #999;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
  margin-bottom: -2px;
  white-space: nowrap; /* 强制文字不换行 */
}
.tab-item:hover { color: #ff6b35; }
.tab-item.active { color: #ff6b35; border-bottom-color: #ff6b35; }

/* Tab内容区 */
.tab-content { padding: 24px; }

/* 面板通用样式 */
.panel-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.panel-header h3 { margin: 0; font-size: 18px; color: #333; }
.count { font-size: 13px; color: #aaa; }
.no-data { text-align: center; padding: 60px 0; color: #bbb; }

/* 笔记/评价列表 */
.note-list, .review-list { display: flex; flex-direction: column; gap: 20px; }
.note-item, .review-item {
  border-bottom: 1px solid #f5f5f5; padding-bottom: 20px;
}
.note-item:last-child, .review-item:last-child { border-bottom: none; }

/* 头部信息 */
.note-header, .review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.note-author, .review-author { display: flex; align-items: center; gap: 10px; }
.note-avatar, .review-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; font-size: 14px; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}
.note-name, .review-name { font-size: 14px; font-weight: 600; color: #333; }
.note-time, .review-time { font-size: 12px; color: #aaa; }
.note-score, .review-score { font-size: 14px; }

/* 内容 */
.note-title { font-size: 15px; font-weight: 600; color: #222; margin-bottom: 6px; }
.note-content, .review-content {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

/* 图片 */
.note-images { display: flex; gap: 8px; margin-bottom: 12px; }
.note-img { width: 80px; height: 60px; border-radius: 8px; object-fit: cover; }

/* 回复区域 */
.reply-section { margin-top: 10px; }
.existing-reply {
  background: #fff3ee; border-left: 3px solid #ff6b35;
  border-radius: 8px; padding: 10px 14px; font-size: 13px; color: #666;
}
.reply-label { color: #ff6b35; font-weight: 600; margin-right: 4px; }
.edit-reply { color: #aaa; font-size: 12px; margin-left: 12px; cursor: pointer; }
.edit-reply:hover { color: #ff6b35; }
.reply-input-wrap { display: flex; gap: 8px; margin-top: 8px; }
.reply-input {
  flex: 1; border: 2px solid #eee; border-radius: 20px;
  padding: 8px 16px; font-size: 13px; outline: none;
}
.reply-input:focus { border-color: #ff6b35; }
.reply-btn {
  background: #ff6b35; color: white; border: none;
  border-radius: 20px; padding: 8px 16px; cursor: pointer; font-size: 13px;
}
.reply-cancel {
  background: #f5f5f5; color: #666; border: none;
  border-radius: 20px; padding: 8px 16px; cursor: pointer; font-size: 13px;
}
.start-reply-btn {
  background: none; border: 1px solid #eee; border-radius: 16px;
  padding: 6px 14px; font-size: 12px; color: #aaa;
  cursor: pointer; margin-top: 8px; transition: all 0.2s;
}
.start-reply-btn:hover { border-color: #ff6b35; color: #ff6b35; }
/* 修改地址栏的新布局 */
.edit-address-wrap {
  display: flex;
  gap: 8px;
}
.edit-address-wrap input {
  background: #f8f9fa;
  cursor: not-allowed;
}
.map-btn {
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  background: #fff3ee;
  color: #ff6b35;
  font-weight: bold;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.map-btn:hover {
  background: #ffe4d7;
}

/* 地图弹窗样式 */
.map-dialog-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.5); backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.map-dialog {
  background: white; width: 800px; max-width: 90vw;
  border-radius: 16px; overflow: hidden;
  box-shadow: 0 12px 40px rgba(0,0,0,0.15);
  animation: dialogPop 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
@keyframes dialogPop {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
.dialog-header {
  padding: 16px 24px; border-bottom: 1px solid #eee;
  display: flex; justify-content: space-between; align-items: center;
}
.dialog-header h3 { margin: 0; font-size: 18px; color: #333; }
.close-btn { font-size: 20px; color: #999; cursor: pointer; }
.close-btn:hover { color: #ff4444; }
.dialog-body { padding: 24px; }
.dialog-footer {
  padding: 16px 24px; border-top: 1px solid #eee; background: #fafafa;
  display: flex; justify-content: flex-end; gap: 12px;
}
.cancel-btn {
  padding: 10px 24px; border: 1px solid #ddd; border-radius: 8px;
  background: white; color: #666; cursor: pointer; font-weight: 600;
}
.confirm-btn {
  padding: 10px 24px; border: none; border-radius: 8px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white; cursor: pointer; font-weight: 600;
}
/* 图片上传相关样式 */
.shop-image-upload-item {
  margin-top: 20px;
}
.upload-btn-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}
.upload-trigger-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 10px;
  background: #fdf6ec; /* 选用柔和的橙色系，区别于保存按钮 */
  color: #e6a23c;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}
.upload-trigger-btn:hover {
  background: #fcefd6;
}
.upload-trigger-btn i {
  margin-right: 6px;
}
.upload-tip {
  font-size: 13px;
  color: #999;
}
.upload-tip b {
  color: #ff6b35;
}
.hidden-file-input {
  display: none !important; /* 强制隐藏原本的文本输入框 */
}
.rejected { background: #fff1f0; color: #f5222d; border: 1px solid #ffa39e; }
/* 封禁状态专属样式：采用更严肃的黑红配色提醒商家 */
.banned { background: #2b2b2b; color: #ff4d4f; border: 1px solid #434343; }
/* 菜品管理网格布局 */
.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.dish-card {
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  transition: transform 0.2s;
}
.dish-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.dish-img-wrap {
  position: relative;
  width: 100%;
  height: 140px;
  background: #f8f9fa;
}
.dish-img { width: 100%; height: 100%; object-fit: cover; }
.dish-no-img {
  width: 100%; height: 100%;
  display: flex; align-items: center; justify-content: center;
  color: #bbb; font-size: 13px;
}
.dish-status-tag {
  position: absolute; top: 8px; right: 8px;
  padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; color: white;
}
.on-sale { background: rgba(82, 196, 26, 0.85); }
.off-sale { background: rgba(0, 0, 0, 0.5); }
.dish-info { padding: 12px; }
.dish-name { font-size: 15px; font-weight: bold; color: #333; margin-bottom: 6px; }
.dish-price { font-size: 16px; font-weight: 800; color: #ff6b35; margin-bottom: 6px; }
.dish-desc { font-size: 12px; color: #888; height: 34px; overflow: hidden; margin-bottom: 12px; }
.dish-actions { display: flex; gap: 8px; border-top: 1px solid #f5f5f5; padding-top: 12px; }
.dish-btn {
  flex: 1; border: none; border-radius: 6px; padding: 6px 0;
  font-size: 12px; cursor: pointer; transition: background 0.2s;
}
.dish-btn.edit { background: #fdf6ec; color: #e6a23c; }
.dish-btn.edit:hover { background: #fcefd6; }
.dish-btn.toggle { background: #f0f9eb; color: #67c23a; }
.dish-btn.toggle:hover { background: #e1f3d8; }
.dish-btn.delete { background: #fef0f0; color: #f56c6c; }
.dish-btn.delete:hover { background: #fde2e2; }
/* 菜品图片可点击时的鼠标悬停样式 */
.clickable-img {
  cursor: pointer; /* 强制指定光标类型为点击手型 */
}
.dish-price-wrap { display: flex; align-items: baseline; gap: 8px; margin-bottom: 6px; }
.dish-discount { font-size: 16px; font-weight: 900; color: #e53935; }
.old-price { text-decoration: line-through; color: #999; font-size: 12px; font-weight: normal; }
</style>
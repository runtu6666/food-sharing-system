<template>
  <!-- 整个首页容器 -->
  <div class="home-page">

    <!-- ===== 顶部导航栏 ===== -->
    <div class="navbar">
      <!-- 左侧Logo -->
      <div class="nav-logo">🍜 觅食</div>

      <!-- 中间搜索框 -->
      <div class="nav-search-container">

        <div class="search-type-pill-container">
          <div class="pill-track">
            <div
                class="pill-tab"
                :class="{ active: searchType === 'note' }"
                @click="handleTypeChange('note')"
            >
              📝 搜笔记
            </div>
            <div
                class="pill-tab"
                :class="{ active: searchType === 'shop' }"
                @click="handleTypeChange('shop')"
            >
              📍 附近店铺
            </div>
            <div class="pill-slider" :class="{ right: searchType === 'shop' }"></div>
          </div>
        </div>

        <div class="search-bar-inner">
          <span class="search-icon">🔍</span>
          <input
              v-model="searchKey"
              :placeholder="searchType === 'note' ? '搜索您感兴趣的美食笔记...' : '输入店名，查找周边美食店铺...'"
              @keyup.enter="doSearch"
          />
          <button class="search-btn" @click="doSearch">搜索</button>
        </div>
      </div>

      <!-- 右侧用户信息 -->
      <div class="nav-user">
        <!-- 发布笔记按钮 -->
        <button class="publish-btn" @click="$router.push('/publish')">
          ✏️ 发布笔记
        </button>
        <!-- 管理员显示进入后台按钮，普通用户显示个人中心 -->
        <div class="user-info" @click="user.role === 'admin' ? $router.push('/admin') : $router.push('/profile')">
          <div class="avatar">{{ user.nickname ? user.nickname[0] : 'U' }}</div>
          <span class="nickname">{{ user.nickname }}</span>
        </div>
      </div>
    </div>

    <!-- ===== 内容区域 ===== -->
    <div class="content-area">

      <!-- 左侧分类侧边栏 -->
      <div class="sidebar">
        <div class="sidebar-title">美食分类</div>
        <!-- 全部按钮 -->
        <div
            :class="['category-item', selectedCategory === 0 ? 'active' : '']"
            @click="selectCategory(0)"
        >
          🍽️ 全部
        </div>
        <!-- 动态渲染分类列表 -->
        <div
            v-for="cat in categories"
            :key="cat.id"
            :class="['category-item', selectedCategory === cat.id ? 'active' : '']"
            @click="selectCategory(cat.id)"
        >
          {{ getCategoryIcon(cat.name) }} {{ cat.name }}
        </div>

        <!-- 分隔线 -->
        <div class="sidebar-divider"></div>
        <div class="sidebar-title">我的</div>
        <!-- 个人中心导航 -->
        <div class="category-item" @click="$router.push('/profile')">👤 个人中心</div>
        <div class="category-item" @click="goToCollect">⭐ 我的收藏</div>
        <!--
          如果当前登录账号是“商家角色”，在首页侧边栏提供一个返回商家中心的入口。
          这样从商家端返回首页后，仍然可以很直观地回到商家工作台。
        -->
        <div
            v-if="user.role === 'shop'"
            class="category-item"
            @click="$router.push('/merchant')"
        >
          🏪 商家中心
        </div>
        <!-- 退出登录 -->
        <div class="category-item logout" @click="logout">🚪 退出登录</div>
        <div class="admin-contact-box">
          <div class="admin-contact-title">📧 账号问题联系管理员</div>
          <div class="admin-contact-row">
            <a class="admin-contact-link" href="mailto:15154797270@163.com">15154797270@163.com</a>
            <button class="copy-email-btn" @click="copyAdminEmail">复制</button>
          </div>
        </div>
      </div>

      <!-- 右侧主内容区 -->
      <div class="main-content">

        <!-- 顶部筛选栏 -->
        <div class="filter-bar" v-show="searchType === 'note'">
          <!-- 排序方式切换 -->
          <div class="filter-tabs">
            <span
                :class="['filter-tab', sortType === 'latest' ? 'active' : '']"
                @click="changeSort('latest')"
            >🕐 最新发布</span>
            <span
                :class="['filter-tab', sortType === 'hot' ? 'active' : '']"
                @click="changeSort('hot')"
            >🔥 最热笔记</span>
          </div>
          <!-- 当前筛选状态提示 -->
          <div class="filter-info" v-if="selectedCategory !== 0">
            当前分类：{{ currentCategoryName }}
            <span class="clear-filter" @click="selectCategory(0)">✕ 清除</span>
          </div>
        </div>

        <!-- 加载状态提示 -->
        <div v-if="loading" class="loading-tip">
          <span>{{ searchType === 'note' ? '🍜 正在加载美食笔记...' : '📍 正在查找附近店铺...' }}</span>
        </div>

        <!-- 空状态提示 -->
        <div v-else-if="searchType === 'note' && notes.length === 0" class="empty-tip">
          <div class="empty-icon">🍽️</div>
          <p>暂时没有笔记，快去发布第一篇吧！</p>
          <button class="publish-btn-big" @click="$router.push('/publish')">发布探店笔记</button>
        </div>

        <!-- 瀑布流笔记卡片列表 -->
        <div v-else-if="searchType === 'note' && notes.length > 0" class="waterfall">
          <!-- 每一张笔记卡片 -->
          <div
              v-for="note in notes"
              :key="note.id"
              class="note-card"
              @click="goDetail(note.id)"
          >
            <!-- 封面图片区域 -->
            <div class="card-cover">
              <!-- 有图片时显示图片，去掉alt防止文字重复 -->
              <img
                  v-if="note.images"
                  :src="note.images.split('|||')[0]"
                  @error="onImgError"
              />
              <!-- 没有图片时显示默认装饰封面 -->
              <div v-else class="default-cover">
                <span>{{ getDefaultEmoji(note.title) }}</span>
              </div>
              <!-- 左上角分类标签 -->
              <div class="card-category-tag" v-if="note.categoryName">
                {{ note.categoryName }}
              </div>
            </div>

            <!-- 卡片底部信息区域 -->
            <div class="card-body">
              <!-- 笔记标题 -->
              <div class="card-title">{{ note.title || '探店日记' }}</div>
              <!-- 笔记内容预览 -->
              <div class="card-content">{{ note.content }}</div>

              <!-- 底部：作者信息 + 互动数据 -->
              <div class="card-footer">
                <!-- 作者头像和昵称 -->
                <div class="card-author">
                  <div class="author-avatar">
                    {{ note.nickname ? note.nickname[0] : 'U' }}
                  </div>
                  <span class="author-name">{{ note.nickname }}</span>
                </div>
                <!-- 点赞数 -->
                <div class="card-likes" @click.stop="toggleLike(note)">
                  <span :class="note.liked ? 'liked' : ''">
                    {{ note.liked ? '❤️' : '🤍' }}
                  </span>
                  <span class="like-count">{{ note.likeCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="searchType === 'shop' && shops.length === 0" class="empty-tip">
          <div class="empty-icon">📍</div>
          <p>附近没有找到已入驻的店铺哦</p>
        </div>

        <div v-else-if="searchType === 'shop' && shops.length > 0">

          <div class="shop-view-toggle">
            <button :class="{ active: shopViewMode === 'list' }" @click="toggleShopViewMode('list')">📄 列表模式</button>
            <button :class="{ active: shopViewMode === 'map' }" @click="toggleShopViewMode('map')">🗺️ 地图模式</button>
          </div>

          <div v-show="shopViewMode === 'list'" class="shop-list">
            <div v-for="shop in shops" :key="shop.id" class="shop-card" @click="$router.push('/shop/' + shop.id)">
              <div class="shop-cover-wrapper">
                <img
                    v-if="shop.cover || shop.shopImages"
                    :src="getShopCover(shop)"
                    class="shop-cover"
                    style="cursor: zoom-in;"
                    @click.stop="openPreview(shop.shopImages || shop.cover)"
                    @error="onShopImgError"
                />
                <div v-else class="shop-no-cover">
                  <span style="font-size: 24px;">🏪</span>
                  <span>暂无封面</span>
                </div>
              </div>
              <div class="shop-info">
                <div class="shop-name-row">
                  <span class="shop-name">{{ shop.name }}</span>
                  <span class="shop-distance" v-if="shop.distance != null">
                    📍 距你 {{ (shop.distance / 1000).toFixed(2) }} km
                  </span>
                </div>
                <div class="shop-address">地址：{{ shop.address }}</div>
                <div class="shop-hours">营业时间：{{ shop.businessHours }}</div>
              </div>
            </div>
          </div>

          <div v-show="shopViewMode === 'map'" class="shop-map-container">
            <div id="home-map-container" style="width: 100%; height: 100%;"></div>
          </div>

        </div>

        <!-- 加载更多按钮 -->
        <div class="load-more" v-if="notes.length > 0 && !loading">
          <button @click="loadMore">加载更多</button>
        </div>

      </div>
    </div>

    <div v-if="showPreview" class="image-preview-overlay" @click.self="closePreview">
      <span class="preview-close" @click="closePreview">✕</span>
      <img :src="previewList[previewIndex]" class="preview-img" alt="全屏大图" />
      <div class="preview-arrow left" v-if="previewList.length > 1" @click.stop="prevPreview">❮</div>
      <div class="preview-arrow right" v-if="previewList.length > 1" @click.stop="nextPreview">❯</div>
      <div class="preview-count" v-if="previewList.length > 1">
        {{ previewIndex + 1 }} / {{ previewList.length }}
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: 'HomeView',
  data() {
    return {
      // 从本地存储获取当前登录用户信息
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      searchKey: '',          // 搜索关键词
      selectedCategory: 0,   // 当前选中的分类ID，0表示全部
      sortType: 'latest',     // 排序方式：latest=最新，hot=最热
      categories: [],         // 分类列表
      notes: [],              // 笔记列表
      loading: false,         // 是否正在加载
      page: 1,                // 当前页码，用于分页加载
      // 优先从会话缓存中读取上一次离开首页时的页面模式，若无则降级默认展示笔记
      searchType: sessionStorage.getItem('homeSearchType') || 'note',
      shops: [],              // 存放查出来的店铺列表
      userLng: null,          // 当前用户的经度
      userLat: null,          // 当前用户的纬度
      shopViewMode: 'list',
      map: null,           // 存放原生百度地图实例
      mapMarkers: [],      // 存放所有的地图坐标点，方便清理
      showPreview: false,     // 是否展示全屏图片预览
      previewList: [],        // 全屏预览的图片数组
      previewIndex: 0         // 当前预览的图片索引
    }
  },
  computed: {
    // 计算当前选中分类的名称，用于显示筛选提示
    currentCategoryName() {
      const cat = this.categories.find(c => c.id === this.selectedCategory)
      return cat ? cat.name : ''
    }
  },
  // 监听路由变化：每次切换回首页时重新加载笔记列表
  // 这样从详情页返回后，点赞状态和数量都是最新的
  watch: {
    // 监听搜索词变化：清空输入框后，自动恢复显示全部无过滤条件的数据
    searchKey(newVal) {
      if (newVal === '') {
        if (this.searchType === 'note') {
          this.page = 1;
          this.loadNotes();
        } else {
          this.getUserLocationAndFetchShops();
        }
      }
    }
  },
  mounted() {
    if (!this.user || !this.user.id) {
      this.$router.push('/login');
      return;
    }
    this.loadCategories();
    // 根据初始化的页面模式，动态分发底层网络请求
    if (this.searchType === 'note') {
      this.loadNotes();
    } else {
      this.getUserLocationAndFetchShops();
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

    // 加载笔记列表（支持分类筛选和排序）
    async loadNotes() {
      this.loading = true
      try {
        const res = await this.$axios.get('/note/list', {
          params: {
            categoryId: this.selectedCategory || '',  // 分类ID
            sort: this.sortType,                      // 排序方式
            keyword: this.searchKey,                  // 搜索关键词
            page: this.page,                          // 页码
            userId: this.user.id                      // 当前用户ID，用于判断是否已点赞
          }
        })
        if (res.data.code === 200) {
          // page=1时替换列表，page>1时追加（加载更多）
          if (this.page === 1) {
            this.notes = res.data.data
          } else {
            this.notes = [...this.notes, ...res.data.data]
          }
        }
      } catch (e) {
        this.$message.error('加载失败，请重试')
      }
      this.loading = false
    },

    // 切换分类筛选，支持笔记和附近店铺双模式联动
    selectCategory(id) {
      this.selectedCategory = id;
      if (this.searchType === 'note') {
        // 笔记模式：重置到第一页并重新拉取笔记
        this.page = 1;
        this.loadNotes();
      } else {
        // 店铺模式：点击分类直接按新分类ID重新拉取周边店铺
        this.getUserLocationAndFetchShops();
      }
    },

    // 切换排序方式
    changeSort(type) {
      this.sortType = type
      this.page = 1
      this.loadNotes()
    },

    // 点击搜索分类选项卡，触发视图切换并持久化当前状态至浏览器内存
    handleTypeChange(type) {
      if (this.searchType === type) return;
      this.searchType = type;
      sessionStorage.setItem('homeSearchType', type);
      this.onSearchTypeChange();
    },

    // 根据选择的搜索模式，初始化相关数据并拉取对应列表
    onSearchTypeChange() {
      if (this.searchType === 'shop') {
        // 切换到找店模式：清空搜索框和之前的店铺数据，并开始获取当前地理位置
        this.notes = [];
        this.shops = [];
        this.getUserLocationAndFetchShops();
      } else {
        // 切换到笔记模式：清空搜索框，重置页码为第一页，重新加载笔记流
        this.shops = [];
        this.page = 1;
        this.loadNotes();
      }
    },

    // 触发搜索操作
    doSearch() {
      if (this.searchType === 'note') {
        // 笔记模式下：按关键词重新从第一页拉取数据
        this.page = 1
        this.loadNotes()
      } else {
        // 店铺模式下：重新获取位置并计算附近店铺
        this.getUserLocationAndFetchShops();
      }
    },

    // 核心：获取用户地理定位，并调用 LBS 接口请求周边店铺
    getUserLocationAndFetchShops() {
      this.loading = true;
      // 检查浏览器是否支持定位
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            // 成功获取定位
            (position) => {
              this.userLng = position.coords.longitude;
              this.userLat = position.coords.latitude;
              this.loadNearbyShops();
            },
            // 获取失败 (用户拒绝授权或无GPS设备)
            (error) => {
              console.warn("定位失败，使用默认北京天安门坐标进行测试", error);
              // 如果在电脑上测试报错，自动降级使用测试坐标（北京天安门）
              this.userLng = 116.3974;
              this.userLat = 39.9092;
              this.$message.warning("获取位置失败，已使用测试坐标");
              this.loadNearbyShops();
            },
            { timeout: 5000 } // 设置超时时间5秒，防止长时间请求导致页面白屏
        );
      } else {
        this.$message.error('您的浏览器不支持地理位置服务');
        this.loading = false;
      }
    },

    // 携带坐标、搜索词、分类ID请求后端 LBS 接口，获取按距离排序的店铺列表
    async loadNearbyShops() {
      try {
        const res = await this.$axios.get('/shop/nearby', {
          params: {
            lng: this.userLng,
            lat: this.userLat,
            radius: 100000, // 设定搜索半径，方便在测试数据较少时能查出结果
            keyword: this.searchKey, // 传入顶部搜索框的店名关键词进行模糊匹配
            categoryId: this.selectedCategory || '' // 传入左侧选中的分类ID（0或空代表全部分类）
          }
        });
        if (res.data.code === 200) {
          this.shops = res.data.data;
        }
      } catch (e) {
        this.$message.error('获取附近店铺失败');
      }
      this.loading = false;
    },

    // 加载更多（下一页）
    loadMore() {
      this.page++
      this.loadNotes()
    },

    // 跳转到笔记详情页
    goDetail(id) {
      this.$router.push('/note/' + id)
    },

    // 点赞/取消点赞
    async toggleLike(note) {
      try {
        const res = await this.$axios.post('/note/like', {
          noteId: note.id,
          userId: this.user.id
        })
        if (res.data.code === 200) {
          // 找到notes数组中对应的笔记，整体替换触发响应式更新
          const index = this.notes.findIndex(n => n.id === note.id)
          if (index !== -1) {
            const newLiked = !this.notes[index].liked
            const current = this.notes[index].likeCount || 0
            const newCount = newLiked ? current + 1 : current - 1
            // 用splice替换整个对象，强制Vue检测到变化
            this.notes.splice(index, 1, {
              ...this.notes[index],
              liked: newLiked,
              likeCount: newCount
            })
          }
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },

    // 跳转到个人中心并切换到收藏标签
    goToCollect() {
      this.$router.push('/profile?tab=collect')
    },

    // 退出登录：清除本地存储，跳转登录页
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    },

    async copyAdminEmail() {
      const email = '15154797270@163.com'
      try {
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(email)
        } else {
          const input = document.createElement('textarea')
          input.value = email
          document.body.appendChild(input)
          input.select()
          document.execCommand('copy')
          document.body.removeChild(input)
        }
        this.$message.success('邮箱已复制')
      } catch (e) {
        this.$message.error('复制失败，请手动复制')
      }
    },

    // 开启全屏大图预览（自动解析单图或多图数据）
    openPreview(imagesStr) {
      if (!imagesStr) return;
      if (imagesStr.includes('|||')) {
        this.previewList = imagesStr.split('|||').filter(Boolean);
      } else {
        this.previewList = [imagesStr];
      }
      this.previewIndex = 0;
      this.showPreview = true;
    },

    // 关闭预览
    closePreview() {
      this.showPreview = false;
      this.previewList = [];
    },

    // 预览上一张
    prevPreview() {
      this.previewIndex = this.previewIndex > 0 ? this.previewIndex - 1 : this.previewList.length - 1;
    },

    // 预览下一张
    nextPreview() {
      this.previewIndex = this.previewIndex < this.previewList.length - 1 ? this.previewIndex + 1 : 0;
    },

    // 根据图片加载失败时的处理：隐藏破损图片
    onImgError(e) {
      e.target.style.display = 'none'
    },

    // 店铺封面加载异常时的 DOM 替换处理，防止出现破损图标
    onShopImgError(e) {
      e.target.style.display = 'none'
      e.target.insertAdjacentHTML('afterend', '<div class="shop-no-cover"><span style="font-size: 24px;">🏪</span><span>暂无封面</span></div>');
    },

    // 解析店铺首图：安全处理单图、多图、普通URL以及Base64编码格式
    getShopCover(shop) {
      if (shop.cover) return shop.cover;
      if (!shop.shopImages) return '';

      const str = shop.shopImages;

      // 处理以 '|||' 作为多图分隔符的情况
      if (str.includes('|||')) {
        return str.split('|||')[0];
      }

      // 处理以逗号作为多图分隔符的情况
      if (str.includes(',')) {
        const arr = str.split(',');
        // Base64格式本身的头部包含一个逗号，因此需要取前两段进行拼接才是一张完整的图片
        if (arr[0].startsWith('data:image')) {
          return arr[0] + ',' + arr[1];
        }
        // 普通的URL图片链接，直接取第一段
        return arr[0];
      }

      // 既没有'|||'也没有逗号，说明只有单张图片
      return str;
    },

    // 根据分类名称返回对应emoji图标
    getCategoryIcon(name) {
      const icons = {
        '火锅': '🍲', '烧烤': '🔥', '甜点': '🍰',
        '粤菜': '🥘', '日料': '🍱', '快餐': '🍔',
        '鲁菜': '🥩', '其他': '🍽️'
      }
      return icons[name] || '🍴'
    },

    // 没有图片时，根据标题返回一个装饰emoji
    getDefaultEmoji(title) {
      const emojis = ['🍜', '🍲', '🥘', '🍱', '🍛', '🍝', '🥗', '🍣']
      if (!title) return '🍜'
      // 根据标题第一个字的编码决定显示哪个emoji（伪随机，同一标题永远相同）
      return emojis[title.charCodeAt(0) % emojis.length]
    },

    // 加载百度地图全局脚本
    loadBaiduMapScript() {
      return new Promise((resolve, reject) => {
        if (window.BMap) {
          resolve()
          return
        }
        const script = document.createElement('script')
        script.type = 'text/javascript'
        // 使用与商家页面完全相同的 AK
        script.src = 'https://api.map.baidu.com/api?v=3.0&ak=uvkALDXOlA5wZxZacGNK6NlwweuadRUV&callback=initHomeBMap'
        script.onerror = reject
        window.initHomeBMap = () => resolve()
        document.head.appendChild(script)
      })
    },

    // 监听模式切换，初始化并渲染地图
    async toggleShopViewMode(mode) {
      this.shopViewMode = mode;
      if (mode === 'map') {
        // 延迟等待 DOM 中的 #home-map-container 渲染完毕
        this.$nextTick(async () => {
          if (!this.map) {
            await this.loadBaiduMapScript();
            this.map = new window.BMap.Map('home-map-container');
            this.map.enableScrollWheelZoom(true);
          }
          this.renderMapMarkers();
        });
      }
    },

    // 在地图上绘制用户定位和所有周边店铺的坐标
    renderMapMarkers() {
      if (!this.map) return;
      this.map.clearOverlays(); // 清空旧标记

      // 设置中心点为用户坐标，如果没有则默认北京
      const centerPoint = new window.BMap.Point(this.userLng || 116.404, this.userLat || 39.915);
      this.map.centerAndZoom(centerPoint, 13);

      // 绘制用户的蓝色定位点
      if (this.userLng && this.userLat) {
        const userMarker = new window.BMap.Marker(centerPoint);
        this.map.addOverlay(userMarker);
        const userLabel = new window.BMap.Label("我的位置", { offset: new window.BMap.Size(-20, -25) });
        userLabel.setStyle({ color: '#ff6b35', border: 'none', background: 'white', padding: '2px 6px', borderRadius: '4px', boxShadow: '0 2px 4px rgba(0,0,0,0.2)'});
        userMarker.setLabel(userLabel);
      }

      // 遍历店铺列表，绘制红色坐标点
      this.shops.forEach(shop => {
        const shopLng = shop.longitude || shop.lng;
        const shopLat = shop.latitude || shop.lat;
        if (shopLng && shopLat) {
          const pt = new window.BMap.Point(shopLng, shopLat);
          const shopMarker = new window.BMap.Marker(pt);
          this.map.addOverlay(shopMarker);

          // 为每个店铺绑定点击事件，弹出信息浮窗
          shopMarker.addEventListener('click', () => {
            const distanceText = shop.distance ? `距你 ${(shop.distance / 1000).toFixed(2)} km` : '';
            const infoWindow = new window.BMap.InfoWindow(`
              <div style="padding: 5px 0;">
                <h4 style="margin: 0 0 8px; font-size: 15px; color: #333;">${shop.name}</h4>
                <p style="margin: 0 0 5px; font-size: 13px; color: #ff6b35; font-weight: bold;">${distanceText}</p>
                <p style="margin: 0; font-size: 12px; color: #666;">地址: ${shop.address}</p>
              </div>
            `);
            this.map.openInfoWindow(infoWindow, pt);
          });
        }
      });
    }
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.home-page {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* ===== 顶部导航栏 ===== */
.navbar {
  position: sticky;         /* 滚动时固定在顶部 */
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 32px;
  height: 64px;
  background: white;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

/* Logo */
.nav-logo {
  font-size: 22px;
  font-weight: 900;
  color: #ff6b35;
  letter-spacing: 2px;
  white-space: nowrap;
}

/* ===== 导航栏搜索区域 ===== */
.nav-search-container {
  flex: 1;
  max-width: 650px; /* 拓宽容器宽度 */
  display: flex;
  flex-direction: row; /* 【重点】改为左右横向排列 */
  align-items: center;
  gap: 12px; /* 药丸按钮和搜索框之间的间距 */
}

/* 药丸型切换器容器 */
.search-type-pill-container {
  background-color: #f5f6f8;
  border-radius: 100px;
  padding: 4px;
  margin-bottom: 0; /* 移除以前的底部间距 */
  flex-shrink: 0; /* 防止药丸按钮被搜索框挤压变形 */
}

/* 药丸型切换器轨道 */
.pill-track {
  display: flex;
  position: relative;
  width: 220px;
}

/* 选项卡基础样式 */
.pill-tab {
  flex: 1;
  text-align: center;
  padding: 6px 0;
  font-size: 14px;
  color: #666;
  font-weight: 600;
  cursor: pointer;
  z-index: 2;
  transition: color 0.3s;
}

/* 选中状态字体颜色变白 */
.pill-tab.active {
  color: white;
}

/* 背景滑块动画层 */
.pill-slider {
  position: absolute;
  left: 0;
  top: 0;
  width: 50%;
  height: 100%;
  background: linear-gradient(135deg, #ff6b35, #ff8c42);
  border-radius: 100px;
  z-index: 1;
  transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(255,107,53,0.3);
}

/* 滑块划到右边 */
.pill-slider.right {
  transform: translateX(100%);
}

/* 输入框内部布局 */
.search-bar-inner {
  width: 100%;
  position: relative;
  display: flex;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border-radius: 100px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  z-index: 2;
}

.search-bar-inner input {
  flex: 1;
  border: 1px solid #ffd1c0;
  border-right: none;
  background-color: white;
  border-radius: 100px 0 0 100px;
  padding: 10px 10px 10px 40px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.search-bar-inner input:focus {
  border-color: #ff6b35;
}

.search-bar-inner input:disabled {
  background-color: #fafafa;
  color: #999;
}

.search-bar-inner .search-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 0 100px 100px 0;
  padding: 0 24px;
  cursor: pointer;
  font-weight: bold;
  font-size: 14px;
}

/* 右侧用户区 */
.nav-user {
  display: flex;
  align-items: center;
  gap: 16px;
}
.publish-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(255,107,53,0.3);
  transition: transform 0.2s;
}
.publish-btn:hover { transform: translateY(-1px); }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #ffd93d);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 15px;
}
.nickname { font-size: 14px; color: #333; }

/* ===== 内容区域：左右布局 ===== */
.content-area {
  display: flex;
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 24px 16px;
  gap: 24px;
}

/* ===== 左侧分类侧边栏 ===== */
.sidebar {
  width: 180px;
  flex-shrink: 0;
}
.sidebar-title {
  font-size: 12px;
  color: #aaa;
  font-weight: 600;
  letter-spacing: 1px;
  padding: 8px 12px;
  margin-top: 8px;
}
.category-item {
  padding: 10px 16px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  color: #555;
  transition: all 0.2s;
  margin-bottom: 4px;
}
.category-item:hover { background: #fff3ee; color: #ff6b35; }
.category-item.active {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255,107,53,0.3);
}
.sidebar-divider { height: 1px; background: #eee; margin: 16px 12px; }
.logout { color: #ff4444 !important; }
.logout:hover { background: #fff0f0 !important; }
.admin-contact-box {
  margin-top: 10px;
  padding: 10px 12px;
  border: 1px solid #ffe4d7;
  border-radius: 10px;
  background: #fffaf7;
}
.admin-contact-title {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}
.admin-contact-link {
  font-size: 12px;
  color: #ff6b35;
  word-break: break-all;
  text-decoration: none;
}
.admin-contact-link:hover {
  text-decoration: underline;
}
.admin-contact-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.copy-email-btn {
  border: none;
  border-radius: 12px;
  background: #ffe9de;
  color: #ff6b35;
  font-size: 12px;
  padding: 4px 8px;
  cursor: pointer;
  flex-shrink: 0;
}
.copy-email-btn:hover {
  background: #ffd9c6;
}

/* ===== 右侧主内容区 ===== */
.main-content { flex: 1; min-width: 0; }

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.filter-tabs { display: flex; gap: 8px; }
.filter-tab {
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  background: white;
  border: 1px solid #eee;
  transition: all 0.2s;
}
.filter-tab.active {
  background: #ff6b35;
  color: white;
  border-color: #ff6b35;
}
.filter-info {
  font-size: 13px;
  color: #ff6b35;
  background: #fff3ee;
  padding: 4px 12px;
  border-radius: 12px;
}
.clear-filter { cursor: pointer; margin-left: 8px; }

/* 加载和空状态 */
.loading-tip, .empty-tip {
  text-align: center;
  padding: 80px 0;
  color: #999;
}
.empty-icon { font-size: 60px; margin-bottom: 16px; }
.empty-tip p { margin-bottom: 24px; font-size: 15px; }
.publish-btn-big {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border: none;
  border-radius: 24px;
  padding: 12px 32px;
  font-size: 15px;
  cursor: pointer;
}

/* ===== 瀑布流卡片布局 ===== */
.waterfall {
  columns: 4;
  column-gap: 16px;
}

/* 单张笔记卡片 */
.note-card {
  break-inside: avoid;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.12);
}

/* 卡片封面：固定16:10比例，图片自动裁剪填充 */
.card-cover {
  position: relative;
  overflow: hidden;
  width: 100%;
  padding-top: 75%;   /* 控制封面高度比例 */
}
.card-cover img {
  position: absolute;
  top: 0; left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;  /* 图片裁剪填充，不变形 */
  display: block;
}
.default-cover {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: linear-gradient(135deg, #fff3ee, #ffe0cc);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 50px;
}

/* 分类角标 */
.card-category-tag {
  position: absolute;
  top: 10px; left: 10px;
  background: rgba(0,0,0,0.5);
  color: white;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 8px;
  backdrop-filter: blur(4px);
}

/* 卡片内容区 */
.card-body { padding: 12px; }
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
  /* 最多显示2行，超出用...省略 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-content {
  font-size: 12px;
  color: #888;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 卡片底部：作者+点赞 */
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-author { display: flex; align-items: center; gap: 6px; }
.author-avatar {
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
.author-name { font-size: 12px; color: #999; }
.card-likes {
  display: flex;
  align-items: center;
  gap: 3px;
  cursor: pointer;
}
.liked { animation: heartbeat 0.3s ease; }
.like-count { font-size: 12px; color: #999; }

/* 加载更多按钮 */
.load-more {
  text-align: center;
  padding: 24px 0;
}
.load-more button {
  background: white;
  border: 1px solid #eee;
  border-radius: 20px;
  padding: 10px 32px;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.load-more button:hover { border-color: #ff6b35; color: #ff6b35; }

/* 点赞动画 */
@keyframes heartbeat {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* 响应式：屏幕变窄时减少列数 */
@media (max-width: 1200px) { .waterfall { columns: 3; } }
@media (max-width: 900px) { .waterfall { columns: 2; } }

/* 搜索类型下拉框 */
.search-type-select {
  border: none;
  background: transparent;
  font-size: 14px;
  color: #ff6b35;
  font-weight: bold;
  outline: none;
  margin-right: 8px;
  cursor: pointer;
}

/* 店铺列表布局 */
.shop-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
/* 单个店铺卡片：采用弹性布局，带有悬浮阴影增强点击暗示 */
.shop-card {
  display: flex;
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.05);
  transition: transform 0.2s, box-shadow 0.2s;
  cursor: pointer;
  overflow: hidden;
}

.shop-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(255,107,53,0.15);
}

/* 店铺封面图的固定容器，防止图片加载前后布局抖动 */
.shop-cover-wrapper {
  width: 110px;
  height: 110px;
  margin-right: 20px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
}

/* 店铺真实封面图片 */
.shop-cover {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 保证图片按比例填充缩放，不变形 */
  display: block;
}

/* 店铺封面加载失败或无封面时的纯色兜底占位图 */
.shop-no-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f5f6f8, #e9ecef);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #888;
  font-size: 13px;
  font-weight: bold;
}

/* 店铺右侧文字信息容器 */
.shop-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  padding: 4px 0;
}

/* 店铺名称与距离的水平排布容器 */
.shop-name-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

/* 店铺名称字号加粗突出显示 */
.shop-name {
  font-size: 20px;
  font-weight: 800;
  color: #222;
  margin-right: 12px;
}

/* 基于LBS计算的距离标签样式 */
.shop-distance {
  font-size: 14px;
  color: #ff6b35;
  font-weight: 900;
  background: #fff3ee;
  padding: 6px 12px;
  border-radius: 100px;
  white-space: nowrap; /* 强制不换行，防止距离文本折行 */
  border: 1px solid #ffd1c0;
}

/* 店铺地址与营业时间等基础信息样式 */
.shop-address, .shop-hours {
  font-size: 14px;
  color: #666;
  display: flex;
  align-items: center;
  margin-top: 6px;
}

/* ==================== 全屏图片预览组件全局样式 ==================== */

/* * 遮罩层底层：
 * 使用 fixed 定位脱离文档流，强行铺满整个屏幕 (100vw/100vh)。
 * 背景采用 92% 不透明度的纯黑，提供极佳的沉浸感。
 * z-index 设为 9999 确保压盖住页面上所有的导航栏和层级元素。
 * flex 居中布局，确保内部弹出的图片永远处于屏幕正中央。
 */
.image-preview-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.92); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}

/* * 预览大图主体：
 * max-width/max-height 限制在 90%，四周留白，防止超大图片直接贴边。
 * object-fit: contain 保证无论横图竖图，都按原始比例完整缩放展示，绝不裁剪。
 * 绑定 popZoom 动画，配合特殊的 cubic-bezier 贝塞尔曲线，实现带有物理弹簧阻尼感的“砰”弹入效果。
 */
.preview-img {
  max-width: 90vw; max-height: 90vh; object-fit: contain;
  animation: popZoom 0.3s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}

/* * 弹出缩放动画关键帧：
 * 初始状态：缩小至 80%，完全透明。
 * 结束状态：恢复 100% 原始缩放，完全不透明。配合上面的贝塞尔曲线产生回弹感。
 */
@keyframes popZoom {
  0% { transform: scale(0.8); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

/* * 右上角关闭按钮：
 * 绝对定位固定在右上角，初始设置 60% 透明度降低视觉干扰。
 * hover 时恢复 100% 不透明度，提供明确的交互反馈。
 */
.preview-close {
  position: absolute; top: 32px; right: 40px; color: white;
  font-size: 32px; cursor: pointer; opacity: 0.6; transition: opacity 0.2s;
}
.preview-close:hover { opacity: 1; }

/* * 左右切换翻页箭头基础样式：
 * 垂直居中定位，采用 56px 的大圆角设计扩大点击热区。
 * 默认使用 10% 透明度的白色背景，在黑色遮罩上呈现磨砂高级感。
 */
.preview-arrow {
  position: absolute; top: 50%; transform: translateY(-50%);
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,0.1); color: white;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 24px; transition: background 0.2s;
}

/* 箭头悬停状态：加深背景白色的透明度至 25% */
.preview-arrow:hover { background: rgba(255,255,255,0.25); }

/* 左箭头定位：距离左侧边缘 40px */
.preview-arrow.left { left: 40px; }

/* 右箭头定位：距离右侧边缘 40px */
.preview-arrow.right { right: 40px; }

/* * 底部图片进度页码 (例如: 1 / 3)：
 * 绝对定位固定在屏幕正下方居中，使用 letter-spacing 增加字符间距提升阅读体验。
 */
.preview-count {
  position: absolute; bottom: 40px; left: 50%; transform: translateX(-50%);
  color: white; font-size: 16px; letter-spacing: 2px; opacity: 0.8;
}
/* ==================== LBS 地图可视化样式 ==================== */
.shop-view-toggle {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  justify-content: flex-end; /* 按钮靠右对齐，显得高级 */
}

.shop-view-toggle button {
  padding: 8px 24px;
  border-radius: 20px;
  border: 1px solid #ff6b35;
  background: white;
  color: #ff6b35;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
}

.shop-view-toggle button:hover {
  background: #fff3ee;
}

.shop-view-toggle button.active {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(255,107,53,0.3);
}

.shop-map-container {
  width: 100%;
  height: 600px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  border: 1px solid #eee;
  background: white;
  animation: fadeUp 0.4s ease-out; /* 切换地图时的浮现动画 */
}

.bm-view {
  width: 100%;
  height: 100%;
}

@keyframes fadeUp {
  0% { opacity: 0; transform: translateY(10px); }
  100% { opacity: 1; transform: translateY(0); }
}
</style>

<template>
  <div class="profile-page">

    <!-- 顶部导航 -->
    <div class="navbar">
      <div class="nav-back" @click="$router.push('/home')">← 返回首页</div>
      <div class="nav-title">个人中心</div>
      <div style="width:80px"></div>
    </div>

    <div class="profile-body">

      <!-- 左侧：用户信息卡片 -->
      <div class="profile-sidebar">

        <!-- 头像和基本信息 -->
        <div class="user-card">
          <div class="big-avatar">{{ user.nickname ? user.nickname[0] : 'U' }}</div>
          <div class="user-nickname">{{ user.nickname }}</div>
          <div class="user-username">@{{ user.username }}</div>
          <div class="user-role-tag">
            {{ user.role === 'shop' ? '🏪 商家' : '🌟 美食达人' }}
          </div>
          <!--
            资料编辑入口：
            用户和商家都可以在这里修改用户名、昵称，提交后会做唯一性校验。
          -->
          <button class="edit-profile-btn" @click="openProfileEdit">编辑资料</button>
          <!-- 统计数据 -->
          <div class="user-stats">
            <!-- 点击“笔记”数字，切换到我的笔记标签 -->
            <div class="user-stat" @click="activeTab = 'notes'">
              <div class="stat-num">{{ myNotes.length }}</div>
              <div class="stat-label">笔记</div>
            </div>
            <div class="stat-divider"></div>
            <!-- 点击“收藏”数字，切换到我的收藏标签并刷新收藏列表 -->
            <div class="user-stat" @click="activeTab = 'collect'; loadCollects()">
              <div class="stat-num">{{ collectNotes.length }}</div>
              <div class="stat-label">收藏</div>
            </div>
            <div class="stat-divider"></div>
            <!--
              “获赞”数字不仅做统计展示，也作为快捷入口：
              点击后直接切到“获赞记录”标签，并重新请求最新获赞数据。
            -->
            <div class="user-stat" @click="activeTab = 'likes'; loadLikeRecords()">
              <div class="stat-num">{{ totalLikes }}</div>
              <div class="stat-label">获赞</div>
            </div>
            <div class="stat-divider"></div>
            <!-- 点击“评论”数字，切换到收到评论并刷新评论记录 -->
            <div class="user-stat" @click="activeTab = 'comments'; loadCommentRecords()">
              <div class="stat-num">{{ commentRecords.length }}</div>
              <div class="stat-label">评论</div>
            </div>
          </div>
        </div>

        <!-- 快捷菜单 -->
        <div class="quick-menu">
          <div
              :class="['menu-item', activeTab === 'notes' ? 'active' : '']"
              @click="activeTab = 'notes'"
          >📝 我的笔记</div>
          <div
              :class="['menu-item', activeTab === 'collect' ? 'active' : '']"
              @click="activeTab = 'collect'; loadCollects()"
          >⭐ 我的收藏</div>

          <div
              :class="['menu-item', activeTab === 'my_likes' ? 'active' : '']"
              @click="activeTab = 'my_likes'; loadMyLikedNotes()"
          >👍 点赞记录</div>

          <!--
            “获赞记录”菜单：
            用于展示“谁点赞了我的哪篇笔记”，数据来源是 /note/likes/received。
          -->
          <div
              :class="['menu-item', activeTab === 'likes' ? 'active' : '']"
              @click="activeTab = 'likes'; loadLikeRecords()"
          >❤️ 获赞记录</div>
          <!-- 收到评论提醒：展示别人对我笔记的评论 -->
          <div
              :class="['menu-item', activeTab === 'comments' ? 'active' : '']"
              @click="activeTab = 'comments'; loadCommentRecords()"
          >💬 收到评论</div>
          <div class="menu-item" @click="$router.push('/publish')">✏️ 发布笔记</div>
          <div class="menu-item logout" @click="logout">🚪 退出登录</div>
        </div>

      </div>

      <!-- 右侧：内容区 -->
      <div class="profile-main">

        <!-- 我的笔记 -->
        <div v-if="activeTab === 'notes'">
          <div class="section-header">
            <h2>我的笔记</h2>
            <button class="publish-btn" @click="$router.push('/publish')">+ 发布新笔记</button>
          </div>

          <!-- 空状态 -->
          <div v-if="myNotes.length === 0" class="empty-state">
            <div class="empty-icon">📝</div>
            <p>还没有发布过笔记</p>
            <button class="empty-btn" @click="$router.push('/publish')">去发布第一篇</button>
          </div>

          <!-- 笔记列表 -->
          <div class="my-notes-list">
            <!--
              点击整张卡片都可以进入详情页，避免只能点标题/封面的交互限制。
              删除按钮使用 @click.stop 阻止冒泡，避免误触发卡片跳转。
            -->
            <div v-for="note in myNotes" :key="note.id" class="my-note-item clickable" @click="goDetail(note.id)">
              <!-- 封面图 -->
              <div class="my-note-cover">
                <img v-if="note.images" :src="note.images.split('|||')[0]" alt="封面"/>
                <div v-else class="cover-empty">🍜</div>
              </div>
              <!-- 笔记信息 -->
              <div class="my-note-info">
                <div class="my-note-title">{{ note.title }}</div>
                <div class="my-note-content">{{ note.content }}</div>
                <div class="my-note-meta">
                  <!-- 审核状态标签 -->
                  <span :class="['status-tag', getStatusClass(note.status)]">
                    {{ getStatusText(note.status) }}
                  </span>
                  <span class="meta-item">❤️ {{ note.likeCount || note.like_count || 0 }}</span>
                  <span class="meta-item">⭐ {{ note.collectCount || note.collect_count || 0 }}</span>
                  <span class="meta-time">{{ note.createTime }}</span>
                </div>
                <!-- 被驳回时显示原因 -->
                <div class="reject-reason" v-if="note.status === 2 && (note.rejectReason || note.reject_reason)">
                  驳回原因：{{ note.rejectReason || note.reject_reason }}
                </div>
              </div>
              <!-- 笔记操作区：支持编辑和删除 -->
              <div class="note-actions">
                <button class="edit-note-btn" @click.stop="openNoteEdit(note)">编辑</button>
                <button class="delete-btn" @click.stop="deleteNote(note.id)">🗑️</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeTab === 'collect'">
          <div class="section-header">
            <h2>我的收藏</h2>
          </div>

          <div v-if="collectNotes.length === 0" class="empty-state">
            <div class="empty-icon">⭐</div>
            <p>还没有收藏任何笔记</p>
            <button class="empty-btn" @click="$router.push('/home')">去发现美食</button>
          </div>

          <!-- 收藏的笔记列表（和我的笔记相同布局） -->
          <div class="my-notes-list">
            <div v-for="note in collectNotes" :key="note.id" class="my-note-item clickable" @click="goDetail(note.id)">
              <div class="my-note-cover">
                <img v-if="note.images" :src="note.images.split('|||')[0]" alt="封面"/>
                <div v-else class="cover-empty">🍜</div>
              </div>
              <div class="my-note-info">
                <div class="my-note-title">{{ note.title }}</div>
                <div class="my-note-content">{{ note.content }}</div>
                <div class="my-note-meta">
                  <span class="meta-item">作者：{{ note.nickname }}</span>
                  <span class="meta-item">❤️ {{ note.likeCount || note.like_count || 0 }}</span>
                </div>
              </div>
              <!-- 取消收藏按钮 -->
              <button class="delete-btn" @click.stop="cancelCollect(note.id)">✕</button>
            </div>
          </div>
        </div>

        <div v-if="activeTab === 'my_likes'">
          <div class="section-header">
            <h2>点赞记录</h2>
          </div>

          <div v-if="myLikedNotes.length === 0" class="empty-state">
            <div class="empty-icon">👍</div>
            <p>还没有点赞过任何笔记</p>
            <button class="empty-btn" @click="$router.push('/home')">去发现美食</button>
          </div>

          <div class="my-notes-list">
            <div v-for="note in myLikedNotes" :key="note.id" class="my-note-item clickable" @click="goDetail(note.id)">
              <div class="my-note-cover">
                <img v-if="note.images" :src="note.images.split('|||')[0]" alt="封面"/>
                <div v-else class="cover-empty">🍜</div>
              </div>
              <div class="my-note-info">
                <div class="my-note-title">{{ note.title }}</div>
                <div class="my-note-content">{{ note.content }}</div>
                <div class="my-note-meta">
                  <span class="meta-item">作者：{{ note.nickname }}</span>
                  <span class="meta-item">❤️ {{ note.likeCount || note.like_count || 0 }}</span>
                  <span class="meta-item">⭐ {{ note.collectCount || note.collect_count || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 获赞记录 -->
        <div v-if="activeTab === 'likes'">
          <div class="section-header">
            <h2>获赞记录</h2>
          </div>

          <div v-if="likeRecords.length === 0" class="empty-state">
            <div class="empty-icon">❤️</div>
            <p>还没有人给你的笔记点赞</p>
          </div>

          <!--
            获赞记录列表中每一行都可点击跳转到对应笔记详情，
            方便作者快速查看被点赞笔记的上下文内容。
          -->
          <div v-else class="my-notes-list">
            <div v-for="record in likeRecords" :key="record.id" class="my-note-item clickable" @click="goDetail(record.noteId || record.note_id)">
              <div class="my-note-info">
                <div class="my-note-title">{{ record.title }}</div>
                <div class="my-note-content">
                  <span class="meta-item" style="color:#ff6b35;font-weight:600">👍 {{ record.likerNickname || record.liker_nickname }}</span>
                  点赞了这篇笔记
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 收到评论 -->
        <div v-if="activeTab === 'comments'">
          <div class="section-header">
            <h2>收到评论</h2>
          </div>

          <div v-if="commentRecords.length === 0" class="empty-state">
            <div class="empty-icon">💬</div>
            <p>暂时还没有新的评论</p>
          </div>

          <div v-else class="my-notes-list">
            <div
                v-for="record in commentRecords"
                :key="record.id"
                class="my-note-item clickable"
                @click="goDetail(record.noteId || record.note_id)"
            >
              <div class="my-note-info">
                <div class="my-note-title">{{ record.title }}</div>
                <div class="my-note-content">
                  <span class="meta-item" style="color:#ff6b35;font-weight:600">
                    {{ (record.commenterNickname || record.commenter_nickname) === '商家' ? '🏪' : '💬' }}
                    {{ record.commenterNickname || record.commenter_nickname }}
                  </span>
                  {{ (record.commenterNickname || record.commenter_nickname) === '商家' ? '回应：' : '评论：' }}{{ record.content }}
                </div>
                <div class="my-note-meta">
                  <span class="meta-time">{{ record.createTime || record.create_time }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 资料编辑弹窗 -->
    <div v-if="showProfileModal" class="modal-mask" @click.self="showProfileModal = false">
      <div class="modal-card">
        <div class="modal-title">修改个人资料</div>
        <div class="modal-sub">用户名和昵称都需要保持唯一</div>
        <div class="modal-form-item">
          <label>用户名</label>
          <input v-model="profileForm.username" class="modal-input" placeholder="请输入新用户名"/>
        </div>
        <div class="modal-form-item">
          <label>昵称</label>
          <input v-model="profileForm.nickname" class="modal-input" placeholder="请输入新昵称"/>
        </div>
        <div class="modal-actions">
          <button class="modal-btn gray" @click="showProfileModal = false">取消</button>
          <button class="modal-btn orange" @click="saveProfile">保存</button>
        </div>
      </div>
    </div>

    <!-- 笔记编辑弹窗 -->
    <div v-if="showNoteModal" class="modal-mask" @click.self="showNoteModal = false">
      <div class="modal-card">
        <div class="modal-title">修改笔记</div>
        <div class="modal-form-item">
          <label>标题</label>
          <input v-model="noteForm.title" class="modal-input" placeholder="请输入标题"/>
        </div>
        <div class="modal-form-item">
          <label>内容</label>
          <textarea v-model="noteForm.content" class="modal-textarea" placeholder="请输入内容"></textarea>
        </div>
        <div class="modal-form-item">
          <label>美食分类</label>
          <select v-model="noteForm.categoryId" class="modal-input">
            <option :value="null">不选择分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
        <div class="modal-form-item">
          <label>关联店铺</label>
          <div v-if="noteSelectedShopName" class="selected-shop-box">
            <span>已选择：{{ noteSelectedShopName }}</span>
            <button class="clear-shop-btn" @click="clearNoteShop">清除</button>
          </div>
          <input
              v-else
              v-model="noteShopKeyword"
              class="modal-input"
              placeholder="输入店铺名称搜索并关联"
              @input="searchNoteShops"
          />
          <div v-if="noteShopList.length > 0 && !noteSelectedShopName" class="shop-list-box">
            <div
                v-for="shop in noteShopList"
                :key="shop.id"
                class="shop-list-item"
                @click="selectNoteShop(shop)"
            >
              {{ shop.name }}
            </div>
          </div>
        </div>
        <div class="modal-form-item">
          <label>笔记图片</label>
          <div class="modal-image-list">
            <div
                v-for="(img, idx) in noteForm.images"
                :key="idx"
                class="modal-image-item"
                :class="{ cover: idx === 0 }"
            >
              <img :src="img" alt="图片"/>
              <div class="img-actions">
                <button @click="setNoteCover(idx)">设封面</button>
                <button @click="removeNoteImage(idx)">删除</button>
              </div>
            </div>
            <label class="modal-upload-btn">
              + 添加图片
              <input type="file" multiple accept="image/*" @change="handleNoteImagesUpload" style="display:none"/>
            </label>
          </div>
        </div>
        <div class="modal-form-item">
          <label>评分（1-5）</label>
          <input v-model.number="noteForm.score" type="number" min="1" max="5" class="modal-input"/>
        </div>
        <div class="modal-actions">
          <button class="modal-btn gray" @click="showNoteModal = false">取消</button>
          <button class="modal-btn orange" @click="saveNote">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProfileView',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeTab: 'notes',   // 当前激活的标签
      myNotes: [],          // 我发布的笔记列表
      collectNotes: [],     // 我收藏的笔记列表
      myLikedNotes: [],     // 我点赞过的笔记列表
      likeRecords: [],      // 我的获赞明细列表
      commentRecords: [],   // 我收到的评论记录
      categories: [],       // 分类列表（用于编辑笔记时选择分类）
      showProfileModal: false, // 是否显示资料编辑弹窗
      showNoteModal: false,    // 是否显示笔记编辑弹窗
      // 资料编辑表单
      profileForm: {
        username: '',
        nickname: ''
      },
      // 笔记编辑表单
      noteForm: {
        id: null,
        title: '',
        content: '',
        score: 5,
        images: [],
        categoryId: null,
        shopId: null
      },
      noteShopKeyword: '',      // 笔记编辑弹窗中的店铺搜索词
      noteShopList: [],         // 笔记编辑弹窗中的店铺搜索结果
      noteSelectedShopName: '', // 笔记编辑弹窗中已选店铺名
      originalNotePayloadStr: '', // 用来存绝对标准化的 JSON 字符串快照
    }
  },
  computed: {
    // 计算我所有笔记的总获赞数，兼容驼峰和下划线两种字段名
    totalLikes() {
      return this.myNotes.reduce((sum, n) => sum + (n.likeCount || n.like_count || 0), 0)
    }
  },
  mounted() {
    if (!this.user || !this.user.id) {
      this.$router.push('/login')
      return
    }
    this.loadMyNotes()
    this.loadCollects()  // 同时加载收藏，收藏数量统计可以正确显示
    this.loadLikeRecords()
    // 进入个人中心时预加载“收到评论”，用于顶部统计数字展示
    this.loadCommentRecords()
    this.loadCategories()
    // 如果URL带了tab=collect参数，自动切换到收藏标签
    if (this.$route.query.tab === 'collect') {
      this.activeTab = 'collect'
    }
    if (this.$route.query.tab === 'my_likes') {
      this.activeTab = 'my_likes'
      // 重新加载点赞列表，这样取消点赞的笔记一返回就会自动从列表消失
      this.loadMyLikedNotes()
    }
    if (this.$route.query.tab === 'likes') {
      this.activeTab = 'likes'
    }
    if (this.$route.query.tab === 'comments') {
      this.activeTab = 'comments'
      // 通过详情页返回到“收到评论”时，确保列表会立即刷新
      this.loadCommentRecords()
    }
  },
  methods: {
    // 加载分类列表：编辑笔记时用于选择“所属美食分类”
    // 数据清洗与格式化，屏蔽浏览器的隐形转换干扰
    generateNotePayloadStr() {
      return JSON.stringify({
        title: String(this.noteForm.title || '').trim(),
        content: String(this.noteForm.content || '').trim().replace(/\r\n/g, '\n'),
        score: Number(this.noteForm.score || 5),
        categoryId: this.noteForm.categoryId ? Number(this.noteForm.categoryId) : null,
        shopId: this.noteForm.shopId ? Number(this.noteForm.shopId) : null,
        images: (this.noteForm.images || []).join('|||')
      });
    },

    async loadCategories() {
      const res = await this.$axios.get('/category/list')
      if (res.data.code === 200) this.categories = res.data.data
    },

    // 加载我发布的笔记
    async loadMyNotes() {
      const res = await this.$axios.get('/note/my', {
        params: { userId: this.user.id }
      })
      if (res.data.code === 200) this.myNotes = res.data.data
    },

    // 加载我收藏的笔记
    async loadCollects() {
      const res = await this.$axios.get('/note/collects', {
        params: { userId: this.user.id }
      })
      if (res.data.code === 200) this.collectNotes = res.data.data
    },

    // 加载我点赞过的笔记
    async loadMyLikedNotes() {
      const res = await this.$axios.get('/note/liked', {
        params: { userId: this.user.id }
      })
      if (res.data.code === 200) this.myLikedNotes = res.data.data
    },

    // 加载我的获赞记录（谁点赞了哪篇笔记）
    // 这里用独立接口，避免从“我的笔记”里二次拼接数据，减少前端计算复杂度。
    async loadLikeRecords() {
      const res = await this.$axios.get('/note/likes/received', {
        params: { userId: this.user.id }
      })
      if (res.data.code === 200) this.likeRecords = res.data.data
    },

    // 加载我收到的评论记录（谁评论了我的笔记）
    async loadCommentRecords() {
      const res = await this.$axios.get('/note/comments/received', {
        params: { userId: this.user.id }
      })
      if (res.data.code === 200) this.commentRecords = res.data.data
    },

    // 打开资料编辑弹窗：先把当前用户信息回填，便于直接修改
    openProfileEdit() {
      this.profileForm.username = this.user.username || ''
      this.profileForm.nickname = this.user.nickname || ''
      this.showProfileModal = true
    },

    // 保存资料：后端会校验用户名和昵称是否重复
    async saveProfile() {
      if (!this.profileForm.username.trim() || !this.profileForm.nickname.trim()) {
        this.$message.warning('用户名和昵称不能为空')
        return
      }
      const res = await this.$axios.post('/user/profile/update', {
        id: this.user.id,
        username: this.profileForm.username,
        nickname: this.profileForm.nickname
      })
      if (res.data.code === 200) {
        // 更新当前页面和localStorage中的用户信息，保证全站头像昵称实时同步
        this.user.username = res.data.data.username
        this.user.nickname = res.data.data.nickname
        localStorage.setItem('user', JSON.stringify(this.user))
        this.$message.success('资料修改成功')
        this.showProfileModal = false
      } else {
        this.$message.error(res.data.message)
      }
    },

    // 打开笔记编辑弹窗：将当前笔记内容回填到表单
    openNoteEdit(note) {
      this.noteForm.id = note.id
      this.noteForm.title = note.title || ''
      this.noteForm.content = note.content || ''
      this.noteForm.score = note.score || 5
      this.noteForm.images = note.images ? note.images.split('|||') : []
      this.noteForm.categoryId = note.categoryId || note.category_id || null
      this.noteForm.shopId = note.shopId || note.shop_id || null
      this.noteSelectedShopName = note.shopName || note.shop_name || ''
      this.noteShopKeyword = ''
      this.noteShopList = []
      this.showNoteModal = true
      // 数据填入表单后，立刻洗干净并拍下死快照
      this.originalNotePayloadStr = this.generateNotePayloadStr()
    },

    // 笔记编辑弹窗中上传图片：沿用发布笔记的多图上传方式
    handleNoteImagesUpload(e) {
      const files = Array.from(e.target.files || [])
      const remaining = 9 - this.noteForm.images.length
      files.slice(0, remaining).forEach(file => {
        const reader = new FileReader()
        reader.onload = (ev) => {
          this.noteForm.images.push(ev.target.result)
        }
        reader.readAsDataURL(file)
      })
      e.target.value = ''
    },

    // 设置封面：把选中的图片放到数组第一位，详情和列表都会使用第一张作为封面
    setNoteCover(index) {
      if (index === 0) return
      const selected = this.noteForm.images.splice(index, 1)[0]
      this.noteForm.images.unshift(selected)
    },

    // 删除某张图片
    removeNoteImage(index) {
      this.noteForm.images.splice(index, 1)
    },

    // 搜索店铺（笔记编辑弹窗）
    async searchNoteShops() {
      if (!this.noteShopKeyword) {
        this.noteShopList = []
        return
      }
      const res = await this.$axios.get('/shop/search', {
        params: { keyword: this.noteShopKeyword }
      })
      if (res.data.code === 200) {
        this.noteShopList = res.data.data
      }
    },

    // 选择关联店铺
    selectNoteShop(shop) {
      this.noteForm.shopId = shop.id
      this.noteSelectedShopName = shop.name
      this.noteShopKeyword = ''
      this.noteShopList = []
    },

    // 清除关联店铺
    clearNoteShop() {
      this.noteForm.shopId = null
      this.noteSelectedShopName = ''
      this.noteShopKeyword = ''
      this.noteShopList = []
    },

    // 保存笔记修改：只允许修改自己的笔记
    async saveNote() {
      // 拦截器逻辑，对比当前表单和快照
      if (this.originalNotePayloadStr) {
        if (this.generateNotePayloadStr() === this.originalNotePayloadStr) {
          this.$message.warning('您未做任何修改，无需重复提交审核');
          this.showNoteModal = false; // 直接关闭弹窗
          return; // 强制中断，不发请求
        }
      }
      if (!this.noteForm.title.trim() || !this.noteForm.content.trim()) {
        this.$message.warning('标题和内容不能为空')
        return
      }
      if (this.noteForm.score < 1 || this.noteForm.score > 5) {
        this.$message.warning('评分范围是1到5')
        return
      }
      const res = await this.$axios.post('/note/update', {
        id: this.noteForm.id,
        userId: this.user.id,
        title: this.noteForm.title,
        content: this.noteForm.content,
        score: this.noteForm.score,
        images: this.noteForm.images.join('|||'),
        categoryId: this.noteForm.categoryId,
        shopId: this.noteForm.shopId
      })
      if (res.data.code === 200) {
        // 动态读取后端 Result.success() 传过来的 message 字段
        this.$message.success(res.data.message)
        // 发布成功后跳回首页
        setTimeout(() => this.$router.push('/home'), 1500)
      } else {
        this.$message.error(res.data.message)
      }
    },

    // 删除自己的笔记
    async deleteNote(id) {
      // 删除前二次确认，明确提示该操作不可撤销
      const ok = window.confirm('确认删除这篇笔记吗？\n此操作无法撤销！')
      if (!ok) return
      const res = await this.$axios.post('/note/delete', { id, userId: this.user.id })
      if (res.data.code === 200) {
        this.$message.success('删除成功')
        this.loadMyNotes()
      }
    },

    // 取消收藏
    async cancelCollect(noteId) {
      const res = await this.$axios.post('/note/collect', {
        noteId, userId: this.user.id
      })
      if (res.data.code === 200) {
        this.$message.success('已取消收藏')
        this.loadCollects()
      }
    },

    // 跳转笔记详情
    // 这里带上来源和当前标签，用于详情页返回时回到个人中心对应板块
    goDetail(id) {
      this.$router.push({
        path: '/note/' + id,
        query: { from: 'profile', tab: this.activeTab }
      })
    },

    // 根据审核状态返回对应样式类名
    getStatusClass(status) {
      return { 0: 'pending', 1: 'passed', 2: 'rejected', 3: 'rejected' }[status] || 'pending'
    },

    // 根据审核状态返回对应文字
    getStatusText(status) {
      return { 0: '⏳ 待审核', 1: '✅ 已通过', 2: '❌ 已驳回', 3: '⚠️ 违规屏蔽' }[status] || '待审核'
    },

    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f5f5f5; }

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

/* 主体布局 */
.profile-body {
  display: flex; gap: 24px;
  max-width: 1100px; margin: 24px auto; padding: 0 24px;
}

/* 左侧边栏 */
.profile-sidebar { width: 240px; flex-shrink: 0; display: flex; flex-direction: column; gap: 16px; }

/* 用户信息卡片 */
.user-card {
  background: white; border-radius: 16px; padding: 24px;
  text-align: center; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}
.big-avatar {
  width: 72px; height: 72px; border-radius: 50%; margin: 0 auto 12px;
  background: linear-gradient(135deg, #ff6b35, #ffd93d);
  color: white; font-size: 28px; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
}
.user-nickname { font-size: 18px; font-weight: 700; color: #333; margin-bottom: 4px; }
.user-username { font-size: 13px; color: #aaa; margin-bottom: 8px; }
.user-role-tag {
  display: inline-block; background: #fff3ee; color: #ff6b35;
  font-size: 12px; padding: 4px 12px; border-radius: 12px; margin-bottom: 16px;
}
.edit-profile-btn {
  border: none;
  border-radius: 16px;
  padding: 6px 14px;
  font-size: 12px;
  color: #ff6b35;
  background: #fff4ee;
  cursor: pointer;
  margin-bottom: 12px;
}
.edit-profile-btn:hover { background: #ffe9de; }
.user-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 4px;
}
.user-stat {
  text-align: center;
  cursor: pointer;
  border-radius: 10px;
  padding: 6px 0;
  transition: all 0.2s;
}
.user-stat:hover { background: #fff6f1; }
.user-stat:hover .stat-label { color: #ff6b35; }
.stat-num { font-size: 20px; font-weight: 700; color: #ff6b35; }
.stat-label { font-size: 12px; color: #aaa; margin-top: 2px; }
.stat-divider { display: none; }

/* 快捷菜单 */
.quick-menu { background: white; border-radius: 16px; padding: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.menu-item {
  padding: 12px 16px; border-radius: 10px; cursor: pointer;
  font-size: 14px; color: #555; transition: all 0.2s; margin-bottom: 4px;
}
.menu-item:hover { background: #fff3ee; color: #ff6b35; }
.menu-item.active { background: linear-gradient(135deg, #ff6b35, #f7931e); color: white; font-weight: 600; }
.logout { color: #ff4444 !important; }
.logout:hover { background: #fff0f0 !important; }

/* 右侧内容区 */
.profile-main { flex: 1; min-width: 0; }
.section-header {
  display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px;
}
.section-header h2 { margin: 0; color: #333; }
.publish-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white; border: none; border-radius: 20px;
  padding: 8px 20px; cursor: pointer; font-size: 14px;
}

/* 空状态 */
.empty-state { text-align: center; padding: 60px 0; background: white; border-radius: 16px; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }
.empty-state p { color: #aaa; margin-bottom: 20px; }
.empty-btn {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white; border: none; border-radius: 20px;
  padding: 10px 24px; cursor: pointer; font-size: 14px;
}

/* 笔记列表 */
.my-notes-list { display: flex; flex-direction: column; gap: 12px; }
.my-note-item {
  display: flex; gap: 16px; background: white;
  border-radius: 16px; padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05); align-items: flex-start;
}
.my-note-item.clickable { cursor: pointer; }
.my-note-item.clickable:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
.my-note-cover {
  width: 100px; height: 80px; border-radius: 10px;
  overflow: hidden; flex-shrink: 0; cursor: pointer;
}
.my-note-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-empty {
  width: 100%; height: 100%; background: #fff3ee;
  display: flex; align-items: center; justify-content: center; font-size: 28px;
}
.my-note-info { flex: 1; min-width: 0; }
.my-note-title {
  font-size: 15px; font-weight: 600; color: #333;
  margin-bottom: 6px; cursor: pointer;
}
.my-note-title:hover { color: #ff6b35; }
.my-note-content {
  font-size: 13px; color: #888; margin-bottom: 8px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.my-note-meta { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.meta-item { font-size: 12px; color: #aaa; }
.meta-time { font-size: 12px; color: #bbb; margin-left: auto; }

/* 审核状态标签 */
.status-tag { font-size: 12px; padding: 2px 8px; border-radius: 8px; }
.pending { background: #fff7e6; color: #fa8c16; }
.passed { background: #f6ffed; color: #52c41a; }
.rejected { background: #fff1f0; color: #ff4d4f; }

/* 驳回原因 */
.reject-reason {
  margin-top: 6px; font-size: 12px; color: #ff4d4f;
  background: #fff1f0; padding: 6px 10px; border-radius: 6px;
}

/* 删除/取消收藏按钮 */
.delete-btn {
  background: none; border: none; cursor: pointer;
  font-size: 18px; color: #ccc; padding: 4px; transition: color 0.2s;
}
.delete-btn:hover { color: #ff4d4f; }
.note-actions { display: flex; align-items: center; gap: 6px; }
.edit-note-btn {
  border: none;
  border-radius: 12px;
  padding: 4px 10px;
  font-size: 12px;
  color: #ff6b35;
  background: #fff4ee;
  cursor: pointer;
}
.edit-note-btn:hover { background: #ffe9de; }

/* 通用弹窗样式：用于资料编辑和笔记编辑 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal-card {
  width: 420px;
  max-width: calc(100vw - 24px);
  background: white;
  border-radius: 14px;
  padding: 18px;
}
.modal-title { font-size: 18px; font-weight: 700; color: #333; }
.modal-sub { color: #999; font-size: 12px; margin-top: 4px; margin-bottom: 10px; }
.modal-form-item { margin-top: 10px; }
.modal-form-item label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.modal-input, .modal-textarea {
  width: 100%;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
}
.modal-textarea { min-height: 100px; resize: vertical; }
.modal-input:focus, .modal-textarea:focus { border-color: #ff6b35; }
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}
.modal-btn {
  border: none;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
}
.modal-btn.gray { background: #f2f2f2; color: #666; }
.modal-btn.orange { background: #ff6b35; color: white; }
.selected-shop-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  background: #fff7f2;
  border: 1px solid #ffe1d2;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
  color: #ff6b35;
}
.clear-shop-btn {
  border: none;
  border-radius: 12px;
  background: #ffe5d6;
  color: #ff6b35;
  font-size: 12px;
  padding: 4px 8px;
  cursor: pointer;
}
.shop-list-box {
  margin-top: 8px;
  max-height: 140px;
  overflow: auto;
  border: 1px solid #eee;
  border-radius: 8px;
}
.shop-list-item {
  padding: 8px 10px;
  font-size: 13px;
  cursor: pointer;
}
.shop-list-item:hover { background: #fff4ee; color: #ff6b35; }
.modal-image-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}
.modal-image-item {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
}
.modal-image-item.cover { border-color: #ff6b35; }
.modal-image-item img {
  width: 100%;
  height: 76px;
  object-fit: cover;
  display: block;
}
.img-actions {
  display: flex;
  gap: 4px;
  padding: 6px;
}
.img-actions button {
  border: none;
  border-radius: 6px;
  font-size: 11px;
  padding: 4px 6px;
  cursor: pointer;
  background: #f2f2f2;
}
.img-actions button:hover { background: #ffe9de; color: #ff6b35; }
.modal-upload-btn {
  border: 1px dashed #ffb08f;
  border-radius: 8px;
  min-height: 106px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff6b35;
  font-size: 13px;
  cursor: pointer;
  background: #fffaf7;
}
/* 查看详情按钮 */
.btn-view {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  color: white;
  border: none;
  border-radius: 16px;
  padding: 7px 20px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 3px 8px rgba(79,172,254,0.4);
}
.btn-view:hover { transform: translateY(-1px); opacity: 0.9; }

/* 屏蔽按钮 */
.btn-delete-note {
  background: linear-gradient(135deg, #ff6b6b, #ee0979);
  color: white;
  border: none;
  border-radius: 16px;
  padding: 7px 20px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 3px 8px rgba(238,9,121,0.4);
}
.btn-delete-note:hover { transform: translateY(-1px); opacity: 0.9; }

@media (max-width: 900px) {
  .profile-body {
    flex-direction: column;
    padding: 0 12px;
    margin: 12px auto;
    gap: 12px;
  }
  .profile-sidebar {
    width: 100%;
  }
  .user-card {
    padding: 18px;
  }
}
</style>

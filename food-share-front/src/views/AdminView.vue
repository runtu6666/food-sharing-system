<template>
  <!-- 管理员后台整体布局：左侧菜单 + 右侧内容 -->
  <div class="admin-layout">

    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <!-- 系统Logo区域 -->
      <div class="sidebar-logo">
        <span class="logo-icon">🍜</span>
        <span class="logo-text">美食探店</span>
        <span class="logo-sub">管理后台</span>
      </div>

      <!-- 导航菜单列表 -->
      <div class="sidebar-menu">
        <!-- 数据概览 -->
        <div
            :class="['menu-item', activeMenu === 'overview' ? 'active' : '']"
            @click="activeMenu = 'dashboard'"
        >
          <span class="menu-icon">📊</span>
          <span class="menu-text">数据概览</span>
        </div>

        <!-- 笔记审核 -->
        <div
            :class="['menu-item', activeMenu === 'notes' ? 'active' : '']"
            @click="activeMenu = 'notes'; loadNotes()"
        >
          <span class="menu-icon">📝</span>
          <span class="menu-text">笔记审核</span>
          <!-- 待审核数量角标 -->
          <span class="menu-badge" v-if="stats.pendingNotes > 0">{{ stats.pendingNotes }}</span>
        </div>

        <!-- 商家审批 -->
        <div
            :class="['menu-item', activeMenu === 'shops' ? 'active' : '']"
            @click="activeMenu = 'shops'; loadShops()"
        >
          <span class="menu-icon">🏪</span>
          <span class="menu-text">商家审批</span>
        </div>

        <!-- 笔记管理 -->
        <div
            :class="['menu-item', activeMenu === 'noteList' ? 'active' : '']"
            @click="activeMenu = 'noteList'; loadAllNotes()"
        >
          <span class="menu-icon">📋</span>
          <span class="menu-text">笔记管理</span>
        </div>

        <!-- 商家管理 -->
        <div
            :class="['menu-item', activeMenu === 'shopList' ? 'active' : '']"
            @click="activeMenu = 'shopList'; loadAllShops()"
        >
          <span class="menu-icon">🏬</span>
          <span class="menu-text">商家管理</span>
        </div>

        <!-- 用户管理 -->
        <div
            :class="['menu-item', activeMenu === 'users' ? 'active' : '']"
            @click="activeMenu = 'users'; loadUsers()"
        >
          <span class="menu-icon">👥</span>
          <span class="menu-text">用户管理</span>
        </div>

        <!-- 分类管理 -->
        <div
            :class="['menu-item', activeMenu === 'category' ? 'active' : '']"
            @click="activeMenu = 'category'; loadCategories()"
        >
          <span class="menu-icon">🏷️</span>
          <span class="menu-text">分类管理</span>
        </div>

        <div
            :class="['menu-item', activeMenu === 'sensitiveWords' ? 'active' : '']"
            @click="activeMenu = 'sensitiveWords'; loadSensitiveWords()"
        >
          <span class="menu-icon">🛡️</span>
          <span class="menu-text">风控管理</span>
        </div>
      </div>

      <!-- 底部：管理员信息和退出 -->
      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">A</div>
          <div class="admin-detail">
            <div class="admin-name">管理员</div>
            <div class="admin-role">超级管理员</div>
          </div>
        </div>
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>

    <!-- 右侧主区域 -->
    <div class="main">

      <!-- 内容区 -->
      <div class="content">

        <!-- ===== 数据概览 ===== -->
        <div v-if="activeMenu === 'dashboard'">

          <!-- 页面标题 -->
          <div class="overview-header">
            <h2 class="overview-title">📊 数据概览</h2>
            <span class="overview-sub">实时掌握平台运营数据</span>
          </div>

          <!-- 四个统计卡片 -->
          <div class="stat-cards">
            <!-- 注册用户数 -->
            <div class="stat-card card-blue">
              <div class="stat-icon">👤</div>
              <div class="stat-info">
                <div class="stat-num">{{ stats.userCount }}</div>
                <div class="stat-label">注册用户</div>
              </div>
            </div>
            <!-- 入驻商家数 -->
            <div class="stat-card card-green">
              <div class="stat-icon">🏪</div>
              <div class="stat-info">
                <div class="stat-num">{{ stats.shopCount }}</div>
                <div class="stat-label">入驻商家</div>
              </div>
            </div>
            <!-- 探店笔记总数 -->
            <div class="stat-card card-orange">
              <div class="stat-icon">📝</div>
              <div class="stat-info">
                <div class="stat-num">{{ stats.noteCount }}</div>
                <div class="stat-label">探店笔记</div>
              </div>
            </div>
            <!-- 待审核笔记数 -->
            <div class="stat-card card-red">
              <div class="stat-icon">⏳</div>
              <div class="stat-info">
                <div class="stat-num">{{ stats.pendingCount }}</div>
                <div class="stat-label">待审核笔记</div>
              </div>
            </div>
          </div>

          <!-- 快捷操作区 -->
          <div class="quick-actions">
            <div class="section-title">⚡ 快捷操作</div>
            <div class="action-cards">
              <!-- 点击直接跳转到笔记审核 -->
              <div class="action-card" @click="activeMenu = 'notes'; loadNotes()">
                <div class="action-icon">📝</div>
                <div class="action-text">
                  <div class="action-name">笔记审核</div>
                  <div class="action-desc">{{ stats.pendingNotes || stats.pendingCount }} 篇待审核</div>
                </div>
                <div class="action-arrow">→</div>
              </div>
              <!-- 点击直接跳转到商家审批 -->
              <div class="action-card" @click="activeMenu = 'shops'; loadShops()">
                <div class="action-icon">🏪</div>
                <div class="action-text">
                  <div class="action-name">商家审批</div>
                  <div class="action-desc">点击查看待审批商家</div>
                </div>
                <div class="action-arrow">→</div>
              </div>
              <!-- 点击直接跳转到用户管理 -->
              <div class="action-card" @click="activeMenu = 'users'; loadUsers()">
                <div class="action-icon">👥</div>
                <div class="action-text">
                  <div class="action-name">用户管理</div>
                  <div class="action-desc">共 {{ stats.userCount }} 位用户</div>
                </div>
                <div class="action-arrow">→</div>
              </div>
            </div>
          </div>

        </div>

        <!-- ===== 笔记审核（卡片风格） ===== -->
        <div v-if="activeMenu === 'notes'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
            <h2 style="margin:0">笔记审核</h2>
            <span style="color:#999;font-size:14px">共 {{ notes.length }} 篇待审核</span>
          </div>

          <!-- 空状态 -->
          <div v-if="notes.length === 0" style="text-align:center;padding:80px 0;color:#aaa">
            <div style="font-size:48px;margin-bottom:16px">✅</div>
            <p>暂无待审核笔记</p>
          </div>

          <!-- 笔记卡片列表 -->
          <div class="audit-card-list">
            <div v-for="note in notes" :key="note.id" class="audit-card">

              <!-- 左侧图片区 -->
              <div class="audit-card-images">
                <div v-if="note.images" class="image-viewer">
                  <!-- 显示当前选中的图片 -->
                  <img
                      :src="note.images.split('|||')[currentImageIndex[note.id] || 0]"
                      class="main-image"
                      alt="笔记图片"
                  />
                  <!-- 多图时显示切换小圆点 -->
                  <div v-if="note.images.split('|||').length > 1" class="image-nav">
                    <button
                        v-for="(img, idx) in note.images.split('|||')"
                        :key="idx"
                        :class="['img-dot', (currentImageIndex[note.id] || 0) === idx ? 'active' : '']"
                        @click="switchImage(note.id, idx)"
                    ></button>
                  </div>
                  <!-- 图片计数角标 -->
                  <div class="image-count" v-if="note.images.split('|||').length > 1">
                    {{ (currentImageIndex[note.id] || 0) + 1 }} / {{ note.images.split('|||').length }}
                  </div>
                </div>
                <!-- 无图片占位 -->
                <div v-else class="no-image">
                  <span>🍽️</span>
                  <p>暂无图片</p>
                </div>
              </div>

              <!-- 右侧内容区 -->
              <div class="audit-card-content">
                <!-- 顶部：作者信息 + 评分 -->
                <div class="audit-card-header">
                  <div class="audit-author">
                    <div class="audit-avatar">{{ note.nickname ? note.nickname[0] : 'U' }}</div>
                    <div>
                      <div class="audit-nickname">{{ note.nickname }}</div>
                      <div class="audit-time">{{ note.createTime }}</div>
                    </div>
                  </div>
                  <div class="audit-score">
                    <span v-for="n in (note.score || 5)" :key="n">⭐</span>
                  </div>
                </div>

                <!-- 笔记标题 -->
                <div class="audit-title">{{ note.title }}</div>

                <!-- 笔记正文（超过3行可展开） -->
                <div class="audit-content" :class="{ expanded: expandedNotes[note.id] }">
                  {{ note.content }}
                </div>
                <span
                    class="expand-btn"
                    v-if="note.content && note.content.length > 100"
                    @click="toggleExpand(note.id)"
                >
                  {{ expandedNotes[note.id] ? '收起 ▲' : '展开全文 ▼' }}
                </span>

                <!-- 底部操作区 -->
                <div class="audit-actions">
                  <div class="audit-id">笔记ID：{{ note.id }}</div>
                  <div class="action-btns">
                    <button class="btn-pass" @click="auditNote(note.id, 1)">✅ 审核通过</button>
                    <button class="btn-reject" @click="showReject(note.id)">❌ 驳回</button>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>

        <!-- 商家审批 -->
        <div v-if="activeMenu === 'shops'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
            <h2 style="margin:0">商家审批</h2>
            <span style="color:#999;font-size:14px">共 {{ shops.length }} 家待审批</span>
          </div>

          <!-- 空状态 -->
          <div v-if="shops.length === 0" style="text-align:center;padding:60px 0;color:#aaa">
            <div style="font-size:48px;margin-bottom:16px">✅</div>
            <p>暂无待审批商家</p>
          </div>

          <!-- 商家审批卡片列表 -->
          <div class="shop-audit-list">
            <div v-for="shop in shops" :key="shop.id" class="shop-audit-card">

              <!-- 左侧：店铺实景照片 -->
              <div class="shop-audit-images">
                <!-- 有照片则显示，支持多图切换 -->
                <div v-if="shop.shopImages || shop.shop_images" class="image-viewer">
                  <img
                      :src="(shop.shopImages || shop.shop_images).split('|||')[shopImgIndex[shop.id] || 0]"
                      class="shop-main-img"
                      alt="店铺照片"
                  />
                  <!-- 多图切换圆点 -->
                  <div v-if="(shop.shopImages || shop.shop_images).split('|||').length > 1" class="image-nav">
                    <button
                        v-for="(img, idx) in (shop.shopImages || shop.shop_images || '').split('|||').filter(Boolean)"
                        :key="idx"
                        :class="['img-dot', (shopImgIndex[shop.id] || 0) === idx ? 'active' : '']"
                        @click="shopImgIndex = {...shopImgIndex, [shop.id]: idx}"
                    ></button>
                  </div>
                </div>
                <!-- 没有照片的占位 -->
                <div v-else class="no-shop-img">
                  <span>🏪</span>
                  <p>未上传照片</p>
                </div>
              </div>

              <!-- 右侧：商家详情 -->
              <div class="shop-audit-content">
                <!-- 店铺名称和提交时间 -->
                <div class="shop-audit-header">
                  <div class="shop-audit-name">{{ shop.name }}</div>
                  <div class="shop-audit-time">{{ shop.createTime }}</div>
                </div>

                <!-- 资质信息列表 -->
                <div class="shop-info-grid">
                  <div class="info-item">
                    <span class="info-label">📍 地址</span>
                    <span class="info-value">{{ shop.address }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">📞 电话</span>
                    <span class="info-value">{{ shop.phone }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">👤 法人</span>
                    <!-- 有法人姓名显示，没有显示未填写 -->
                    <span class="info-value" :class="!shop.legalName ? 'missing' : ''">
              {{ shop.legalName || shop.legal_name || '未填写' }}
            </span>
                  </div>
                  <div class="info-item" v-if="shop.businessHours">
                    <span class="info-label">🕐 营业</span>
                    <span class="info-value">{{ shop.businessHours }}</span>
                  </div>
                </div>

                <!-- 底部操作 -->
                <div class="shop-audit-actions">
                  <div class="shop-id">ID：{{ shop.id }}</div>
                  <div class="action-btns">
                    <button class="btn-pass" @click="approveShop(shop.id)">✅ 审批通过</button>
                    <button class="btn-reject" @click="rejectShop(shop.id)">❌ 拒绝</button>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>

        <!-- ===== 用户管理 ===== -->
        <div v-if="activeMenu === 'users'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px">
            <h2 style="margin:0">用户管理</h2>
          </div>

          <div class="admin-search-bar" style="margin-bottom: 16px;">
            <input v-model="searchUserKeyword" placeholder="🔍 输入用户名或昵称..." class="admin-search-input" @keyup.enter="loadUsers" />
            <button class="btn-pass" @click="loadUsers">搜索</button>
          </div>
          <el-table :data="users" border style="margin-top:16px">
            <el-table-column prop="id" label="ID" width="60"/>
            <el-table-column prop="username" label="用户名"/>
            <el-table-column prop="nickname" label="昵称"/>
            <el-table-column prop="role" label="角色" width="80"/>
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-tag :type="scope.row.status===0?'success':'danger'">
                  {{ scope.row.status===0?'正常':'封禁' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="220">
              <template #default="scope">
                <el-button
                    size="small"
                    :type="scope.row.status===0?'danger':'success'"
                    :disabled="scope.row.role === 'admin'"
                    @click="scope.row.role === 'admin' ? null : toggleUser(scope.row)"
                >
                  {{ scope.row.status===0?'封禁':'解封' }}
                </el-button>
                <el-button
                    v-if="scope.row.role !== 'admin'"
                    size="small"
                    type="warning"
                    @click="deleteUser(scope.row)"
                >
                  删除
                </el-button>
                <span v-else style="font-size:12px;color:#bbb;margin-left:6px">管理员账号不可删除</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ===== 分类管理 ===== -->
        <div v-if="activeMenu === 'category'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
            <h2 style="margin:0">分类管理</h2>
          </div>

          <!-- 添加新分类 -->
          <div class="category-add-bar">
            <input
                v-model="newCategory"
                class="category-input"
                placeholder="输入新分类名称"
                @keyup.enter="addCategory"
            />
            <button class="btn-pass" @click="addCategory">✅ 添加分类</button>
          </div>

          <!-- 分类列表 -->
          <div class="category-list">
            <div v-for="cat in categories" :key="cat.id" class="category-item-row">

              <!-- 普通展示模式 -->
              <div v-if="editingCategoryId !== cat.id" class="category-view">
                <span class="category-id">ID: {{ cat.id }}</span>
                <span class="category-name">{{ cat.name }}</span>
                <div class="category-actions">
                  <!-- 点击修改按钮进入编辑模式 -->
                  <button class="btn-pass" @click="startEdit(cat)">✏️ 修改</button>
                  <button class="btn-reject" @click="deleteCategory(cat.id)">🗑️ 删除</button>
                </div>
              </div>

              <!-- 编辑模式 -->
              <div v-else class="category-edit">
                <span class="category-id">ID: {{ cat.id }}</span>
                <input
                    v-model="editingCategoryName"
                    class="category-input"
                    @keyup.enter="confirmEdit(cat.id)"
                />
                <div class="category-actions">
                  <!-- 确认修改 -->
                  <button class="btn-pass" @click="confirmEdit(cat.id)">✅ 确认</button>
                  <!-- 取消编辑 -->
                  <button class="btn-reject" @click="editingCategoryId = null">❌ 取消</button>
                </div>
              </div>

            </div>
          </div>
        </div>

        <!-- ===== 笔记管理 ===== -->
        <div v-if="activeMenu === 'noteList'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
            <h2 style="margin:0">笔记管理</h2>
            <span style="color:#999;font-size:14px">共 {{ allNotes.length }} 篇笔记</span>
          </div>

          <div class="admin-search-bar">
            <input v-model="searchNoteKeyword" placeholder="🔍 输入笔记标题或内容..." class="admin-search-input" @keyup.enter="loadAllNotes" />
            <button class="btn-pass" @click="loadAllNotes">搜索</button>
          </div>

          <!-- 空状态 -->
          <div v-if="allNotes.length === 0" style="text-align:center;padding:80px 0;color:#aaa">
            <div style="font-size:48px;margin-bottom:16px">📝</div>
            <p>暂无已发布笔记</p>
          </div>

          <!-- 笔记列表 -->
          <div class="note-manage-list">
            <div v-for="note in allNotes" :key="note.id" class="note-manage-item">

              <!-- 封面图，点击新标签页查看详情 -->
              <div class="note-manage-cover" @click="goNoteDetail(note.id)" style="cursor:pointer">
                <img v-if="note.images" :src="note.images.split('|||')[0]" alt="封面"/>
                <div v-else class="cover-placeholder">🍜</div>
              </div>

              <!-- 笔记信息 -->
              <div class="note-manage-info">
                <!-- 标题点击查看详情 -->
                <div class="note-manage-title" @click="goNoteDetail(note.id)" style="cursor:pointer;color:#ff6b35">
                  {{ note.title }}
                </div>
                <div class="note-manage-content">{{ note.content }}</div>
                <div class="note-manage-meta">
                  <!-- 违规屏蔽时显示红色标签 -->
                  <span v-if="note.status === 3" style="color:#ff4d4f;font-weight:600">⚠️ 已违规屏蔽</span>
                  <span>👤 {{ note.nickname }}</span>
                  <span>❤️ {{ note.likeCount || note.like_count || 0 }}</span>
                  <span>⭐ {{ note.collectCount || note.collect_count || 0 }}</span>
                  <span>🕐 {{ (note.createTime || note.create_time || '').toString().replace('T', ' ').slice(0,16) }}</span>
                </div>
              </div>

              <!-- 右侧操作区 -->
              <div class="shop-manage-actions">
                <span style="font-size:12px;color:#bbb">ID: {{ note.id }}</span>
                <!-- 查看详情按钮 -->
                <button class="btn-pass" @click="goNoteDetail(note.id)">🔍 查看</button>
                <!-- 屏蔽/解除屏蔽按钮 -->
                <button
                    :class="note.status === 3 ? 'btn-pass' : 'btn-reject'"
                    @click="adminBanNote(note)"
                >
                  {{ note.status === 3 ? '✅ 解除屏蔽' : '🚫 屏蔽' }}
                </button>
              </div>

            </div>
          </div>
        </div>

        <!-- ===== 商家管理 ===== -->
        <div v-if="activeMenu === 'shopList'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
            <h2 style="margin:0">商家管理</h2>
            <span style="color:#999;font-size:14px">共 {{ allShops.length }} 家已入驻商家</span>
          </div>

          <div class="admin-search-bar">
            <input v-model="searchShopKeyword" placeholder="🔍 输入店铺名称或地址..." class="admin-search-input" @keyup.enter="loadAllShops" />
            <button class="btn-pass" @click="loadAllShops">搜索</button>
          </div>

          <!-- 空状态 -->
          <div v-if="allShops.length === 0" style="text-align:center;padding:80px 0;color:#aaa">
            <div style="font-size:48px;margin-bottom:16px">🏪</div>
            <p>暂无已入驻商家</p>
          </div>

          <!-- 商家列表 -->
          <div class="shop-manage-list">
            <div v-for="shop in allShops" :key="shop.id" class="shop-manage-item">

              <!-- 店铺封面图 -->
              <div class="shop-manage-cover">
                <img
                    v-if="shop.shopImages || shop.shop_images"
                    :src="(shop.shopImages || shop.shop_images).split('|||')[0]"
                    alt="店铺照片"
                />
                <div v-else class="cover-placeholder">🏪</div>
              </div>

              <!-- 店铺信息 -->
              <div class="shop-manage-info">
                <!-- 店铺名称 -->
                <div class="shop-manage-name">{{ shop.name }}</div>
                <div class="shop-manage-meta">
                  <span>📍 {{ shop.address }}</span>
                  <span>📞 {{ shop.phone }}</span>
                </div>
                <div class="shop-manage-meta">
                  <!-- 优先显示法人名称，没有则显示商家用户昵称 -->
                  <span>👔 法人：{{ shop.legalName || shop.legal_name || '未填写' }}</span>
                  <span>👤 账号：{{ shop.nickname }}</span>
                  <span v-if="shop.businessHours || shop.business_hours">
            🕐 {{ shop.businessHours || shop.business_hours }}
          </span>
                </div>
              </div>

              <!-- 右侧：状态标签 + 操作按钮 -->
              <div class="shop-manage-actions">
                <!-- 当前状态标签 -->
                <span :class="['shop-status-tag', shop.status === 2 ? 'banned' : 'normal']">
          {{ shop.status === 2 ? '已封禁' : '正常' }}
        </span>
                <!-- 封禁/解封按钮 -->
                <button
                    :class="['shop-ban-btn', shop.status === 2 ? 'btn-unban' : 'btn-ban']"
                    @click="toggleShopBan(shop)"
                >
                  {{ shop.status === 2 ? '解封' : '封禁' }}
                </button>
              </div>

            </div>
          </div>
        </div>

        <div v-if="activeMenu === 'sensitiveWords'">
          <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:20px">
            <h2 style="margin:0">🛡️ 敏感词与风控管理</h2>
          </div>

          <div class="category-add-bar">
            <input
                v-model="newWord"
                class="category-input"
                placeholder="输入新的违禁词..."
                @keyup.enter="addWord"
            />
            <button class="btn-pass" @click="addWord">⚡ 立刻添加并热更新</button>
          </div>

          <div class="category-list">
            <div v-for="word in sensitiveWords" :key="word.id" class="category-item-row">
              <div class="category-view">
                <span class="category-id">ID: {{ word.id }}</span>
                <span class="category-name" style="color: #ff4d4f;">{{ word.word }}</span>
                <div class="category-actions">
                  <button class="btn-reject" @click="deleteWord(word.id)">🗑️ 移除</button>
                </div>
              </div>
            </div>
            <div v-if="sensitiveWords.length === 0" style="text-align:center;padding:60px 0;color:#aaa">
              <div style="font-size:48px;margin-bottom:16px">✨</div>
              <p>当前风控词库极其纯净</p>
            </div>
          </div>
        </div>

      </div>
    </div>

    <!-- 驳回原因弹窗 -->
    <el-dialog v-model="rejectDialog" title="填写驳回原因" width="400px">
      <el-input v-model="rejectReason" placeholder="请输入驳回原因" type="textarea"/>
      <template #footer>
        <el-button @click="rejectDialog=false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: 'AdminView',
  data() {
    return {
      // 从本地存储获取当前登录用户信息
      user: JSON.parse(localStorage.getItem('user') || '{}'),
      activeMenu: 'dashboard',  // 当前激活的菜单项
      // 数据概览统计数据
      stats: { userCount: 0, shopCount: 0, noteCount: 0, pendingCount: 0 },
      notes: [],       // 待审核笔记列表
      users: [],       // 用户列表
      categories: [],  // 分类列表
      shops: [],       // 待审批商家列表
      newCategory: '', // 新分类名称输入
      editingCategoryId: null,   // 当前正在编辑的分类ID
      editingCategoryName: '',   // 编辑中的分类名称
      rejectDialog: false,   // 驳回弹窗是否显示
      rejectReason: '',      // 驳回原因
      rejectNoteId: null,    // 当前要驳回的笔记ID
      currentImageIndex: {}, // 每篇笔记当前显示的图片下标
      expandedNotes: {},     // 每篇笔记是否展开全文
      shopImgIndex: {},// 记录商家审批列表中每家店铺当前展示的照片下标，key是店铺id，value是当前显示的图片序号（从0开始）
      allNotes: [],   // 所有已发布笔记列表
      allShops: [],   // 所有已入驻商家列表
      searchUserKeyword: '', // 用户搜索词
      searchShopKeyword: '', // 商家搜索词
      searchNoteKeyword: '', // 笔记搜索词
      sensitiveWords: [],    // 敏感词列表数据
      newWord: '',           // 输入框绑定的新敏感词
    }
  },
  mounted() {
    // 检查是否是管理员，不是则跳回登录页
    if (!this.user || this.user.role !== 'admin') {
      this.$router.push('/login')
      return
    }
    // 如果URL带了tab参数，自动切换到对应菜单
    if (this.$route.query.tab) {
      this.activeMenu = this.$route.query.tab
      // 根据tab加载对应数据
      if (this.$route.query.tab === 'noteList') this.loadAllNotes()
      if (this.$route.query.tab === 'shopList') this.loadAllShops()
      if (this.$route.query.tab === 'notes') this.loadNotes()
      if (this.$route.query.tab === 'users') this.loadUsers()
      if (this.$route.query.tab === 'sensitiveWords') this.loadSensitiveWords()
    } else {
      this.loadStats()
    }
  },
  methods: {
    // 点击菜单时切换内容区并加载对应数据
    handleSelect(key) {
      this.activeMenu = key
      if (key === 'notes') this.loadNotes()
      if (key === 'users') this.loadUsers()
      if (key === 'category') this.loadCategories()
      if (key === 'shops') this.loadShops()
    },

    // 加载数据概览统计
    async loadStats() {
      const res = await this.$axios.get('/admin/stats')
      if (res.data.code === 200) this.stats = res.data.data
    },

    // 加载待审核笔记列表
    async loadNotes() {
      const res = await this.$axios.get('/admin/notes/pending')
      if (res.data.code === 200) this.notes = res.data.data
    },

    // 加载用户列表
    async loadUsers() {
      const res = await this.$axios.get('/admin/users', { params: { keyword: this.searchUserKeyword } })
      if (res.data.code === 200) this.users = res.data.data
    },

    // 加载分类列表
    async loadCategories() {
      const res = await this.$axios.get('/category/list')
      if (res.data.code === 200) this.categories = res.data.data
    },

    // 加载待审批商家列表
    async loadShops() {
      const res = await this.$axios.get('/admin/shops/pending')
      if (res.data.code === 200) this.shops = res.data.data
    },

    // 审核通过笔记（status=1表示通过）
    async auditNote(id, status) {
      await this.$axios.post('/admin/notes/audit', { id, status, rejectReason: '' })
      this.$message.success('审核通过')
      this.loadNotes()
      this.loadStats()
    },

    // 显示驳回弹窗
    showReject(id) {
      this.rejectNoteId = id
      this.rejectReason = ''
      this.rejectDialog = true
    },

    // 确认驳回（status=2表示驳回）
    async confirmReject() {
      if (!this.rejectReason) {
        this.$message.warning('请填写驳回原因')
        return
      }
      await this.$axios.post('/admin/notes/audit', {
        id: this.rejectNoteId,
        status: 2,
        rejectReason: this.rejectReason
      })
      this.$message.success('已驳回')
      this.rejectDialog = false
      this.loadNotes()
    },

    // 封禁/解封用户
    async toggleUser(row) {
      if (row.role === 'admin') {
        this.$message.warning('管理员账号不允许封禁')
        return
      }
      const newStatus = row.status === 0 ? 1 : 0
      await this.$axios.post('/admin/users/status', { id: row.id, status: newStatus })
      this.$message.success('操作成功')
      this.loadUsers()
    },

    // 删除用户：管理员确认后执行，后端会联动清理关联数据
    async deleteUser(row) {
      if (row.role === 'admin') {
        this.$message.warning('管理员账号不允许删除')
        return
      }
      const ok = confirm(`确认删除用户“${row.username}”吗？\n此操作无法撤销！`)
      if (!ok) return
      const res = await this.$axios.post('/admin/users/delete', { id: row.id })
      if (res.data.code === 200) {
        this.$message.success('删除成功')
        this.loadUsers()
        this.loadStats()
      } else {
        this.$message.error(res.data.message)
      }
    },

    // 添加分类
    async addCategory() {
      if (!this.newCategory) return
      await this.$axios.post('/category/add', { name: this.newCategory })
      this.$message.success('添加成功')
      this.newCategory = ''
      this.loadCategories()
    },

    // 删除分类
    async deleteCategory(id) {
      await this.$axios.post('/category/delete', { id })
      this.$message.success('删除成功')
      this.loadCategories()
    },

    // 点击修改按钮，进入编辑模式
    startEdit(cat) {
      this.editingCategoryId = cat.id
      this.editingCategoryName = cat.name
    },

// 确认修改分类名称
    async confirmEdit(id) {
      if (!this.editingCategoryName.trim()) {
        this.$message.warning('分类名称不能为空')
        return
      }
      const res = await this.$axios.post('/category/update', {
        id,
        name: this.editingCategoryName
      })
      if (res.data.code === 200) {
        this.$message.success('修改成功')
        // 退出编辑模式
        this.editingCategoryId = null
        // 重新加载分类列表
        this.loadCategories()
      }
    },

    // 审批商家通过
    async approveShop(id) {
      await this.$axios.post('/admin/shops/approve', { id })
      this.$message.success('审批通过')
      this.loadShops()
    },

    // 切换笔记图片（多图时）
    switchImage(noteId, idx) {
      this.currentImageIndex = { ...this.currentImageIndex, [noteId]: idx }
    },

    // 展开/收起笔记正文
    toggleExpand(noteId) {
      this.expandedNotes = { ...this.expandedNotes, [noteId]: !this.expandedNotes[noteId] }
    },

    // 拒绝商家入驻申请
    // 调用后端接口将该店铺状态改为-1（拒绝）
    // 操作完成后刷新商家列表
    async rejectShop(id) {
      // 发送拒绝请求，传入店铺id
      await this.$axios.post('/admin/shops/reject', { id })
      // 提示操作成功
      this.$message.success('已拒绝该商家申请')
      // 重新加载待审批商家列表
      this.loadShops()
    },

    // 加载所有已发布笔记列表
    async loadAllNotes() {
      const res = await this.$axios.get('/note/admin/all', { params: { keyword: this.searchNoteKeyword } })
      if (res.data.code === 200) this.allNotes = res.data.data
    },

    // 跳转到笔记详情，携带from=admin和tab=noteList参数，方便返回时回到笔记管理页
    goNoteDetail(id) {
      this.$router.push('/note/' + id + '?from=admin&tab=noteList')
    },

// 管理员屏蔽/解除屏蔽笔记
    async adminBanNote(note) {
      // status=3表示违规屏蔽，status=1表示恢复正常
      const newStatus = note.status === 3 ? 1 : 3
      const action = newStatus === 3 ? '屏蔽' : '解除屏蔽'
      if (!confirm(`确定要${action}这篇笔记吗？`)) return
      const res = await this.$axios.post('/admin/note/ban', { id: note.id, status: newStatus })
      if (res.data.code === 200) {
        this.$message.success(`${action}成功`)
        // 重新加载笔记列表刷新状态
        this.loadAllNotes()
      }
    },

// 加载所有已入驻商家列表
    async loadAllShops() {
      const res = await this.$axios.get('/admin/shops/all', { params: { keyword: this.searchShopKeyword } })
      if (res.data.code === 200) this.allShops = res.data.data
    },

    // 封禁/解封商家
    async toggleShopBan(shop) {
      // status=2表示封禁，status=1表示正常
      const newStatus = shop.status === 2 ? 1 : 2
      await this.$axios.post('/admin/shops/ban', { id: shop.id, status: newStatus })
      this.$message.success(newStatus === 2 ? '已封禁该商家' : '已解封该商家')
      // 重新加载商家列表刷新状态
      this.loadAllShops()
    },

    // ==================== 敏感词风控管理 ====================
    // 拉取敏感词列表
    async loadSensitiveWords() {
      const res = await this.$axios.get('/admin/sensitive-word/list')
      if (res.data.code === 200) {
        this.sensitiveWords = res.data.data
      }
    },

    // 添加敏感词并触发热更新
    async addWord() {
      if (!this.newWord.trim()) {
        this.$message.warning('请填写违禁词')
        return
      }
      const res = await this.$axios.post('/admin/sensitive-word/add', { word: this.newWord })
      if (res.data.code === 200) {
        this.$message.success(res.data.message) // 弹出炫酷的热更新成功提示
        this.newWord = ''
        this.loadSensitiveWords() // 刷新列表
      } else {
        this.$message.error(res.data.message)
      }
    },

    // 删除敏感词
    async deleteWord(id) {
      if (!confirm('确定要将该词移出风控词库吗？')) return
      const res = await this.$axios.post('/admin/sensitive-word/delete', { id })
      if (res.data.code === 200) {
        this.$message.success(res.data.message)
        this.loadSensitiveWords() // 刷新列表
      }
    },

    // 退出登录
    logout() {
      localStorage.removeItem('user')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.admin-layout { display: flex; height: 100vh; }

/* ===== 左侧导航栏 ===== */
.sidebar {
  width: 220px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: sticky;
  top: 0;
}

/* Logo区域 */
.sidebar-logo {
  padding: 28px 20px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-icon { font-size: 36px; margin-bottom: 8px; }
.logo-text {
  font-size: 18px;
  font-weight: 800;
  color: white;
  letter-spacing: 2px;
}
.logo-sub {
  font-size: 11px;
  color: rgba(255,255,255,0.4);
  margin-top: 2px;
  letter-spacing: 1px;
}

/* 菜单区域 */
.sidebar-menu {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 单个菜单项 */
.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
  color: rgba(255,255,255,0.55);
  font-size: 14px;
}
.menu-item:hover {
  background: rgba(255,255,255,0.08);
  color: rgba(255,255,255,0.9);
}
/* 选中状态：橙色渐变高亮 */
.menu-item.active {
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255,107,53,0.4);
}
.menu-icon { font-size: 18px; flex-shrink: 0; }
.menu-text { flex: 1; }

/* 待审核数量角标 */
.menu-badge {
  background: #ff4d4f;
  color: white;
  font-size: 11px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

/* 底部管理员信息区 */
.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid rgba(255,255,255,0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.admin-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
/* 管理员头像圆圈 */
.admin-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  font-weight: 700;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.admin-name { font-size: 13px; color: white; font-weight: 600; }
.admin-role { font-size: 11px; color: rgba(255,255,255,0.4); margin-top: 2px; }

/* 退出登录按钮 */
.logout-btn {
  width: 100%;
  padding: 10px;
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 10px;
  background: transparent;
  color: rgba(255,255,255,0.6);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.logout-btn:hover {
  background: rgba(255,77,79,0.2);
  border-color: #ff4d4f;
  color: #ff4d4f;
}

/* ===== 右侧主区域 ===== */
.main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

/* 顶部栏 */
.topbar {
  height: 50px;
  background: white;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
  gap: 12px;
}

/* 内容区 */
.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f5f7fa;
}
.content h2 { margin-bottom: 8px; color: #333; }

/* ===== 数据概览样式 ===== */

/* 页面标题区 */
.overview-header {
  margin-bottom: 24px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.overview-title { margin: 0; color: #333; font-size: 22px; }
.overview-sub { font-size: 13px; color: #aaa; }

/* 统计卡片行 */
.stat-cards { display: flex; gap: 16px; margin-bottom: 32px; }

/* 单个统计卡片 */
.stat-card {
  flex: 1;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  color: white;
}
.stat-icon { font-size: 36px; }
.stat-num { font-size: 32px; font-weight: 800; line-height: 1; }
.stat-label { font-size: 13px; margin-top: 4px; opacity: 0.85; }

/* 四种颜色主题 */
.card-blue   { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.card-green  { background: linear-gradient(135deg, #11998e, #38ef7d); }
.card-orange { background: linear-gradient(135deg, #ff6b35, #f7931e); }
.card-red    { background: linear-gradient(135deg, #ff6b6b, #ee0979); }

/* 快捷操作区 */
.quick-actions { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.section-title { font-size: 16px; font-weight: 700; color: #333; margin-bottom: 16px; }
.action-cards { display: flex; flex-direction: column; gap: 12px; }

/* 单个快捷操作卡片 */
.action-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #eee;
}
.action-card:hover {
  background: #fff3ee;
  border-color: #ff6b35;
  transform: translateX(4px);
}
.action-icon { font-size: 28px; flex-shrink: 0; }
.action-text { flex: 1; }
.action-name { font-size: 15px; font-weight: 600; color: #333; }
.action-desc { font-size: 13px; color: #aaa; margin-top: 2px; }
.action-arrow { font-size: 18px; color: #ccc; }

/* ===== 笔记审核卡片样式 ===== */
.audit-card-list { display: flex; flex-direction: column; gap: 20px; }

/* 单张审核卡片：左图右文布局 */
.audit-card {
  display: flex;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid #eee;
}

/* 左侧图片区域 */
.audit-card-images {
  width: 280px;
  flex-shrink: 0;
  position: relative;
  background: #f5f5f5;
}
.image-viewer { width: 100%; height: 100%; position: relative; }
.main-image { width: 100%; height: 220px; object-fit: cover; display: block; }

/* 多图切换小圆点 */
.image-nav {
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}
.img-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  border: none;
  background: rgba(255,255,255,0.5);
  cursor: pointer;
  padding: 0;
}
.img-dot.active { background: white; }

/* 图片计数角标 */
.image-count {
  position: absolute;
  top: 10px; right: 10px;
  background: rgba(0,0,0,0.5);
  color: white;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 10px;
}

/* 无图片占位区域 */
.no-image {
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 13px;
  gap: 8px;
}
.no-image span { font-size: 40px; }

/* 右侧内容区域 */
.audit-card-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 顶部作者信息行 */
.audit-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.audit-author { display: flex; align-items: center; gap: 10px; }
.audit-avatar {
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
.audit-nickname { font-size: 14px; font-weight: 600; color: #333; }
.audit-time { font-size: 12px; color: #aaa; margin-top: 2px; }
.audit-score { font-size: 14px; }

/* 笔记标题 */
.audit-title { font-size: 18px; font-weight: 700; color: #222; }

/* 笔记正文：默认显示3行，展开后显示全部 */
.audit-content {
  font-size: 14px;
  color: #555;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.audit-content.expanded { display: block; }
.expand-btn { color: #ff6b35; font-size: 13px; cursor: pointer; }

/* 底部操作区 */
.audit-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
.audit-id { font-size: 12px; color: #bbb; }
.action-btns { display: flex; gap: 12px; }

/* 通过按钮 */
.btn-pass {
  background: linear-gradient(135deg, #11998e, #38ef7d);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 24px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(17,153,142,0.3);
  transition: transform 0.2s;
}
.btn-pass:hover { transform: translateY(-1px); }

/* 驳回按钮 */
.btn-reject {
  background: linear-gradient(135deg, #ff6b6b, #ee0979);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 8px 24px;
  font-size: 14px;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(238,9,121,0.3);
  transition: transform 0.2s;
}
.btn-reject:hover { transform: translateY(-1px); }
/* ===== 商家审批卡片样式 ===== */
.shop-audit-list { display: flex; flex-direction: column; gap: 16px; }

.shop-audit-card {
  display: flex;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid #eee;
}

.shop-audit-images {
  width: 240px; flex-shrink: 0;
  position: relative; background: #f5f5f5;
}
.shop-main-img { width: 100%; height: 200px; object-fit: cover; display: block; }
.no-shop-img {
  height: 200px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: #ccc; gap: 8px;
}
.no-shop-img span { font-size: 40px; }

.shop-audit-content { flex: 1; padding: 20px; display: flex; flex-direction: column; gap: 12px; }
.shop-audit-header { display: flex; align-items: center; justify-content: space-between; }
.shop-audit-name { font-size: 20px; font-weight: 700; color: #222; }
.shop-audit-time { font-size: 12px; color: #aaa; }

.shop-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.info-item { display: flex; align-items: center; gap: 8px; }
.info-label { font-size: 13px; color: #aaa; white-space: nowrap; }
.info-value { font-size: 13px; color: #333; font-weight: 500; }
.missing { color: #ff4d4f !important; }

.shop-audit-actions {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: auto; padding-top: 12px; border-top: 1px solid #f0f0f0;
}
.shop-id { font-size: 12px; color: #bbb; }
/* ===== 笔记管理和商家管理列表样式 ===== */
.note-manage-list, .shop-manage-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 单条笔记/商家管理行 */
.note-manage-item, .shop-manage-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #eee;
}

/* 封面图 */
.note-manage-cover, .shop-manage-cover {
  width: 80px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}
.note-manage-cover img, .shop-manage-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 封面占位符 */
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

/* 笔记/商家信息区 */
.note-manage-info, .shop-manage-info { flex: 1; min-width: 0; }
.note-manage-title, .shop-manage-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}
.note-manage-content {
  font-size: 13px;
  color: #888;
  margin-bottom: 6px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.note-manage-meta, .shop-manage-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #aaa;
}

/* ID标签 */
.note-manage-id {
  font-size: 12px;
  color: #bbb;
  flex-shrink: 0;
}

/* 商家管理右侧操作区 */
.shop-manage-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 商家状态标签 */
.shop-status-tag {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 600;
}
.shop-status-tag.normal { background: #f6ffed; color: #52c41a; }
.shop-status-tag.banned { background: #fff1f0; color: #ff4d4f; }

/* 封禁/解封按钮 */
.shop-ban-btn {
  border: none;
  border-radius: 16px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 600;
  transition: opacity 0.2s;
}
.shop-ban-btn:hover { opacity: 0.85; }
.btn-ban { background: #ff4d4f; color: white; }
.btn-unban { background: #52c41a; color: white; }

/* 后台通用搜索栏 */
.admin-search-bar { display: flex; gap: 12px; margin-bottom: 20px; }
.admin-search-input {
  flex: 1; max-width: 300px; border: 2px solid #eee; border-radius: 10px;
  padding: 8px 16px; font-size: 14px; outline: none; transition: all 0.3s;
}
.admin-search-input:focus { border-color: #ff6b35; }

/* ===== 分类管理样式 ===== */
.category-add-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

/* 分类输入框 */
.category-input {
  border: 2px solid #eee;
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s;
  width: 220px;
}
.category-input:focus { border-color: #ff6b35; }

/* 分类列表 */
.category-list { display: flex; flex-direction: column; gap: 10px; }

/* 单行分类 */
.category-item-row {
  background: white;
  border-radius: 12px;
  padding: 14px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border: 1px solid #eee;
}

/* 普通展示模式 */
.category-view {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 编辑模式 */
.category-edit {
  display: flex;
  align-items: center;
  gap: 16px;
}

.category-id { font-size: 12px; color: #bbb; width: 50px; flex-shrink: 0; }
.category-name { flex: 1; font-size: 15px; font-weight: 600; color: #333; }
.category-actions { display: flex; gap: 10px; flex-shrink: 0; }
</style>

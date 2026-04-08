<template>
  <!-- 登录页面最外层容器 -->
  <div class="login-page">

    <!-- 背景装饰圆圈（纯视觉效果） -->
    <div class="bg-circle circle1"></div>
    <div class="bg-circle circle2"></div>
    <div class="bg-circle circle3"></div>

    <!-- 页面主体内容 -->
    <div class="login-wrapper">

      <!-- 左侧品牌介绍区域 -->
      <div class="brand-panel">
        <!-- Logo区域 -->
        <div class="brand-logo">
          <span class="logo-icon">🍜</span>
          <span class="logo-text">觅食</span>
        </div>
        <!-- 主标题 -->
        <h1 class="brand-title">发现城市里<br/>被遗忘的美味</h1>
        <!-- 副标题 -->
        <p class="brand-desc">真实探店笔记 · 本地特色美食 · 用户口碑推荐</p>
        <!-- 三个特色标签 -->
        <div class="brand-tags">
          <span class="tag">📍 附近美食地图</span>
          <span class="tag">✍️ 探店笔记分享</span>
          <span class="tag">⭐ 真实口碑评价</span>
        </div>
        <!-- 底部数据展示 -->
        <div class="brand-stats">
          <div class="stat">
            <div class="stat-num">10,000+</div>
            <div class="stat-label">探店笔记</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <div class="stat-num">500+</div>
            <div class="stat-label">特色店铺</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <div class="stat-num">5,000+</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="form-panel">
        <div class="form-card">
          <!-- 表单顶部标题 -->
          <div class="form-header">
            <h2>{{ activeTab === 'login' ? '欢迎回来 👋' : '加入我们 🎉' }}</h2>
            <p>{{ activeTab === 'login' ? '登录后探索身边的美食世界' : '注册账号，开始你的美食之旅' }}</p>
          </div>

          <!-- 登录/注册切换按钮 -->
          <div class="tab-switcher">
            <button
                :class="['tab-btn', activeTab === 'login' ? 'active' : '']"
                @click="activeTab = 'login'"
            >登录</button>
            <button
                :class="['tab-btn', activeTab === 'register' ? 'active' : '']"
                @click="activeTab = 'register'"
            >注册</button>
          </div>

          <!-- 登录表单 -->
          <div v-if="activeTab === 'login'" class="form-body">
            <!-- 用户名输入框 -->
            <div class="input-group">
              <label>用户名</label>
              <div class="input-wrap">
                <span class="input-icon">👤</span>
                <input
                    v-model="loginForm.username"
                    type="text"
                    placeholder="请输入用户名"
                    @keyup.enter="doLogin"
                />
              </div>
            </div>
            <!-- 密码输入框 -->
            <div class="input-group">
              <label>密码</label>
              <div class="input-wrap">
                <span class="input-icon">🔒</span>
                <input
                    v-model="loginForm.password"
                    :type="showPwd ? 'text' : 'password'"
                    placeholder="请输入密码"
                    @keyup.enter="doLogin"
                />
                <!-- 显示/隐藏密码切换 -->
                <span class="pwd-toggle" @click="showPwd = !showPwd">
                  {{ showPwd ? '🙈' : '👁️' }}
                </span>
              </div>
            </div>
            <!-- 登录按钮 -->
            <div v-if="loginError" class="login-error-box">
              <div style="font-weight: bold; margin-bottom: 4px;">🚫 登录失败</div>
              <div>{{ loginError }}</div>
              <div style="margin-top: 6px; font-size: 12px; color: #666;">
                申诉邮箱：<span style="color: #ff6b35; user-select: text;">15154797270@163.com</span>
              </div>
            </div>

            <button class="submit-btn" :class="{ loading }" @click="doLogin">
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </button>
            <!-- 切换到注册 -->
            <p class="switch-tip">
              还没有账号？
              <span class="switch-link" @click="activeTab = 'register'">立即注册</span>
            </p>
          </div>

          <!-- 注册表单 -->
          <div v-if="activeTab === 'register'" class="form-body">
            <!-- 用户名 -->
            <div class="input-group">
              <label>用户名 <span class="tip">（用于登录，不对外展示）</span></label>
              <div class="input-wrap">
                <span class="input-icon">👤</span>
                <input v-model="registerForm.username" type="text" placeholder="设置登录用户名"/>
              </div>
            </div>
            <!-- 昵称 -->
            <div class="input-group">
              <label>昵称 <span class="tip">（展示给其他用户看）</span></label>
              <div class="input-wrap">
                <span class="input-icon">✨</span>
                <input v-model="registerForm.nickname" type="text" placeholder="设置你的昵称"/>
              </div>
            </div>
            <!-- 密码 -->
            <div class="input-group">
              <label>密码</label>
              <div class="input-wrap">
                <span class="input-icon">🔒</span>
                <input
                    v-model="registerForm.password"
                    :type="showPwd ? 'text' : 'password'"
                    placeholder="设置登录密码"
                />
                <span class="pwd-toggle" @click="showPwd = !showPwd">
                  {{ showPwd ? '🙈' : '👁️' }}
                </span>
              </div>
            </div>
            <!-- 注册按钮 -->
            <button class="submit-btn green" :class="{ loading }" @click="doRegister">
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </button>
            <!-- 切换到登录 -->
            <p class="switch-tip">
              已有账号？
              <span class="switch-link" @click="activeTab = 'login'">去登录</span>
            </p>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginView',
  data() {
    return {
      activeTab: 'login',   // 当前激活的标签：login=登录，register=注册
      showPwd: false,       // 是否显示密码明文
      loading: false,       // 按钮加载状态，防止重复点击
      loginError: '',       // 用于在页面上永久显示的登录错误信息
      // 登录表单数据
      loginForm: {
        username: '',
        password: ''
      },
      // 注册表单数据
      registerForm: {
        username: '',
        nickname: '',
        password: ''
      }
    }
  },
  methods: {
    // 登录：校验输入 → 调后端接口 → 根据角色跳转不同页面
    async doLogin() {
      this.loginError = '' //保证每次点击登录前清空旧的报错
      if (!this.loginForm.username || !this.loginForm.password) {
        this.$message.warning('请输入用户名和密码')
        return
      }
      this.loading = true
      try {
        const res = await this.$axios.post('/user/login', this.loginForm)
        if (res.data.code === 200) {
          // 提取token和用户信息
          const { token, userInfo } = res.data.data

          // 保存token到localStorage,用于后续请求验证身份
          localStorage.setItem('token', token)

          // 保存用户信息到localStorage,供页面显示使用
          localStorage.setItem('user', JSON.stringify(userInfo))

          // 保存到Vuex store
          this.$store.commit('setUser', userInfo)

          this.$message.success('登录成功！')

          // 根据角色跳转到不同页面
          const role = userInfo.role
          if (role === 'admin') this.$router.push('/admin')
          else if (role === 'shop') this.$router.push('/merchant')
          else this.$router.push('/home')
        } else {
          // 如果是封禁，拦截掉 Vue 的短暂 Toast，改在页面上永久显示
          if (res.data.message.includes('封禁')) {
            this.loginError = '该账号因违规已被限制登录，如有疑问请联系管理员。';
          } else {
            // 密码错误等普通情况，依然用气泡提示
            this.$message.error(res.data.message);
          }
        }
      } catch (e) {
        this.$message.error('网络错误，请稍后重试')
      }
      this.loading = false
    },

    // 注册：校验输入 → 调后端接口 → 成功后切换到登录标签
    async doRegister() {
      if (!this.registerForm.username || !this.registerForm.password || !this.registerForm.nickname) {
        this.$message.warning('请填写完整信息')
        return
      }
      this.loading = true
      try {
        const res = await this.$axios.post('/user/register', this.registerForm)
        if (res.data.code === 200) {
          this.$message.success('注册成功，请登录！')
          this.activeTab = 'login'
          // 注册成功后把用户名填入登录框，方便用户直接登录
          this.loginForm.username = this.registerForm.username
        } else {
          this.$message.error(res.data.message)
        }
      } catch (e) {
        this.$message.error('网络错误，请稍后重试')
      }
      this.loading = false
    }
  }
}
</script>

<style scoped>
/* ===== 页面基础 ===== */
.login-page {
  min-height: 100vh;
  background: #0f0c29;
  background: linear-gradient(135deg, #0f0c29, #302b63, #24243e);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰圆圈 ===== */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}
.circle1 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, #ff6b6b, transparent);
  top: -150px; left: -100px;
}
.circle2 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, #ffd93d, transparent);
  bottom: -100px; right: 200px;
}
.circle3 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, #6bceff, transparent);
  top: 50%; right: -50px;
}

/* ===== 主体布局：左右两栏 ===== */
.login-wrapper {
  display: flex;
  align-items: center;
  gap: 80px;
  z-index: 1;
  padding: 40px;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  color: white;
  max-width: 420px;
}

/* Logo：图标+文字 */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
}
.logo-icon { font-size: 36px; }
.logo-text {
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #ffd93d, #ff6b6b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 主标题 */
.brand-title {
  font-size: 44px;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 16px;
  text-shadow: 0 2px 20px rgba(0,0,0,0.3);
}

/* 副标题 */
.brand-desc {
  font-size: 15px;
  opacity: 0.7;
  margin-bottom: 32px;
  letter-spacing: 1px;
}

/* 特色标签 */
.brand-tags {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 40px;
}
.tag {
  display: inline-block;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.15);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  padding: 10px 20px;
  font-size: 14px;
  width: fit-content;
}

/* 底部统计数据 */
.brand-stats {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255,255,255,0.08);
  border-radius: 16px;
  padding: 20px 28px;
  border: 1px solid rgba(255,255,255,0.1);
}
.stat { text-align: center; }
.stat-num {
  font-size: 22px;
  font-weight: 800;
  color: #ffd93d;
}
.stat-label { font-size: 12px; opacity: 0.6; margin-top: 4px; }
.stat-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.2); }

/* ===== 右侧表单区 ===== */
.form-panel {
  width: 420px;
}

/* 卡片容器 */
.form-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.3);
}

/* 表单顶部标题 */
.form-header { margin-bottom: 28px; }
.form-header h2 {
  font-size: 26px;
  color: #1a1a2e;
  margin-bottom: 6px;
}
.form-header p { color: #888; font-size: 14px; }

/* 登录/注册切换按钮 */
.tab-switcher {
  display: flex;
  background: #f0f0f5;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;
}
.tab-btn {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-size: 15px;
  color: #888;
  cursor: pointer;
  transition: all 0.3s;
}
.tab-btn.active {
  background: white;
  color: #ff6b35;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* 输入框组 */
.input-group { margin-bottom: 20px; }
.input-group label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 8px;
  font-weight: 600;
}
.tip { color: #aaa; font-weight: normal; }

/* 输入框包裹层 */
.input-wrap {
  display: flex;
  align-items: center;
  background: #f8f8fc;
  border: 2px solid #eee;
  border-radius: 12px;
  padding: 0 16px;
  transition: border-color 0.3s;
}
.input-wrap:focus-within {
  border-color: #ff6b35;
  background: white;
}
.input-icon { font-size: 16px; margin-right: 10px; }
.input-wrap input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 14px 0;
  font-size: 15px;
  color: #333;
  outline: none;
}
.pwd-toggle { cursor: pointer; font-size: 16px; padding: 4px; }

/* 账号异常提示框样式（带震动提示效果） */
.login-error-box {
  background-color: #fff1f0;
  border: 1px solid #ffa39e;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 20px;
  color: #f5222d;
  font-size: 13px;
  line-height: 1.5;
  animation: shake 0.4s ease-in-out;
}
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff6b35, #f7931e);
  color: white;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  margin-top: 8px;
  letter-spacing: 4px;
  transition: all 0.3s;
  box-shadow: 0 4px 16px rgba(255,107,53,0.4);
}
.submit-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(255,107,53,0.5); }
.submit-btn:active { transform: translateY(0); }
.submit-btn.green {
  background: linear-gradient(135deg, #11998e, #38ef7d);
  box-shadow: 0 4px 16px rgba(17,153,142,0.4);
}
.submit-btn.loading { opacity: 0.7; cursor: not-allowed; }

/* 切换提示文字 */
.switch-tip {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 14px;
}
.switch-link {
  color: #ff6b35;
  cursor: pointer;
  font-weight: 600;
}
.switch-link:hover { text-decoration: underline; }
</style>

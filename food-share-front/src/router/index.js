import { createRouter, createWebHashHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const routes = [
  // 默认跳转登录页
  { path: '/', redirect: '/login' },
  // 登录页
  { path: '/login', component: LoginView },
  // 用户首页（懒加载：只有访问时才加载组件，加快初始速度）
  { path: '/home', component: () => import('../views/HomeView.vue') },
  // 管理员后台
  { path: '/admin', component: () => import('../views/AdminView.vue') },
  // 发布页
  { path: '/publish', component: () => import('../views/PublishView.vue') },
  // 笔记详情页
  { path: '/note/:id', component: () => import('../views/NoteDetailView.vue') },
  // 用户个人中心页
  { path: '/profile', component: () => import('../views/ProfileView.vue') },
  // 商家中心页
  { path: '/merchant', component: () => import('../views/MerchantView.vue') },
  // 店铺详情页
  { path: '/shop/:id', component: () => import('../views/ShopDetailView.vue') },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  // 捕获并恢复历史记录栈中的原生滚动条坐标
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  }
})

// 全局路由前置守卫：拦截非法访问与越权跳转
router.beforeEach((to, from, next) => {
  // 1. 若目标路径为登录页，无需鉴权，直接放行
  if (to.path === '/login') {
    return next();
  }

  // 2. 提取本地会话中的用户凭证
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    // 凭证缺失：拦截并强制重定向至登录页
    return next('/login');
  }

  const user = JSON.parse(userStr);

  // 3. RBAC 前端视图层权限校验
  // 拦截非 admin 角色访问管理员后台
  if (to.path === '/admin' && user.role !== 'admin') {
    alert('越权访问：无管理员后台权限');
    return next('/home');
  }

  // 拦截普通用户访问商家中心 (仅允许 shop 角色或 admin 巡查进入)
  if (to.path === '/merchant' && user.role !== 'shop' && user.role !== 'admin') {
    alert('越权访问：仅限入驻商家访问');
    return next('/home');
  }

  // 4. 权限校验通过，放行路由请求
  next();
})
export default router
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ElMessage } from 'element-plus'

// 引入 Axios
import axios from 'axios'

const app = createApp(App)

// ========== Axios配置 ==========

// 配置 axios 基础地址（后端地址）
axios.defaults.baseURL = 'http://localhost:8080'

// 请求拦截器:每次发送请求前,自动在Header中添加JWT token
axios.interceptors.request.use(
    (config) => {
        // 从localStorage中获取token
        const token = localStorage.getItem('token')

        // 如果token存在,添加到请求头的Authorization字段
        // 格式: Authorization: Bearer {token}
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器:处理后端返回的错误
let bannedHandling = false
axios.interceptors.response.use(
    (response) => {
        const data = response && response.data ? response.data : null

        // 处理账号被封禁的情况
        if (
            data &&
            data.code !== 200 &&
            typeof data.message === 'string' &&
            data.message.includes('封禁')
        ) {
            if (!bannedHandling) {
                bannedHandling = true
                localStorage.removeItem('user')
                localStorage.removeItem('token')
                ElMessage.error('此账号已被封禁，请联系管理员')
                if (window.location.hash !== '#/login') {
                    router.push('/login')
                }
                setTimeout(() => { bannedHandling = false }, 1200)
            }
        }

        return response
    },
    (error) => {
        // 处理401错误:token无效或过期
        if (error.response && error.response.status === 401) {
            // 清除本地存储的token和用户信息
            localStorage.removeItem('token')
            localStorage.removeItem('user')

            // 提示用户登录已过期
            ElMessage.error('登录已过期，请重新登录')

            // 跳转到登录页
            if (window.location.hash !== '#/login') {
                router.push('/login')
            }
        }

        return Promise.reject(error)
    }
)

// 将axios挂载到全局,组件中可通过this.$axios使用
app.config.globalProperties.$axios = axios

// ========== 挂载Vue实例 ==========

app.use(store)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
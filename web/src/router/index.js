import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import AdminReviewView from '../views/AdminReviewView.vue'

const routes = [
  { path: '/', redirect: '/user/home' },
  { path: '/login', component: LoginView, meta: { public: true } },
  // 用户端
  { path: '/user/home', component: HomeView, meta: { role: 'USER' } },
  // 管理端（商家端页面后续按同一模式补充：/merchant/products 等）
  { path: '/admin/reviews', component: AdminReviewView, meta: { role: 'ADMIN' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 路由守卫：未登录跳登录页；角色不符也跳登录页。
 * 真正的权限校验在后端拦截器，这里只是体验优化。
 */
router.beforeEach((to) => {
  if (to.meta.public) {
    return true
  }
  const token = localStorage.getItem('token')
  if (!token) {
    return '/login'
  }
  const role = localStorage.getItem('role')
  if (to.meta.role && to.meta.role !== role) {
    return '/login'
  }
  return true
})

export default router

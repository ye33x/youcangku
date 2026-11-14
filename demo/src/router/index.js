import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

import Login from '../views/Login.vue'
import ShopLayout from '../layouts/ShopLayout.vue'
import ConsoleLayout from '../layouts/ConsoleLayout.vue'

import Home from '../views/user/Home.vue'
import ProductList from '../views/user/ProductList.vue'
import ProductDetail from '../views/user/ProductDetail.vue'
import Cart from '../views/user/Cart.vue'
import Checkout from '../views/user/Checkout.vue'
import Orders from '../views/user/Orders.vue'
import Profile from '../views/user/Profile.vue'
import Notices from '../views/user/Notices.vue'

import MerchantDashboard from '../views/merchant/Dashboard.vue'
import MerchantProducts from '../views/merchant/Products.vue'
import MerchantProductEdit from '../views/merchant/ProductEdit.vue'
import MerchantOrders from '../views/merchant/Orders.vue'
import MerchantAfterSales from '../views/merchant/AfterSales.vue'
import MerchantReviews from '../views/merchant/Reviews.vue'
import MerchantShop from '../views/merchant/Shop.vue'

import AdminDashboard from '../views/admin/Dashboard.vue'
import AdminAccounts from '../views/admin/Accounts.vue'
import AdminOrders from '../views/admin/Orders.vue'
import AdminCategories from '../views/admin/Categories.vue'
import AdminNotices from '../views/admin/Notices.vue'
import AdminReviews from '../views/admin/Reviews.vue'
import AdminLogs from '../views/admin/Logs.vue'

const routes = [
  { path: '/login', component: Login, meta: { public: true, title: '登录' } },
  {
    path: '/',
    component: ShopLayout,
    meta: { role: 'USER' },
    children: [
      { path: '', redirect: '/user/home' },
      { path: 'user/home', component: Home, meta: { title: '首页' } },
      { path: 'user/products', component: ProductList, meta: { title: '全部商品' } },
      { path: 'user/product/:id', component: ProductDetail, meta: { title: '商品详情' } },
      { path: 'user/cart', component: Cart, meta: { title: '购物车' } },
      { path: 'user/checkout', component: Checkout, meta: { title: '确认订单' } },
      { path: 'user/orders', component: Orders, meta: { title: '我的订单' } },
      { path: 'user/profile', component: Profile, meta: { title: '个人中心' } },
      { path: 'user/notices', component: Notices, meta: { title: '公告与通知' } }
    ]
  },
  {
    path: '/merchant',
    component: ConsoleLayout,
    meta: { role: 'MERCHANT' },
    children: [
      { path: '', redirect: '/merchant/dashboard' },
      { path: 'dashboard', component: MerchantDashboard, meta: { title: '工作台' } },
      { path: 'products', component: MerchantProducts, meta: { title: '商品管理' } },
      { path: 'product/new', component: MerchantProductEdit, meta: { title: '发布商品' } },
      { path: 'product/:id', component: MerchantProductEdit, meta: { title: '编辑商品' } },
      { path: 'orders', component: MerchantOrders, meta: { title: '订单管理' } },
      { path: 'after-sales', component: MerchantAfterSales, meta: { title: '售后处理' } },
      { path: 'reviews', component: MerchantReviews, meta: { title: '评价管理' } },
      { path: 'shop', component: MerchantShop, meta: { title: '店铺信息' } }
    ]
  },
  {
    path: '/admin',
    component: ConsoleLayout,
    meta: { role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: AdminDashboard, meta: { title: '平台总览' } },
      { path: 'accounts', component: AdminAccounts, meta: { title: '账号管理' } },
      { path: 'orders', component: AdminOrders, meta: { title: '订单管理' } },
      { path: 'categories', component: AdminCategories, meta: { title: '分类管理' } },
      { path: 'notices', component: AdminNotices, meta: { title: '公告管理' } },
      { path: 'reviews', component: AdminReviews, meta: { title: '评价审核' } },
      { path: 'logs', component: AdminLogs, meta: { title: '操作日志' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/user/home' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

const homeOf = (role) =>
  role === 'ADMIN' ? '/admin/dashboard' : role === 'MERCHANT' ? '/merchant/dashboard' : '/user/home'

router.beforeEach((to) => {
  if (to.meta.public) return true
  if (!store.state.token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const needRole = to.matched.map((r) => r.meta?.role).filter(Boolean).pop()
  if (needRole && needRole !== store.state.role) {
    return homeOf(store.state.role)
  }
  return true
})

export default router

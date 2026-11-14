<template>
  <el-container class="console">
    <el-aside width="216px" class="aside">
      <div class="brand">
        🌰 优果仓
        <span class="role">{{ store.state.role === 'ADMIN' ? '管理端' : '商家端' }}</span>
      </div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          <el-icon><component :is="m.icon" /></el-icon>
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="head">
        <div class="title">
          {{ route.meta.title || '控制台' }}
          <span class="muted sub">/ {{ store.state.user?.nickname }}</span>
        </div>
        <div class="ops">
          <el-button size="small" :icon="Setting" @click="uiState.demoPanelVisible = true">演示控制</el-button>
          <el-button size="small" :icon="Shop" @click="goShop">去用户端看看</el-button>
          <el-dropdown @command="onCommand">
            <span class="user">{{ store.state.user?.username }}<el-icon><ArrowDown /></el-icon></span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="merchant">切换到商家端</el-dropdown-item>
                <el-dropdown-item command="admin">切换到管理端</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Odometer, Goods, List, RefreshLeft, ChatDotRound, Shop,
  User, Menu, Bell, Document, ArrowDown, Setting
} from '@element-plus/icons-vue'
import store from '../store'
import { uiState } from '../mock/config'

const route = useRoute()
const router = useRouter()

const merchantMenus = [
  { path: '/merchant/dashboard', label: '工作台', icon: Odometer },
  { path: '/merchant/products', label: '商品管理', icon: Goods },
  { path: '/merchant/orders', label: '订单管理', icon: List },
  { path: '/merchant/after-sales', label: '售后处理', icon: RefreshLeft },
  { path: '/merchant/reviews', label: '评价管理', icon: ChatDotRound },
  { path: '/merchant/shop', label: '店铺信息', icon: Shop }
]

const adminMenus = [
  { path: '/admin/dashboard', label: '平台总览', icon: Odometer },
  { path: '/admin/accounts', label: '账号管理', icon: User },
  { path: '/admin/orders', label: '订单管理', icon: List },
  { path: '/admin/categories', label: '分类管理', icon: Menu },
  { path: '/admin/notices', label: '公告管理', icon: Bell },
  { path: '/admin/reviews', label: '评价审核', icon: ChatDotRound },
  { path: '/admin/logs', label: '操作日志', icon: Document }
]

const menus = computed(() => (store.state.role === 'ADMIN' ? adminMenus : merchantMenus))

function goShop() {
  router.push('/user/home')
}

function onCommand(cmd) {
  if (cmd === 'logout') {
    store.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
    return
  }
  store.switchRole(cmd === 'admin' ? 'ADMIN' : 'MERCHANT')
  router.push(cmd === 'admin' ? '/admin/dashboard' : '/merchant/dashboard')
}
</script>

<style scoped>
.console { height: 100vh; }
.aside { background: #1f2937; color: #e5e7eb; display: flex; flex-direction: column; }
.brand { height: 60px; display: flex; align-items: center; gap: 8px; padding: 0 18px; font-weight: 700; color: #fff; border-bottom: 1px solid rgba(255, 255, 255, 0.08); }
.role { font-size: 11px; padding: 2px 6px; border-radius: 4px; background: rgba(245, 158, 11, 0.25); color: #fbbf24; font-weight: 500; }
.menu { border-right: none; background: transparent; flex: 1; }
.menu :deep(.el-menu-item) { color: #cbd5e1; }
.menu :deep(.el-menu-item:hover) { background: rgba(255, 255, 255, 0.06); color: #fff; }
.menu :deep(.el-menu-item.is-active) { background: #b45309; color: #fff; }
.head { background: #fff; border-bottom: 1px solid var(--ygc-border); display: flex; align-items: center; justify-content: space-between; }
.title { font-size: 17px; font-weight: 600; }
.sub { font-size: 12px; font-weight: 400; }
.ops { display: flex; align-items: center; gap: 10px; }
.user { cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 4px; }
.main { background: var(--ygc-bg); padding: 0; }
</style>

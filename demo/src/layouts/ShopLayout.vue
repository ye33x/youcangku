<template>
  <div class="shop-layout">
    <header class="shop-header">
      <div class="inner">
        <router-link to="/user/home" class="logo">🌰 优果仓</router-link>
        <div class="search">
          <el-input v-model="keyword" placeholder="搜索核桃、葡萄干、红枣…" clearable @keyup.enter="onSearch">
            <template #append><el-button :icon="Search" @click="onSearch" /></template>
          </el-input>
        </div>
        <nav class="nav">
          <router-link to="/user/home">首页</router-link>
          <router-link to="/user/products">全部商品</router-link>
          <router-link to="/user/orders">我的订单</router-link>
          <router-link to="/user/notices">
            消息<el-badge v-if="store.unreadCount.value" :value="store.unreadCount.value" class="badge" />
          </router-link>
        </nav>
        <div class="right">
          <el-button text size="small" :icon="Setting" @click="uiState.demoPanelVisible = true">演示控制</el-button>
          <router-link to="/user/cart" class="cart">
            <el-badge :value="store.cartCount.value" :hidden="!store.cartCount.value">
              <el-button :icon="ShoppingCart" circle />
            </el-badge>
          </router-link>
          <el-dropdown v-if="store.state.user" @command="onCommand">
            <span class="user">
              {{ store.state.user.nickname }}<el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="orders">我的订单</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="shop-main"><router-view /></main>

    <footer class="shop-footer">
      <div>优果仓 · 界面演示 Demo（纯前端 / 模拟数据）</div>
      <div class="muted">用于评估界面布局与操作流程，不含真实业务逻辑</div>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, ShoppingCart, ArrowDown, Setting } from '@element-plus/icons-vue'
import store from '../store'
import { uiState } from '../mock/config'

const router = useRouter()
const keyword = ref('')

function onSearch() {
  router.push({ path: '/user/products', query: { keyword: keyword.value } })
}

function onCommand(cmd) {
  if (cmd === 'logout') {
    store.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } else if (cmd === 'profile') {
    router.push('/user/profile')
  } else {
    router.push('/user/orders')
  }
}
</script>

<style scoped>
.shop-header { position: sticky; top: 0; z-index: 100; background: #fff; border-bottom: 1px solid var(--ygc-border); }
.inner { max-width: 1180px; margin: 0 auto; height: 64px; display: flex; align-items: center; gap: 18px; padding: 0 24px; }
.logo { font-size: 19px; font-weight: 800; color: var(--ygc-primary); white-space: nowrap; }
.search { width: 260px; }
.nav { display: flex; gap: 18px; font-size: 14px; }
.nav a { color: #374151; padding: 4px 0; position: relative; }
.nav a.router-link-active { color: var(--ygc-primary); font-weight: 600; }
.badge { margin-left: 2px; transform: translateY(-8px); }
.right { margin-left: auto; display: flex; align-items: center; gap: 12px; }
.cart { display: flex; align-items: center; }
.user { cursor: pointer; font-size: 14px; display: flex; align-items: center; gap: 4px; color: #374151; }
.shop-main { min-height: calc(100vh - 64px - 76px); }
.shop-footer { border-top: 1px solid var(--ygc-border); padding: 20px 24px; text-align: center; font-size: 12px; color: #6b7280; background: #fff; }
</style>

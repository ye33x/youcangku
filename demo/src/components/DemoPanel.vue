<template>
  <el-drawer v-model="uiState.demoPanelVisible" title="演示控制面板" size="380px">
    <div class="block">
      <div class="label">当前身份</div>
      <div class="row-gap"><el-tag :type="roleTag">{{ roleText }}</el-tag></div>
      <div class="row-gap mt-8">
        <el-button size="small" @click="switchTo('USER')">切到用户端</el-button>
        <el-button size="small" @click="switchTo('MERCHANT')">切到商家端</el-button>
        <el-button size="small" @click="switchTo('ADMIN')">切到管理端</el-button>
      </div>
    </div>

    <el-divider />

    <div class="block">
      <div class="label">接口延迟：{{ demoConfig.latency }} ms</div>
      <el-slider v-model="demoConfig.latency" :min="0" :max="2000" :step="100" />
      <div class="tip muted">调到 1500ms 以上可清楚看到骨架屏与加载态。</div>
    </div>

    <div class="block">
      <div class="label">模拟错误率：{{ demoConfig.errorRate }}%</div>
      <el-slider v-model="demoConfig.errorRate" :min="0" :max="100" :step="10" />
      <div class="tip muted">调高后刷新页面，可看到错误提示与「重新加载」按钮。</div>
    </div>

    <div class="block">
      <div class="label">空数据模式</div>
      <el-switch v-model="demoConfig.emptyMode" active-text="开启后列表返回空" />
      <div class="tip muted">开启后进入任意列表页，可看到空状态插画与引导按钮。</div>
    </div>

    <el-divider />

    <div class="block">
      <div class="label">关键流程快捷入口</div>
      <el-button text class="link" @click="go('/user/home')">1. 用户端首页（个性化推荐）</el-button>
      <el-button text class="link" @click="go('/user/product/3')">2. 商品详情 → 加购</el-button>
      <el-button text class="link" @click="go('/user/cart')">3. 购物车 → 结算</el-button>
      <el-button text class="link" @click="go('/user/orders')">4. 我的订单（多状态）</el-button>
      <el-button text class="link" @click="go('/merchant/dashboard')">5. 商家工作台</el-button>
      <el-button text class="link" @click="go('/merchant/products')">6. 商家商品管理</el-button>
      <el-button text class="link" @click="go('/admin/reviews')">7. 管理端评价审核</el-button>
      <el-button text class="link" @click="go('/admin/logs')">8. 操作日志</el-button>
    </div>

    <el-divider />
    <el-button type="danger" plain style="width: 100%" @click="onReset">重置演示数据并退出登录</el-button>
    <p class="tip muted mt-8">所有数据仅存在于浏览器内存 / sessionStorage，不会请求任何后端接口。</p>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import store from '../store'
import { demoConfig, uiState } from '../mock/config'

const router = useRouter()

const roleText = computed(() => ({ USER: '用户端', MERCHANT: '商家端', ADMIN: '管理端' }[store.state.role] || '未登录'))
const roleTag = computed(() => ({ USER: 'success', MERCHANT: 'warning', ADMIN: 'danger' }[store.state.role] || 'info'))

function switchTo(role) {
  store.switchRole(role)
  ElMessage.success('已切换到' + (role === 'USER' ? '用户端' : role === 'MERCHANT' ? '商家端' : '管理端'))
  const target = role === 'ADMIN' ? '/admin/dashboard' : role === 'MERCHANT' ? '/merchant/dashboard' : '/user/home'
  router.push(target)
  uiState.demoPanelVisible = false
}

function go(path) {
  if (!store.state.token) {
    ElMessage.warning('请先在登录页登录（任意端）')
    router.push('/login')
    return
  }
  router.push(path)
  uiState.demoPanelVisible = false
}

function onReset() {
  store.resetDemo()
}
</script>

<style scoped>
.block { margin-bottom: 18px; }
.label { font-size: 13px; font-weight: 600; margin-bottom: 8px; }
.tip { font-size: 12px; margin-top: 6px; line-height: 1.6; }
.link { display: block; padding-left: 0; font-size: 13px; justify-content: flex-start; }
</style>

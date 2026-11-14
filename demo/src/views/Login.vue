<template>
  <div class="login-page">
    <div class="brand-side">
      <h1>🌰 优果仓</h1>
      <p class="slogan">干果电商平台 · 界面与流程演示</p>
      <div class="roles">
        <div
          v-for="r in roles" :key="r.value" class="role-card"
          :class="{ active: form.role === r.value }" @click="pickRole(r.value)"
        >
          <div class="role-icon">{{ r.icon }}</div>
          <div class="role-name">{{ r.label }}</div>
          <div class="role-desc">{{ r.desc }}</div>
        </div>
      </div>
      <p class="note">本页面不连接任何后端，登录仅用于演示三端入口与跳转关系。</p>
    </div>

    <div class="form-side">
      <div class="form-card">
        <h2>登录{{ currentRoleLabel }}</h2>
        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" class="mb" />
        <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="账号">
            <el-input v-model="form.account" placeholder="用户名 / 手机号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password
              placeholder="演示密码：123456" :prefix-icon="Lock" @keyup.enter="onSubmit" />
          </el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">
            {{ loading ? '登录中…' : '登 录' }}
          </el-button>
        </el-form>

        <div class="quick">
          <span class="muted">快速填入：</span>
          <el-link v-for="q in quickList" :key="q.account" type="primary" :underline="false" @click="quickFill(q)">
            {{ q.label }}
          </el-link>
        </div>

        <el-divider />
        <p class="hint">演示密码统一为 <b>123456</b>；密码输错会触发错误提示，可观察表单错误态。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import store from '../store'

const router = useRouter()
const route = useRoute()

const roles = [
  { value: 'USER', label: '用户端', icon: '🛒', desc: '浏览 · 下单 · 售后', account: 'user01' },
  { value: 'MERCHANT', label: '商家端', icon: '🏪', desc: '商品 · 订单 · 售后', account: 'guo_shop' },
  { value: 'ADMIN', label: '管理端', icon: '🛡️', desc: '审核 · 账号 · 日志', account: 'admin' }
]

const quickList = [
  { label: 'user01', role: 'USER', account: 'user01' },
  { label: 'user02', role: 'USER', account: 'user02' },
  { label: 'guo_shop', role: 'MERCHANT', account: 'guo_shop' },
  { label: 'nuts_shop', role: 'MERCHANT', account: 'nuts_shop' },
  { label: 'admin', role: 'ADMIN', account: 'admin' }
]

const form = reactive({ role: 'USER', account: 'user01', password: '123456' })
const loading = ref(false)
const errorMsg = ref('')

const currentRoleLabel = computed(() => roles.find((r) => r.value === form.role)?.label || '')

function pickRole(role) {
  form.role = role
  form.account = roles.find((r) => r.value === role).account
  form.password = '123456'
  errorMsg.value = ''
}

function quickFill(q) {
  form.role = q.role
  form.account = q.account
  form.password = '123456'
  errorMsg.value = ''
}

async function onSubmit() {
  errorMsg.value = ''
  loading.value = true
  try {
    await store.login(form.role, form.account, form.password)
    ElMessage.success('登录成功')
    const target = form.role === 'ADMIN' ? '/admin/dashboard' : form.role === 'MERCHANT' ? '/merchant/dashboard' : '/user/home'
    router.push(route.query.redirect || target)
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: grid; grid-template-columns: 1.1fr 1fr; }
.brand-side { background: linear-gradient(150deg, #78350f 0%, #b45309 55%, #f59e0b 100%); color: #fff; padding: 56px 48px; display: flex; flex-direction: column; }
.brand-side h1 { font-size: 34px; margin: 0; }
.slogan { opacity: 0.85; margin-top: 8px; }
.roles { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 40px; }
.role-card { background: rgba(255, 255, 255, 0.12); border: 1px solid rgba(255, 255, 255, 0.25); border-radius: 12px; padding: 16px 12px; cursor: pointer; transition: all 0.2s; }
.role-card:hover { background: rgba(255, 255, 255, 0.2); }
.role-card.active { background: #fff; color: #b45309; border-color: #fff; }
.role-icon { font-size: 24px; }
.role-name { font-weight: 700; margin-top: 6px; }
.role-desc { font-size: 12px; opacity: 0.85; margin-top: 4px; }
.note { margin-top: auto; font-size: 12px; opacity: 0.75; line-height: 1.7; }
.form-side { display: flex; align-items: center; justify-content: center; padding: 32px; }
.form-card { width: 380px; }
.form-card h2 { margin: 0 0 20px; }
.mb { margin-bottom: 16px; }
.quick { margin-top: 18px; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; font-size: 13px; }
.hint { font-size: 12px; color: var(--ygc-muted); line-height: 1.7; }
</style>

<template>
  <el-card class="login-card">
    <h3>登录优果仓</h3>
    <el-form :model="form" label-width="72px">
      <el-form-item label="角色">
        <el-select v-model="form.role">
          <el-option label="用户" value="USER" />
          <el-option label="商家" value="MERCHANT" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>
      <el-form-item label="账号">
        <el-input v-model="form.account" placeholder="用户名或手机号" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">登录</el-button>
      </el-form-item>
    </el-form>
    <p class="hint">演示账号：user01 / admin / guo_shop，密码均为 123456</p>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'

const router = useRouter()
const loading = ref(false)
const form = reactive({ role: 'USER', account: 'user01', password: '123456' })

async function submit() {
  loading.value = true
  try {
    const data = await request.post('/public/auth/login', form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('name', data.name)
    router.push(data.role === 'ADMIN' ? '/admin/reviews' : '/user/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-card {
  max-width: 420px;
  margin: 60px auto;
}
.hint {
  color: #9ca3af;
  font-size: 12px;
  margin-top: 12px;
}
</style>

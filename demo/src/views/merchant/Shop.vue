<template>
  <div class="console-page">
    <div class="card-panel">
      <div class="panel-title">店铺信息</div>
      <el-form :model="form" label-width="96px">
        <el-form-item label="店铺名称"><el-input v-model="form.shopName" maxlength="30" show-word-limit /></el-form-item>
        <el-form-item label="店铺简介">
          <el-input v-model="form.intro" type="textarea" :rows="3" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="营业状态"><el-switch v-model="form.opened" active-text="营业中" inactive-text="已打烊" /></el-form-item>
        <el-form-item><el-button type="primary" :loading="saving" @click="onSave">保存</el-button></el-form-item>
      </el-form>

      <el-alert type="info" :closable="false" title="说明"
        description="该页面演示店铺资料的维护流程；真实项目中店铺信息审核通过后才会在前台展示。" show-icon />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { merchants } from '../../mock/db'
import store from '../../store'

const current = merchants.find((m) => m.id === (store.state.user?.id || 1)) || merchants[0]
const saving = ref(false)
const form = reactive({
  shopName: current.shopName,
  intro: current.intro,
  phone: current.phone,
  opened: current.status === 1
})

async function onSave() {
  if (!form.shopName.trim()) {
    ElMessage.warning('店铺名称不能为空')
    return
  }
  saving.value = true
    await new Promise((r) => setTimeout(r, 500))
  current.shopName = form.shopName
  current.intro = form.intro
  current.phone = form.phone
  current.status = form.opened ? 1 : 0
  saving.value = false
  ElMessage.success('店铺信息已保存')
}
</script>

<style scoped>
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 22px; max-width: 640px; }
.panel-title { font-weight: 600; margin-bottom: 18px; }
</style>

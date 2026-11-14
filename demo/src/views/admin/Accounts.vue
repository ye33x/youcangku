<template>
  <div class="console-page">
    <el-tabs v-model="kind" @tab-change="load">
      <el-tab-pane label="商家账号" name="merchant" />
      <el-tab-pane label="用户账号" name="user" />
    </el-tabs>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="暂无账号数据" @retry="load"
      >
        <el-table :data="list">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column :prop="kind === 'merchant' ? 'shopName' : 'nickname'"
            :label="kind === 'merchant' ? '店铺名称' : '昵称'" min-width="160" />
          <el-table-column prop="username" label="登录名" width="140" />
          <el-table-column prop="phone" label="手机号" width="150" />
          <el-table-column prop="createdAt" :label="kind === 'merchant' ? '入驻时间' : '注册时间'" width="140" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '启用' : '已禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button text size="small" :type="row.status === 1 ? 'danger' : 'primary'" @click="onToggle(row)">
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <p class="muted small mt-16">
          禁用后该账号将无法登录对应端；演示环境不会真正拦截，仅改变列表状态与日志。
        </p>
      </AsyncBlock>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchAccounts } from '../../mock/api'
import store from '../../store'

const kind = ref('merchant')
const loading = ref(false)
const error = ref('')
const list = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await fetchAccounts(kind.value)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function onToggle(row) {
  const disabling = row.status === 1
  try {
    await ElMessageBox.confirm(
      disabling ? '禁用后该账号将无法登录，确认？' : '确认恢复该账号？',
      disabling ? '禁用账号' : '启用账号',
      { type: disabling ? 'warning' : 'info' }
    )
    row.status = disabling ? 0 : 1
    store.addLog(disabling ? 'ACCOUNT_DISABLE' : 'ACCOUNT_ENABLE', '管理员', '账号 #' + row.id)
    ElMessage.success(disabling ? '已禁用' : '已启用')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.small { font-size: 12px; }
</style>

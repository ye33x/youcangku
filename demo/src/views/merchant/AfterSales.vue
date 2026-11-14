<template>
  <div class="console-page">
    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="暂无售后申请" @retry="load"
      >
        <div v-for="a in list" :key="a.id" class="sale">
          <div class="sale-head">
            <el-tag size="small" type="warning">{{ a.type }}</el-tag>
            <b>{{ a.productName }}</b>
            <span class="muted small">售后单 #{{ a.id }}　订单 {{ a.orderNo }}</span>
            <el-tag class="right" size="small" :type="statusType(a.status)">{{ a.status }}</el-tag>
          </div>
          <div class="sale-body">
            <div>申请原因：{{ a.reason }}</div>
            <div class="muted small">申请时间：{{ a.createdAt }}　退款金额：{{ money(a.amount) }}</div>
          </div>
          <div v-if="a.status === '待商家处理'" class="sale-ops">
            <el-button size="small" :loading="busyId === a.id" @click="handle(a, false)">拒绝</el-button>
            <el-button size="small" type="primary" :loading="busyId === a.id" @click="handle(a, true)">同意</el-button>
          </div>
        </div>
      </AsyncBlock>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchAfterSales } from '../../mock/api'
import { money } from '../../mock/config'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const list = ref([])
const busyId = ref(null)

const statusType = (s) => (s === '待商家处理' ? 'warning' : s === '商家已同意' ? 'success' : 'danger')

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await fetchAfterSales({ merchantId: store.state.user?.id || 1 })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function handle(a, agree) {
  try {
    await ElMessageBox.confirm(
      agree ? '同意后将触发退款流程，确认同意？' : '确认拒绝该售后申请？买家可申请平台介入。',
      agree ? '同意售后' : '拒绝售后',
      { type: agree ? 'info' : 'warning' }
    )
    busyId.value = a.id
    await store.handleAfterSale(a.id, agree)
    ElMessage.success(agree ? '已同意，退款将在 1-3 个工作日到账' : '已拒绝，买家可申请平台介入')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  } finally {
    busyId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.sale { border: 1px solid var(--ygc-border); border-radius: 10px; padding: 14px 16px; margin-bottom: 12px; }
.sale-head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.right { margin-left: auto; }
.sale-body { margin: 10px 0; font-size: 13px; line-height: 1.8; }
.sale-ops { display: flex; justify-content: flex-end; gap: 8px; }
.small { font-size: 12px; }
</style>

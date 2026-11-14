<template>
  <div class="console-page">
    <div class="toolbar">
      <el-select v-model="operatorType" placeholder="操作人类型" clearable style="width: 150px" @change="reload">
        <el-option label="管理员" value="管理员" />
        <el-option label="商家" value="商家" />
        <el-option label="买家" value="买家" />
      </el-select>
      <el-select v-model="action" placeholder="操作类型" clearable style="width: 190px" @change="reload">
        <el-option v-for="a in actionOptions" :key="a.value" :label="a.label" :value="a.value" />
      </el-select>
      <el-button @click="reset">重置</el-button>
      <el-tag type="warning" style="margin-left: auto">日志只读，不提供删除</el-tag>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="没有符合条件的日志" empty-action="重置筛选"
        @retry="reload" @empty-action="reset"
      >
        <el-table :data="list" size="small">
          <el-table-column prop="createdAt" label="时间" width="180" />
          <el-table-column prop="operatorType" label="操作人类型" width="110" />
          <el-table-column prop="operatorName" label="操作人" width="130" />
          <el-table-column prop="actionLabel" label="操作" width="140" />
          <el-table-column prop="target" label="对象" min-width="160" />
          <el-table-column prop="ip" label="IP" width="120" />
        </el-table>

        <div class="pager">
          <el-pagination v-model:current-page="page" :page-size="size" :total="total"
            layout="total, prev, pager, next" background @current-change="reload" />
        </div>
      </AsyncBlock>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchLogs } from '../../mock/api'

const operatorType = ref('')
const action = ref('')
const loading = ref(false)
const error = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 8

const actionOptions = [
  { label: '上架商品', value: 'PRODUCT_ONLINE' },
  { label: '下架商品', value: 'PRODUCT_OFFLINE' },
  { label: '发布商品', value: 'PRODUCT_CREATE' },
  { label: '审核通过评价', value: 'REVIEW_APPROVE' },
  { label: '拒绝评价', value: 'REVIEW_REJECT' },
  { label: '订单发货', value: 'ORDER_SHIP' },
  { label: '处理售后', value: 'AFTER_SALE_HANDLE' },
  { label: '发布公告', value: 'NOTICE_PUBLISH' }
]

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchLogs({ action: action.value, operatorType: operatorType.value, page: page.value, size })
    list.value = res.list
    total.value = res.total
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function reset() {
  operatorType.value = ''
  action.value = ''
  page.value = 1
  reload()
}

onMounted(reload)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

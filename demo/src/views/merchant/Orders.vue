<template>
  <div class="console-page">
    <div class="toolbar">
      <el-radio-group v-model="status" size="small" @change="reload">
        <el-radio-button :value="''">全部</el-radio-button>
        <el-radio-button :value="1">待发货</el-radio-button>
        <el-radio-button :value="2">已发货</el-radio-button>
        <el-radio-button :value="3">已完成</el-radio-button>
        <el-radio-button :value="5">售后中</el-radio-button>
      </el-radio-group>
      <span class="muted" style="margin-left: auto">共 {{ total }} 笔</span>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="当前筛选条件下没有订单" empty-action="查看全部订单"
        @retry="reload" @empty-action="resetFilter"
      >
        <el-table :data="list">
          <el-table-column prop="orderNo" label="订单号" width="180" />
          <el-table-column label="商品" min-width="220">
            <template #default="{ row }">
              <div class="cell-product">
                <div class="mini-thumb">{{ row.items[0].emoji }}</div>
                <div>
                  <div>{{ row.items[0].name }}</div>
                  <div class="muted small">
                    {{ row.items.length > 1 ? '等 ' + row.items.length + ' 件商品' : row.items[0].spec }}
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="110">
            <template #default="{ row }"><span class="price">{{ money(row.payAmount) }}</span></template>
          </el-table-column>
          <el-table-column prop="createdAt" label="下单时间" width="170" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="ORDER_STATUS[row.status].type" size="small">{{ ORDER_STATUS[row.status].text }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button text size="small" @click="openDetail(row)">详情</el-button>
              <el-button v-if="row.status === 1" text size="small" type="primary"
                :loading="busyId === row.id" @click="onShip(row)">发货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </AsyncBlock>
    </div>

    <el-drawer v-model="drawer" title="订单详情" size="480px">
      <template v-if="current">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="ORDER_STATUS[current.status].type" size="small">{{ ORDER_STATUS[current.status].text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="收货人">{{ current.receiver }}　{{ current.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址">{{ current.receiverAddr }}</el-descriptions-item>
          <el-descriptions-item label="买家备注">{{ current.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div class="panel-title mt-16">商品明细</div>
        <div v-for="it in current.items" :key="it.productId" class="drawer-item">
          <span>{{ it.emoji }} {{ it.name }}</span>
          <span class="muted">{{ money(it.price) }} × {{ it.quantity }}</span>
        </div>

        <div class="panel-title mt-16">订单流程</div>
        <el-timeline>
          <el-timeline-item v-for="(t, i) in current.timeline" :key="i" :timestamp="t.time" :type="t.done ? 'primary' : 'info'">
            {{ t.label }}
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchOrders } from '../../mock/api'
import { ORDER_STATUS, money } from '../../mock/config'
import store from '../../store'

const status = ref('')
const loading = ref(false)
const error = ref('')
const list = ref([])
const total = ref(0)
const busyId = ref(null)
const drawer = ref(false)
const current = ref(null)

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchOrders({
      merchantId: store.state.user?.id || 1,
      status: status.value === '' ? null : status.value,
      size: 20
    })
    list.value = res.list
    total.value = res.total
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function resetFilter() {
  status.value = ''
  reload()
}

function openDetail(row) {
  current.value = row
  drawer.value = true
}

async function onShip(row) {
  try {
    await ElMessageBox.confirm('确认已发出该订单商品？', '订单发货', { type: 'info' })
    busyId.value = row.id
    await store.shipOrder(row.id)
    ElMessage.success('发货成功，等待买家确认收货')
    reload()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  } finally {
    busyId.value = null
  }
}

onMounted(reload)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.cell-product { display: flex; align-items: center; gap: 10px; }
.mini-thumb { width: 40px; height: 40px; border-radius: 8px; background: linear-gradient(135deg, #fde68a, #fbbf24); display: flex; align-items: center; justify-content: center; font-size: 18px; }
.small { font-size: 12px; }
.panel-title { font-weight: 600; margin-bottom: 10px; }
.drawer-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed var(--ygc-border); font-size: 13px; }
</style>

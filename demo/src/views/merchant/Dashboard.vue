<template>
  <div class="console-page">
    <AsyncBlock :loading="loading" :error="error" :empty="false" :rows="5" @retry="load">
      <div class="stats">
        <div class="stat-card">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-hint">含全部状态</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">成交金额</div>
          <div class="stat-value">{{ money(stats.revenue) }}</div>
          <div class="stat-hint">不含已取消订单</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">在售商品</div>
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-hint">含下架商品</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">待发货</div>
          <div class="stat-value warn">{{ stats.pendingShip }}</div>
          <div class="stat-hint"><el-link type="primary" :underline="false" @click="$router.push('/merchant/orders')">去处理</el-link></div>
        </div>
      </div>

      <div class="row">
        <div class="card-panel">
          <div class="panel-title">近 6 个月销量趋势 <span class="muted small">（演示数据）</span></div>
          <div class="fake-chart">
            <div v-for="b in bars" :key="b.label" class="fake-bar" :style="{ height: b.height + '%' }">
              <span>{{ b.label }}</span>
            </div>
          </div>
          <div style="height: 26px"></div>
        </div>

        <div class="card-panel">
          <div class="panel-title">待办事项</div>
          <div class="todo" @click="$router.push('/merchant/orders')">
            <div>
              <div>待发货订单</div>
              <div class="muted small">买家已付款，需尽快发货</div>
            </div>
            <el-badge :value="stats.pendingShip" type="warning" />
          </div>
          <div class="todo" @click="$router.push('/merchant/after-sales')">
            <div>
              <div>待处理售后</div>
              <div class="muted small">退货退款 / 仅退款申请</div>
            </div>
            <el-badge :value="2" type="danger" />
          </div>
          <div class="todo" @click="$router.push('/merchant/reviews')">
            <div>
              <div>待审核评价</div>
              <div class="muted small">需平台审核后展示</div>
            </div>
            <el-badge :value="stats.pendingAudit" type="info" />
          </div>
        </div>
      </div>

      <div class="card-panel mt-16">
        <div class="panel-title">热销商品 Top 5</div>
        <el-table :data="topProducts" size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="name" label="商品" />
          <el-table-column prop="originPlace" label="产地" width="140" />
          <el-table-column prop="stock" label="库存" width="90" />
          <el-table-column prop="sales" label="销量" width="90" sortable />
          <el-table-column label="价格" width="110">
            <template #default="{ row }">{{ money(row.price) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </AsyncBlock>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchStats } from '../../mock/api'
import { money } from '../../mock/config'
import { products } from '../../mock/db'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const stats = ref({ orderCount: 0, revenue: 0, productCount: 0, pendingShip: 0, pendingAudit: 0 })
const topProducts = ref([])

const bars = [
  { label: '6月', height: 42 }, { label: '7月', height: 58 }, { label: '8月', height: 36 },
  { label: '9月', height: 74 }, { label: '10月', height: 88 }, { label: '11月', height: 64 }
]

async function load() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await fetchStats('merchant', store.state.user?.id || 1)
    topProducts.value = products
      .filter((p) => p.merchantId === (store.state.user?.id || 1))
      .sort((a, b) => b.sales - a.sales)
      .slice(0, 5)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.warn { color: #d97706; }
.row { display: grid; grid-template-columns: 1.4fr 1fr; gap: 14px; margin-top: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 18px; }
.panel-title { font-weight: 600; margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }
.small { font-size: 12px; }
.todo { display: flex; align-items: center; justify-content: space-between; padding: 12px; border: 1px solid var(--ygc-border); border-radius: 10px; margin-bottom: 10px; cursor: pointer; transition: all 0.2s; }
.todo:hover { border-color: #f59e0b; background: #fffbeb; }
</style>

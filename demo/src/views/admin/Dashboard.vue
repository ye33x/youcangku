<template>
  <div class="console-page">
    <AsyncBlock :loading="loading" :error="error" :empty="false" :rows="5" @retry="load">
      <div class="stats">
        <div class="stat-card">
          <div class="stat-label">入驻商家</div>
          <div class="stat-value">{{ stats.merchantCount }}</div>
          <div class="stat-hint">含已禁用账号</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">注册用户</div>
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-hint">平台累计</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">在架商品</div>
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-hint">含已下架</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">待审核评价</div>
          <div class="stat-value warn">{{ stats.pendingAudit }}</div>
          <div class="stat-hint"><el-link type="primary" :underline="false" @click="$router.push('/admin/reviews')">去审核</el-link></div>
        </div>
      </div>

      <div class="row">
        <div class="card-panel">
          <div class="panel-title">分类销量分布</div>
          <div v-for="c in stats.categoryStats" :key="c.name" class="bar-row">
            <span class="bar-label">{{ c.name }}</span>
            <div class="bar-track"><div class="bar-fill" :style="{ width: barWidth(c.value) + '%' }"></div></div>
            <span class="bar-value">{{ c.value }}</span>
          </div>
          <p class="muted small mt-16">按商品累计销量汇总，用于观察各品类结构。</p>
        </div>

        <div class="card-panel">
          <div class="panel-title">最新操作</div>
          <el-timeline>
            <el-timeline-item v-for="l in recentLogs" :key="l.id" :timestamp="l.createdAt"
              :type="l.operatorType === '管理员' ? 'danger' : 'primary'">
              <b>{{ l.operatorName }}</b> {{ l.actionLabel }}　<span class="muted small">{{ l.target }}</span>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </AsyncBlock>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchLogs, fetchStats } from '../../mock/api'

const loading = ref(false)
const error = ref('')
const stats = ref({ merchantCount: 0, userCount: 0, productCount: 0, pendingAudit: 0, categoryStats: [] })
const recentLogs = ref([])

const maxValue = () => Math.max(...(stats.value.categoryStats || []).map((c) => c.value), 1)
const barWidth = (v) => Math.round((v / maxValue()) * 100)

async function load() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await fetchStats('admin')
    const res = await fetchLogs({ size: 6 })
    recentLogs.value = res.list
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
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-top: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 18px; }
.panel-title { font-weight: 600; margin-bottom: 16px; }
.bar-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; font-size: 13px; }
.bar-label { width: 72px; color: var(--ygc-muted); }
.bar-track { flex: 1; height: 10px; background: #f3f4f6; border-radius: 999px; overflow: hidden; }
.bar-fill { height: 100%; background: linear-gradient(90deg, #fbbf24, #f59e0b); }
.bar-value { width: 56px; text-align: right; }
.small { font-size: 12px; }
</style>

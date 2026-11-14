<template>
  <div class="console-page">
    <div class="toolbar">
      <el-radio-group v-model="status" size="small" @change="load">
        <el-radio-button value="待审核">待审核</el-radio-button>
        <el-radio-button value="已通过">已通过</el-radio-button>
        <el-radio-button value="已拒绝">已拒绝</el-radio-button>
        <el-radio-button value="">全部</el-radio-button>
      </el-radio-group>
      <span class="muted" style="margin-left: auto">两级审核：用户评价 → 平台审核 → 前台展示</span>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        :empty-text="'没有' + (status || '') + '评价'" empty-action="看全部"
        @retry="load" @empty-action="onShowAll"
      >
        <div v-for="r in list" :key="r.id" class="review">
          <div class="r-head">
            <el-tag size="small" :type="AUDIT_STATUS[r.status]?.type">{{ r.status }}</el-tag>
            <b>{{ r.userName }}</b>
            <el-rate :model-value="r.score" disabled size="small" />
            <span class="muted small">{{ r.createdAt }}</span>
          </div>
          <p class="content">{{ r.content }}</p>
          <div class="muted small">商品：{{ r.productName }}　订单：{{ r.orderId }}</div>
          <div v-if="r.reply" class="reply">商家回复：{{ r.reply }}</div>
          <div v-if="r.auditRemark" class="reply">审核意见：{{ r.auditRemark }}</div>
          <div v-if="r.status === '待审核'" class="ops">
            <el-button size="small" @click="onAudit(r, false)">拒绝</el-button>
            <el-button size="small" type="primary" @click="onAudit(r, true)">通过</el-button>
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
import { fetchReviews } from '../../mock/api'
import { AUDIT_STATUS } from '../../mock/config'
import store from '../../store'

const status = ref('待审核')
const loading = ref(false)
const error = ref('')
const list = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchReviews({ status: status.value, size: 50 })
    list.value = res.list
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function onShowAll() {
  status.value = ''
  load()
}

async function onAudit(r, approve) {
  let remark = approve ? '内容正常' : '内容不符合规范'
  if (!approve) {
    try {
      const { value } = await ElMessageBox.prompt('请填写拒绝原因（会展示给商家）', '拒绝评价', {
        confirmButtonText: '确认拒绝', inputPlaceholder: '如：含广告信息 / 与商品无关'
      })
      remark = value || remark
    } catch {
      return
    }
  }
  try {
    await store.auditReview(r.id, approve, remark)
    ElMessage.success(approve ? '已通过，前台可见' : '已拒绝，前台不展示')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.review { border-bottom: 1px solid var(--ygc-border); padding: 14px 0; }
.review:last-child { border-bottom: none; }
.r-head { display: flex; align-items: center; gap: 10px; }
.content { margin: 8px 0 6px; line-height: 1.7; }
.reply { margin-top: 8px; background: #f9fafb; border-radius: 8px; padding: 8px 10px; font-size: 13px; }
.ops { margin-top: 10px; display: flex; gap: 8px; justify-content: flex-end; }
.small { font-size: 12px; }
</style>

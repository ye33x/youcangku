<template>
  <div class="console-page">
    <el-alert class="mb" type="success" :closable="false" show-icon
      title="评价提交时先过关键词库：未命中自动通过，命中才进入「待人工复核」" />

    <div class="toolbar">
      <el-radio-group v-model="status" size="small" @change="load">
        <el-radio-button value="待人工复核">待人工复核</el-radio-button>
        <el-radio-button value="已通过">已通过</el-radio-button>
        <el-radio-button value="已拒绝">已拒绝</el-radio-button>
        <el-radio-button value="">全部</el-radio-button>
      </el-radio-group>
      <el-button size="small" :icon="Setting" @click="keywordDrawer = true">关键词库</el-button>
      <span class="muted" style="margin-left: auto">共 {{ list.length }} 条</span>
    </div>

    <div v-if="status === '待人工复核' && list.length" class="batch-bar">
      <el-checkbox v-model="allChecked" :indeterminate="indeterminate" @change="onCheckAll">全选</el-checkbox>
      <span class="muted">已选 {{ selected.length }} 条</span>
      <el-button size="small" type="primary" :disabled="!selected.length" :loading="batching" @click="batchAudit(true)">
        批量通过
      </el-button>
      <el-button size="small" type="danger" plain :disabled="!selected.length" :loading="batching" @click="batchAudit(false)">
        批量拒绝
      </el-button>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        :empty-text="status === '待人工复核' ? '没有需要人工复核的评价，全部自动通过了' : '没有' + (status || '') + '评价'"
        empty-action="看全部" @retry="load" @empty-action="onShowAll"
      >
        <div v-for="r in list" :key="r.id" class="review">
          <div class="r-head">
            <el-checkbox v-if="status === '待人工复核'" :model-value="selected.includes(r.id)" @change="(v) => toggle(r.id, v)" />
            <el-tag size="small" :type="AUDIT_STATUS[r.status]?.type">{{ r.status }}</el-tag>
            <b>{{ r.userName }}</b>
            <el-rate :model-value="r.score" disabled size="small" />
            <span class="muted small">{{ r.createdAt }}</span>
            <span v-if="r.autoAudited" class="muted small">· 自动审核</span>
          </div>

          <p class="content">{{ r.content }}</p>
          <div class="muted small">商品：{{ r.productName }}　订单：{{ r.orderId }}</div>

          <div v-if="r.matchedKeywords?.length" class="hits">
            <span class="muted small">命中关键词：</span>
            <el-tag v-for="w in r.matchedKeywords" :key="w" size="small" type="danger" effect="light">{{ w }}</el-tag>
          </div>
          <div v-else-if="r.auditRemark" class="muted small">{{ r.auditRemark }}</div>

          <div v-if="r.reply" class="reply">商家回复：{{ r.reply }}</div>
          <div v-if="r.status === '已拒绝' && r.auditRemark" class="reply">拒绝原因：{{ r.auditRemark }}</div>

          <div v-if="r.status === '待人工复核'" class="ops">
            <el-button size="small" @click="onAudit(r, false)">拒绝</el-button>
            <el-button size="small" type="primary" @click="onAudit(r, true)">通过</el-button>
          </div>
        </div>
      </AsyncBlock>
    </div>

    <el-drawer v-model="keywordDrawer" title="关键词库" size="420px">
      <p class="muted small">评价内容命中任意启用中的关键词即进入人工复核；停用的关键词不参与匹配。</p>
      <div class="add-row">
        <el-input v-model="newWord" size="small" placeholder="输入关键词，如：刷单" @keyup.enter="onAddKeyword" />
        <el-button size="small" type="primary" @click="onAddKeyword">添加</el-button>
      </div>

      <div v-for="k in keywords" :key="k.id" class="kw">
        <div class="kw-main">
          <b>{{ k.word }}</b>
          <el-tag size="small" type="info" effect="plain">{{ k.category }}</el-tag>
          <span class="muted small">命中 {{ k.hit }} 次</span>
        </div>
        <div class="kw-ops">
          <el-switch :model-value="k.enabled" size="small" @change="(v) => toggleKeyword(k.id, v)" />
          <el-button text size="small" type="danger" @click="removeKeyword(k.id)">删除</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchReviews } from '../../mock/api'
import { AUDIT_STATUS } from '../../mock/config'
import { keywords, addKeyword, removeKeyword, toggleKeyword } from '../../mock/keywords'
import store from '../../store'

const status = ref('待人工复核')
const loading = ref(false)
const error = ref('')
const list = ref([])
const selected = ref([])
const batching = ref(false)
const keywordDrawer = ref(false)
const newWord = ref('')

const allChecked = computed(() => list.value.length > 0 && selected.value.length === list.value.length)
const indeterminate = computed(() => selected.value.length > 0 && selected.value.length < list.value.length)

async function load() {
  loading.value = true
  error.value = ''
  selected.value = []
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

function toggle(id, checked) {
  if (checked) {
    if (!selected.value.includes(id)) selected.value.push(id)
  } else {
    selected.value = selected.value.filter((x) => x !== id)
  }
}

function onCheckAll(checked) {
  selected.value = checked ? list.value.map((r) => r.id) : []
}

async function doAudit(ids, approve, remark) {
  for (const id of ids) {
    await store.auditReview(id, approve, remark)
  }
}

async function onAudit(r, approve) {
  let remark = approve ? '人工复核通过' : '命中关键词，判定不通过'
  if (!approve) {
    try {
      const { value } = await ElMessageBox.prompt('请填写拒绝原因（展示给商家与用户）', '拒绝评价', {
        confirmButtonText: '确认拒绝', inputPlaceholder: '如：含引流信息'
      })
      remark = value || remark
    } catch {
      return
    }
  }
  await doAudit([r.id], approve, remark)
  ElMessage.success(approve ? '已通过，前台可见' : '已拒绝，前台不展示')
  load()
}

async function batchAudit(approve) {
  try {
    await ElMessageBox.confirm(
      `确认批量${approve ? '通过' : '拒绝'} ${selected.value.length} 条评价？`,
      approve ? '批量通过' : '批量拒绝',
      { type: 'warning' }
    )
  } catch {
    return
  }
  batching.value = true
  try {
    await doAudit([...selected.value], approve, approve ? '人工复核通过' : '命中关键词，批量驳回')
    ElMessage.success(`已${approve ? '通过' : '拒绝'} ${selected.value.length} 条`)
    load()
  } finally {
    batching.value = false
  }
}

function onAddKeyword() {
  try {
    const w = addKeyword(newWord.value)
    newWord.value = ''
    ElMessage.success('已添加关键词：' + w)
  } catch (e) {
    ElMessage.warning(e.message)
  }
}

onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 14px; }
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.batch-bar {
  display: flex; align-items: center; gap: 14px; margin-bottom: 12px;
  background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; padding: 10px 14px;
}
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.review { border-bottom: 1px solid var(--ygc-border); padding: 14px 0; }
.review:last-child { border-bottom: none; }
.r-head { display: flex; align-items: center; gap: 10px; }
.content { margin: 8px 0 6px; line-height: 1.7; }
.hits { display: flex; align-items: center; gap: 6px; margin-top: 8px; flex-wrap: wrap; }
.reply { margin-top: 8px; background: #f9fafb; border-radius: 8px; padding: 8px 10px; font-size: 13px; }
.ops { margin-top: 10px; display: flex; gap: 8px; justify-content: flex-end; }
.small { font-size: 12px; }
.add-row { display: flex; gap: 8px; margin: 14px 0 18px; }
.kw { display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: 1px dashed var(--ygc-border); }
.kw-main { display: flex; align-items: center; gap: 8px; }
.kw-ops { display: flex; align-items: center; gap: 6px; }
</style>

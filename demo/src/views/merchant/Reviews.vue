<template>
  <div class="console-page">
    <el-alert class="mb" type="info" :closable="false" show-icon
      title="评价与回复均需平台审核通过后才会在前台展示" />

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="暂无用户评价" @retry="load"
      >
        <div v-for="r in list" :key="r.id" class="review">
          <div class="review-head">
            <b>{{ r.userName }}</b>
            <el-rate :model-value="r.score" disabled size="small" />
            <span class="muted small">{{ r.createdAt }}</span>
            <el-tag class="right" size="small" :type="AUDIT_STATUS[r.status]?.type">{{ r.status }}</el-tag>
          </div>
          <p class="content">{{ r.content }}</p>
          <div class="muted small">商品：{{ r.productName }}</div>
          <div v-if="r.reply" class="reply"><b>我的回复：</b>{{ r.reply }}</div>
          <div class="ops">
            <el-button size="small" @click="openReply(r)">{{ r.reply ? '修改回复' : '回复评价' }}</el-button>
            <span v-if="r.status === '待人工复核'" class="muted small">等待平台审核中，暂不可在前台展示</span>
          </div>
        </div>
      </AsyncBlock>
    </div>

    <el-dialog v-model="dialog" title="回复评价" width="480px">
      <p class="muted small">评价原文：{{ editing?.content }}</p>
      <el-input v-model="replyText" type="textarea" :rows="4" maxlength="200" show-word-limit
        placeholder="回复内容同样需要平台审核后展示" />
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchReviews } from '../../mock/api'
import { AUDIT_STATUS } from '../../mock/config'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const list = ref([])
const dialog = ref(false)
const editing = ref(null)
const replyText = ref('')
const submitting = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchReviews({ merchantId: store.state.user?.id || 1, size: 50 })
    list.value = res.list
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function openReply(r) {
  editing.value = r
  replyText.value = r.reply || ''
  dialog.value = true
}

async function onSubmit() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }
  submitting.value = true
  try {
    await store.replyReview(editing.value.id, replyText.value)
    dialog.value = false
    ElMessage.success('回复已提交，等待平台审核')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.review { border-bottom: 1px solid var(--ygc-border); padding: 14px 0; }
.review:last-child { border-bottom: none; }
.review-head { display: flex; align-items: center; gap: 10px; }
.right { margin-left: auto; }
.content { margin: 8px 0 6px; line-height: 1.7; }
.reply { margin-top: 8px; background: #f9fafb; border-radius: 8px; padding: 8px 10px; font-size: 13px; }
.ops { margin-top: 10px; display: flex; align-items: center; gap: 10px; }
.small { font-size: 12px; }
</style>

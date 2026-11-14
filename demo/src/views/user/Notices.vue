<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">公告与通知</h2>
        <div class="page-sub">平台公告与订单站内消息</div>
      </div>
      <el-button size="small" :disabled="store.unreadCount.value === 0" @click="onReadAll">全部标为已读</el-button>
    </div>

    <div class="layout">
      <div class="card">
        <div class="block-title">平台公告</div>
        <AsyncBlock :loading="loading" :error="error" :empty="!loading && list.length === 0" empty-text="暂无公告" @retry="load">
          <el-timeline>
            <el-timeline-item v-for="n in list" :key="n.id" :timestamp="n.createdAt" placement="top">
              <div class="notice">
                <div class="notice-head">
                  <b>{{ n.title }}</b>
                  <el-tag size="small" :type="n.status === '已发布' ? 'success' : 'info'">{{ n.status }}</el-tag>
                </div>
                <p class="muted">{{ n.content }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </AsyncBlock>
      </div>

      <div class="card">
        <div class="block-title">
          站内消息
          <el-tag v-if="store.unreadCount.value" size="small" type="danger">{{ store.unreadCount.value }} 条未读</el-tag>
        </div>
        <div v-for="m in store.state.notifications" :key="m.id" class="msg" :class="{ unread: !m.read }">
          <div class="msg-head">
            <el-tag size="small">{{ m.type }}</el-tag>
            <b>{{ m.title }}</b>
            <span class="muted small">{{ m.createdAt }}</span>
          </div>
          <p class="muted small">{{ m.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchNotices } from '../../mock/api'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const list = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const all = await fetchNotices()
    list.value = all.filter((n) => n.status === '已发布')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function onReadAll() {
  store.markAllRead()
  ElMessage.success('已全部标为已读')
}

onMounted(load)
</script>

<style scoped>
.layout { display: grid; grid-template-columns: 1.3fr 1fr; gap: 16px; align-items: start; }
.block-title { font-weight: 600; margin-bottom: 14px; display: flex; align-items: center; gap: 8px; }
.notice-head { display: flex; align-items: center; gap: 10px; }
.notice p { font-size: 13px; line-height: 1.7; margin: 6px 0 0; }
.msg { border-bottom: 1px dashed var(--ygc-border); padding: 12px 0; }
.msg.unread { background: #fffbeb; margin: 0 -12px; padding: 12px; border-radius: 8px; }
.msg-head { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.msg p { margin: 6px 0 0; }
.small { font-size: 12px; }
</style>

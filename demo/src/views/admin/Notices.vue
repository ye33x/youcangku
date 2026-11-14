<template>
  <div class="console-page">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openEdit(null)">发布公告</el-button>
      <span class="muted" style="margin-left: auto">共 {{ list.length }} 条</span>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="还没有发布任何公告" empty-action="发布公告"
        @retry="load" @empty-action="openEdit(null)"
      >
        <el-table :data="list">
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="发布时间" width="170" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === '已发布' ? 'success' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button text size="small" @click="openEdit(row)">编辑</el-button>
              <el-button text size="small" :type="row.status === '已发布' ? 'warning' : 'primary'" @click="onToggle(row)">
                {{ row.status === '已发布' ? '下架' : '上架' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </AsyncBlock>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑公告' : '发布公告'" width="520px">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" maxlength="40" show-word-limit /></el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { notices } from '../../mock/db'
import { fakeRequest, sleep } from '../../mock/config'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const list = ref([])
const dialog = ref(false)
const editing = ref(null)
const form = reactive({ title: '', content: '' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await fakeRequest([...notices].sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1)), {
      label: '加载公告'
    })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editing.value = row
  form.title = row?.title || ''
  form.content = row?.content || ''
  dialog.value = true
}

async function onSave() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('标题与内容不能为空')
    return
  }
  await sleep(400)
  if (editing.value) {
    editing.value.title = form.title
    editing.value.content = form.content
    store.addLog('NOTICE_UPDATE', '管理员', '公告 ' + form.title)
    ElMessage.success('公告已更新')
  } else {
    notices.unshift({
      id: Date.now(),
      title: form.title,
      content: form.content,
      status: '已发布',
      createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
    store.addLog('NOTICE_PUBLISH', '管理员', '公告 ' + form.title)
    ElMessage.success('公告已发布')
  }
  dialog.value = false
  load()
}

function onToggle(row) {
  row.status = row.status === '已发布' ? '已下架' : '已发布'
  store.addLog(row.status === '已发布' ? 'NOTICE_PUBLISH' : 'NOTICE_OFFLINE', '管理员', '公告 ' + row.title)
  ElMessage.success(row.status === '已发布' ? '已重新上架' : '已下架')
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
</style>

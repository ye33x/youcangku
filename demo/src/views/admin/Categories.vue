<template>
  <div class="console-page">
    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openAdd(null)">新增一级分类</el-button>
      <span class="muted" style="margin-left: auto">删除校验：分类下存在商品时禁止删除</span>
    </div>

    <div class="card-panel">
      <AsyncBlock :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0" empty-text="暂无分类" @retry="load">
        <el-table :data="tree" row-key="id" default-expand-all>
          <el-table-column prop="name" label="分类名称" min-width="200" />
          <el-table-column prop="sort" label="排序" width="90" />
          <el-table-column label="商品数" width="100">
            <template #default="{ row }">
              <span :class="{ 'warn-text': productCount(row.id) > 0 }">{{ productCount(row.id) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'info'">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.parentId === 0" text size="small" @click="openAdd(row)">新增子类</el-button>
              <el-button text size="small" @click="openEdit(row)">编辑</el-button>
              <el-button text size="small" type="danger" @click="onDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </AsyncBlock>
    </div>

    <el-dialog v-model="dialog" :title="editing ? '编辑分类' : '新增分类'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="上级分类"><el-input :model-value="parentName" disabled /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" maxlength="12" show-word-limit /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchCategories } from '../../mock/api'
import { categories, products } from '../../mock/db'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const list = ref([])
const dialog = ref(false)
const editing = ref(null)
const parent = ref(null)
const form = reactive({ name: '', sort: 0 })

const tree = computed(() => {
  const tops = list.value.filter((c) => c.parentId === 0)
  return tops.map((t) => ({ ...t, children: list.value.filter((c) => c.parentId === t.id) }))
})

const parentName = computed(() => (parent.value ? parent.value.name : '顶级分类'))

// 一级分类要连子分类的商品一起算，删除校验才准确
const productCount = (categoryId) =>
  products.filter((p) => {
    if (p.categoryId === categoryId) return true
    const cat = categories.find((c) => c.id === p.categoryId)
    return cat && cat.parentId === categoryId
  }).length

async function load() {
  loading.value = true
  error.value = ''
  try {
    const tops = await fetchCategories()
    list.value = tops.flatMap((t) => [t, ...t.children])
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function openAdd(row) {
  editing.value = null
  parent.value = row
  form.name = ''
  form.sort = 0
  dialog.value = true
}

function openEdit(row) {
  editing.value = row
  parent.value = row.parentId ? categories.find((c) => c.id === row.parentId) : null
  form.name = row.name
  form.sort = row.sort
  dialog.value = true
}

function onSave() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (editing.value) {
    editing.value.name = form.name
    editing.value.sort = form.sort
    ElMessage.success('分类已更新')
  } else {
    const id = Math.max(...categories.map((c) => c.id)) + 1
    const created = { id, parentId: parent.value?.id || 0, name: form.name, sort: form.sort, status: 1 }
    categories.push(created)
    list.value.push(created)
    ElMessage.success('分类已新增')
  }
  dialog.value = false
}

async function onDelete(row) {
  const count = products.filter((p) => p.categoryId === row.id).length
  if (count > 0) {
    ElMessageBox.alert(`「${row.name}」下还有 ${count} 件商品，禁止删除。请先将商品转移到其他分类。`,
      '无法删除', { type: 'error', confirmButtonText: '知道了' })
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除分类「${row.name}」？`, '删除分类', { type: 'warning' })
    const idx = categories.findIndex((c) => c.id === row.id)
    if (idx >= 0) categories.splice(idx, 1)
    list.value = list.value.filter((c) => c.id !== row.id)
    store.addLog('CATEGORY_DELETE', '管理员', '分类 ' + row.name)
    ElMessage.success('分类已删除')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.warn-text { color: #dc2626; font-weight: 600; }
</style>

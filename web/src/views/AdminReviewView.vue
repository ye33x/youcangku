<template>
  <div>
    <div class="toolbar">
      <h3>评价审核</h3>
      <el-tag type="warning">待审核 {{ pendingCount }}</el-tag>
      <el-radio-group v-model="status" @change="load">
        <el-radio-button :value="0">待审核</el-radio-button>
        <el-radio-button :value="1">已通过</el-radio-button>
        <el-radio-button :value="2">已拒绝</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="rows" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="productId" label="商品" width="90" />
      <el-table-column prop="userId" label="用户" width="90" />
      <el-table-column prop="score" label="评分" width="80" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" @click="audit(row.id, true)">通过</el-button>
            <el-button size="small" type="danger" @click="audit(row.id, false)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const rows = ref([])
const status = ref(0)
const pendingCount = ref(0)

const statusText = (value) => ['待审核', '已通过', '已拒绝'][value] ?? value

async function load() {
  rows.value = (await request.get('/admin/reviews', { params: { status: status.value, size: 50 } })).list
  pendingCount.value = await request.get('/admin/reviews/pending-count')
}

async function audit(id, approve) {
  await request.put(`/admin/reviews/${id}/audit`, { approve, remark: approve ? '内容正常' : '内容不符合规范' })
  ElMessage.success(approve ? '已通过' : '已拒绝')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
</style>

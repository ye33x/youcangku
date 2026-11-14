<template>
  <div class="console-page">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品名称" clearable style="width: 240px"
        @keyup.enter="reload" @clear="reload" />
      <el-select v-model="status" placeholder="全部状态" clearable style="width: 130px" @change="reload">
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Plus" @click="$router.push('/merchant/product/new')">发布商品</el-button>
      <span class="muted" style="margin-left: auto">共 {{ total }} 件</span>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        empty-text="还没有商品，发布第一件干果吧" empty-action="发布商品"
        @retry="reload" @empty-action="$router.push('/merchant/product/new')"
      >
        <el-table :data="list" size="default">
          <el-table-column label="商品" min-width="260">
            <template #default="{ row }">
              <div class="cell-product">
                <div class="mini-thumb">{{ row.emoji }}</div>
                <div>
                  <div>{{ row.name }}</div>
                  <div class="muted small">{{ row.spec }} · {{ row.originPlace }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="价格" width="120">
            <template #default="{ row }"><span class="price">{{ money(row.price) }}</span></template>
          </el-table-column>
          <el-table-column prop="stock" label="库存" width="90">
            <template #default="{ row }"><span :class="{ 'warn-text': row.stock <= 0 }">{{ row.stock }}</span></template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="90" sortable />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1" size="small" @change="onToggle(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button text size="small" @click="$router.push('/merchant/product/' + row.id)">编辑</el-button>
              <el-popconfirm title="确认删除该商品？" @confirm="onDelete(row)">
                <template #reference><el-button text size="small" type="danger">删除</el-button></template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager">
          <el-pagination v-model:current-page="page" :page-size="size" :total="total"
            layout="total, prev, pager, next" background @current-change="reload" />
        </div>
      </AsyncBlock>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchProducts } from '../../mock/api'
import { money } from '../../mock/config'
import store from '../../store'

const keyword = ref('')
const status = ref(null)
const loading = ref(false)
const error = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 8

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchProducts({
      merchantId: store.state.user?.id || 1,
      includeOffline: true,
      keyword: keyword.value,
      page: page.value,
      size
    })
    let rows = res.list
    if (status.value !== null && status.value !== '') rows = rows.filter((r) => r.status === status.value)
    list.value = rows
    total.value = res.total
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function onToggle(row) {
  if (row.stock <= 0 && row.status === 0) {
    ElMessage.warning('库存为 0，无法上架')
    return
  }
  store.toggleProductStatus(row.id)
  ElMessage.success(row.status === 1 ? '已上架' : '已下架')
}

function onDelete(row) {
  row.status = 0
  ElMessage.success('已删除（演示环境软删除）')
}

onMounted(reload)
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.cell-product { display: flex; align-items: center; gap: 10px; }
.mini-thumb { width: 42px; height: 42px; border-radius: 8px; background: linear-gradient(135deg, #fde68a, #fbbf24); display: flex; align-items: center; justify-content: center; font-size: 20px; }
.small { font-size: 12px; }
.warn-text { color: #dc2626; font-weight: 600; }
.pager { display: flex; justify-content: flex-end; margin-top: 14px; }
</style>

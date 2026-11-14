<template>
  <div>
    <div class="toolbar">
      <h3>猜你喜欢</h3>
      <el-tag v-if="strategy" type="success">{{ strategy }}</el-tag>
      <el-button size="small" @click="load">刷新</el-button>
    </div>

    <el-empty v-if="!loading && items.length === 0" description="暂无推荐，先去逛逛干果吧" />

    <el-row :gutter="16">
      <el-col v-for="item in items" :key="item.productId" :span="6">
        <el-card class="product-card" shadow="hover">
          <div class="name">{{ item.name }}</div>
          <div class="price">¥{{ item.price }}</div>
          <div class="meta">{{ item.originPlace }} · {{ item.spec }}</div>
          <!-- 推荐理由：可解释性的直接体现 -->
          <div class="reason">{{ item.reason }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '../api/request'

const items = ref([])
const loading = ref(false)
const strategy = ref('')

async function load() {
  loading.value = true
  try {
    items.value = await request.get('/user/recommend/home')
    strategy.value = items.value.length ? items.value[0].strategyLabel : ''
  } finally {
    loading.value = false
  }
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
.product-card {
  margin-bottom: 16px;
}
.name {
  font-weight: 600;
  min-height: 44px;
}
.price {
  color: #e5484d;
  font-size: 18px;
  margin: 8px 0;
}
.meta {
  color: #6b7280;
  font-size: 12px;
}
.reason {
  margin-top: 8px;
  padding: 6px 8px;
  background: #f3f4f6;
  border-radius: 4px;
  font-size: 12px;
  color: #4b5563;
}
</style>

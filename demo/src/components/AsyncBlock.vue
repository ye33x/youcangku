<template>
  <!-- 统一承载「加载中 / 空状态 / 错误提示」三种界面态，避免每个页面重复写 -->
  <div v-if="loading" class="state-wrap">
    <el-skeleton :rows="rows" animated />
  </div>
  <div v-else-if="error" class="state-wrap">
    <el-result icon="error" title="加载失败" :sub-title="error">
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重新加载</el-button>
      </template>
    </el-result>
  </div>
  <div v-else-if="empty" class="state-wrap">
    <el-empty :description="emptyText">
      <el-button v-if="emptyAction" type="primary" @click="$emit('empty-action')">{{ emptyAction }}</el-button>
    </el-empty>
  </div>
  <slot v-else />
</template>

<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' },
  empty: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无数据' },
  emptyAction: { type: String, default: '' },
  rows: { type: Number, default: 4 }
})
defineEmits(['retry', 'empty-action'])
</script>

<style scoped>
.state-wrap { padding: 24px 0; }
</style>

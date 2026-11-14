<template>
  <div class="console-page">
    <div class="toolbar">
      <el-button :icon="Back" @click="$router.push('/merchant/products')">返回列表</el-button>
      <span class="muted">{{ isEdit ? '编辑商品' : '发布新商品' }}</span>
    </div>

    <div class="card-panel">
      <el-form :model="form" label-width="96px" style="max-width: 720px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" maxlength="60" show-word-limit placeholder="如：新疆阿克苏薄皮核桃 500g" />
        </el-form-item>
        <el-form-item label="商品分类" required>
          <el-cascader v-model="form.categoryPath" :options="categoryOptions"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            style="width: 100%" placeholder="请选择一级 / 二级分类" />
        </el-form-item>
        <el-form-item label="售价" required>
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          <span class="muted" style="margin-left: 10px">元</span>
        </el-form-item>
        <el-form-item label="划线价">
          <el-input-number v-model="form.originPrice" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="产地"><el-input v-model="form.originPlace" placeholder="如：新疆阿克苏" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="form.spec" placeholder="如：500g/袋" /></el-form-item>
        <el-form-item label="库存" required>
          <el-input-number v-model="form.stock" :min="0" :step="10" />
          <span v-if="form.stock === 0" class="warn-text" style="margin-left: 10px">库存为 0 时商品无法上架</span>
        </el-form-item>
        <el-form-item label="商品图">
          <div class="emoji-picker">
            <span v-for="e in emojis" :key="e" class="emoji" :class="{ active: form.emoji === e }" @click="form.emoji = e">{{ e }}</span>
            <span class="muted small" style="margin-left: 8px">（演示环境用表情占位，真实项目为图片上传）</span>
          </div>
        </el-form-item>
        <el-form-item label="图文介绍">
          <el-input v-model="form.detail" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
          <el-button @click="$router.push('/merchant/products')">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back } from '@element-plus/icons-vue'
import { categories, products } from '../../mock/db'
import store from '../../store'

const route = useRoute()
const router = useRouter()
const saving = ref(false)

const isEdit = computed(() => !!route.params.id)
const emojis = ['🌰', '🥜', '🍇', '🫘', '🌻', '🎃', '🥭', '🥣', '🎁']

const form = reactive({
  id: null, name: '', categoryPath: [1, 11], price: 39.9, originPrice: 59.9,
  originPlace: '', spec: '', stock: 100, emoji: '🌰', detail: ''
})

const categoryOptions = categories
  .filter((c) => c.parentId === 0)
  .map((c) => ({ ...c, children: categories.filter((x) => x.parentId === c.id) }))

onMounted(() => {
  if (!isEdit.value) return
  const p = products.find((x) => x.id === Number(route.params.id))
  if (!p) {
    ElMessage.error('商品不存在')
    router.push('/merchant/products')
    return
  }
  const parent = categories.find((c) => c.id === p.categoryId)?.parentId || 1
  Object.assign(form, {
    id: p.id, name: p.name, categoryPath: [parent, p.categoryId], price: p.price,
    originPrice: p.originPrice, originPlace: p.originPlace, spec: p.spec,
    stock: p.stock, emoji: p.emoji, detail: p.detail
  })
})

async function onSave() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写商品名称')
    return
  }
  if (!form.price || form.price <= 0) {
    ElMessage.warning('售价必须大于 0')
    return
  }
  saving.value = true
  await new Promise((r) => setTimeout(r, 600))
  store.saveProduct({
    id: form.id, name: form.name,
    categoryId: form.categoryPath?.[1] || form.categoryPath?.[0],
    price: form.price, originPrice: form.originPrice, originPlace: form.originPlace,
    spec: form.spec, stock: form.stock, emoji: form.emoji, detail: form.detail
  })
  saving.value = false
  ElMessage.success(isEdit.value ? '商品已更新' : '商品已发布（默认上架）')
  router.push('/merchant/products')
}
</script>

<style scoped>
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 22px; }
.emoji-picker { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.emoji { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; font-size: 22px; border: 1px solid var(--ygc-border); border-radius: 8px; cursor: pointer; }
.emoji.active { border-color: #b45309; background: #fffbeb; }
.warn-text { color: #dc2626; font-size: 12px; }
.small { font-size: 12px; }
</style>

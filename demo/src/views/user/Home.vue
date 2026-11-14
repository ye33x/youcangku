<template>
  <div class="page">
    <section class="banner">
      <div>
        <div class="tag">双十一 · 干果专场</div>
        <h2>新疆产地直采，满 99 减 20</h2>
        <p class="muted">每日坚果 / 薄皮核桃 / 吐鲁番葡萄干 现货速发</p>
        <el-button type="primary" @click="$router.push('/user/products')">去逛逛</el-button>
      </div>
      <div class="banner-emoji">🥜</div>
    </section>

    <section class="cats">
      <div v-for="c in cats" :key="c.id" class="cat" @click="$router.push('/user/products?categoryId=' + c.id)">
        <div class="cat-icon">{{ c.icon }}</div>
        <div>{{ c.name }}</div>
      </div>
    </section>

    <div class="section-title">
      <h3>猜你喜欢</h3>
      <div class="row-gap" style="align-items: center">
        <el-tag :type="coldStart ? 'info' : 'success'" size="small">
          {{ coldStart ? '冷启动 · 热门兜底' : '基于物品的协同过滤' }}
        </el-tag>
        <el-switch v-model="coldStart" size="small" active-text="模拟新用户" @change="loadRecommend" />
      </div>
    </div>

    <AsyncBlock
      :loading="loading" :error="error"
      :empty="!loading && !error && recList.length === 0"
      empty-text="暂时没有推荐商品" empty-action="刷新看看"
      @retry="loadRecommend" @empty-action="loadRecommend"
    >
      <div class="grid">
        <ProductCard v-for="p in recList" :key="p.id" :product="p" @click="$router.push('/user/product/' + p.id)">
          <template #actions>
            <el-button size="small" type="primary" @click.stop="addToCart(p)">加入购物车</el-button>
          </template>
        </ProductCard>
      </div>
    </AsyncBlock>

    <div class="section-title">
      <h3>分类热销</h3>
      <el-link type="primary" :underline="false" @click="$router.push('/user/products')">查看全部</el-link>
    </div>

    <div class="hot-row">
      <div v-for="(p, i) in hotList" :key="p.id" class="hot-item" @click="$router.push('/user/product/' + p.id)">
        <div class="rank">{{ i + 1 }}</div>
        <div class="hot-thumb">{{ p.emoji }}</div>
        <div class="hot-info">
          <div class="hot-name">{{ p.name }}</div>
          <div class="price">{{ money(p.price) }}</div>
        </div>
      </div>
    </div>

    <p class="demo-hint mt-16">
      演示提示：打开右下角「演示控制」可调整接口延迟、错误率与空数据模式，观察本页的加载 / 错误 / 空状态。
    </p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import ProductCard from '../../components/ProductCard.vue'
import { fetchRecommend, fetchProducts } from '../../mock/api'
import { money } from '../../mock/config'
import store from '../../store'

const loading = ref(false)
const error = ref('')
const recList = ref([])
const hotList = ref([])
const coldStart = ref(false)

const cats = [
  { id: 1, name: '坚果炒货', icon: '🌰' },
  { id: 2, name: '果干蜜饯', icon: '🍇' },
  { id: 3, name: '籽类', icon: '🌻' },
  { id: 4, name: '礼盒装', icon: '🎁' }
]

async function loadRecommend() {
  loading.value = true
  error.value = ''
  try {
    recList.value = await fetchRecommend({ hasBehavior: !coldStart.value, limit: 8 })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function loadHot() {
  try {
    const res = await fetchProducts({ sort: 'sales', page: 1, size: 5 })
    hotList.value = res.list
  } catch (e) {
    /* 热销区失败不阻塞首屏 */
  }
}

function addToCart(p) {
  if (p.stock <= 0) {
    ElMessage.warning('该商品暂时缺货')
    return
  }
  store.addToCart(p.id, 1)
  ElMessage.success('已加入购物车：' + p.name)
}

onMounted(() => {
  loadRecommend()
  loadHot()
})
</script>

<style scoped>
.banner { background: linear-gradient(120deg, #fff7ed, #fef3c7); border: 1px solid #fde68a; border-radius: 16px; padding: 28px 32px; display: flex; align-items: center; justify-content: space-between; }
.banner h2 { margin: 10px 0 6px; font-size: 24px; }
.tag { display: inline-block; background: #b45309; color: #fff; font-size: 12px; padding: 3px 10px; border-radius: 999px; }
.banner-emoji { font-size: 84px; }
.cats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-top: 18px; }
.cat { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 18px; text-align: center; cursor: pointer; transition: all 0.2s; }
.cat:hover { border-color: #f59e0b; color: #b45309; }
.cat-icon { font-size: 28px; margin-bottom: 6px; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.hot-row { display: flex; gap: 12px; flex-wrap: wrap; }
.hot-item { flex: 1; min-width: 200px; display: flex; align-items: center; gap: 10px; background: #fff; border: 1px solid var(--ygc-border); border-radius: 10px; padding: 10px 12px; cursor: pointer; }
.rank { width: 20px; height: 20px; border-radius: 50%; background: #f59e0b; color: #fff; font-size: 12px; display: flex; align-items: center; justify-content: center; }
.hot-thumb { font-size: 26px; }
.hot-name { font-size: 13px; line-height: 1.4; }
</style>

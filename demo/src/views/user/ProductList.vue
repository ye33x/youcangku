<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">全部商品</h2>
        <div class="page-sub">
          共 <b>{{ total }}</b> 件商品
          <span v-if="query.keyword">，关键词「{{ query.keyword }}」</span>
        </div>
      </div>
      <el-radio-group v-model="sort" size="small" @change="reload">
        <el-radio-button value="sales">销量优先</el-radio-button>
        <el-radio-button value="price_asc">价格从低到高</el-radio-button>
        <el-radio-button value="price_desc">价格从高到低</el-radio-button>
        <el-radio-button value="new">最新上架</el-radio-button>
      </el-radio-group>
    </div>

    <div class="layout">
      <aside class="filters card">
        <div class="filter-title">分类</div>
        <el-tree :data="treeData" :props="{ label: 'name', children: 'children' }" highlight-current
          :expand-on-click-node="false" @node-click="onCategoryClick" />
        <div class="filter-title mt-16">价格区间</div>
        <div class="row-gap">
          <el-button v-for="p in priceRanges" :key="p.label" size="small"
            :type="rangeActive === p.label ? 'primary' : 'default'" @click="onPriceRange(p)">
            {{ p.label }}
          </el-button>
        </div>
        <div class="filter-title mt-16">产地</div>
        <el-select v-model="origin" placeholder="全部产地" clearable size="small" style="width: 100%" @change="reload">
          <el-option v-for="o in origins" :key="o" :label="o" :value="o" />
        </el-select>
        <el-button class="mt-16" size="small" style="width: 100%" @click="resetFilters">重置筛选</el-button>
      </aside>

      <section>
        <AsyncBlock
          :loading="loading" :error="error"
          :empty="!loading && !error && list.length === 0"
          empty-text="没有符合条件的商品，换个筛选条件试试" empty-action="重置筛选" :rows="5"
          @retry="reload" @empty-action="resetFilters"
        >
          <div class="grid">
            <ProductCard v-for="p in list" :key="p.id" :product="p" @click="$router.push('/user/product/' + p.id)">
              <template #actions>
                <el-button size="small" type="primary" :disabled="p.stock <= 0" @click.stop="addToCart(p)">
                  {{ p.stock <= 0 ? '缺货' : '加入购物车' }}
                </el-button>
              </template>
            </ProductCard>
          </div>
          <div class="pager">
            <el-pagination v-model:current-page="page" :page-size="size" :total="total"
              layout="prev, pager, next" background @current-change="reload" />
          </div>
        </AsyncBlock>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import ProductCard from '../../components/ProductCard.vue'
import { fetchCategories, fetchProducts } from '../../mock/api'
import store from '../../store'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 8
const sort = ref('sales')
const origin = ref('')
const rangeActive = ref('')
const priceRange = reactive({ min: null, max: null })
const query = reactive({ keyword: '', categoryId: null })
const cats = ref([])

const priceRanges = [
  { label: '20 元以下', min: null, max: 20 },
  { label: '20-40 元', min: 20, max: 40 },
  { label: '40-80 元', min: 40, max: 80 },
  { label: '80 元以上', min: 80, max: null }
]

const origins = ['新疆', '云南', '美国', '伊朗', '内蒙古', '甘肃', '广西', '多产地']
const treeData = computed(() => [{ id: null, name: '全部分类', children: cats.value }])

async function loadCats() {
  try {
    cats.value = await fetchCategories()
  } catch (e) {
    /* 分类失败不影响列表 */
  }
}

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchProducts({
      keyword: query.keyword, categoryId: query.categoryId,
      minPrice: priceRange.min, maxPrice: priceRange.max, origin: origin.value,
      sort: sort.value, page: page.value, size
    })
    list.value = res.list
    total.value = res.total
  } catch (e) {
    error.value = e.message
    list.value = []
  } finally {
    loading.value = false
  }
}

function onCategoryClick(node) {
  query.categoryId = node.id
  page.value = 1
  reload()
}

function onPriceRange(p) {
  if (rangeActive.value === p.label) {
    rangeActive.value = ''
    priceRange.min = null
    priceRange.max = null
  } else {
    rangeActive.value = p.label
    priceRange.min = p.min
    priceRange.max = p.max
  }
  page.value = 1
  reload()
}

function resetFilters() {
  query.categoryId = null
  query.keyword = ''
  rangeActive.value = ''
  priceRange.min = null
  priceRange.max = null
  origin.value = ''
  page.value = 1
  router.replace('/user/products')
  reload()
}

function addToCart(p) {
  store.addToCart(p.id, 1)
  ElMessage.success('已加入购物车')
}

watch(
  () => route.query,
  (q) => {
    query.keyword = q.keyword || ''
    query.categoryId = q.categoryId ? Number(q.categoryId) : null
    page.value = 1
    reload()
  },
  { immediate: true }
)

onMounted(loadCats)
</script>

<style scoped>
.layout { display: grid; grid-template-columns: 240px 1fr; gap: 18px; align-items: start; }
.filters { position: sticky; top: 84px; }
.filter-title { font-size: 13px; font-weight: 600; margin-bottom: 10px; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
.pager { display: flex; justify-content: center; margin-top: 20px; }
</style>

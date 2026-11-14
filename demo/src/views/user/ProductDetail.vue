<template>
  <div class="page">
    <el-breadcrumb separator="/" class="crumb">
      <el-breadcrumb-item :to="{ path: '/user/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/user/products' }">全部商品</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product?.name || '商品详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <AsyncBlock :loading="loading" :error="error" :empty="false" :rows="6" @retry="load">
      <div v-if="product" class="detail">
        <div class="thumb lg">{{ product.emoji }}</div>
        <div class="info">
          <h1>{{ product.name }}</h1>
          <div class="price-box">
            <span class="price big">{{ money(product.price) }}</span>
            <span class="price-origin">{{ money(product.originPrice) }}</span>
            <el-tag size="small" type="danger" effect="dark" style="margin-left: 10px">限时特惠</el-tag>
          </div>

          <el-descriptions :column="2" border size="small" class="desc">
            <el-descriptions-item label="产地">{{ product.originPlace }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ product.spec }}</el-descriptions-item>
            <el-descriptions-item label="库存">
              <span :class="{ 'out-stock': product.stock <= 0 }">
                {{ product.stock > 0 ? product.stock + ' 件' : '暂时缺货' }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="累计销量">{{ product.sales }} 件</el-descriptions-item>
          </el-descriptions>

          <div class="buy-row">
            <span class="muted">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="Math.max(product.stock, 1)" size="default" />
            <el-button type="primary" :icon="ShoppingCart" :disabled="product.stock <= 0"
              :loading="adding" @click="onAddCart">加入购物车</el-button>
            <el-button :disabled="product.stock <= 0" @click="onBuyNow">立即购买</el-button>
            <el-button :icon="starIcon" text @click="onFavorite">{{ isFav ? '已收藏' : '收藏' }}</el-button>
          </div>

          <p v-if="product.stock <= 0" class="demo-hint mt-16">
            这是「零库存」演示商品：加购与立即购买按钮应处于禁用态。
          </p>
        </div>
      </div>

      <el-card class="mt-16" v-if="product">
        <el-tabs v-model="tab">
          <el-tab-pane label="商品详情" name="detail">
            <p class="detail-text">{{ product.detail }}</p>
            <p class="detail-text muted">
              产地：{{ product.originPlace }}　规格：{{ product.spec }}　储存：阴凉干燥处密封保存
            </p>
          </el-tab-pane>
          <el-tab-pane :label="`用户评价（${approvedReviews.length}）`" name="reviews">
            <el-empty v-if="approvedReviews.length === 0" description="还没有已通过审核的评价" />
            <div v-for="r in approvedReviews" :key="r.id" class="review">
              <div class="review-head">
                <b>{{ r.userName }}</b>
                <el-rate :model-value="r.score" disabled size="small" />
                <span class="muted">{{ r.createdAt }}</span>
              </div>
              <p>{{ r.content }}</p>
              <div v-if="r.reply" class="reply">商家回复：{{ r.reply }}</div>
            </div>
          </el-tab-pane>
          <el-tab-pane label="售后说明" name="after">
            <p class="detail-text">支持 7 天无理由退货（食品类需未拆封）。收到货 48 小时内可申请破损补发。</p>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </AsyncBlock>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, Star, StarFilled } from '@element-plus/icons-vue'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchProduct, fetchReviews } from '../../mock/api'
import { money } from '../../mock/config'
import store from '../../store'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref('')
const product = ref(null)
const quantity = ref(1)
const adding = ref(false)
const tab = ref('detail')
const allReviews = ref([])

const isFav = computed(() => (product.value ? store.isFavorite(product.value.id) : false))
const starIcon = computed(() => (isFav.value ? StarFilled : Star))
const approvedReviews = computed(() =>
  allReviews.value.filter((r) => r.status === '已通过' && r.productId === product.value?.id)
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    product.value = await fetchProduct(route.params.id)
    const res = await fetchReviews({ size: 100 })
    allReviews.value = res.list
  } catch (e) {
    error.value = e.message
    product.value = null
  } finally {
    loading.value = false
  }
}

async function onAddCart() {
  adding.value = true
  await new Promise((r) => setTimeout(r, 400))
  store.addToCart(product.value.id, quantity.value)
  adding.value = false
  ElMessage.success(`已加入购物车 ${quantity.value} 件`)
}

function onBuyNow() {
  router.push(`/user/checkout?buyNow=${product.value.id}&qty=${quantity.value}`)
}

function onFavorite() {
  store.toggleFavorite(product.value.id)
  ElMessage.success(isFav.value ? '已收藏' : '已取消收藏')
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<style scoped>
.crumb { margin-bottom: 16px; }
.detail { display: grid; grid-template-columns: 380px 1fr; gap: 28px; background: #fff; border: 1px solid var(--ygc-border); border-radius: 14px; padding: 24px; }
.info h1 { margin: 0 0 14px; font-size: 22px; }
.price-box { background: #fef2f2; border-radius: 10px; padding: 14px 16px; margin-bottom: 18px; }
.price.big { font-size: 30px; }
.desc { margin-bottom: 18px; }
.out-stock { color: #dc2626; }
.buy-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.detail-text { line-height: 1.9; font-size: 14px; }
.review { padding: 12px 0; border-bottom: 1px dashed var(--ygc-border); }
.review-head { display: flex; align-items: center; gap: 10px; font-size: 13px; }
.reply { margin-top: 6px; background: #f9fafb; border-radius: 6px; padding: 8px 10px; font-size: 13px; color: #4b5563; }
</style>

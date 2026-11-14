<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">购物车</h2>
        <div class="page-sub">共 {{ store.state.cart.length }} 种商品，已选 {{ store.checkedCount.value }} 种</div>
      </div>
      <el-button v-if="items.length" text type="danger" @click="onClear">清空购物车</el-button>
    </div>

    <el-empty v-if="items.length === 0" description="购物车还是空的，去挑几样干果吧">
      <el-button type="primary" @click="$router.push('/user/products')">去逛逛</el-button>
    </el-empty>

    <template v-else>
      <div class="card list">
        <div class="list-head">
          <el-checkbox :model-value="allChecked" @change="onCheckAll">全选</el-checkbox>
          <span class="col-name">商品</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-sub">小计</span>
          <span class="col-op">操作</span>
        </div>

        <div v-for="it in items" :key="it.productId" class="row">
          <el-checkbox :model-value="it.checked" @change="(v) => store.toggleCartChecked(it.productId, v)" />
          <div class="col-name product" @click="$router.push('/user/product/' + it.product.id)">
            <div class="mini-thumb">{{ it.product.emoji }}</div>
            <div>
              <div class="pname">{{ it.product.name }}</div>
              <div class="muted small">{{ it.product.spec }} · {{ it.product.originPlace }}</div>
            </div>
          </div>
          <span class="col-price">{{ money(it.product.price) }}</span>
          <span class="col-qty">
            <el-input-number :model-value="it.quantity" :min="1" :max="it.product.stock" size="small"
              @change="(v) => store.updateCartQuantity(it.productId, v)" />
          </span>
          <span class="col-sub price">{{ money(it.product.price * it.quantity) }}</span>
          <span class="col-op">
            <el-popconfirm title="确认从购物车移除这件商品？" @confirm="onRemove(it)">
              <template #reference>
                <el-button text type="danger" size="small">移除</el-button>
              </template>
            </el-popconfirm>
          </span>
        </div>
      </div>

      <div class="settle card">
        <div class="left">
          <el-checkbox :model-value="allChecked" @change="onCheckAll">全选</el-checkbox>
          <span class="muted">已选 {{ store.checkedCount.value }} 件</span>
        </div>
        <div class="right">
          <span class="muted">合计：</span>
          <span class="price total">{{ money(store.checkedAmount.value) }}</span>
          <el-button type="primary" size="large" :disabled="store.checkedCount.value === 0" @click="onCheckout">
            去结算
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { products } from '../../mock/db'
import { money } from '../../mock/config'
import store from '../../store'

const router = useRouter()

const items = computed(() =>
  store.state.cart
    .map((c) => ({ ...c, product: products.find((p) => p.id === c.productId) }))
    .filter((x) => x.product)
)

const allChecked = computed(() => items.value.length > 0 && items.value.every((i) => i.checked))

function onCheckAll(value) {
  items.value.forEach((i) => store.toggleCartChecked(i.productId, value))
}

function onRemove(it) {
  store.removeFromCart(it.productId)
  ElMessage.success('已移除：' + it.product.name)
}

function onClear() {
  store.state.cart.splice(0, store.state.cart.length)
  ElMessage.success('购物车已清空')
}

function onCheckout() {
  router.push('/user/checkout')
}
</script>

<style scoped>
.list { padding: 0; overflow: hidden; }
.list-head, .row { display: grid; grid-template-columns: 40px 1fr 110px 150px 120px 80px; align-items: center; gap: 10px; padding: 14px 18px; }
.list-head { background: #faf7f2; font-size: 13px; color: var(--ygc-muted); border-bottom: 1px solid var(--ygc-border); }
.row + .row { border-top: 1px solid var(--ygc-border); }
.product { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.mini-thumb { width: 56px; height: 56px; flex: 0 0 56px; border-radius: 8px; background: linear-gradient(135deg, #fde68a, #fbbf24); display: flex; align-items: center; justify-content: center; font-size: 26px; }
.pname { font-size: 14px; }
.small { font-size: 12px; }
.col-sub.price { font-size: 15px; }
.settle { margin-top: 16px; display: flex; align-items: center; justify-content: space-between; position: sticky; bottom: 16px; }
.left, .right { display: flex; align-items: center; gap: 12px; }
.total { font-size: 24px; }
</style>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">确认订单</h2>
        <div class="page-sub">请核对收货信息与商品清单</div>
      </div>
      <el-steps :active="1" simple :align-center="true" class="steps">
        <el-step title="购物车" />
        <el-step title="确认订单" />
        <el-step title="完成" />
      </el-steps>
    </div>

    <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon class="mb" :closable="false" />

    <div class="card">
      <div class="block-title">收货地址</div>
      <div class="addr-list">
        <div v-for="a in store.state.addresses" :key="a.id" class="addr"
          :class="{ active: selectedAddressId === a.id }" @click="selectedAddressId = a.id">
          <div class="addr-name">
            {{ a.receiver }}<span class="muted">{{ a.phone }}</span>
            <el-tag v-if="a.isDefault" size="small" type="warning">默认</el-tag>
          </div>
          <div class="addr-detail">{{ a.region.join(' ') }} {{ a.detail }}</div>
        </div>
        <div class="addr add" @click="$router.push('/user/profile')">
          <el-icon><Plus /></el-icon>新增地址
        </div>
      </div>
    </div>

    <div class="card mt-16">
      <div class="block-title">商品清单</div>
      <div v-for="it in items" :key="it.productId" class="item">
        <div class="mini-thumb">{{ it.emoji }}</div>
        <div class="item-name">
          {{ it.name }}
          <div class="muted small">{{ it.spec }}</div>
        </div>
        <div class="muted">{{ money(it.price) }} × {{ it.quantity }}</div>
        <div class="price">{{ money(it.price * it.quantity) }}</div>
      </div>
    </div>

    <div class="card mt-16">
      <div class="block-title">订单备注</div>
      <el-input v-model="remark" type="textarea" :rows="2" maxlength="100" show-word-limit placeholder="选填，如：请尽快发货" />
      <div class="amount">
        <div><span class="muted">商品件数：</span>{{ totalQty }} 件</div>
        <div><span class="muted">运费：</span>¥0.00</div>
        <div class="pay">应付金额：<span class="price total">{{ money(totalAmount) }}</span></div>
      </div>
    </div>

    <div class="submit-bar">
      <span class="muted">提交后将进入「待付款」状态，可在订单页继续支付</span>
      <el-button type="primary" size="large" :loading="submitting" @click="onSubmit">
        {{ submitting ? '提交中…' : '提交订单' }}
      </el-button>
    </div>

    <el-dialog v-model="successVisible" title="下单成功" width="420px" :close-on-click-modal="false">
      <el-result icon="success" title="订单提交成功" :sub-title="'订单号：' + createdOrderNo">
        <template #extra>
          <el-button type="primary" @click="goPay">立即支付</el-button>
          <el-button @click="goOrders">查看订单</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { products } from '../../mock/db'
import { money } from '../../mock/config'
import store from '../../store'

const route = useRoute()
const router = useRouter()

const remark = ref('')
const submitting = ref(false)
const errorMsg = ref('')
const successVisible = ref(false)
const createdOrderNo = ref('')
const selectedAddressId = ref(store.state.addresses.find((a) => a.isDefault)?.id || store.state.addresses[0]?.id)

const items = computed(() => {
  if (route.query.buyNow) {
    const p = products.find((x) => x.id === Number(route.query.buyNow))
    const qty = Number(route.query.qty || 1)
    return p ? [{ productId: p.id, name: p.name, emoji: p.emoji, spec: p.spec, price: p.price, quantity: qty, merchantId: p.merchantId }] : []
  }
  return store.state.cart
    .filter((c) => c.checked)
    .map((c) => {
      const p = products.find((x) => x.id === c.productId)
      return p ? { productId: p.id, name: p.name, emoji: p.emoji, spec: p.spec, price: p.price, quantity: c.quantity, merchantId: p.merchantId } : null
    })
    .filter(Boolean)
})

const totalQty = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
const totalAmount = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))

async function onSubmit() {
  errorMsg.value = ''
  if (!selectedAddressId.value) {
    errorMsg.value = '请先选择收货地址'
    return
  }
  if (items.value.length === 0) {
    errorMsg.value = '没有可结算的商品'
    return
  }
  submitting.value = true
  try {
    const address = store.state.addresses.find((a) => a.id === selectedAddressId.value)
    const order = await store.createOrder({ items: items.value, address, remark: remark.value })
    createdOrderNo.value = order.orderNo
    successVisible.value = true
  } catch (e) {
    errorMsg.value = e.message
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

function goPay() {
  successVisible.value = false
  router.push('/user/orders')
  ElMessage.info('已跳到订单页，点「去支付」完成支付')
}

function goOrders() {
  successVisible.value = false
  router.push('/user/orders')
}

onMounted(() => {
  if (items.value.length === 0) ElMessage.warning('没有待结算的商品，请先加入购物车')
})
</script>

<style scoped>
.mb { margin-bottom: 14px; }
.block-title { font-weight: 600; margin-bottom: 12px; }
.addr-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.addr { border: 1px solid var(--ygc-border); border-radius: 10px; padding: 12px 14px; cursor: pointer; transition: all 0.2s; }
.addr.active { border-color: #b45309; background: #fffbeb; }
.addr.add { display: flex; align-items: center; justify-content: center; gap: 6px; color: var(--ygc-muted); border-style: dashed; }
.addr-name { font-weight: 600; display: flex; align-items: center; gap: 8px; }
.addr-detail { font-size: 12px; color: var(--ygc-muted); margin-top: 6px; line-height: 1.5; }
.item { display: grid; grid-template-columns: 56px 1fr 160px 120px; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px dashed var(--ygc-border); }
.item:last-child { border-bottom: none; }
.mini-thumb { width: 48px; height: 48px; border-radius: 8px; background: linear-gradient(135deg, #fde68a, #fbbf24); display: flex; align-items: center; justify-content: center; font-size: 22px; }
.small { font-size: 12px; }
.amount { margin-top: 16px; padding-top: 14px; border-top: 1px solid var(--ygc-border); display: flex; flex-direction: column; gap: 8px; align-items: flex-end; font-size: 14px; }
.total { font-size: 26px; }
.submit-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 18px; }
.steps { width: 520px; }
.steps :deep(.el-step__title) { white-space: nowrap; }
</style>

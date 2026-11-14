<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">我的订单</h2>
        <div class="page-sub">演示不同状态下的订单操作入口</div>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="reload">
      <el-tab-pane v-for="t in tabs" :key="t.value" :label="t.label" :name="t.value" />
    </el-tabs>

    <AsyncBlock
      :loading="loading" :error="error"
      :empty="!loading && !error && list.length === 0"
      :empty-text="'暂无' + currentTabLabel + '订单'" empty-action="去逛逛" :rows="4"
      @retry="reload" @empty-action="$router.push('/user/products')"
    >
      <div v-for="o in list" :key="o.id" class="order card">
        <div class="order-head">
          <span class="muted">订单号 {{ o.orderNo }}</span>
          <span class="muted">{{ o.createdAt }}</span>
          <el-tag :type="ORDER_STATUS[o.status].type" size="small" effect="light">
            {{ ORDER_STATUS[o.status].text }}
          </el-tag>
        </div>

        <div class="items">
          <div v-for="it in o.items" :key="it.productId" class="item" @click="$router.push('/user/product/' + it.productId)">
            <div class="mini-thumb">{{ it.emoji }}</div>
            <div class="item-info">
              <div>{{ it.name }}</div>
              <div class="muted small">{{ it.spec }}</div>
            </div>
            <div class="muted">{{ money(it.price) }} × {{ it.quantity }}</div>
            <div class="price">{{ money(it.amount) }}</div>
          </div>
        </div>

        <div class="order-foot">
          <div class="muted small">收货人：{{ o.receiver }}　{{ o.receiverAddr }}</div>
          <div class="actions">
            <span class="muted">实付：</span>
            <span class="price">{{ money(o.payAmount) }}</span>

            <template v-if="o.status === 0">
              <el-button size="small" @click="onCancel(o)">取消订单</el-button>
              <el-button size="small" type="primary" :loading="busyId === o.id" @click="onPay(o)">去支付</el-button>
            </template>
            <template v-else-if="o.status === 1">
              <el-button size="small" @click="onCancel(o)">申请取消</el-button>
              <el-button size="small" type="primary" plain @click="onRemind">提醒发货</el-button>
            </template>
            <template v-else-if="o.status === 2">
              <el-button size="small" @click="onAfterSale(o)">申请售后</el-button>
              <el-button size="small" type="primary" :loading="busyId === o.id" @click="onConfirm(o)">确认收货</el-button>
            </template>
            <template v-else-if="o.status === 3">
              <el-button size="small" @click="openReview(o)">去评价</el-button>
              <el-button size="small" type="primary" plain @click="onBuyAgain(o)">再次购买</el-button>
            </template>
            <template v-else-if="o.status === 5">
              <el-button size="small" type="warning" plain @click="onRemind">查看售后进度</el-button>
            </template>
            <template v-else>
              <el-button size="small" plain @click="onBuyAgain(o)">再次购买</el-button>
            </template>
          </div>
        </div>
      </div>

      <div class="pager">
        <el-pagination v-model:current-page="page" :page-size="size" :total="total"
          layout="prev, pager, next" background @current-change="reload" />
      </div>
    </AsyncBlock>

    <el-dialog v-model="reviewVisible" title="提交评价" width="480px">
      <p class="muted">{{ reviewingOrder?.items[0]?.name }}</p>
      <el-form label-position="top">
        <el-form-item label="评分"><el-rate v-model="reviewForm.score" show-text /></el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="200"
            show-word-limit placeholder="说说你的使用感受，审核通过后将对外展示" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { fetchOrders } from '../../mock/api'
import { ORDER_STATUS, money } from '../../mock/config'
import store from '../../store'

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待付款', value: '0' },
  { label: '待发货', value: '1' },
  { label: '待收货', value: '2' },
  { label: '已完成', value: '3' },
  { label: '售后中', value: '5' }
]

const activeTab = ref('all')
const loading = ref(false)
const error = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 4
const busyId = ref(null)

const reviewVisible = ref(false)
const reviewSubmitting = ref(false)
const reviewingOrder = ref(null)
const reviewForm = reactive({ score: 5, content: '' })

const currentTabLabel = computed(() => tabs.find((t) => t.value === activeTab.value)?.label || '')

async function reload() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchOrders({
      userId: store.state.user?.id || 1,
      status: activeTab.value === 'all' ? null : activeTab.value,
      page: page.value, size
    })
    list.value = res.list
    total.value = res.total
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function onPay(o) {
  busyId.value = o.id
  try {
    await store.payOrder(o.id)
    ElMessage.success('支付成功，等待商家发货')
    reload()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    busyId.value = null
  }
}

async function onConfirm(o) {
  busyId.value = o.id
  try {
    await store.confirmReceive(o.id)
    ElMessage.success('已确认收货，欢迎评价')
    reload()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    busyId.value = null
  }
}

async function onCancel(o) {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因（选填）', '取消订单', {
      confirmButtonText: '确认取消', cancelButtonText: '再想想', inputPlaceholder: '如：不想要了 / 地址填错了'
    })
    await store.cancelOrder(o.id, value)
    ElMessage.success('订单已取消')
    reload()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

async function onAfterSale(o) {
  try {
    await ElMessageBox.confirm('确认对该订单发起退货退款？发起后订单将进入「售后中」。', '申请售后', { type: 'warning' })
    await store.applyAfterSale(o)
    ElMessage.success('售后申请已提交，等待商家处理')
    reload()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '操作失败')
  }
}

function onRemind() {
  ElMessage.success('已提醒商家，请耐心等待')
}

function onBuyAgain(o) {
  o.items.forEach((it) => store.addToCart(it.productId, it.quantity))
  ElMessage.success('已重新加入购物车')
}

function openReview(o) {
  reviewingOrder.value = o
  reviewForm.score = 5
  reviewForm.content = ''
  reviewVisible.value = true
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  reviewSubmitting.value = true
  try {
    await store.submitReview({
      orderId: reviewingOrder.value.id,
      productId: reviewingOrder.value.items[0].productId,
      score: reviewForm.score,
      content: reviewForm.content
    })
    reviewVisible.value = false
    ElMessage.success('评价已提交，等待平台审核后展示')
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    reviewSubmitting.value = false
  }
}

onMounted(reload)
</script>

<style scoped>
.order + .order { margin-top: 14px; }
.order-head { display: flex; align-items: center; gap: 16px; font-size: 13px; padding-bottom: 10px; border-bottom: 1px solid var(--ygc-border); }
.order-head .el-tag { margin-left: auto; }
.items { padding: 6px 0; }
.item { display: grid; grid-template-columns: 56px 1fr 160px 110px; align-items: center; gap: 12px; padding: 8px 0; cursor: pointer; }
.mini-thumb { width: 48px; height: 48px; border-radius: 8px; background: linear-gradient(135deg, #fde68a, #fbbf24); display: flex; align-items: center; justify-content: center; font-size: 22px; }
.small { font-size: 12px; }
.order-foot { display: flex; align-items: center; justify-content: space-between; border-top: 1px dashed var(--ygc-border); padding-top: 12px; gap: 12px; flex-wrap: wrap; }
.actions { display: flex; align-items: center; gap: 8px; }
.pager { display: flex; justify-content: center; margin-top: 18px; }
</style>

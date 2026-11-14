<template>
  <div class="console-page">
    <el-alert
      class="mb"
      type="warning"
      :closable="false"
      show-icon
      title="平台介入仅在「商家拒绝售后」或「商家超时未处理」时开放，裁定结果不可撤销"
    />

    <div class="toolbar">
      <el-radio-group v-model="status" size="small" @change="load">
        <el-radio-button value="待平台介入">待平台介入</el-radio-button>
        <el-radio-button value="处理中">处理中</el-radio-button>
        <el-radio-button value="已解决">已解决</el-radio-button>
        <el-radio-button value="">全部</el-radio-button>
      </el-radio-group>
      <span class="muted" style="margin-left: auto">共 {{ list.length }} 笔</span>
    </div>

    <div class="card-panel">
      <AsyncBlock
        :loading="loading" :error="error"
        :empty="!loading && !error && list.length === 0"
        :empty-text="'没有' + (status || '') + '的纠纷订单'" empty-action="看全部"
        @retry="load" @empty-action="onShowAll"
      >
        <div v-for="d in list" :key="d.id" class="dispute">
          <div class="d-head">
            <el-tag size="small" :type="statusType(d.status)">{{ d.status }}</el-tag>
            <el-tag size="small" type="info" effect="plain">{{ d.type }}</el-tag>
            <b>{{ d.productName }}</b>
            <span class="muted small">订单 {{ d.orderNo }}</span>
            <el-tag v-if="d.resolution" class="right" size="small"
              :type="d.resolution === '支持买家' ? 'danger' : 'success'">{{ d.resolution }}</el-tag>
          </div>

          <div class="d-body">
            <div class="row"><span class="k">买家</span><span>{{ d.buyer }}</span></div>
            <div class="row"><span class="k">商家</span><span>{{ d.merchant }}</span></div>
            <div class="row"><span class="k">退款金额</span><span class="price">{{ money(d.amount) }}</span></div>
            <div class="row"><span class="k">买家申诉</span><span>{{ d.buyerReason }}</span></div>
            <div class="row"><span class="k">商家处理</span><span>{{ d.merchantReply }}</span></div>
            <div v-if="d.adminRemark" class="row">
              <span class="k">平台裁定</span>
              <span>{{ d.adminRemark }}<span class="muted small">（{{ d.resolvedAt }}）</span></span>
            </div>
          </div>

          <div class="d-ops">
            <el-button v-if="d.status !== '已解决'" size="small" type="primary" @click="openResolve(d)">
              平台介入裁定
            </el-button>
            <span v-else class="muted small">已于 {{ d.resolvedAt }} 处理完成</span>
          </div>
        </div>
      </AsyncBlock>
    </div>

    <el-dialog v-model="dialog" title="平台介入裁定" width="520px" :close-on-click-modal="false">
      <el-descriptions v-if="current" :column="1" border size="small" class="mb">
        <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="商品">{{ current.productName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ money(current.amount) }}</el-descriptions-item>
        <el-descriptions-item label="买家申诉">{{ current.buyerReason }}</el-descriptions-item>
      </el-descriptions>

      <el-form label-position="top">
        <el-form-item label="裁定结果">
          <el-radio-group v-model="form.decision">
            <el-radio value="支持买家">支持买家（强制退款）</el-radio>
            <el-radio value="支持商家">支持商家（驳回申请）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="裁定说明">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="120"
            show-word-limit
            placeholder="将同步给买家与商家，作为处理依据"
          />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="form.decision === '支持买家'"
        type="error"
        :closable="false"
        show-icon
        title="选择「支持买家」将直接发起强制退款，操作不可撤销"
      />

      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onSubmit">提交裁定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncBlock from '../../components/AsyncBlock.vue'
import { money } from '../../mock/config'
import { fetchDisputes, resolveDispute } from '../../mock/dispute'

const status = ref('待平台介入')
const loading = ref(false)
const error = ref('')
const list = ref([])
const dialog = ref(false)
const current = ref(null)
const submitting = ref(false)
const form = reactive({ decision: '支持买家', remark: '' })

const statusType = (s) => (s === '待平台介入' ? 'warning' : s === '处理中' ? 'primary' : 'success')

async function load() {
  loading.value = true
  error.value = ''
  try {
    list.value = await fetchDisputes({ status: status.value })
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

function onShowAll() {
  status.value = ''
  load()
}

function openResolve(d) {
  current.value = d
  form.decision = '支持买家'
  form.remark = ''
  dialog.value = true
}

async function onSubmit() {
  if (!form.remark.trim()) {
    ElMessage.warning('请填写裁定说明')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认裁定为「${form.decision}」？提交后不可撤销。`,
      '最终确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  submitting.value = true
  try {
    await resolveDispute(current.value.id, { decision: form.decision, remark: form.remark })
    dialog.value = false
    ElMessage.success(form.decision === '支持买家' ? '已裁定支持买家，退款将原路退回' : '已裁定支持商家，买家申请被驳回')
    load()
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mb { margin-bottom: 14px; }
.toolbar { display: flex; align-items: center; margin-bottom: 14px; }
.card-panel { background: #fff; border: 1px solid var(--ygc-border); border-radius: 12px; padding: 16px; }
.dispute { border: 1px solid var(--ygc-border); border-radius: 10px; padding: 14px 16px; margin-bottom: 12px; }
.d-head { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.d-head .right { margin-left: auto; }
.d-body { margin: 12px 0 8px; display: grid; gap: 6px; font-size: 13px; }
.row { display: flex; gap: 10px; line-height: 1.6; }
.k { width: 72px; flex: 0 0 72px; color: var(--ygc-muted); }
.d-ops { display: flex; justify-content: flex-end; }
.small { font-size: 12px; }
</style>

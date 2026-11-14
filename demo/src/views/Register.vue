<template>
  <div class="register-page">
    <div class="brand-side">
      <h1>🌰 优果仓</h1>
      <p class="slogan">干果电商平台 · 注册与入驻</p>

      <div class="roles">
        <div class="role-card" :class="{ active: role === 'USER' }" @click="switchRole('USER')">
          <div class="role-icon">🛒</div>
          <div class="role-name">用户注册</div>
          <div class="role-desc">逛干果、下单、售后</div>
        </div>
        <div class="role-card" :class="{ active: role === 'MERCHANT' }" @click="switchRole('MERCHANT')">
          <div class="role-icon">🏪</div>
          <div class="role-name">商家入驻</div>
          <div class="role-desc">开店、上架、接单</div>
        </div>
      </div>

      <ul class="benefits">
        <li v-for="b in benefits" :key="b">{{ b }}</li>
      </ul>

      <p class="note">Demo 环境：注册信息只写入浏览器内存，不会真的发送短信或提交资质。</p>
    </div>

    <div class="form-side">
      <div class="form-card">
        <h2>{{ role === 'USER' ? '注册用户账号' : '商家入驻申请' }}</h2>

        <el-alert v-if="errorMsg" :title="errorMsg" type="error" show-icon :closable="false" class="mb" />

        <el-form :model="form" label-position="top" @submit.prevent="onSubmit">
          <el-form-item label="登录账号" :error="errors.username">
            <el-input v-model="form.username" placeholder="4-20 位字母、数字或下划线" :prefix-icon="User" />
          </el-form-item>

          <template v-if="role === 'MERCHANT'">
            <el-form-item label="店铺名称" :error="errors.shopName">
              <el-input v-model="form.shopName" placeholder="如：果然多干果旗舰店" :prefix-icon="Shop" />
            </el-form-item>
            <el-form-item label="主营品类">
              <el-select v-model="form.category" placeholder="请选择" style="width: 100%">
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </template>

          <el-form-item label="手机号" :error="errors.phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" :prefix-icon="Iphone" />
          </el-form-item>

          <el-form-item label="短信验证码" :error="errors.code">
            <div class="code-row">
              <el-input v-model="form.code" maxlength="6" placeholder="6 位验证码" />
              <el-button :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? countdown + 's 后重发' : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="设置密码" :error="errors.password">
            <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" :prefix-icon="Lock" />
          </el-form-item>

          <el-form-item label="确认密码" :error="errors.confirm">
            <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入密码" :prefix-icon="Lock" />
          </el-form-item>

          <el-form-item :error="errors.agree">
            <el-checkbox v-model="form.agree">
              我已阅读并同意《{{ role === 'USER' ? '用户协议' : '商家入驻协议' }}》与《隐私政策》
            </el-checkbox>
          </el-form-item>

          <el-button type="primary" :loading="submitting" style="width: 100%" @click="onSubmit">
            {{ submitting ? '提交中…' : role === 'USER' ? '注 册' : '提交入驻申请' }}
          </el-button>
        </el-form>

        <div class="foot">
          已有账号？
          <el-link type="primary" :underline="false" @click="goLogin">去登录</el-link>
        </div>
      </div>
    </div>

    <el-dialog v-model="successVisible" :title="role === 'USER' ? '注册成功' : '入驻申请已提交'" width="440px"
      :close-on-click-modal="false">
      <el-result :icon="'success'" :title="resultTitle" :sub-title="resultSub">
        <template #extra>
          <el-button type="primary" @click="goLoginWithAccount">去登录</el-button>
          <el-button @click="resetForm">继续注册</el-button>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Shop } from '@element-plus/icons-vue'
import { registerUser, registerMerchant } from '../mock/auth'

const router = useRouter()

const role = ref('USER')
const submitting = ref(false)
const errorMsg = ref('')
const successVisible = ref(false)
const countdown = ref(0)
const sentCode = ref('')

const categories = ['坚果炒货', '果干蜜饯', '籽类', '礼盒装']

const form = reactive({
  username: '',
  shopName: '',
  category: '坚果炒货',
  phone: '',
  code: '',
  password: '',
  confirm: '',
  agree: false
})

const errors = reactive({ username: '', shopName: '', phone: '', code: '', password: '', confirm: '', agree: '' })

const benefits = computed(() =>
  role.value === 'USER'
    ? ['新用户专享满 99 减 20', '支持 7 天无理由退货', '订单状态实时通知']
    : ['首批入驻免三个月佣金', '独立店铺与商品管理后台', '平台统一处理售后纠纷']
)

const resultTitle = computed(() =>
  role.value === 'USER' ? '账号创建成功' : '入驻申请已提交'
)
const resultSub = computed(() =>
  role.value === 'USER'
    ? `账号 ${form.username} 可直接登录，去挑几样干果吧`
    : `店铺「${form.shopName}」资料将在 1 个工作日内审核，审核期间可先登录熟悉后台`
)

function switchRole(r) {
  role.value = r
  errorMsg.value = ''
  clearErrors()
}

function clearErrors() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
}

function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.phone = '请先填写正确的手机号'
    return
  }
  errors.phone = ''
  sentCode.value = '123456'
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
  ElMessage.success('验证码已发送（演示验证码：123456）')
}

function validate() {
  clearErrors()
  let ok = true
  if (!/^[A-Za-z0-9_]{4,20}$/.test(form.username)) {
    errors.username = '4-20 位字母、数字或下划线'
    ok = false
  }
  if (role.value === 'MERCHANT' && !form.shopName.trim()) {
    errors.shopName = '请填写店铺名称'
    ok = false
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    errors.phone = '请输入正确的 11 位手机号'
    ok = false
  }
  if (form.code !== sentCode.value || !form.code) {
    errors.code = '验证码不正确，请先获取验证码'
    ok = false
  }
  if (form.password.length < 6) {
    errors.password = '密码至少 6 位'
    ok = false
  }
  if (form.confirm !== form.password) {
    errors.confirm = '两次输入的密码不一致'
    ok = false
  }
  if (!form.agree) {
    errors.agree = '请先阅读并同意协议'
    ok = false
  }
  return ok
}

async function onSubmit() {
  errorMsg.value = ''
  if (!validate()) {
    ElMessage.warning('请检查表单中标红的部分')
    return
  }
  submitting.value = true
  try {
    if (role.value === 'USER') {
      await registerUser({ username: form.username, phone: form.phone, password: form.password })
    } else {
      await registerMerchant({
        username: form.username,
        phone: form.phone,
        password: form.password,
        shopName: form.shopName,
        category: form.category
      })
    }
    successVisible.value = true
  } catch (e) {
    errorMsg.value = e.message
  } finally {
    submitting.value = false
  }
}

function goLogin() {
  router.push({ path: '/login', query: { role: role.value } })
}

function goLoginWithAccount() {
  successVisible.value = false
  router.push({ path: '/login', query: { role: role.value, account: form.username } })
}

function resetForm() {
  successVisible.value = false
  Object.assign(form, { username: '', shopName: '', phone: '', code: '', password: '', confirm: '', agree: false })
  sentCode.value = ''
  countdown.value = 0
  clearErrors()
}
</script>

<style scoped>
.register-page { min-height: 100vh; display: grid; grid-template-columns: 1.05fr 1fr; }
.brand-side {
  background: linear-gradient(150deg, #78350f 0%, #b45309 55%, #f59e0b 100%);
  color: #fff; padding: 52px 44px; display: flex; flex-direction: column;
}
.brand-side h1 { font-size: 32px; margin: 0; }
.slogan { opacity: 0.85; margin-top: 8px; }
.roles { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; margin-top: 34px; }
.role-card {
  background: rgba(255, 255, 255, 0.12); border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 12px; padding: 16px 14px; cursor: pointer; transition: all 0.2s;
}
.role-card:hover { background: rgba(255, 255, 255, 0.2); }
.role-card.active { background: #fff; color: #b45309; border-color: #fff; }
.role-icon { font-size: 22px; }
.role-name { font-weight: 700; margin-top: 6px; }
.role-desc { font-size: 12px; opacity: 0.85; margin-top: 4px; }
.benefits { margin: 26px 0 0; padding-left: 18px; font-size: 13px; line-height: 2; opacity: 0.92; }
.note { margin-top: auto; font-size: 12px; opacity: 0.75; line-height: 1.7; }
.form-side { display: flex; align-items: center; justify-content: center; padding: 32px; overflow-y: auto; }
.form-card { width: 400px; }
.form-card h2 { margin: 0 0 18px; }
.mb { margin-bottom: 16px; }
.code-row { display: flex; gap: 10px; width: 100%; }
.code-row .el-input { flex: 1; }
.foot { margin-top: 16px; font-size: 13px; color: var(--ygc-muted); text-align: center; }
</style>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">个人中心</h2>
        <div class="page-sub">{{ store.state.user?.nickname }} · {{ store.state.user?.phone }}</div>
      </div>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="基本资料" name="info">
        <div class="card form-card">
          <el-form :model="profile" label-width="90px">
            <el-form-item label="昵称"><el-input v-model="profile.nickname" maxlength="20" show-word-limit /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="profile.phone" disabled /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="profile.email" placeholder="选填" /></el-form-item>
            <el-form-item label="登录密码"><el-button size="small" @click="onChangePwd">修改密码</el-button></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="onSave">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收货地址" name="address">
        <div class="addr-grid">
          <div v-for="a in store.state.addresses" :key="a.id" class="card addr">
            <div class="addr-head">
              <b>{{ a.receiver }}</b>
              <span class="muted">{{ a.phone }}</span>
              <el-tag v-if="a.isDefault" size="small" type="warning">默认</el-tag>
            </div>
            <div class="addr-text">{{ a.region.join(' ') }} {{ a.detail }}</div>
            <div class="addr-ops">
              <el-button v-if="!a.isDefault" text size="small" @click="store.setDefaultAddress(a.id)">设为默认</el-button>
              <el-popconfirm title="确认删除该地址？" @confirm="onRemoveAddress(a)">
                <template #reference><el-button text size="small" type="danger">删除</el-button></template>
              </el-popconfirm>
            </div>
          </div>
          <div class="card addr add" @click="addrVisible = true">
            <el-icon><Plus /></el-icon>新增收货地址
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`我的收藏（${favProducts.length}）`" name="fav">
        <el-empty v-if="favProducts.length === 0" description="还没有收藏商品">
          <el-button type="primary" @click="$router.push('/user/products')">去逛逛</el-button>
        </el-empty>
        <div v-else class="grid">
          <ProductCard v-for="p in favProducts" :key="p.id" :product="p" @click="$router.push('/user/product/' + p.id)">
            <template #actions>
              <el-button size="small" @click.stop="onUnfav(p)">取消收藏</el-button>
              <el-button size="small" type="primary" @click.stop="onAddCart(p)">加入购物车</el-button>
            </template>
          </ProductCard>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="addrVisible" title="新增收货地址" width="460px">
      <el-form :model="addrForm" label-width="76px">
        <el-form-item label="收货人"><el-input v-model="addrForm.receiver" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="addrForm.phone" /></el-form-item>
        <el-form-item label="所在地区">
          <el-cascader v-model="addrForm.region" :options="regionOptions" style="width: 100%" placeholder="请选择省 / 市 / 区" />
        </el-form-item>
        <el-form-item label="详细地址"><el-input v-model="addrForm.detail" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="设为默认"><el-switch v-model="addrForm.isDefault" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addrVisible = false">取消</el-button>
        <el-button type="primary" @click="onAddAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import ProductCard from '../../components/ProductCard.vue'
import { products } from '../../mock/db'
import store from '../../store'

const tab = ref('info')
const saving = ref(false)
const addrVisible = ref(false)

const profile = reactive({
  nickname: store.state.user?.nickname || '',
  phone: store.state.user?.phone || '',
  email: 'user01@example.com'
})

const addrForm = reactive({ receiver: '', phone: '', region: ['江苏省', '南京市', '鼓楼区'], detail: '', isDefault: false })

const regionOptions = [
  { value: '江苏省', label: '江苏省', children: [
    { value: '南京市', label: '南京市', children: [{ value: '鼓楼区', label: '鼓楼区' }, { value: '玄武区', label: '玄武区' }] },
    { value: '苏州市', label: '苏州市', children: [{ value: '姑苏区', label: '姑苏区' }] }
  ] },
  { value: '上海市', label: '上海市', children: [
    { value: '上海市', label: '上海市', children: [{ value: '徐汇区', label: '徐汇区' }, { value: '浦东新区', label: '浦东新区' }] }
  ] },
  { value: '广东省', label: '广东省', children: [
    { value: '深圳市', label: '深圳市', children: [{ value: '南山区', label: '南山区' }] }
  ] }
]

const favProducts = computed(() => products.filter((p) => store.state.favorites.includes(p.id)))

async function onSave() {
  if (!profile.nickname.trim()) {
    ElMessage.warning('昵称不能为空')
    return
  }
  saving.value = true
  await new Promise((r) => setTimeout(r, 600))
  store.state.user.nickname = profile.nickname
  sessionStorage.setItem('ygc-user', JSON.stringify(store.state.user))
  saving.value = false
  ElMessage.success('资料已保存')
}

function onChangePwd() {
  ElMessage.info('演示环境：修改密码流程省略，真实项目需校验原密码')
}

function onAddAddress() {
  if (!addrForm.receiver || !addrForm.phone || !addrForm.detail) {
    ElMessage.warning('请填写收货人、手机号与详细地址')
    return
  }
  store.addAddress({ ...addrForm, region: [...addrForm.region] })
  addrVisible.value = false
  ElMessage.success('地址已添加')
  addrForm.receiver = ''
  addrForm.phone = ''
  addrForm.detail = ''
  addrForm.isDefault = false
}

function onRemoveAddress(a) {
  store.removeAddress(a.id)
  ElMessage.success('地址已删除')
}

function onUnfav(p) {
  store.toggleFavorite(p.id)
  ElMessage.success('已取消收藏')
}

function onAddCart(p) {
  store.addToCart(p.id, 1)
  ElMessage.success('已加入购物车')
}
</script>

<style scoped>
.form-card { max-width: 520px; }
.addr-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.addr-head { display: flex; align-items: center; gap: 8px; }
.addr-text { font-size: 13px; color: var(--ygc-muted); margin: 8px 0; line-height: 1.6; }
.addr-ops { display: flex; gap: 6px; }
.addr.add { display: flex; align-items: center; justify-content: center; gap: 6px; color: var(--ygc-muted); border-style: dashed; cursor: pointer; min-height: 120px; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 14px; }
</style>

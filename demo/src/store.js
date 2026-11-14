import { computed, reactive } from 'vue'
import {
  products, merchants, users, orders, reviews, afterSales,
  notifications as seedNotifications, shoppingCart,
  addresses as seedAddresses, logs
} from './mock/db'
import { demoConfig, fakeRequest, sleep } from './mock/config'

const ADMIN = { id: 1, username: 'admin', nickname: '平台管理员' }

const state = reactive({
  token: sessionStorage.getItem('ygc-token') || '',
  role: sessionStorage.getItem('ygc-role') || '',
  user: JSON.parse(sessionStorage.getItem('ygc-user') || 'null'),
  cart: shoppingCart.map((c) => ({ ...c })),
  favorites: [3, 7],
  addresses: seedAddresses.map((a) => ({ ...a })),
  notifications: seedNotifications.map((n) => ({ ...n })),
  lastOrderId: null
})

const persist = () => {
  sessionStorage.setItem('ygc-token', state.token)
  sessionStorage.setItem('ygc-role', state.role)
  sessionStorage.setItem('ygc-user', JSON.stringify(state.user))
}

export const store = {
  state,

  isLogged: computed(() => !!state.token),
  role: computed(() => state.role),
  user: computed(() => state.user),
  cartCount: computed(() => state.cart.reduce((s, c) => s + c.quantity, 0)),
  checkedCount: computed(() => state.cart.filter((c) => c.checked).length),
  checkedAmount: computed(() =>
    state.cart.filter((c) => c.checked).reduce((s, c) => {
      const p = products.find((x) => x.id === c.productId)
      return s + (p ? p.price * c.quantity : 0)
    }, 0)
  ),
  unreadCount: computed(() => state.notifications.filter((n) => !n.read).length),

  async login(role, account, password) {
    if (!account || !password) throw new Error('请输入账号和密码')
    await fakeRequest([], { allowEmpty: false, label: '登录' })
    if (password !== '123456') throw new Error('账号或密码错误（演示密码为 123456）')

    if (role === 'USER') {
      const u = users.find((x) => x.username === account || x.phone === account)
      if (!u) throw new Error('该用户不存在')
      if (u.status === 0) throw new Error('账号已被禁用，请联系平台')
      state.user = { id: u.id, username: u.username, nickname: u.nickname, phone: u.phone }
    } else if (role === 'MERCHANT') {
      const m = merchants.find((x) => x.username === account || x.phone === account)
      if (!m) throw new Error('该商家不存在')
      if (m.status === 0) throw new Error('商家账号已被禁用，无法登录')
      state.user = { id: m.id, username: m.username, nickname: m.shopName, phone: m.phone }
    } else {
      if (account !== 'admin') throw new Error('管理员账号不存在')
      state.user = { ...ADMIN }
    }
    state.role = role
    state.token = 'mock-token-' + Date.now()
    persist()
    return state.user
  },

  logout() {
    state.token = ''
    state.role = ''
    state.user = null
    sessionStorage.clear()
  },

  switchRole(role) {
    const map = {
      USER: { id: 1, username: 'user01', nickname: '核桃爱好者', phone: '13700000001' },
      MERCHANT: { id: 1, username: 'guo_shop', nickname: '果然多干果旗舰店', phone: '13900000001' },
      ADMIN: { ...ADMIN }
    }
    state.role = role
    state.user = map[role]
    state.token = 'mock-token-' + Date.now()
    persist()
  },

  // ---------------- 购物车 ----------------
  addToCart(productId, quantity = 1) {
    const exist = state.cart.find((c) => c.productId === productId)
    if (exist) exist.quantity += quantity
    else state.cart.push({ productId, quantity, checked: true })
  },

  updateCartQuantity(productId, quantity) {
    const item = state.cart.find((c) => c.productId === productId)
    if (item) item.quantity = Math.max(1, quantity)
  },

  toggleCartChecked(productId, checked) {
    const item = state.cart.find((c) => c.productId === productId)
    if (item) item.checked = checked
  },

  removeFromCart(productId) {
    const idx = state.cart.findIndex((c) => c.productId === productId)
    if (idx >= 0) state.cart.splice(idx, 1)
  },

  clearCheckedCart() {
    state.cart = state.cart.filter((c) => !c.checked)
  },

  // ---------------- 收藏 ----------------
  toggleFavorite(productId) {
    const idx = state.favorites.indexOf(productId)
    if (idx >= 0) state.favorites.splice(idx, 1)
    else state.favorites.push(productId)
  },

  isFavorite(productId) {
    return state.favorites.includes(productId)
  },

  // ---------------- 地址 ----------------
  addAddress(payload) {
    const id = Date.now()
    if (payload.isDefault) state.addresses.forEach((a) => (a.isDefault = false))
    state.addresses.push({ id, ...payload })
  },

  removeAddress(id) {
    const idx = state.addresses.findIndex((a) => a.id === id)
    if (idx >= 0) state.addresses.splice(idx, 1)
  },

  setDefaultAddress(id) {
    state.addresses.forEach((a) => (a.isDefault = a.id === id))
  },

  // ---------------- 订单 ----------------
  async createOrder({ items, address, remark }) {
    await sleep(demoConfig.latency)
    if (demoConfig.errorRate > 0 && Math.random() * 100 < demoConfig.errorRate) {
      throw new Error('提交订单失败：模拟网络异常')
    }
    const total = +items.reduce((s, it) => s + it.price * it.quantity, 0).toFixed(2)
    const now = new Date().toISOString().slice(0, 19).replace('T', ' ')
    const order = {
      id: Date.now(),
      orderNo: 'YG' + Date.now(),
      userId: state.user?.id || 1,
      merchantId: items[0]?.merchantId || 1,
      status: 0,
      items: items.map((it) => ({ ...it, amount: +(it.price * it.quantity).toFixed(2) })),
      totalAmount: total,
      payAmount: total,
      receiver: address.receiver,
      receiverPhone: address.phone,
      receiverAddr: address.region.join('') + address.detail,
      remark,
      createdAt: now,
      timeline: [{ label: '提交订单', time: now, done: true }, { label: '等待买家付款', time: '', done: false }]
    }
    orders.unshift(order)
    state.lastOrderId = order.id
    this.clearCheckedCart()
    this.addLog('ORDER_CREATE', '买家', '订单 ' + order.orderNo)
    return order
  },

  findOrder(id) {
    return orders.find((o) => o.id === Number(id))
  },

  async payOrder(id) {
    await sleep(600)
    const order = this.findOrder(id)
    if (!order) throw new Error('订单不存在')
    order.status = 1
    order.timeline = [
      { label: '提交订单', time: order.createdAt, done: true },
      { label: '买家付款', time: '刚刚', done: true },
      { label: '等待商家发货', time: '', done: false }
    ]
    this.addLog('ORDER_PAY', '买家', '订单 ' + order.orderNo)
    return order
  },

  async cancelOrder(id, reason = '') {
    await sleep(500)
    const order = this.findOrder(id)
    if (!order) throw new Error('订单不存在')
    order.status = 4
    order.timeline = [
      { label: '提交订单', time: order.createdAt, done: true },
      { label: '订单已取消' + (reason ? '：' + reason : ''), time: '刚刚', done: true }
    ]
    this.addLog('ORDER_CANCEL', '买家', '订单 ' + order.orderNo)
    return order
  },

  async confirmReceive(id) {
    await sleep(500)
    const order = this.findOrder(id)
    if (!order) throw new Error('订单不存在')
    order.status = 3
    this.addLog('ORDER_CONFIRM', '买家', '订单 ' + order.orderNo)
    return order
  },

  async shipOrder(id) {
    await sleep(600)
    const order = this.findOrder(id)
    if (!order) throw new Error('订单不存在')
    order.status = 2
    this.addLog('ORDER_SHIP', '商家', '订单 ' + order.orderNo)
    return order
  },

  async applyAfterSale(order) {
    await sleep(500)
    afterSales.unshift({
      id: Date.now(),
      orderId: order.id,
      orderNo: order.orderNo,
      productName: order.items[0].name,
      type: '退货退款',
      reason: '用户在演示中提交的售后申请',
      status: '待商家处理',
      amount: order.payAmount,
      createdAt: new Date().toISOString().slice(0, 19).replace('T', ' '),
      merchantId: order.merchantId
    })
    order.status = 5
    this.addLog('AFTER_SALE_APPLY', '买家', '订单 ' + order.orderNo)
    return order
  },

  findAfterSale(id) {
    return afterSales.find((a) => a.id === Number(id))
  },

  async handleAfterSale(id, agree) {
    await sleep(500)
    const item = this.findAfterSale(id)
    if (!item) throw new Error('售后单不存在')
    item.status = agree ? '商家已同意' : '商家已拒绝'
    this.addLog('AFTER_SALE_HANDLE', '商家', '售后单 #' + id)
    return item
  },

  // ---------------- 评价 ----------------
  async submitReview({ orderId, productId, score, content }) {
    await sleep(600)
    const product = products.find((p) => p.id === productId)
    const order = this.findOrder(orderId)
    reviews.unshift({
      id: Date.now(),
      orderId,
      productId,
      productName: product?.name || order?.items[0]?.name || '商品',
      userName: state.user?.nickname || '匿名用户',
      score,
      content,
      status: '待审核',
      reply: '',
      createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
    this.addLog('REVIEW_SUBMIT', '买家', '评价 ' + (product?.name || ''))
    return true
  },

  findReview(id) {
    return reviews.find((r) => r.id === Number(id))
  },

  async auditReview(id, approve, remark = '') {
    await sleep(450)
    const review = this.findReview(id)
    if (!review) throw new Error('评价不存在')
    review.status = approve ? '已通过' : '已拒绝'
    review.auditRemark = remark
    this.addLog(approve ? 'REVIEW_APPROVE' : 'REVIEW_REJECT', '管理员', '评价 #' + id)
    return review
  },

  async replyReview(id, content) {
    await sleep(450)
    const review = this.findReview(id)
    if (!review) throw new Error('评价不存在')
    review.reply = content
    this.addLog('REVIEW_REPLY', '商家', '评价 #' + id)
    return review
  },

  // ---------------- 商品 ----------------
  toggleProductStatus(id) {
    const product = products.find((p) => p.id === id)
    if (!product) return
    product.status = product.status === 1 ? 0 : 1
    this.addLog(product.status === 1 ? 'PRODUCT_ONLINE' : 'PRODUCT_OFFLINE', '商家', product.name)
  },

  saveProduct(payload) {
    if (payload.id) {
      const product = products.find((p) => p.id === payload.id)
      Object.assign(product, payload)
      this.addLog('PRODUCT_UPDATE', '商家', product.name)
      return product
    }
    const id = products.length + 1
    const product = {
      id,
      merchantId: state.user?.id || 1,
      sales: 0,
      status: 1,
      emoji: payload.emoji || '🌰',
      createdAt: new Date().toISOString().slice(0, 10),
      ...payload
    }
    products.push(product)
    this.addLog('PRODUCT_CREATE', '商家', product.name)
    return product
  },

  // ---------------- 通知 / 日志 ----------------
  markAllRead() {
    state.notifications.forEach((n) => (n.read = true))
  },

  addLog(action, operatorType, target) {
    logs.unshift({
      id: Date.now() + Math.random(),
      action,
      actionLabel: action,
      operatorType,
      operatorName: operatorType === '管理员' ? 'admin' : state.user?.username || 'system',
      target,
      detail: '演示操作',
      ip: '127.0.0.1',
      createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ')
    })
  },

  resetDemo() {
    sessionStorage.clear()
    location.reload()
  }
}

export default store

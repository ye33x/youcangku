import { products, categories, orders, reviews, afterSales, notices, logs, merchants, users } from './db'
import { fakeRequest, paginate, demoConfig } from './config'

const visibleProducts = () => products.filter((p) => p.status === 1)

/** 商品列表：支持关键词 / 分类 / 价格 / 产地 / 排序 / 分页 */
export async function fetchProducts(query = {}) {
  const {
    keyword = '', categoryId = null, minPrice = null, maxPrice = null, origin = '',
    sort = 'sales', page = 1, size = 8, merchantId = null, includeOffline = false
  } = query

  let list = includeOffline ? [...products] : visibleProducts()
  if (merchantId) list = list.filter((p) => p.merchantId === merchantId)
  if (categoryId) {
    const childIds = categories.filter((c) => c.parentId === categoryId).map((c) => c.id)
    const ids = [categoryId, ...childIds]
    list = list.filter((p) => ids.includes(p.categoryId))
  }
  if (keyword) list = list.filter((p) => p.name.includes(keyword) || p.originPlace.includes(keyword))
  if (minPrice !== null) list = list.filter((p) => p.price >= minPrice)
  if (maxPrice !== null) list = list.filter((p) => p.price <= maxPrice)
  if (origin) list = list.filter((p) => p.originPlace.includes(origin))

  if (sort === 'price_asc') list.sort((a, b) => a.price - b.price)
  else if (sort === 'price_desc') list.sort((a, b) => b.price - a.price)
  else if (sort === 'new') list.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
  else list.sort((a, b) => b.sales - a.sales)

  return fakeRequest(paginate(list, page, size), { label: '加载商品' })
}

export async function fetchProduct(id) {
  const product = products.find((p) => p.id === Number(id))
  if (!product) throw new Error('商品不存在或已下架')
  await fakeRequest([], { label: '加载商品详情' })
  return product
}

export async function fetchCategories() {
  const tops = categories.filter((c) => c.parentId === 0)
  return fakeRequest(
    tops.map((t) => ({ ...t, children: categories.filter((c) => c.parentId === t.id) })),
    { label: '加载分类' }
  )
}

/**
 * 推荐位：有行为数据时按「同分类 + 热销」模拟协同过滤，无行为时走热门冷启动。
 * 每条都带 reason，用于演示「可解释推荐」的界面呈现。
 */
export async function fetchRecommend({ hasBehavior = true, scene = 'home', limit = 8 } = {}) {
  const pool = visibleProducts()
  if (!hasBehavior) {
    const list = [...pool].sort((a, b) => b.sales - a.sales).slice(0, limit)
    return fakeRequest(
      list.map((p, i) => ({ ...p, strategy: 'COLD_START', reason: `全站热销第 ${i + 1} 名，已售 ${p.sales} 件` })),
      { label: '生成推荐' }
    )
  }

  const seen = [1, 3, 7]
  const anchor = pool.find((p) => p.id === 7)
  const list = pool
    .filter((p) => !seen.includes(p.id))
    .sort((a, b) => {
      const sameA = a.categoryId === anchor.categoryId ? 1 : 0
      const sameB = b.categoryId === anchor.categoryId ? 1 : 0
      return sameB - sameA || b.sales - a.sales
    })
    .slice(0, limit)

  return fakeRequest(
    list.map((p) => ({
      ...p,
      strategy: 'ITEM_CF',
      reason:
        p.categoryId === anchor.categoryId
          ? '因为你浏览过「新疆吐鲁番葡萄干 500g」，同类买家也常一起看'
          : `全站热销，已售 ${p.sales} 件`
    })),
    { label: '生成推荐' }
  )
}

export async function fetchOrders({ merchantId = null, userId = null, status = null, page = 1, size = 6 } = {}) {
  let list = [...orders]
  if (merchantId) list = list.filter((o) => o.merchantId === merchantId)
  if (userId) list = list.filter((o) => o.userId === userId)
  if (status !== null && status !== '') list = list.filter((o) => o.status === Number(status))
  list.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
  return fakeRequest(paginate(list, page, size), { label: '加载订单' })
}

export async function fetchReviews({ merchantId = null, status = '', page = 1, size = 10 } = {}) {
  let list = [...reviews]
  if (merchantId) list = list.filter((r) => products.find((p) => p.id === r.productId)?.merchantId === merchantId)
  if (status) list = list.filter((r) => r.status === status)
  list.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
  return fakeRequest(paginate(list, page, size), { label: '加载评价' })
}

export async function fetchAfterSales({ merchantId = null } = {}) {
  let list = [...afterSales]
  if (merchantId) list = list.filter((a) => a.merchantId === merchantId)
  return fakeRequest(list, { label: '加载售后单' })
}

export async function fetchNotices() {
  return fakeRequest([...notices].sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1)), { label: '加载公告' })
}

export async function fetchLogs({ action = '', operatorType = '', page = 1, size = 8 } = {}) {
  let list = [...logs]
  if (action) list = list.filter((l) => l.action === action)
  if (operatorType) list = list.filter((l) => l.operatorType === operatorType)
  return fakeRequest(paginate(list, page, size), { label: '加载日志' })
}

export async function fetchAccounts(kind = 'merchant') {
  return fakeRequest(kind === 'merchant' ? [...merchants] : [...users], { label: '加载账号' })
}

/** 工作台统计：按订单/商品做简单汇总，保证数字与列表一致 */
export async function fetchStats(scope = 'merchant', merchantId = null) {
  const scopeOrders = merchantId ? orders.filter((o) => o.merchantId === merchantId) : orders
  const scopeProducts = merchantId ? products.filter((p) => p.merchantId === merchantId) : products
  const paidOrders = scopeOrders.filter((o) => o.status !== 4)
  const stats = {
    orderCount: scopeOrders.length,
    revenue: +paidOrders.reduce((s, o) => s + o.payAmount, 0).toFixed(2),
    productCount: scopeProducts.length,
    pendingShip: scopeOrders.filter((o) => o.status === 1).length,
    pendingAudit: reviews.filter((r) => r.status === '待人工复核').length,
    merchantCount: merchants.length,
    userCount: users.length,
    categoryStats: Object.entries(
      scopeProducts.reduce((acc, p) => {
        const parentId = categories.find((x) => x.id === p.categoryId)?.parentId || p.categoryId
        const top = categories.find((c) => c.id === parentId)
        const key = top?.name || '其他'
        acc[key] = (acc[key] || 0) + p.sales
        return acc
      }, {})
    ).map(([name, value]) => ({ name, value }))
  }
  return fakeRequest(stats, { label: '加载统计' })
}

export { demoConfig }

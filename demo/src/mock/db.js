/**
 * 演示用静态数据。
 * 所有数据都在浏览器内存里，刷新后回到初始状态（可通过演示面板「重置数据」恢复）。
 * 不依赖任何后端接口。
 */

export const categories = [
  { id: 1, parentId: 0, name: '坚果炒货', sort: 1, status: 1 },
  { id: 2, parentId: 0, name: '果干蜜饯', sort: 2, status: 1 },
  { id: 3, parentId: 0, name: '籽类', sort: 3, status: 1 },
  { id: 4, parentId: 0, name: '礼盒装', sort: 4, status: 1 },
  { id: 11, parentId: 1, name: '核桃', sort: 1, status: 1 },
  { id: 12, parentId: 1, name: '巴旦木', sort: 2, status: 1 },
  { id: 13, parentId: 1, name: '开心果', sort: 3, status: 1 },
  { id: 21, parentId: 2, name: '葡萄干', sort: 1, status: 1 },
  { id: 22, parentId: 2, name: '红枣', sort: 2, status: 1 },
  { id: 31, parentId: 3, name: '瓜子', sort: 1, status: 1 },
  { id: 32, parentId: 3, name: '南瓜子', sort: 2, status: 1 },
  { id: 41, parentId: 4, name: '每日坚果', sort: 1, status: 1 },
  { id: 42, parentId: 4, name: '送礼礼盒', sort: 2, status: 1 }
]

export const merchants = [
  { id: 1, username: 'guo_shop', shopName: '果然多干果旗舰店', phone: '13900000001', status: 1, intro: '新疆产地直采，主营核桃、巴旦木、葡萄干', createdAt: '2025-11-02' },
  { id: 2, username: 'nuts_shop', shopName: '坚果部落', phone: '13900000002', status: 1, intro: '专注混合坚果与每日坚果礼盒', createdAt: '2025-11-18' },
  { id: 3, username: 'dried_shop', shopName: '蜜饯小铺', phone: '13900000003', status: 0, intro: '果干蜜饯、红枣、芒果干', createdAt: '2026-01-09' }
]

export const users = [
  { id: 1, username: 'user01', nickname: '核桃爱好者', phone: '13700000001', status: 1, createdAt: '2026-02-01' },
  { id: 2, username: 'user02', nickname: '干果控', phone: '13700000002', status: 1, createdAt: '2026-02-14' },
  { id: 3, username: 'user03', nickname: '养生青年', phone: '13700000003', status: 1, createdAt: '2026-03-02' },
  { id: 4, username: 'user04', nickname: '送礼专业户', phone: '13700000004', status: 1, createdAt: '2026-03-21' },
  { id: 5, username: 'user05', nickname: '办公室零食', phone: '13700000005', status: 0, createdAt: '2026-04-08' }
]

const rawProducts = [
  [1, 11, '新疆阿克苏薄皮核桃 500g', 39.9, 59.9, '新疆阿克苏', '500g/袋', 200, 860, '🌰', '皮薄如纸，手剥即开，果仁饱满。'],
  [1, 11, '云南漾濞核桃仁 250g', 29.9, 45, '云南大理', '250g/罐', 150, 430, '🌰', '去壳核桃仁，淡盐水烘烤，无添加蔗糖。'],
  [1, 12, '美国进口巴旦木 500g', 45, 68, '美国加州', '500g/袋', 180, 720, '🥜', '原味烘焙，颗粒均匀，酥脆不油腻。'],
  [2, 12, '盐焗巴旦木 250g', 26.8, 36, '美国加州', '250g/袋', 220, 305, '🥜', '盐焗工艺，咸香适口，独立小包装。'],
  [2, 13, '伊朗开心果 400g', 58, 78, '伊朗', '400g/袋', 90, 260, '🥬', '自然开口率高，紫衣绿仁，原味烘焙。'],
  [2, 13, '盐焗开心果 500g', 62, 88, '伊朗', '500g/袋', 85, 210, '🥬', '盐焗调味，适合追剧下酒。'],
  [1, 21, '新疆吐鲁番葡萄干 500g', 22.9, 32, '新疆吐鲁番', '500g/袋', 300, 1180, '🍇', '无核白葡萄干，自然晾晒，酸甜软糯。'],
  [3, 21, '黑加仑葡萄干 250g', 25.5, 35, '新疆吐鲁番', '250g/袋', 160, 390, '🍇', '黑加仑品种，果香浓郁，颗粒饱满。'],
  [3, 22, '新疆和田大枣 1kg', 49.9, 69.9, '新疆和田', '1kg/袋', 240, 950, '🫘', '骏枣品种，肉厚核小，甜而不腻。'],
  [3, 22, '若羌灰枣 500g', 32, 45, '新疆若羌', '500g/袋', 200, 640, '🫘', '个头小巧，皮薄肉甜，适合煲汤泡茶。'],
  [3, 31, '原味葵花籽 500g', 15.9, 22, '内蒙古巴彦淖尔', '500g/袋', 350, 1420, '🌻', '大颗粒葵花籽，低温烘焙，原味不腻。'],
  [2, 32, '五香南瓜子 250g', 18.8, 26, '甘肃武威', '250g/袋', 280, 780, '🎃', '五香卤制入味，籽仁饱满易剥。'],
  [1, 41, '每日坚果混合装 750g（30 袋）', 99, 149, '多产地拼配', '25g×30 袋', 120, 560, '🥣', '六种坚果三种果干，独立分装，随身携带。'],
  [2, 42, '干果礼盒 高档送礼装 1.2kg', 168, 228, '多产地拼配', '1.2kg/盒', 60, 190, '🎁', '含巴旦木、开心果、核桃、葡萄干与红枣。'],
  [1, 42, '春节年货坚果大礼包 2kg', 238, 328, '多产地拼配', '2kg/箱', 40, 95, '🎁', '八款坚果果干组合，红色喜庆包装。'],
  [3, 21, '芒果干 200g', 19.9, 28, '广西百色', '200g/袋', 0, 150, '🥭', '低温烘干，保留果香，无添加色素。（零库存演示）']
]

export const products = rawProducts.map((p, i) => ({
  id: i + 1,
  merchantId: p[0],
  categoryId: p[1],
  name: p[2],
  price: p[3],
  originPrice: p[4],
  originPlace: p[5],
  spec: p[6],
  stock: p[7],
  sales: p[8],
  emoji: p[9],
  detail: p[10],
  status: i === 15 ? 0 : 1,
  createdAt: '2026-09-' + String((i % 27) + 1).padStart(2, '0')
}))

export const shoppingCart = [
  { productId: 7, quantity: 2, checked: true },
  { productId: 1, quantity: 1, checked: true },
  { productId: 3, quantity: 1, checked: false }
]

export const addresses = [
  { id: 1, receiver: '张伟', phone: '13700000001', region: ['江苏省', '南京市', '鼓楼区'], detail: '中山北路 100 号 3 栋 502', isDefault: true },
  { id: 2, receiver: '张伟', phone: '13700000001', region: ['上海市', '上海市', '徐汇区'], detail: '漕溪北路 88 号 A 座 1201', isDefault: false }
]

const day = (offset) => {
  const d = new Date('2026-11-14T10:00:00')
  d.setDate(d.getDate() - offset)
  return d.toISOString().slice(0, 19).replace('T', ' ')
}

const orderSeed = [
  { status: 0, items: [[2, 1]], days: 0, merchantId: 1, receiver: '张伟' },
  { status: 1, items: [[6, 1]], days: 1, merchantId: 2, receiver: '张伟' },
  { status: 2, items: [[7, 2], [9, 1]], days: 3, merchantId: 1, receiver: '张伟' },
  { status: 3, items: [[5, 1]], days: 9, merchantId: 2, receiver: '李娜' },
  { status: 4, items: [[13, 1]], days: 12, merchantId: 1, receiver: '张伟' },
  { status: 5, items: [[3, 1]], days: 15, merchantId: 1, receiver: '张伟' },
  { status: 3, items: [[11, 2]], days: 20, merchantId: 3, receiver: '王强' },
  { status: 1, items: [[13, 1], [14, 1]], days: 2, merchantId: 2, receiver: '李娜' }
]

export const orders = orderSeed.map((o, i) => {
  const items = o.items.map(([productId, quantity]) => {
    const p = products.find((x) => x.id === productId)
    return {
      productId,
      name: p.name,
      emoji: p.emoji,
      spec: p.spec,
      price: p.price,
      quantity,
      amount: +(p.price * quantity).toFixed(2)
    }
  })
  const total = +items.reduce((s, it) => s + it.amount, 0).toFixed(2)
  return {
    id: 1001 + i,
    orderNo: 'YG2026111400' + String(i + 1).padStart(2, '0'),
    userId: [1, 1, 1, 2, 1, 1, 3, 4][i] || 1,
    merchantId: o.merchantId,
    status: o.status,
    items,
    totalAmount: total,
    payAmount: total,
    receiver: o.receiver,
    receiverPhone: '1370000000' + (i % 9),
    receiverAddr: '江苏省南京市鼓楼区中山北路 100 号 3 栋 502',
    remark: i === 2 ? '请尽快发货，谢谢' : '',
    createdAt: day(o.days),
    timeline: buildTimeline(o.status, day(o.days))
  }
})

function buildTimeline(status, createdAt) {
  const t = [{ label: '提交订单', time: createdAt, done: true }]
  if (status === 4) {
    t.push({ label: '订单已取消', time: createdAt, done: true })
    return t
  }
  if (status >= 1) t.push({ label: '买家付款', time: createdAt, done: true })
  if (status >= 2) t.push({ label: '商家发货', time: createdAt, done: true })
  if (status >= 3) t.push({ label: '确认收货', time: createdAt, done: true })
  if (status === 5) t.push({ label: '售后处理中', time: createdAt, done: true })
  t.push({ label: status === 0 ? '等待买家付款' : '流程结束', time: '', done: false })
  return t
}

export const afterSales = [
  { id: 2001, orderId: 1006, orderNo: 'YG20261114006', productName: '美国进口巴旦木 500g', type: '退货退款', reason: '收到货有破损，希望退货', status: '待商家处理', amount: 45, createdAt: day(15), merchantId: 1 },
  { id: 2002, orderId: 1004, orderNo: 'YG20261114004', productName: '伊朗开心果 400g', type: '仅退款', reason: '少发了一袋', status: '商家已同意', amount: 58, createdAt: day(9), merchantId: 2 }
]

export const reviews = [
  { id: 3001, orderId: 1004, productId: 5, productName: '伊朗开心果 400g', userName: '干果控', score: 4, content: '开心果开口率不错，就是盐味稍重。', status: '待审核', reply: '', createdAt: day(8) },
  { id: 3002, orderId: 1003, productId: 7, productName: '新疆吐鲁番葡萄干 500g', userName: '张伟', score: 5, content: '葡萄干很甜，颗粒大，回购第二次了。', status: '已通过', reply: '感谢支持，我们会继续保持品质！', createdAt: day(2) },
  { id: 3003, orderId: 1007, productId: 11, productName: '原味葵花籽 500g', userName: '王强', score: 5, content: '瓜子很饱满，原味不腻。', status: '待审核', reply: '', createdAt: day(18) },
  { id: 3004, orderId: 1001, productId: 2, productName: '云南漾濞核桃仁 250g', userName: '核桃爱好者', score: 2, content: '有点受潮，口感不够脆。', status: '已拒绝', reply: '', createdAt: day(5), auditRemark: '含主观差评，无凭证' }
]

export const notices = [
  { id: 4001, title: '平台上线公告', content: '优果仓干果电商平台正式上线，欢迎各位商家入驻，首批入驻商家可享受三个月免佣金政策。', status: '已发布', createdAt: day(30) },
  { id: 4002, title: '关于评价审核规则的说明', content: '为保障内容质量，用户评价与商家回复均需平台审核通过后才会对外展示，通常 24 小时内完成审核。', status: '已发布', createdAt: day(12) },
  { id: 4003, title: '双十一活动通知', content: '11 月 1 日至 11 月 11 日，全站干果满 99 减 20，礼盒类商品满 199 减 50。', status: '已下架', createdAt: day(6) }
]


export const notifications = [
  { id: 5001, type: '订单通知', title: '订单已发货', content: '你的订单 YG20261114003 已由商家发货，请注意查收。', read: false, createdAt: day(1) },
  { id: 5002, type: '系统通知', title: '双十一活动开始', content: '全站干果满 99 减 20，快来挑选吧。', read: false, createdAt: day(3) },
  { id: 5003, type: '售后通知', title: '售后已受理', content: '你的退货退款申请已提交，商家将在 48 小时内处理。', read: true, createdAt: day(15) }
]

const actions = [
  ['PRODUCT_ONLINE', '商家', '上架商品'],
  ['PRODUCT_OFFLINE', '商家', '下架商品'],
  ['PRODUCT_CREATE', '商家', '发布商品'],
  ['REVIEW_APPROVE', '管理员', '审核通过评价'],
  ['REVIEW_REJECT', '管理员', '拒绝评价'],
  ['NOTICE_PUBLISH', '管理员', '发布公告'],
  ['MERCHANT_DISABLE', '管理员', '禁用商家账号'],
  ['ORDER_SHIP', '商家', '订单发货'],
  ['AFTER_SALE_HANDLE', '商家', '处理售后申请']
]

export const logs = Array.from({ length: 26 }, (_, i) => {
  const [action, operatorType, label] = actions[i % actions.length]
  return {
    id: 6001 + i,
    action,
    actionLabel: label,
    operatorType,
    operatorName: operatorType === '管理员' ? 'admin' : ['guo_shop', 'nuts_shop', 'dried_shop'][i % 3],
    target: label + ' #' + (100 + i),
    detail: '演示数据',
    ip: '127.0.0.1',
    createdAt: day(i)
  }
})

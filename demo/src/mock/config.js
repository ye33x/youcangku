import { reactive } from 'vue'

/** 演示控制项：面板里可实时调整，用来观察不同网络条件下的界面状态。 */
export const demoConfig = reactive({
  latency: 450,
  errorRate: 0,
  emptyMode: false
})

/** 全局 UI 状态：演示控制面板的开关，顶栏按钮与面板共用 */
export const uiState = reactive({
  demoPanelVisible: false
})

export const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

/** 统一的「假请求」：带延迟、可按概率抛错、可强制返回空。 */
export async function fakeRequest(payload, { allowEmpty = true, label = '' } = {}) {
  await sleep(demoConfig.latency)
  if (demoConfig.errorRate > 0 && Math.random() * 100 < demoConfig.errorRate) {
    throw new Error(label ? `${label}失败：模拟网络异常（可刷新重试）` : '模拟网络异常，请稍后重试')
  }
  if (allowEmpty && demoConfig.emptyMode) {
    return Array.isArray(payload) ? [] : null
  }
  return payload
}

export function paginate(list, page = 1, size = 10) {
  const total = list.length
  const start = (page - 1) * size
  return { list: list.slice(start, start + size), total, page, size }
}

export const money = (n) => '¥' + Number(n).toFixed(2)

export const ORDER_STATUS = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '待发货', type: 'primary' },
  2: { text: '待收货', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' },
  5: { text: '售后中', type: 'danger' }
}

export const AUDIT_STATUS = {
  待人工复核: { type: 'warning' },
  待审核: { type: 'warning' },
  已通过: { type: 'success' },
  已拒绝: { type: 'danger' }
}

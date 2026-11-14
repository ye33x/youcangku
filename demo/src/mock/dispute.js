/**
 * 纠纷订单（平台介入）演示数据。
 * 场景来源：用户申请售后后，商家拒绝或超时未处理，买家申请平台介入。
 */
import { fakeRequest, sleep } from './config'
import store from '../store'

export const disputes = [
  {
    id: 7001,
    orderNo: 'YG20261114006',
    buyer: '核桃爱好者',
    merchant: '果然多干果旗舰店',
    productName: '美国进口巴旦木 500g',
    amount: 45,
    type: '退货退款',
    buyerReason: '收到货包装破损，联系商家三天未回复',
    merchantReply: '已拒绝：买家未提供破损照片',
    status: '待平台介入',
    createdAt: '2026-11-10 09:20:00'
  },
  {
    id: 7002,
    orderNo: 'YG20261114004',
    buyer: '干果控',
    merchant: '坚果部落',
    productName: '伊朗开心果 400g',
    amount: 58,
    type: '仅退款',
    buyerReason: '少发一袋，商家只说补发但一直没有物流',
    merchantReply: '已同意退款，但 7 天未实际打款',
    status: '待平台介入',
    createdAt: '2026-11-08 14:05:00'
  },
  {
    id: 7003,
    orderNo: 'YG20261114002',
    buyer: '养生青年',
    merchant: '果然多干果旗舰店',
    productName: '盐焗开心果 500g',
    amount: 62,
    type: '退货退款',
    buyerReason: '口味不喜欢，要求无理由退货',
    merchantReply: '已同意退货，买家已寄回但未提供单号',
    status: '处理中',
    createdAt: '2026-11-06 11:40:00'
  },
  {
    id: 7004,
    orderNo: 'YG20261114007',
    buyer: '送礼专业户',
    merchant: '蜜饯小铺',
    productName: '原味葵花籽 500g',
    amount: 31.8,
    type: '仅退款',
    buyerReason: '货未收到，物流显示已签收',
    merchantReply: '已提供签收记录，买家已收到',
    status: '已解决',
    resolution: '支持商家',
    adminRemark: '物流签收记录完整，买家无法提供未收到证据',
    resolvedAt: '2026-11-07 16:12:00',
    createdAt: '2026-11-05 10:00:00'
  }
]

export async function fetchDisputes({ status = '' } = {}) {
  let list = [...disputes]
  if (status) list = list.filter((d) => d.status === status)
  list.sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
  return fakeRequest(list, { label: '加载纠纷订单' })
}

/** 平台裁定：支持买家（强制退款）或支持商家（驳回申请） */
export async function resolveDispute(id, { decision, remark }) {
  await sleep(600)
  const item = disputes.find((d) => d.id === Number(id))
  if (!item) throw new Error('纠纷单不存在')
  if (item.status === '已解决') throw new Error('该纠纷已处理完成')
  item.status = '已解决'
  item.resolution = decision
  item.adminRemark = remark
  item.resolvedAt = new Date().toISOString().slice(0, 19).replace('T', ' ')
  store.addLog(
    decision === '支持买家' ? 'DISPUTE_REFUND_BUYER' : 'DISPUTE_REJECT_BUYER',
    '管理员',
    '纠纷单 #' + id
  )
  return item
}

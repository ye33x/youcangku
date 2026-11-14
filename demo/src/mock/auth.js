/**
 * 注册 / 入驻（纯前端模拟）。
 * 新账号写入内存中的 users / merchants 数组，刷新页面后恢复初始数据。
 */
import { users, merchants } from './db'
import { fakeRequest } from './config'

const today = () => new Date().toISOString().slice(0, 10)

export async function registerUser({ username, phone, password }) {
  await fakeRequest([], { allowEmpty: false, label: '注册' })
  if (users.some((u) => u.username === username)) throw new Error('用户名已被注册，请换一个')
  if (users.some((u) => u.phone === phone)) throw new Error('该手机号已注册，可直接登录')
  users.push({
    id: Date.now(),
    username,
    nickname: username,
    phone,
    status: 1,
    createdAt: today(),
    isNew: true
  })
  return { username }
}

export async function registerMerchant({ username, phone, password, shopName, category }) {
  await fakeRequest([], { allowEmpty: false, label: '提交入驻申请' })
  if (merchants.some((m) => m.username === username)) throw new Error('该商家账号已存在')
  if (merchants.some((m) => m.phone === phone)) throw new Error('该手机号已入驻')
  const merchant = {
    id: Date.now(),
    username,
    shopName,
    phone,
    status: 1,
    intro: category ? '主营' + category : '',
    createdAt: today(),
    auditStatus: '待审核',
    isNew: true
  }
  merchants.push(merchant)
  return { username, shopName }
}

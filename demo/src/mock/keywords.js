/**
 * 评价关键词审核。
 *
 * 规则：评价提交时先过一遍关键词库，命中任意启用中的关键词 → 进入「待人工复核」，
 * 未命中 → 自动通过。管理端只需要处理命中项，并可维护关键词库。
 */
import { reactive } from 'vue'

export const keywords = reactive([
  { id: 1, word: '加微信', category: '引流', enabled: true, hit: 0 },
  { id: 2, word: '微商', category: '引流', enabled: true, hit: 0 },
  { id: 3, word: '代购', category: '引流', enabled: true, hit: 0 },
  { id: 4, word: '私聊', category: '引流', enabled: true, hit: 0 },
  { id: 5, word: 'QQ', category: '引流', enabled: true, hit: 0 },
  { id: 6, word: '电话联系', category: '引流', enabled: true, hit: 0 },
  { id: 7, word: '假货', category: '违禁', enabled: true, hit: 0 },
  { id: 8, word: '骗子', category: '违禁', enabled: true, hit: 0 },
  { id: 9, word: '12315', category: '投诉', enabled: true, hit: 0 },
  { id: 10, word: '投诉', category: '投诉', enabled: true, hit: 0 }
])

/** 返回命中的关键词数组 */
export function scanText(text = '') {
  const lowered = String(text).toLowerCase()
  return keywords
    .filter((k) => k.enabled && lowered.includes(k.word.toLowerCase()))
    .map((k) => k.word)
}

/**
 * 生成评价的自动审核结果，直接展开进评价对象。
 * 命中 → 待人工复核；未命中 → 已通过。
 */
export function autoAudit(content) {
  const matched = scanText(content)
  matched.forEach((w) => {
    const k = keywords.find((x) => x.word === w)
    if (k) k.hit += 1
  })
  return {
    matchedKeywords: matched,
    status: matched.length ? '待人工复核' : '已通过',
    autoAudited: true,
    auditRemark: matched.length ? '命中关键词：' + matched.join('、') : '未命中关键词，自动通过'
  }
}

export function addKeyword(word, category = '自定义') {
  const w = String(word || '').trim()
  if (!w) throw new Error('关键词不能为空')
  if (keywords.some((k) => k.word === w)) throw new Error('关键词已存在')
  keywords.push({ id: Date.now(), word: w, category, enabled: true, hit: 0 })
  return w
}

export function removeKeyword(id) {
  const idx = keywords.findIndex((k) => k.id === id)
  if (idx >= 0) keywords.splice(idx, 1)
}

export function toggleKeyword(id, enabled) {
  const k = keywords.find((x) => x.id === id)
  if (k) k.enabled = enabled
}

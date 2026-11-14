<template>
  <div class="product-card" @click="$emit('click')">
    <div class="thumb">{{ product.emoji }}</div>
    <div class="body">
      <div class="name">{{ product.name }}</div>
      <div class="meta">{{ product.originPlace }} · {{ product.spec }}</div>
      <div class="row">
        <span class="price">{{ money(product.price) }}</span>
        <span class="price-origin">{{ money(product.originPrice) }}</span>
        <span class="sales">已售 {{ product.sales }}</span>
      </div>
      <div v-if="product.reason" class="reason">{{ product.reason }}</div>
      <div class="actions"><slot name="actions" /></div>
    </div>
  </div>
</template>

<script setup>
import { money } from '../mock/config'
defineProps({ product: { type: Object, required: true } })
defineEmits(['click'])
</script>

<style scoped>
.product-card {
  display: flex; gap: 14px; background: #fff; border: 1px solid var(--ygc-border);
  border-radius: 12px; padding: 12px; cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.product-card:hover { box-shadow: 0 6px 18px rgba(180, 83, 9, 0.12); transform: translateY(-2px); }
.thumb { width: 132px; flex: 0 0 132px; }
.body { flex: 1; min-width: 0; }
.name {
  font-weight: 600; line-height: 1.4; display: -webkit-box;
  -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.meta { color: var(--ygc-muted); font-size: 12px; margin-top: 4px; }
.row { display: flex; align-items: baseline; gap: 4px; margin-top: 8px; }
.sales { margin-left: auto; font-size: 12px; color: var(--ygc-muted); }
.actions { margin-top: 10px; display: flex; gap: 8px; }
</style>

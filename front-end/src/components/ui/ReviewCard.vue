<script setup>
defineProps({
  quote: { type: String, required: true },
  author: { type: String, default: '' },
  compact: { type: Boolean, default: false }, // dạng có viền, chữ nhỏ hơn (trang chi tiết phòng)
})
</script>

<template>
  <figure :class="['rounded-card bg-surface p-7', compact && 'border border-line']">
    <blockquote :class="['mb-[18px] font-serif leading-[1.55]', compact ? 'text-[17px]' : 'text-lg']">
      {{ quote }}
    </blockquote>
    <figcaption class="text-sm text-muted">{{ author }}</figcaption>
  </figure>
</template>

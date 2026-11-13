<script setup>
import { money } from '@/utils/format'

defineProps({
  price: { type: Number, required: true },
  large: { type: Boolean, default: false },
})
</script>

<template>
  <span class="text-sm text-muted">
    Từ <b :class="['text-ink', large ? 'text-[28px]' : 'text-xl']">{{ money(price) }}</b> / đêm
  </span>
</template>

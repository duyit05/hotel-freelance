<script setup>
// Ảnh phủ kín khung cha; nếu ảnh lỗi thì hiện khung màu kèm chú thích
import { ref, watch } from 'vue'

defineOptions({ inheritAttrs: false })
const props = defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
})

const failed = ref(false)
watch(() => props.src, () => (failed.value = false))
</script>

<template>
  <div v-if="failed || !src" v-bind="$attrs" class="relative size-full bg-linear-to-b from-soft to-line">
    <span class="absolute bottom-3 left-3.5 text-sm font-medium text-muted">{{ alt }}</span>
  </div>
  <img
    v-else
    v-bind="$attrs"
    :src="src"
    :alt="alt"
    decoding="async"
    class="block size-full bg-img object-cover"
    @error="failed = true"
  />
</template>

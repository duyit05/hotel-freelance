<script setup>
import BaseButton from '@/components/ui/BaseButton.vue'
import PriceTag from '@/components/ui/PriceTag.vue'

defineProps({
  price: { type: Number, required: true },
})
defineEmits(['book'])
</script>

<template>
  <!-- Thanh cố định ở đáy màn hình, chỉ hiện trên điện thoại / máy tính bảng -->
  <div
    class="fixed inset-x-0 bottom-0 z-40 flex items-center justify-between gap-4 border-t border-line bg-surface px-5 py-3 shadow-[0_-10px_24px_-16px_rgba(0,0,0,.25)] md:hidden"
  >
    <PriceTag :price="price" />
    <BaseButton size="sm" @click="$emit('book')">Đặt phòng</BaseButton>
  </div>
</template>

<script setup>
// Khối nội dung chuẩn của trang: khoảng đệm trên dưới + khung căn giữa
defineProps({
  soft: { type: Boolean, default: false }, // nền màu be nhạt
})
</script>

<template>
  <section :class="['py-[72px] md:py-[104px]', soft && 'bg-soft']">
    <div class="wrap">
      <slot />
    </div>
  </section>
</template>

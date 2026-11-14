<script setup>
// Đầu trang quản trị: link quay lại (tùy chọn), tiêu đề, một dòng mô tả và nút thao tác (slot "actions") ở góc phải
import AdminIcon from './AdminIcon.vue'

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  back: { type: [String, Object], default: null },
  backLabel: { type: String, default: 'Quay lại' },
})
</script>

<template>
  <header class="mb-6 flex flex-wrap items-end justify-between gap-4">
    <div class="min-w-0 flex-1 basis-[280px]">
      <RouterLink
        v-if="back"
        :to="back"
        class="mb-2 inline-flex items-center gap-1.5 text-[13px] text-ink-2 no-underline hover:text-ink"
      >
        <AdminIcon name="arrowLeft" class="size-4" />
        {{ backLabel }}
      </RouterLink>
      <h1 class="a-title truncate">{{ title }}</h1>
      <p v-if="subtitle" class="a-sub">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="flex shrink-0 flex-wrap items-center gap-2">
      <slot name="actions" />
    </div>
  </header>
</template>

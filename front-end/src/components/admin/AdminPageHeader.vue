<script setup>
// Đầu trang quản trị: link quay lại (tùy chọn), tiêu đề, mô tả và các nút thao tác (slot "actions")
import AdminIcon from './AdminIcon.vue'

defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  back: { type: [String, Object], default: null },
  backLabel: { type: String, default: 'Quay lại' },
})
</script>

<template>
  <header class="mb-7 flex flex-wrap items-end justify-between gap-4">
    <div class="min-w-0">
      <RouterLink
        v-if="back"
        :to="back"
        class="mb-2 inline-flex items-center gap-1.5 text-sm text-muted no-underline hover:text-primary"
      >
        <AdminIcon name="arrowLeft" class="size-4" />
        {{ backLabel }}
      </RouterLink>
      <h1 class="truncate text-[28px] leading-tight">{{ title }}</h1>
      <p v-if="subtitle" class="mt-1 text-muted">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="flex flex-wrap items-center gap-2">
      <slot name="actions" />
    </div>
  </header>
</template>

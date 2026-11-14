<script setup>
// Nhãn + ô nhập (slot) + gợi ý / thông báo lỗi
defineProps({
  label: { type: String, required: true },
  id: { type: String, required: true },
  error: { type: String, default: '' },
  hint: { type: String, default: '' },
  required: { type: Boolean, default: false },
})
</script>

<template>
  <div>
    <label :for="id" class="mb-1.5 block text-[13px] font-medium text-ink-2">
      {{ label }}<span v-if="required" class="text-danger"> *</span>
    </label>
    <slot />
    <p v-if="error" class="mt-1.5 text-[12.5px] text-danger">{{ error }}</p>
    <p v-else-if="hint" class="mt-1.5 text-[12.5px] text-muted">{{ hint }}</p>
  </div>
</template>

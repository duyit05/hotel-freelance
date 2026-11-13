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
    <label :for="id" class="mb-1.5 block text-sm font-medium">
      {{ label }}<span v-if="required" class="text-red-600"> *</span>
    </label>
    <slot />
    <p v-if="error" class="mt-1 text-sm text-red-600">{{ error }}</p>
    <p v-else-if="hint" class="mt-1 text-sm text-muted">{{ hint }}</p>
  </div>
</template>

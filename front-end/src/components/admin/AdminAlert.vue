<script setup>
// Hộp thông báo trong trang: success | error | warning. Thêm closable + @close để có nút đóng
import AdminIcon from './AdminIcon.vue'

defineProps({
  type: { type: String, default: 'success' },
  closable: { type: Boolean, default: false },
})
defineEmits(['close'])

const TONES = { success: 'tone-ok', error: 'tone-danger', warning: 'tone-warn' }
</script>

<template>
  <div :class="['admin-alert', TONES[type]]" :role="type === 'error' ? 'alert' : 'status'">
    <AdminIcon :name="type === 'success' ? 'check' : 'alert'" class="mt-0.5 size-4 shrink-0" />
    <div class="min-w-0 flex-1 text-ink"><slot /></div>
    <button v-if="closable" type="button" class="a-btn a-btn--icon -my-1.5 size-7!" aria-label="Đóng" @click="$emit('close')">
      <AdminIcon name="close" class="size-4" />
    </button>
  </div>
</template>

<style scoped>
.admin-alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid color-mix(in srgb, var(--tone) 26%, transparent);
  background: color-mix(in srgb, var(--tone) 10%, var(--surface));
  color: var(--tone);
  font-size: 13.5px;
}
</style>

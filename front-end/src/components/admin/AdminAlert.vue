<script setup>
// Hộp thông báo: success | error | warning. Thêm closable + @close để có nút đóng
import AdminIcon from './AdminIcon.vue'

defineProps({
  type: { type: String, default: 'success' },
  closable: { type: Boolean, default: false },
})
defineEmits(['close'])

const TONES = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  error: 'border-red-200 bg-red-50 text-red-700',
  warning: 'border-amber-200 bg-amber-50 text-amber-800',
}
</script>

<template>
  <div
    :class="['flex items-start gap-3 rounded-xl border px-4 py-3 text-sm', TONES[type]]"
    :role="type === 'error' ? 'alert' : 'status'"
  >
    <AdminIcon :name="type === 'success' ? 'check' : 'alert'" class="mt-px size-[18px] shrink-0" />
    <div class="flex-1"><slot /></div>
    <button
      v-if="closable"
      type="button"
      class="-m-1 rounded-md p-1 opacity-60 hover:opacity-100"
      aria-label="Đóng"
      @click="$emit('close')"
    >
      <AdminIcon name="close" class="size-4" />
    </button>
  </div>
</template>

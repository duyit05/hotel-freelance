<script setup>
// Popup xác nhận dùng chung cho trang quản trị, dựa trên thẻ <dialog> (Esc để đóng, giữ focus trong popup).
// <AdminConfirmDialog v-model:open="show" title="..." confirm-text="..." :loading="busy" @confirm="doIt">nội dung</AdminConfirmDialog>
import { ref, watch } from 'vue'
import AdminIcon from './AdminIcon.vue'

const props = defineProps({
  title: { type: String, required: true },
  confirmText: { type: String, default: 'Đồng ý' },
  cancelText: { type: String, default: 'Hủy' },
  icon: { type: String, default: 'alert' },
  /** danger: nút xác nhận màu đỏ */
  danger: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
})
const open = defineModel('open', { type: Boolean, default: false })
const emit = defineEmits(['confirm'])

const dialog = ref(null)

watch(open, (value) => {
  if (value) dialog.value?.showModal()
  else dialog.value?.close()
})

function cancel() {
  if (!props.loading) open.value = false
}

/** Bấm ra ngoài vùng popup (lên lớp nền) thì đóng */
function onBackdrop(e) {
  if (e.target === dialog.value) cancel()
}
</script>

<template>
  <dialog
    ref="dialog"
    class="confirm-dialog theme-admin m-auto w-[calc(100%-32px)] max-w-[400px] rounded-2xl border border-line bg-surface p-0 text-ink shadow-[0_24px_64px_rgb(23_33_43/.22)]"
    aria-labelledby="confirm-title"
    @click="onBackdrop"
    @cancel.prevent="cancel"
  >
    <div class="p-6">
      <span
        :class="[
          'mb-4 flex size-11 items-center justify-center rounded-full',
          danger
            ? 'bg-[color-mix(in_srgb,var(--danger)_10%,var(--surface))] text-danger'
            : 'bg-[color-mix(in_srgb,var(--brand)_10%,var(--surface))] text-brand',
        ]"
      >
        <AdminIcon :name="icon" class="size-5" />
      </span>
      <h2 id="confirm-title" class="text-[17px] font-semibold">{{ title }}</h2>
      <div class="mt-1.5 text-[14px] text-ink-2"><slot /></div>
    </div>
    <div class="flex flex-col-reverse gap-2 border-t border-line bg-surface-2 px-6 py-4 sm:flex-row sm:justify-end">
      <button type="button" class="a-btn" :disabled="loading" autofocus @click="cancel">{{ cancelText }}</button>
      <button
        type="button"
        :class="['a-btn', danger ? 'a-btn--danger-solid' : 'a-btn--primary']"
        :disabled="loading"
        @click="emit('confirm')"
      >
        <svg v-if="loading" class="size-4 animate-spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity=".3" stroke-width="2.5" />
          <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
        </svg>
        {{ confirmText }}
      </button>
    </div>
  </dialog>
</template>

<style scoped>
.confirm-dialog::backdrop {
  background: rgb(23 33 43 / 0.4);
  backdrop-filter: blur(2px);
}
.confirm-dialog[open] {
  animation: dialog-in 0.18s var(--ease-soft, ease-out);
}
.confirm-dialog[open]::backdrop {
  animation: backdrop-in 0.18s ease-out;
}
@keyframes dialog-in {
  from { opacity: 0; transform: translateY(8px) scale(0.97); }
}
@keyframes backdrop-in {
  from { opacity: 0; }
}
@media (prefers-reduced-motion: reduce) {
  .confirm-dialog[open],
  .confirm-dialog[open]::backdrop { animation: none; }
}
</style>

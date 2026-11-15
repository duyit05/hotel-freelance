<script setup>
// Ô mật khẩu có nút con mắt để hiện/ẩn. Thuộc tính khác (id, autocomplete, aria-invalid, class...) gắn thẳng vào <input>.
// <AdminPasswordInput id="password" v-model="form.password" autocomplete="new-password" />
import { ref } from 'vue'
import AdminIcon from './AdminIcon.vue'

defineOptions({ inheritAttrs: false })
const model = defineModel({ type: String, default: '' })
const visible = ref(false)
</script>

<template>
  <div class="relative">
    <input v-model="model" :type="visible ? 'text' : 'password'" class="a-input pr-11" v-bind="$attrs" />
    <button
      type="button"
      class="a-btn a-btn--icon absolute top-1/2 right-1 size-8! -translate-y-1/2"
      :aria-label="visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
      :aria-pressed="visible"
      :title="visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
      @click="visible = !visible"
    >
      <AdminIcon v-if="!visible" name="eye" class="size-4" />
      <svg v-else class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
        <path
          d="M3 3l18 18M10.6 5.1A10.4 10.4 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3.2 4M6.6 6.6C3.8 8.4 2 12 2 12s3.5 7 10 7a9.7 9.7 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2"
          stroke-linecap="round"
        />
      </svg>
    </button>
  </div>
</template>

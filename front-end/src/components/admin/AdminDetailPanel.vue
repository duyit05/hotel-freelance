<script setup>
// Panel chi tiết bên phải: cột sticky trên màn hình rộng, drawer trượt từ phải (có lớp phủ) khi < 1150px.
// <AdminDetailPanel :open="!!selected" title="..." @close="selected = null"> nội dung <template #footer>nút</template>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import AdminIcon from './AdminIcon.vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
})
const emit = defineEmits(['close'])
const closeBtn = ref(null)

const onKey = (e) => e.key === 'Escape' && props.open && emit('close')
onMounted(() => window.addEventListener('keydown', onKey))
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))

// Mở panel bằng bàn phím -> đưa focus vào nút đóng (chỉ trên màn hình nhỏ, khi panel là drawer)
watch(
  () => props.open,
  (v) => v && window.matchMedia('(max-width: 1149.98px)').matches && setTimeout(() => closeBtn.value?.focus(), 0),
)
</script>

<template>
  <div v-if="open" class="a-panel-overlay" aria-hidden="true" @click="emit('close')" />
  <aside :class="['a-panel', open && 'is-open']" :aria-label="title || 'Chi tiết'">
    <div class="a-panel-head">
      <h2 class="a-panel-title">{{ open ? title : 'Chi tiết' }}</h2>
      <button v-if="open" ref="closeBtn" type="button" class="a-btn a-btn--icon" aria-label="Đóng (Esc)" title="Đóng (Esc)" @click="emit('close')">
        <AdminIcon name="close" class="size-[18px]" />
      </button>
    </div>
    <div class="a-panel-body">
      <slot v-if="open" />
      <slot v-else name="empty" />
    </div>
    <div v-if="open && $slots.footer" class="a-panel-foot">
      <slot name="footer" />
    </div>
  </aside>
</template>

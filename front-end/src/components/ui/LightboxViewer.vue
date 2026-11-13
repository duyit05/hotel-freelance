<script setup>
// Hộp xem ảnh toàn màn hình, mở bằng useLightbox().open(list, index)
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useLightbox } from '@/composables/useLightbox'

const { state, current, close, next, prev } = useLightbox()
const closeBtn = ref(null)
let returnFocus = null

watch(
  () => state.open,
  async (open) => {
    if (open) {
      returnFocus = document.activeElement
      await nextTick()
      closeBtn.value?.focus()
    } else {
      returnFocus?.focus?.()
    }
  },
)

function onKey(e) {
  if (!state.open) return
  if (e.key === 'Escape') close()
  if (e.key === 'ArrowLeft') prev()
  if (e.key === 'ArrowRight') next()
}
onMounted(() => document.addEventListener('keydown', onKey))
onBeforeUnmount(() => document.removeEventListener('keydown', onKey))

const btn = 'absolute rounded-full bg-white/12 px-[18px] py-2.5 text-[15px] text-white hover:bg-white/25'
</script>

<template>
  <div
    role="dialog"
    aria-modal="true"
    aria-label="Xem ảnh"
    :aria-hidden="!state.open"
    :class="[
      'group fixed inset-0 z-[100] flex items-center justify-center bg-[rgba(28,23,18,.93)] transition-opacity duration-300',
      state.open ? 'is-open opacity-100' : 'pointer-events-none opacity-0',
    ]"
    @click.self="close"
  >
    <img
      v-if="current"
      :src="current.src"
      :alt="current.alt"
      class="max-h-[82vh] max-w-[88vw] scale-96 rounded-lg transition-transform duration-350 ease-soft group-[.is-open]:scale-100"
    />
    <button ref="closeBtn" :class="[btn, 'top-5 right-5']" type="button" @click="close">Đóng</button>
    <button :class="[btn, 'bottom-14 left-5 md:bottom-auto']" type="button" @click="prev">Trước</button>
    <button :class="[btn, 'right-5 bottom-14 md:bottom-auto']" type="button" @click="next">Sau</button>
    <span class="absolute bottom-5 text-sm text-white/80">{{ state.index + 1 }} / {{ state.list.length }}</span>
  </div>
</template>

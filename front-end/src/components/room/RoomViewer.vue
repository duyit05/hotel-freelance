<script setup>
// Ảnh lớn + dải ảnh nhỏ; bấm ảnh nhỏ để đổi, bấm ảnh lớn để phóng to
import { ref } from 'vue'
import AppImage from '@/components/ui/AppImage.vue'
import { useLightbox } from '@/composables/useLightbox'

const props = defineProps({
  photos: { type: Array, required: true }, // [{ src, alt }]
})

const { open } = useLightbox()
const active = ref(0) // ảnh nhỏ đang chọn
const shown = ref(0) // ảnh đang hiển thị ở khung lớn (đổi sau hiệu ứng mờ)
const fading = ref(false)

function select(i) {
  if (i === active.value) return
  active.value = i
  fading.value = true
  setTimeout(() => (shown.value = i), 250)
}
</script>

<template>
  <div
    class="mb-14 grid gap-3.5 md:h-[540px] md:grid-cols-[3fr_1fr] md:grid-rows-[minmax(0,1fr)]"
  >
    <div class="relative aspect-4/3 min-h-0 overflow-hidden rounded-card bg-img md:aspect-auto">
      <button
        type="button"
        class="absolute inset-0 cursor-zoom-in p-0"
        aria-label="Xem ảnh cỡ lớn"
        @click="open(props.photos, active)"
      >
        <AppImage
          :src="photos[shown].src"
          :alt="photos[shown].alt"
          :class="['transition-opacity duration-450', fading && 'opacity-0']"
          @load="fading = false"
        />
      </button>
      <button
        type="button"
        class="absolute right-4 bottom-4 rounded-full bg-surface px-[18px] py-2.5 text-sm font-medium text-ink shadow-[0_8px_20px_-10px_rgba(0,0,0,.4)] hover:bg-bg"
        @click="open(props.photos, 0)"
      >
        Xem tất cả {{ photos.length }} ảnh
      </button>
    </div>

    <div class="grid h-20 min-h-0 grid-cols-4 gap-3.5 md:h-auto md:grid-cols-1 md:grid-rows-4">
      <button
        v-for="(p, i) in photos"
        :key="p.src + i"
        type="button"
        :aria-label="`Xem ảnh ${i + 1}`"
        :aria-pressed="i === active"
        :class="[
          'min-h-0 overflow-hidden rounded-card p-0 outline-2 -outline-offset-2 transition-[opacity,outline-color] duration-300',
          i === active ? 'opacity-100 outline-primary' : 'opacity-60 outline-transparent hover:opacity-90',
        ]"
        @click="select(i)"
      >
        <AppImage :src="p.src" alt="" />
      </button>
    </div>
  </div>
</template>

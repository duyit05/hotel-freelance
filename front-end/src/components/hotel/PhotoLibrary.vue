<script setup>
// Thư viện ảnh có bộ lọc theo nhóm, bấm ảnh để mở lightbox
import { computed, ref } from 'vue'
import AppImage from '@/components/ui/AppImage.vue'
import { PHOTOS, PHOTO_CATEGORIES } from '@/data/hotel'
import { img } from '@/data/images'
import { useLightbox } from '@/composables/useLightbox'

const category = ref('all')
const fading = ref(false)
const { open } = useLightbox()

const visible = computed(() => PHOTOS.filter((p) => category.value === 'all' || p.cat === category.value))

function select(id) {
  if (id === category.value) return
  fading.value = true
  setTimeout(() => {
    category.value = id
    requestAnimationFrame(() => (fading.value = false))
  }, 250)
}

function show(photo) {
  const list = visible.value.map((p) => ({ src: img(p.image), alt: p.alt }))
  open(list, visible.value.indexOf(photo))
}
</script>

<template>
  <div>
    <div v-reveal class="mb-7 flex flex-wrap gap-2.5">
      <button
        v-for="c in PHOTO_CATEGORIES"
        :key="c.id"
        type="button"
        :aria-pressed="category === c.id"
        :class="[
          'rounded-full border px-[18px] py-2 text-[15px] transition-[background-color,color,border-color] duration-250',
          category === c.id
            ? 'border-primary bg-primary text-white'
            : 'border-line bg-surface text-ink hover:border-primary',
        ]"
        @click="select(c.id)"
      >
        {{ c.label }}
      </button>
    </div>

    <div class="grid grid-cols-2 gap-3.5 md:grid-cols-3">
      <button
        v-for="photo in visible"
        :key="photo.image"
        type="button"
        :aria-label="`Xem ảnh ${photo.alt}`"
        :class="[
          'group aspect-4/3 overflow-hidden rounded-card p-0 transition-[opacity,scale] duration-400 ease-soft',
          fading && 'scale-96 opacity-0',
        ]"
        @click="show(photo)"
      >
        <AppImage
          :src="img(photo.image)"
          :alt="photo.alt"
          class="transition-transform duration-800 ease-soft group-hover:scale-107"
        />
      </button>
    </div>
  </div>
</template>

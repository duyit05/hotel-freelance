<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AppImage from '@/components/ui/AppImage.vue'
import AppLink from '@/components/ui/AppLink.vue'
import BookingBar from './BookingBar.vue'
import { HERO_SLIDES } from '@/data/hotel'
import { img } from '@/data/images'
import { prefersReducedMotion } from '@/composables/useReducedMotion'

const INTERVAL = 6000
const current = ref(0)
const hero = ref(null)
let timer, observer
let visible = true

// Tự chuyển ảnh mỗi 6 giây, dừng khi hero khuất khỏi màn hình
function restart() {
  clearInterval(timer)
  if (!prefersReducedMotion() && visible) {
    timer = setInterval(() => (current.value = (current.value + 1) % HERO_SLIDES.length), INTERVAL)
  }
}
function go(i) {
  current.value = i
  restart()
}

onMounted(() => {
  observer = new IntersectionObserver(([e]) => {
    visible = e.isIntersecting
    restart()
  })
  observer.observe(hero.value)
})
onBeforeUnmount(() => {
  clearInterval(timer)
  observer?.disconnect()
})
</script>

<template>
  <section ref="hero" class="relative flex h-[94vh] min-h-[600px] items-end overflow-hidden text-white contain-paint">
    <div
      v-for="(slide, i) in HERO_SLIDES"
      :key="slide.image"
      :class="[
        'absolute inset-0 transition-opacity duration-1400 will-change-[opacity]',
        i === current ? 'opacity-100' : 'opacity-0',
      ]"
    >
      <AppImage
        :src="img(slide.image)"
        :alt="slide.alt"
        :class="['transition-transform duration-[7s] ease-out', i === current ? 'scale-100' : 'scale-108']"
      />
    </div>

    <!-- Lớp phủ tối để chữ dễ đọc -->
    <div
      class="pointer-events-none absolute inset-0 bg-[linear-gradient(to_top,rgba(32,25,18,.7),rgba(32,25,18,.1)_55%,rgba(32,25,18,.35))]"
    />

    <div class="absolute right-6 bottom-[140px] z-[3] hidden flex-col gap-2.5 md:flex" aria-label="Chọn ảnh">
      <button
        v-for="(slide, i) in HERO_SLIDES"
        :key="slide.image"
        type="button"
        :aria-label="`Ảnh ${i + 1}`"
        :class="[
          'size-2.5 rounded-full border-[1.5px] border-white p-0 transition-[background-color,scale] duration-300',
          i === current ? 'scale-120 bg-white' : 'bg-transparent',
        ]"
        @click="go(i)"
      />
    </div>

    <div class="wrap relative z-[2] w-full pb-12">
      <h1 class="mb-4 max-w-[14ch] animate-rise text-[clamp(38px,6vw,70px)]">
        Nghỉ ngơi yên tĩnh, cách biển năm phút đi bộ
      </h1>
      <p class="mb-8 max-w-[46ch] animate-rise text-lg opacity-95 [animation-delay:.15s]">
        48 phòng sáng sủa trên phố biển Mỹ Khê, bữa sáng phục vụ đến 10 giờ và lễ tân hỗ trợ suốt 24 giờ.
      </p>
      <AppLink
        to="/khach-san"
        class="-mt-4 mb-7 inline-block animate-rise font-medium text-white underline underline-offset-[5px] [animation-delay:.3s]"
      >
        Xem giới thiệu khách sạn
      </AppLink>
      <BookingBar class="animate-rise [animation-delay:.3s]" />
    </div>
  </section>
</template>

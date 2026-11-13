<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppLink from '@/components/ui/AppLink.vue'
import { NAV_LINKS } from '@/data/hotel'

const route = useRoute()
const scrolled = ref(false)
const menuOpen = ref(false)

// Header trong suốt trên ảnh hero, chuyển nền trắng khi cuộn xuống hoặc ở trang chi tiết phòng
const solid = computed(() => scrolled.value || route.name === 'room')

let ticking = false
function onScroll() {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    scrolled.value = window.scrollY > 40
    ticking = false
  })
}
onMounted(() => {
  onScroll()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
watch(() => route.fullPath, () => (menuOpen.value = false))

const linkClass = 'block px-5 py-3 text-[15px] no-underline underline-offset-[5px] hover:underline md:inline md:p-0'
</script>

<template>
  <header
    :class="[
      'fixed inset-x-0 top-0 z-50 transition-[background-color,color,box-shadow] duration-350',
      solid ? 'bg-surface text-ink shadow-[0_1px_0_var(--color-line)]' : 'text-white',
    ]"
  >
    <div class="wrap flex h-[72px] items-center justify-between">
      <AppLink to="/" class="font-serif text-[22px] no-underline">Mộc Lan Hotel</AppLink>

      <button
        type="button"
        class="rounded-full border border-current px-3.5 py-1.5 md:hidden"
        :aria-expanded="menuOpen"
        aria-controls="main-menu"
        @click="menuOpen = !menuOpen"
      >
        Menu
      </button>

      <ul
        id="main-menu"
        :class="[
          'absolute top-[72px] right-6 min-w-[220px] flex-col overflow-hidden rounded-card bg-surface text-ink shadow-[0_16px_32px_-16px_rgba(0,0,0,.3)]',
          'md:static md:flex md:min-w-0 md:flex-row md:items-center md:gap-8 md:overflow-visible md:bg-transparent md:text-inherit md:shadow-none',
          menuOpen ? 'flex' : 'hidden',
        ]"
      >
        <li v-for="link in NAV_LINKS" :key="link.to">
          <AppLink :to="link.to" :class="linkClass">{{ link.label }}</AppLink>
        </li>
        <li>
          <AppLink
            to="/#lien-he"
            class="block bg-primary px-5 py-3 text-[15px] text-white no-underline hover:bg-primary-hover md:rounded-full md:px-[18px] md:py-[9px]"
          >
            Đặt phòng
          </AppLink>
        </li>
      </ul>
    </div>
  </header>
</template>

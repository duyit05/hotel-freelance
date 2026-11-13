import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import HotelView from '@/views/HotelView.vue'
import RoomDetailView from '@/views/RoomDetailView.vue'
import { findRoom } from '@/data/rooms'
import { prefersReducedMotion } from '@/composables/useReducedMotion'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/khach-san', name: 'hotel', component: HotelView },
    {
      path: '/phong/:id',
      name: 'room',
      component: RoomDetailView,
      props: true,
      beforeEnter: (to) => (findRoom(to.params.id) ? true : { name: 'home' }),
    },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior(to, from, saved) {
    if (saved) return saved
    // Cuộn mượt khi ở cùng trang, nhảy ngay khi đổi trang
    const behavior = from.path === to.path && !prefersReducedMotion() ? 'smooth' : 'instant'
    if (to.hash) return { el: to.hash, top: 80, behavior }
    return { top: 0, behavior }
  },
})

export default router

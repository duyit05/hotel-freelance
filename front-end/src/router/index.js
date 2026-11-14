import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import HotelView from '@/views/HotelView.vue'
import RoomDetailView from '@/views/RoomDetailView.vue'
import { setUnauthorizedHandler, useAuth } from '@/composables/useAuth'
import { prefersReducedMotion } from '@/composables/useReducedMotion'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/khach-san', name: 'hotel', component: HotelView },
    { path: '/phong/:slug', name: 'room', component: RoomDetailView, props: true },

    /* ----- Khu quản trị (tải riêng, khách thường không phải tải code admin) ----- */
    {
      path: '/admin/dang-nhap',
      name: 'admin-login',
      component: () => import('@/views/admin/AdminLoginView.vue'),
      meta: { admin: true, guestOnly: true },
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { admin: true, requiresAdmin: true },
      children: [
        // meta.title: hiện trên thanh trên cùng; meta.section: mục sáng trên sidebar
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('@/views/admin/AdminDashboardView.vue'),
          meta: { title: 'Tổng quan' },
        },
        {
          path: 'phong',
          name: 'admin-rooms',
          component: () => import('@/views/admin/AdminRoomsView.vue'),
          meta: { title: 'Phòng', section: 'rooms' },
        },
        {
          path: 'phong/moi',
          name: 'admin-room-new',
          component: () => import('@/views/admin/AdminRoomFormView.vue'),
          meta: { title: 'Thêm phòng', section: 'rooms' },
        },
        {
          path: 'phong/:id',
          name: 'admin-room-edit',
          component: () => import('@/views/admin/AdminRoomFormView.vue'),
          props: true,
          meta: { title: 'Sửa phòng', section: 'rooms' },
        },
        {
          path: 'loai-phong',
          name: 'admin-room-types',
          component: () => import('@/views/admin/AdminRoomTypesView.vue'),
          meta: { title: 'Loại phòng', section: 'room-types' },
        },
        {
          path: 'tai-khoan',
          name: 'admin-users',
          component: () => import('@/views/admin/AdminUsersView.vue'),
          meta: { title: 'Tài khoản', section: 'users' },
        },
        {
          path: 'tai-khoan/moi',
          name: 'admin-user-new',
          component: () => import('@/views/admin/AdminUserFormView.vue'),
          meta: { title: 'Thêm tài khoản', section: 'users' },
        },
        {
          path: 'tai-khoan/:id',
          name: 'admin-user-edit',
          component: () => import('@/views/admin/AdminUserFormView.vue'),
          props: true,
          meta: { title: 'Sửa tài khoản', section: 'users' },
        },
      ],
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

// Chặn vào trang admin khi chưa đăng nhập (hoặc token đã hết hạn)
router.beforeEach((to) => {
  const { isAdmin } = useAuth()
  if (to.meta.requiresAdmin && !isAdmin()) {
    return { name: 'admin-login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guestOnly && isAdmin()) {
    return { name: 'admin-dashboard' }
  }
})

// API báo 401 khi đang ở trang admin -> quay về trang đăng nhập
setUnauthorizedHandler(() => {
  const current = router.currentRoute.value
  if (current.meta.requiresAdmin) {
    router.replace({ name: 'admin-login', query: { redirect: current.fullPath, expired: '1' } })
  }
})

export default router

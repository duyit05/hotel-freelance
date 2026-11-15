<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminConfirmDialog from '@/components/admin/AdminConfirmDialog.vue'
import { useAuth } from '@/composables/useAuth'
import { Toaster } from 'vue-sonner'
import 'vue-sonner/style.css'
import { initials } from '@/utils/format'
import '@/assets/admin.css'

const route = useRoute()
const router = useRouter()
const { user, logout } = useAuth()
const drawerOpen = ref(false)

const NAV = [
  { to: 'admin-dashboard', label: 'Tổng quan', icon: 'dashboard', exact: true },
  { to: 'admin-rooms', label: 'Phòng', icon: 'bed', section: 'rooms' },
  { to: 'admin-room-types', label: 'Loại phòng', icon: 'tag', section: 'room-types' },
  { to: 'admin-users', label: 'Tài khoản', icon: 'users', section: 'users' },
]

/** Mục đang chọn: trang con (vd. sửa phòng) vẫn sáng mục cha */
const isActive = (item) => (item.exact ? route.name === item.to : route.meta.section === item.section)
const pageTitle = computed(() => route.meta.title ?? 'Quản trị')

// Đóng menu trên điện thoại khi chuyển trang
watch(() => route.fullPath, () => (drawerOpen.value = false))

// Hỏi lại trước khi đăng xuất
const confirmLogout = ref(false)
const signingOut = ref(false)

async function signOut() {
  signingOut.value = true
  try {
    await logout()
  } finally {
    signingOut.value = false
    confirmLogout.value = false
  }
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="theme-admin min-h-screen md:pl-[232px]">
    <!-- Lớp phủ khi mở menu trên điện thoại -->
    <div v-if="drawerOpen" class="fixed inset-0 z-40 bg-ink/30 md:hidden" @click="drawerOpen = false" />

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-50 flex w-[232px] flex-col border-r border-line bg-surface transition-transform duration-200 md:translate-x-0',
        drawerOpen ? 'translate-x-0 shadow-[12px_0_32px_rgb(23_33_43/.14)] md:shadow-none' : '-translate-x-full',
      ]"
      aria-label="Menu quản trị"
    >
      <div class="flex h-14 items-center justify-between border-b border-line px-4">
        <RouterLink :to="{ name: 'admin-dashboard' }" class="flex items-center gap-2.5 text-ink no-underline">
          <span class="flex size-7 items-center justify-center rounded-[7px] bg-ink text-[13px] font-bold text-white" aria-hidden="true">
            D
          </span>
          <span class="leading-tight">
            <span class="block text-[14px] font-semibold">Duy Nhất Hotel</span>
            <span class="block text-xs text-muted">Quản trị</span>
          </span>
        </RouterLink>
        <button type="button" class="a-btn a-btn--icon md:hidden" aria-label="Đóng menu" @click="drawerOpen = false">
          <AdminIcon name="close" class="size-[18px]" />
        </button>
      </div>

      <nav class="flex-1 overflow-y-auto px-3 py-4">
        <p class="px-2.5 pb-1.5 text-xs text-muted">Quản lý</p>
        <ul class="space-y-px">
          <li v-for="item in NAV" :key="item.to">
            <RouterLink
              :to="{ name: item.to }"
              :class="[
                'flex h-9 items-center gap-2.5 rounded-[7px] px-2.5 text-[13.5px] no-underline',
                isActive(item)
                  ? 'bg-[color-mix(in_srgb,var(--brand)_9%,var(--surface))] font-semibold text-brand'
                  : 'font-medium text-ink-2 hover:bg-surface-2 hover:text-ink',
              ]"
              :aria-current="isActive(item) ? 'page' : undefined"
            >
              <AdminIcon :name="item.icon" class="size-[18px]" />
              {{ item.label }}
            </RouterLink>
          </li>
        </ul>

        <p class="px-2.5 pt-6 pb-1.5 text-xs text-muted">Website</p>
        <a
          href="/"
          target="_blank"
          rel="noopener"
          class="flex h-9 items-center gap-2.5 rounded-[7px] px-2.5 text-[13.5px] font-medium text-ink-2 no-underline hover:bg-surface-2 hover:text-ink"
        >
          <AdminIcon name="external" class="size-[18px]" />
          Xem trang web
        </a>
      </nav>

      <!-- Người đang đăng nhập -->
      <div class="flex items-center gap-2.5 border-t border-line px-3 py-3">
        <span class="flex size-8 shrink-0 items-center justify-center rounded-full border border-line bg-surface-2 text-xs font-semibold text-ink-2">
          {{ initials(user?.fullName) }}
        </span>
        <div class="min-w-0 flex-1 leading-tight">
          <p class="truncate text-[13px] font-semibold">{{ user?.fullName }}</p>
          <p class="truncate text-xs text-muted">@{{ user?.username }}</p>
        </div>
        <button type="button" class="a-btn a-btn--icon" title="Đăng xuất" aria-label="Đăng xuất" @click="confirmLogout = true">
          <AdminIcon name="logout" class="size-[18px]" />
        </button>
      </div>
    </aside>

    <!-- Thanh trên cùng -->
    <header class="sticky top-0 z-30 flex h-14 items-center gap-2 border-b border-line bg-surface px-4 sm:px-8">
      <button type="button" class="a-btn a-btn--icon -ml-2 md:hidden" aria-label="Mở menu" @click="drawerOpen = true">
        <AdminIcon name="menu" class="size-5" />
      </button>
      <p class="text-[13px] text-muted">
        <span class="hidden sm:inline">Quản trị</span>
        <span class="mx-2 hidden sm:inline" aria-hidden="true">/</span>
        <span class="font-semibold text-ink">{{ pageTitle }}</span>
      </p>
    </header>

    <main>
      <RouterView :key="route.path" />
    </main>

    <!-- Toast sau mỗi thao tác (vue-sonner), góc phải dưới. Điện thoại đẩy lên 84px để không bị thanh nút "Lưu" che -->
    <Toaster
      position="bottom-right"
      :mobile-offset="{ bottom: '84px' }"
      rich-colors
      close-button
      :visible-toasts="4"
      :toast-options="{ style: { fontFamily: 'inherit' } }"
    />

    <AdminConfirmDialog
      v-model:open="confirmLogout"
      title="Đăng xuất?"
      icon="logout"
      confirm-text="Đăng xuất"
      :loading="signingOut"
      @confirm="signOut"
    >
      Bạn sẽ cần đăng nhập lại để tiếp tục quản lý khách sạn.
    </AdminConfirmDialog>
  </div>
</template>

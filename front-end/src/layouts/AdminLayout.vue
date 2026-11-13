<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import { useAuth } from '@/composables/useAuth'
import { initials } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const { user, logout } = useAuth()
const drawerOpen = ref(false)

const NAV = [
  { to: 'admin-dashboard', label: 'Tổng quan', icon: 'dashboard', exact: true },
  { to: 'admin-rooms', label: 'Phòng', icon: 'bed', section: 'rooms' },
  { to: 'admin-users', label: 'Tài khoản', icon: 'users', section: 'users' },
]

/** Mục đang chọn: trang con (vd. sửa phòng) vẫn sáng mục cha */
const isActive = (item) => (item.exact ? route.name === item.to : route.meta.section === item.section)
const pageTitle = computed(() => route.meta.title ?? 'Quản trị')

// Đóng menu trên điện thoại khi chuyển trang
watch(() => route.fullPath, () => (drawerOpen.value = false))

function signOut() {
  logout()
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="min-h-screen bg-bg md:pl-[260px]">
    <!-- Lớp mờ khi mở menu trên điện thoại -->
    <Transition
      enter-from-class="opacity-0"
      leave-to-class="opacity-0"
      enter-active-class="transition-opacity"
      leave-active-class="transition-opacity"
    >
      <div v-if="drawerOpen" class="fixed inset-0 z-40 bg-black/40 md:hidden" @click="drawerOpen = false" />
    </Transition>

    <!-- Sidebar -->
    <aside
      :class="[
        'fixed inset-y-0 left-0 z-50 flex w-[260px] flex-col bg-[#24201c] text-white/70 transition-transform duration-300 ease-soft md:translate-x-0',
        drawerOpen ? 'translate-x-0' : '-translate-x-full',
      ]"
      aria-label="Menu quản trị"
    >
      <div class="flex h-[72px] items-center justify-between px-5">
        <RouterLink :to="{ name: 'admin-dashboard' }" class="flex items-center gap-3 text-white no-underline">
          <span class="flex size-9 items-center justify-center rounded-xl bg-accent font-serif text-lg text-[#24201c]">
            M
          </span>
          <span class="leading-tight">
            <span class="block font-serif text-[17px]">Mộc Lan Hotel</span>
            <span class="block text-xs text-white/50">Trang quản trị</span>
          </span>
        </RouterLink>
        <button
          type="button"
          class="rounded-lg p-1.5 hover:bg-white/10 md:hidden"
          aria-label="Đóng menu"
          @click="drawerOpen = false"
        >
          <AdminIcon name="close" class="size-5" />
        </button>
      </div>

      <nav class="flex-1 space-y-1 overflow-y-auto px-3 pt-4">
        <p class="px-3 pb-2 text-[11px] font-medium tracking-[.14em] text-white/35 uppercase">Quản lý</p>
        <RouterLink
          v-for="item in NAV"
          :key="item.to"
          :to="{ name: item.to }"
          :class="[
            'group relative flex items-center gap-3 rounded-xl px-3 py-2.5 text-[15px] no-underline transition-colors',
            isActive(item) ? 'bg-white/10 font-medium text-white' : 'hover:bg-white/5 hover:text-white',
          ]"
          :aria-current="isActive(item) ? 'page' : undefined"
        >
          <span
            v-if="isActive(item)"
            class="absolute top-2 bottom-2 -left-3 w-1 rounded-r-full bg-accent"
            aria-hidden="true"
          />
          <AdminIcon :name="item.icon" :class="['size-5', isActive(item) ? 'text-accent' : '']" />
          {{ item.label }}
        </RouterLink>

        <p class="px-3 pt-6 pb-2 text-[11px] font-medium tracking-[.14em] text-white/35 uppercase">Website</p>
        <a
          href="/"
          target="_blank"
          rel="noopener"
          class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-[15px] no-underline transition-colors hover:bg-white/5 hover:text-white"
        >
          <AdminIcon name="external" class="size-5" />
          Xem trang web
        </a>
      </nav>

      <!-- Người đang đăng nhập -->
      <div class="border-t border-white/10 p-3">
        <div class="flex items-center gap-3 rounded-xl p-2">
          <span
            class="flex size-10 shrink-0 items-center justify-center rounded-full bg-white/10 text-sm font-semibold text-white"
          >
            {{ initials(user?.fullName) }}
          </span>
          <div class="min-w-0 flex-1 leading-tight">
            <p class="truncate text-sm font-medium text-white">{{ user?.fullName }}</p>
            <p class="truncate text-xs text-white/45">@{{ user?.username }}</p>
          </div>
          <button
            type="button"
            class="rounded-lg p-2 text-white/50 transition-colors hover:bg-white/10 hover:text-white"
            title="Đăng xuất"
            aria-label="Đăng xuất"
            @click="signOut"
          >
            <AdminIcon name="logout" class="size-5" />
          </button>
        </div>
      </div>
    </aside>

    <!-- Thanh trên cùng -->
    <header
      class="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-line/70 bg-bg/85 px-4 backdrop-blur-md sm:px-8"
    >
      <button
        type="button"
        class="-ml-1 rounded-lg p-2 text-ink hover:bg-soft md:hidden"
        aria-label="Mở menu"
        @click="drawerOpen = true"
      >
        <AdminIcon name="menu" class="size-5" />
      </button>
      <p class="text-sm text-muted">
        <span class="hidden sm:inline">Quản trị</span>
        <span class="mx-2 hidden text-line sm:inline">/</span>
        <span class="font-medium text-ink">{{ pageTitle }}</span>
      </p>
      <span class="ml-auto hidden text-sm text-muted sm:inline">
        Xin chào, <span class="font-medium text-ink">{{ user?.fullName }}</span>
      </span>
    </header>

    <main class="mx-auto max-w-[1200px] px-4 py-8 sm:px-8">
      <RouterView v-slot="{ Component }">
        <Transition
          mode="out-in"
          enter-from-class="opacity-0 translate-y-2"
          enter-active-class="transition duration-200 ease-soft"
          leave-to-class="opacity-0"
          leave-active-class="transition duration-100"
        >
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

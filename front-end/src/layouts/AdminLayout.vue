<script setup>
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { user, logout } = useAuth()

function signOut() {
  logout()
  router.push({ name: 'admin-login' })
}
</script>

<template>
  <div class="min-h-screen bg-bg">
    <header class="sticky top-0 z-30 border-b border-line bg-surface">
      <div class="mx-auto flex h-16 max-w-[1160px] items-center justify-between gap-4 px-6">
        <div class="flex items-center gap-8">
          <RouterLink :to="{ name: 'admin-rooms' }" class="font-serif text-xl no-underline">
            Mộc Lan <span class="text-sm font-sans text-muted">Quản trị</span>
          </RouterLink>
          <nav class="hidden gap-5 text-[15px] sm:flex">
            <RouterLink
              :to="{ name: 'admin-rooms' }"
              class="no-underline hover:text-primary"
              active-class="font-semibold text-primary"
            >
              Phòng
            </RouterLink>
            <a href="/" target="_blank" rel="noopener" class="no-underline hover:text-primary">Xem trang web ↗</a>
          </nav>
        </div>
        <div class="flex items-center gap-3 text-sm">
          <span class="hidden text-muted sm:inline">{{ user?.username }}</span>
          <button type="button" class="rounded-full border border-line px-4 py-1.5 hover:border-primary" @click="signOut">
            Đăng xuất
          </button>
        </div>
      </div>
    </header>

    <main class="mx-auto max-w-[1160px] px-6 py-8">
      <RouterView />
    </main>
  </div>
</template>

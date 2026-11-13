<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppImage from '@/components/ui/AppImage.vue'
import { useAuth } from '@/composables/useAuth'
import { img } from '@/data/images'

const route = useRoute()
const router = useRouter()
const { login } = useAuth()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const expired = !!route.query.expired
const error = ref('')

async function submit() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  try {
    await login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/admin')
    router.replace(redirect ? route.query.redirect : { name: 'admin-dashboard' })
  } catch (e) {
    error.value = e.message
    password.value = ''
  } finally {
    loading.value = false
  }
}

const FIELD =
  'peer w-full rounded-xl border border-line bg-white py-3 pl-11 pr-4 text-ink outline-none transition-[border-color,box-shadow] placeholder:text-muted/50 focus:border-primary focus:ring-4 focus:ring-primary/10'
const ICON =
  'pointer-events-none absolute top-1/2 left-4 size-[18px] -translate-y-1/2 text-muted transition-colors peer-focus:text-primary'
</script>

<template>
  <main class="grid min-h-screen bg-bg md:grid-cols-[1.1fr_1fr]">
    <!-- Ảnh + thương hiệu (ẩn trên điện thoại) -->
    <aside class="relative hidden overflow-hidden md:block">
      <AppImage :src="img('resort')" alt="Mộc Lan Hotel" class="motion-safe:animate-slow-zoom" />
      <div class="absolute inset-0 bg-linear-to-t from-black/75 via-black/25 to-black/10" />

      <div class="absolute inset-0 flex flex-col justify-between p-12 text-white">
        <RouterLink to="/" class="w-fit font-serif text-2xl text-white no-underline">Mộc Lan Hotel</RouterLink>

        <div class="max-w-md motion-safe:animate-rise">
          <span class="mb-5 block h-px w-12 bg-accent" />
          <p class="font-serif text-[34px] leading-tight">
            Mỗi kỳ nghỉ trọn vẹn bắt đầu từ sự chăm chút phía sau.
          </p>
          <p class="mt-4 text-white/75">Trang quản trị dành cho đội ngũ Mộc Lan: cập nhật phòng, giá và tài khoản.</p>
        </div>
      </div>
    </aside>

    <!-- Form đăng nhập -->
    <section class="relative flex items-center justify-center px-6 py-12">
      <div
        class="pointer-events-none absolute inset-x-0 top-0 h-56 bg-linear-to-b from-soft to-transparent md:hidden"
        aria-hidden="true"
      />

      <div class="relative w-full max-w-[400px] motion-safe:animate-view-in">
        <div class="mb-10">
          <span
            class="mb-6 flex size-12 items-center justify-center rounded-2xl bg-primary font-serif text-xl text-white shadow-[0_10px_24px_-10px_rgba(91,70,54,.7)]"
            aria-hidden="true"
          >
            M
          </span>
          <p class="mb-1 text-sm font-medium tracking-[.12em] text-accent uppercase">Khu quản trị</p>
          <h1 class="text-[34px] leading-tight">Chào mừng trở lại</h1>
          <p class="mt-2 text-muted">Đăng nhập để tiếp tục quản lý khách sạn.</p>
        </div>

        <p
          v-if="expired && !error"
          class="mb-6 flex gap-3 rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800"
          role="status"
        >
          <svg class="mt-0.5 size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" stroke-linecap="round" />
          </svg>
          Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.
        </p>

        <form class="space-y-5" novalidate @submit.prevent="submit">
          <div>
            <label for="username" class="mb-2 block text-sm font-medium">Tên đăng nhập</label>
            <div class="relative">
              <input
                id="username"
                v-model="username"
                :class="FIELD"
                :aria-invalid="!!error"
                placeholder="vd. admin"
                autocomplete="username"
                autocapitalize="off"
                spellcheck="false"
                required
                autofocus
              />
              <svg :class="ICON" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <circle cx="12" cy="8" r="4" />
                <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7" stroke-linecap="round" />
              </svg>
            </div>
          </div>

          <div>
            <label for="password" class="mb-2 block text-sm font-medium">Mật khẩu</label>
            <div class="relative">
              <input
                id="password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                :class="[FIELD, 'pr-12']"
                :aria-invalid="!!error"
                placeholder="••••••••"
                autocomplete="current-password"
                required
              />
              <svg :class="ICON" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="4" y="10" width="16" height="11" rx="2.5" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3" />
              </svg>
              <button
                type="button"
                class="absolute top-1/2 right-2 flex size-9 -translate-y-1/2 items-center justify-center rounded-lg text-muted hover:bg-soft hover:text-ink"
                :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                :aria-pressed="showPassword"
                @click="showPassword = !showPassword"
              >
                <svg v-if="!showPassword" class="size-[18px]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
                <svg v-else class="size-[18px]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M3 3l18 18M10.6 5.1A10.4 10.4 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3.2 4M6.6 6.6C3.8 8.4 2 12 2 12s3.5 7 10 7a9.7 9.7 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2" stroke-linecap="round" />
                </svg>
              </button>
            </div>
          </div>

          <p
            v-if="error"
            class="flex gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
            role="alert"
          >
            <svg class="mt-0.5 size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="9" /><path d="M12 8v5M12 16.5v.01" stroke-linecap="round" />
            </svg>
            {{ error }}
          </p>

          <button
            type="submit"
            class="flex w-full items-center justify-center gap-2 rounded-xl bg-primary py-3.5 font-semibold text-white shadow-[0_12px_24px_-12px_rgba(91,70,54,.8)] transition-[background-color,translate] hover:-translate-y-0.5 hover:bg-primary-hover disabled:translate-y-0 disabled:cursor-wait disabled:opacity-80"
            :disabled="loading"
          >
            <svg v-if="loading" class="size-5 animate-spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity=".3" stroke-width="3" />
              <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="3" stroke-linecap="round" />
            </svg>
            {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
          </button>
        </form>

        <div class="mt-10 flex items-center justify-between border-t border-line pt-6 text-sm text-muted">
          <RouterLink to="/" class="no-underline hover:text-primary">← Về trang chủ</RouterLink>
          <span>© {{ new Date().getFullYear() }} Mộc Lan Hotel</span>
        </div>
      </div>
    </section>
  </main>
</template>

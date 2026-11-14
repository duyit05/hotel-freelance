<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import { useAuth } from '@/composables/useAuth'
import '@/assets/admin.css'

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
</script>

<template>
  <main class="theme-admin flex min-h-screen flex-col bg-surface-2">
    <div class="flex flex-1 items-center justify-center px-4 py-12">
      <div class="w-full max-w-[380px]">
        <div class="mb-6 flex items-center gap-2.5">
          <span class="flex size-8 items-center justify-center rounded-lg bg-ink text-sm font-bold text-white" aria-hidden="true">D</span>
          <span class="leading-tight">
            <span class="block text-[14px] font-semibold">Duy Nhất Hotel</span>
            <span class="block text-xs text-muted">Trang quản trị</span>
          </span>
        </div>

        <div class="a-block p-6 sm:p-7">
          <h1 class="a-title">Đăng nhập</h1>
          <p class="a-sub mb-6">Dùng tài khoản quản trị để tiếp tục.</p>

          <AdminAlert v-if="expired && !error" type="warning" class="mb-5">
            Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.
          </AdminAlert>

          <form class="space-y-4" novalidate @submit.prevent="submit">
            <div>
              <label for="username" class="mb-1.5 block text-[13px] font-medium text-ink-2">Tên đăng nhập</label>
              <input
                id="username"
                v-model="username"
                class="a-input"
                :aria-invalid="!!error"
                placeholder="vd. admin"
                autocomplete="username"
                autocapitalize="off"
                spellcheck="false"
                required
                autofocus
              />
            </div>

            <div>
              <label for="password" class="mb-1.5 block text-[13px] font-medium text-ink-2">Mật khẩu</label>
              <div class="relative">
                <input
                  id="password"
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  class="a-input pr-11"
                  :aria-invalid="!!error"
                  autocomplete="current-password"
                  required
                />
                <button
                  type="button"
                  class="a-btn a-btn--icon absolute top-1/2 right-1 size-8! -translate-y-1/2"
                  :aria-label="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                  :aria-pressed="showPassword"
                  @click="showPassword = !showPassword"
                >
                  <AdminIcon v-if="!showPassword" name="eye" class="size-4" />
                  <svg v-else class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true">
                    <path
                      d="M3 3l18 18M10.6 5.1A10.4 10.4 0 0 1 12 5c6.5 0 10 7 10 7a17 17 0 0 1-3.2 4M6.6 6.6C3.8 8.4 2 12 2 12s3.5 7 10 7a9.7 9.7 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2"
                      stroke-linecap="round"
                    />
                  </svg>
                </button>
              </div>
            </div>

            <AdminAlert v-if="error" type="error">{{ error }}</AdminAlert>

            <button type="submit" class="a-btn a-btn--primary h-10! w-full" :disabled="loading">
              {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
            </button>
          </form>
        </div>

        <div class="mt-5 flex items-center justify-between text-[13px] text-muted">
          <RouterLink to="/" class="inline-flex items-center gap-1.5 text-ink-2 no-underline hover:text-ink">
            <AdminIcon name="arrowLeft" class="size-4" /> Về trang chủ
          </RouterLink>
          <span>© {{ new Date().getFullYear() }} Duy Nhất Hotel</span>
        </div>
      </div>
    </div>
  </main>
</template>

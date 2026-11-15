<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPasswordInput from '@/components/admin/AdminPasswordInput.vue'
import { useAuth } from '@/composables/useAuth'
import { img } from '@/data/images'
import '@/assets/admin.css'

const route = useRoute()
const router = useRouter()
const { login } = useAuth()

const username = ref('')
const password = ref('')
const capsLock = ref(false)
const loading = ref(false)
const expired = !!route.query.expired
const error = ref('')
const year = new Date().getFullYear()

const FEATURES = [
  { icon: 'bed', text: 'Quản lý phòng, giá và hình ảnh' },
  { icon: 'tag', text: 'Sắp xếp loại phòng hiển thị trên website' },
  { icon: 'users', text: 'Phân quyền tài khoản quản trị' },
]

/** Nhắc khi đang bật Caps Lock lúc gõ mật khẩu */
function checkCapsLock(e) {
  capsLock.value = e.getModifierState?.('CapsLock') ?? false
}

async function submit() {
  if (loading.value) return
  if (!username.value.trim() || !password.value) {
    error.value = 'Vui lòng nhập tên đăng nhập và mật khẩu'
    return
  }
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
  <main class="theme-admin grid min-h-screen bg-surface lg:grid-cols-[minmax(0,1.1fr)_minmax(0,1fr)]">
    <!-- Ảnh khách sạn (ẩn trên màn hình nhỏ) -->
    <section class="relative hidden overflow-hidden bg-ink lg:block" aria-hidden="true">
      <img :src="img('resort')" alt="" class="absolute inset-0 size-full object-cover animate-slow-zoom" />
      <div class="absolute inset-0 bg-[linear-gradient(180deg,rgb(16_24_40/.55)_0%,rgb(16_24_40/.15)_35%,rgb(16_24_40/.85)_100%)]" />

      <div class="relative flex h-full flex-col justify-between p-10 text-white xl:p-14">
        <div class="flex items-center gap-3">
          <span class="flex size-10 items-center justify-center rounded-xl bg-white/15 text-base font-bold ring-1 ring-white/25 backdrop-blur">
            D
          </span>
          <span class="leading-tight">
            <span class="block text-[15px] font-semibold">Duy Nhất Hotel</span>
            <span class="block text-xs text-white/70">Trang quản trị</span>
          </span>
        </div>

        <div class="max-w-md">
          <p class="font-serif text-[34px] leading-tight font-medium xl:text-[40px]">
            Chào mừng trở lại.
          </p>
          <p class="mt-3 text-[15px] leading-relaxed text-white/80">
            Cập nhật phòng và hình ảnh, mọi thay đổi hiển thị ngay trên website khách sạn.
          </p>
          <ul class="mt-8 space-y-3">
            <li v-for="f in FEATURES" :key="f.text" class="flex items-center gap-3 text-[14px] text-white/90">
              <span class="flex size-8 items-center justify-center rounded-lg bg-white/12 ring-1 ring-white/20 backdrop-blur">
                <AdminIcon :name="f.icon" class="size-4" />
              </span>
              {{ f.text }}
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- Form đăng nhập -->
    <section class="flex flex-col bg-surface-2 lg:bg-surface">
      <div class="flex flex-1 items-center justify-center px-5 py-12 sm:px-8">
        <div class="w-full max-w-[400px]">
          <!-- Logo cho màn hình nhỏ (màn lớn đã có ở cột ảnh) -->
          <div class="mb-8 flex items-center gap-2.5 lg:hidden">
            <span class="flex size-9 items-center justify-center rounded-[10px] bg-brand text-sm font-bold text-white" aria-hidden="true">D</span>
            <span class="leading-tight">
              <span class="block text-[15px] font-semibold">Duy Nhất Hotel</span>
              <span class="block text-xs text-muted">Trang quản trị</span>
            </span>
          </div>

          <div class="rounded-2xl border border-line bg-surface p-6 shadow-[0_8px_30px_rgb(23_33_43/.06)] sm:p-8 lg:border-0 lg:p-0 lg:shadow-none">
            <span class="mb-5 flex size-11 items-center justify-center rounded-xl bg-[color-mix(in_srgb,var(--brand)_10%,var(--surface))] text-brand">
              <AdminIcon name="shield" class="size-5" />
            </span>
            <h1 class="a-title">Đăng nhập</h1>
            <p class="a-sub mb-7">Nhập tài khoản quản trị để tiếp tục.</p>

            <AdminAlert v-if="expired && !error" type="warning" class="mb-5">
              Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.
            </AdminAlert>

            <form class="space-y-5" novalidate @submit.prevent="submit">
              <div>
                <label for="username" class="mb-1.5 block text-[13px] font-medium text-ink-2">Tên đăng nhập</label>
                <div class="relative">
                  <AdminIcon name="guests" class="pointer-events-none absolute top-1/2 left-3.5 size-[18px] -translate-y-1/2 text-muted" />
                  <input
                    id="username"
                    v-model="username"
                    class="a-input h-11 pl-10!"
                    :aria-invalid="!!error"
                    placeholder="vd. admin"
                    autocomplete="username"
                    autocapitalize="off"
                    spellcheck="false"
                    required
                    autofocus
                  />
                </div>
              </div>

              <div>
                <label for="password" class="mb-1.5 block text-[13px] font-medium text-ink-2">Mật khẩu</label>
                <div class="relative">
                  <AdminIcon name="lock" class="pointer-events-none absolute top-1/2 left-3.5 z-10 size-[18px] -translate-y-1/2 text-muted" />
                  <AdminPasswordInput
                    id="password"
                    v-model="password"
                    class="h-11 pl-10!"
                    :aria-invalid="!!error"
                    placeholder="••••••••"
                    autocomplete="current-password"
                    aria-describedby="caps-hint"
                    required
                    @keydown="checkCapsLock"
                    @keyup="checkCapsLock"
                    @blur="capsLock = false"
                  />
                </div>
                <p id="caps-hint" class="mt-1.5 flex items-center gap-1.5 text-xs text-warn" :class="{ invisible: !capsLock }" aria-live="polite">
                  <AdminIcon name="alert" class="size-3.5" />
                  <span>{{ capsLock ? 'Đang bật Caps Lock' : '' }}</span>
                </p>
              </div>

              <AdminAlert v-if="error" type="error">{{ error }}</AdminAlert>

              <button type="submit" class="a-btn a-btn--primary h-11! w-full text-[14px]!" :disabled="loading">
                <svg v-if="loading" class="size-4 animate-spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-opacity=".3" stroke-width="2.5" />
                  <path d="M21 12a9 9 0 0 0-9-9" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
                </svg>
                {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
                <AdminIcon v-if="!loading" name="arrowRight" class="size-4" />
              </button>
            </form>
          </div>

          <p class="mt-6 text-center text-xs text-muted">
            Quên mật khẩu? Liên hệ quản trị viên khác để được đặt lại.
          </p>
        </div>
      </div>

      <footer class="flex items-center justify-between border-t border-line px-5 py-4 text-[13px] text-muted sm:px-8">
        <RouterLink to="/" class="inline-flex items-center gap-1.5 text-ink-2 no-underline hover:text-ink">
          <AdminIcon name="arrowLeft" class="size-4" /> Về trang chủ
        </RouterLink>
        <span>© {{ year }} Duy Nhất Hotel</span>
      </footer>
    </section>
  </main>
</template>

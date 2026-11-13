<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseButton from '@/components/ui/BaseButton.vue'
import AdminField from '@/components/admin/AdminField.vue'
import { INPUT } from '@/components/admin/styles'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { login } = useAuth()

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref(route.query.expired ? 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.' : '')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await login(username.value.trim(), password.value)
    const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/admin')
    router.replace(redirect ? route.query.redirect : { name: 'admin-rooms' })
  } catch (e) {
    error.value = e.message
    password.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-soft px-6">
    <form class="w-full max-w-sm rounded-card bg-surface p-8 shadow-[0_20px_40px_-24px_rgba(0,0,0,.3)]" @submit.prevent="submit">
      <h1 class="mb-1 text-[28px]">Mộc Lan Hotel</h1>
      <p class="mb-7 text-muted">Đăng nhập trang quản trị</p>

      <div class="space-y-4">
        <AdminField id="username" label="Tên đăng nhập">
          <input id="username" v-model="username" :class="INPUT" autocomplete="username" required autofocus />
        </AdminField>
        <AdminField id="password" label="Mật khẩu">
          <input
            id="password"
            v-model="password"
            type="password"
            :class="INPUT"
            autocomplete="current-password"
            required
          />
        </AdminField>
      </div>

      <p v-if="error" class="mt-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">{{ error }}</p>

      <BaseButton type="submit" class="mt-6 w-full" :disabled="loading">
        {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
      </BaseButton>
      <RouterLink to="/" class="mt-4 block text-center text-sm text-muted no-underline hover:text-primary">
        ← Về trang chủ
      </RouterLink>
    </form>
  </main>
</template>

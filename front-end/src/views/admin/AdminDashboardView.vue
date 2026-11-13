<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import { BTN_PRIMARY, BTN_SECONDARY, CARD, CARD_TITLE } from '@/components/admin/styles'
import { roomApi } from '@/api/rooms'
import { userApi } from '@/api/users'
import { useAuth } from '@/composables/useAuth'
import { initials, money } from '@/utils/format'

const { user } = useAuth()
const rooms = ref([])
const users = ref([])
const loading = ref(true)
const error = ref('')

const greeting = (() => {
  const h = new Date().getHours()
  return h < 11 ? 'Chào buổi sáng' : h < 14 ? 'Chào buổi trưa' : h < 18 ? 'Chào buổi chiều' : 'Chào buổi tối'
})()
const today = new Date().toLocaleDateString('vi-VN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })

async function load() {
  loading.value = true
  error.value = ''
  try {
    ;[rooms.value, users.value] = await Promise.all([roomApi.list(), userApi.list()])
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
onMounted(load)

const prices = computed(() => rooms.value.map((r) => r.price).filter((p) => p != null))
const stats = computed(() => [
  { label: 'Loại phòng', value: rooms.value.length, icon: 'bed', tone: 'bg-[#efe6d8] text-primary' },
  {
    label: 'Giá trung bình / đêm',
    value: prices.value.length ? money(Math.round(prices.value.reduce((a, b) => a + b, 0) / prices.value.length)) : '—',
    icon: 'money',
    tone: 'bg-emerald-50 text-emerald-700',
  },
  {
    label: 'Sức chứa tối đa',
    value: `${rooms.value.reduce((s, r) => s + (r.maxGuests ?? 0), 0)} khách`,
    icon: 'guests',
    tone: 'bg-sky-50 text-sky-700',
  },
  { label: 'Tài khoản quản trị', value: users.value.length, icon: 'users', tone: 'bg-violet-50 text-violet-700' },
])

/** Cảnh báo nội dung chưa đầy đủ để admin bổ sung */
const todo = computed(() =>
  rooms.value
    .map((r) => ({
      room: r,
      issues: [
        r.images.length < 3 && `${r.images.length ? 'chỉ có ' + r.images.length : 'chưa có'} ảnh`,
        !r.shortDescription && 'thiếu mô tả ngắn',
        !r.description && 'thiếu giới thiệu chi tiết',
      ].filter(Boolean),
    }))
    .filter((x) => x.issues.length),
)

const myId = computed(() => users.value.find((u) => u.username === user.value?.username)?.id)

const priceRange = computed(() =>
  prices.value.length ? `${money(Math.min(...prices.value))} – ${money(Math.max(...prices.value))}` : '—',
)
</script>

<template>
  <div class="space-y-6">
    <!-- Lời chào -->
    <section
      class="relative overflow-hidden rounded-2xl bg-linear-to-br from-primary to-[#3b2d22] p-7 text-white sm:p-8"
    >
      <div class="absolute -top-16 -right-10 size-56 rounded-full bg-accent/20 blur-2xl" aria-hidden="true" />
      <div class="relative flex flex-wrap items-end justify-between gap-6">
        <div>
          <p class="text-sm text-white/60 first-letter:uppercase">{{ today }}</p>
          <h1 class="mt-1 text-[30px] leading-tight">{{ greeting }}, {{ user?.fullName }}</h1>
          <p class="mt-2 max-w-lg text-white/70">Đây là tình hình nội dung trên website Mộc Lan Hotel hôm nay.</p>
        </div>
        <div class="flex flex-wrap gap-2">
          <RouterLink
            :to="{ name: 'admin-room-new' }"
            class="inline-flex items-center gap-2 rounded-xl bg-white px-4 py-2.5 font-medium text-primary no-underline hover:bg-soft"
          >
            <AdminIcon name="plus" class="size-[18px]" /> Thêm phòng
          </RouterLink>
          <a
            href="/"
            target="_blank"
            rel="noopener"
            class="inline-flex items-center gap-2 rounded-xl border border-white/25 px-4 py-2.5 font-medium text-white no-underline hover:bg-white/10"
          >
            <AdminIcon name="external" class="size-[18px]" /> Xem website
          </a>
        </div>
      </div>
    </section>

    <AdminAlert v-if="error" type="error">
      {{ error }}
      <button type="button" class="ml-1 font-semibold underline" @click="load">Thử lại</button>
    </AdminAlert>

    <!-- Thống kê -->
    <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <div v-for="s in stats" :key="s.label" :class="[CARD, 'flex items-center gap-4 p-5']">
        <span :class="['flex size-12 shrink-0 items-center justify-center rounded-xl', s.tone]">
          <AdminIcon :name="s.icon" class="size-6" />
        </span>
        <div class="min-w-0">
          <p class="text-sm text-muted">{{ s.label }}</p>
          <p v-if="loading" class="mt-1 h-7 w-20 animate-pulse rounded-md bg-soft" />
          <p v-else class="truncate text-[22px] font-semibold tracking-tight">{{ s.value }}</p>
        </div>
      </div>
    </section>

    <div class="grid items-start gap-6 lg:grid-cols-[1fr_360px]">
      <!-- Phòng -->
      <section :class="[CARD, 'p-0']">
        <div class="flex items-center justify-between px-6 pt-5 pb-3">
          <div>
            <h2 :class="CARD_TITLE">Các loại phòng</h2>
            <p class="text-sm text-muted">Khoảng giá {{ priceRange }}</p>
          </div>
          <RouterLink
            :to="{ name: 'admin-rooms' }"
            class="inline-flex items-center gap-1 text-sm font-medium text-primary no-underline hover:underline"
          >
            Xem tất cả <AdminIcon name="arrowRight" class="size-4" />
          </RouterLink>
        </div>
        <ul class="divide-y divide-line/70">
          <template v-if="loading">
            <li v-for="i in 4" :key="i" class="flex items-center gap-4 px-6 py-3.5">
              <span class="h-12 w-16 animate-pulse rounded-lg bg-soft" />
              <span class="h-4 w-40 animate-pulse rounded bg-soft" />
            </li>
          </template>
          <li v-else-if="!rooms.length" class="px-6 py-10 text-center text-muted">Chưa có phòng nào.</li>
          <li v-for="r in rooms.slice(0, 6)" v-else :key="r.id">
            <RouterLink
              :to="{ name: 'admin-room-edit', params: { id: r.id } }"
              class="flex items-center gap-4 px-6 py-3 no-underline transition-colors hover:bg-soft/40"
            >
              <img v-if="r.images[0]" :src="r.images[0]" alt="" class="h-12 w-16 shrink-0 rounded-lg bg-img object-cover" />
              <span v-else class="h-12 w-16 shrink-0 rounded-lg bg-soft" />
              <span class="min-w-0 flex-1">
                <span class="block truncate font-medium">{{ r.name }}</span>
                <span class="block text-sm text-muted">Tối đa {{ r.maxGuests }} khách · {{ r.images.length }} ảnh</span>
              </span>
              <span class="shrink-0 text-right font-semibold">{{ money(r.price) }}</span>
            </RouterLink>
          </li>
        </ul>
      </section>

      <div class="space-y-6">
        <!-- Cần bổ sung -->
        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-1']">Cần bổ sung</h2>
          <p class="mb-4 text-sm text-muted">Nội dung đầy đủ giúp khách dễ quyết định đặt phòng.</p>
          <div v-if="loading" class="h-16 animate-pulse rounded-xl bg-soft" />
          <p
            v-else-if="!todo.length"
            class="flex items-center gap-2 rounded-xl bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
          >
            <AdminIcon name="check" class="size-[18px]" /> Tất cả phòng đã đủ nội dung.
          </p>
          <ul v-else class="space-y-2">
            <li v-for="t in todo" :key="t.room.id">
              <RouterLink
                :to="{ name: 'admin-room-edit', params: { id: t.room.id } }"
                class="block rounded-xl border border-amber-200/70 bg-amber-50/60 px-4 py-2.5 no-underline hover:border-amber-300"
              >
                <span class="block text-sm font-medium">{{ t.room.name }}</span>
                <span class="block text-[13px] text-amber-800 first-letter:uppercase">{{ t.issues.join(', ') }}</span>
              </RouterLink>
            </li>
          </ul>
        </section>

        <!-- Tài khoản -->
        <section :class="CARD">
          <div class="mb-4 flex items-center justify-between">
            <h2 :class="CARD_TITLE">Tài khoản quản trị</h2>
            <RouterLink :to="{ name: 'admin-users' }" class="text-sm font-medium text-primary no-underline hover:underline">
              Quản lý
            </RouterLink>
          </div>
          <ul class="space-y-3">
            <li v-for="u in users.slice(0, 5)" :key="u.id" class="flex items-center gap-3">
              <span
                class="flex size-9 shrink-0 items-center justify-center rounded-full bg-soft text-xs font-semibold text-primary"
              >
                {{ initials(u.fullName) }}
              </span>
              <span class="min-w-0 leading-tight">
                <span class="block truncate text-sm font-medium">{{ u.fullName }}</span>
                <span class="block truncate text-xs text-muted">@{{ u.username }}</span>
              </span>
            </li>
          </ul>
          <div class="mt-5 grid grid-cols-2 gap-2">
            <RouterLink :to="{ name: 'admin-user-new' }" :class="[BTN_SECONDARY, 'py-2 text-sm']">
              <AdminIcon name="plus" class="size-4" /> Thêm
            </RouterLink>
            <RouterLink
              v-if="myId"
              :to="{ name: 'admin-user-edit', params: { id: myId } }"
              :class="[BTN_PRIMARY, 'py-2 text-sm']"
            >
              <AdminIcon name="key" class="size-4" /> Đổi mật khẩu
            </RouterLink>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

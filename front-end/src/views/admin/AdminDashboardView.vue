<script setup>
import { computed, onMounted, ref } from 'vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
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
const typeCount = computed(() => new Set(rooms.value.map((r) => r.type?.id).filter(Boolean)).size)

/** Ô số liệu: ô có `to` bấm được để mở trang tương ứng */
const stats = computed(() => [
  { label: 'Phòng', value: rooms.value.length, note: 'Đang hiển thị trên web', to: { name: 'admin-rooms' } },
  { label: 'Loại phòng đang dùng', value: typeCount.value, note: 'Có ít nhất 1 phòng', to: { name: 'admin-room-types' } },
  {
    label: 'Giá trung bình / đêm',
    value: prices.value.length ? money(Math.round(prices.value.reduce((a, b) => a + b, 0) / prices.value.length)) : '—',
    note: priceRange.value,
    small: true,
  },
  { label: 'Sức chứa tối đa', value: rooms.value.reduce((s, r) => s + (r.maxGuests ?? 0), 0), note: 'Khách, cộng tất cả phòng' },
  { label: 'Tài khoản quản trị', value: users.value.length, note: 'Được vào trang này', to: { name: 'admin-users' } },
])

/** Cảnh báo nội dung chưa đầy đủ để admin bổ sung */
const todo = computed(() =>
  rooms.value
    .map((r) => ({
      room: r,
      issues: [
        r.images.length < 3 && `${r.images.length ? 'Chỉ có ' + r.images.length : 'Chưa có'} ảnh`,
        !r.shortDescription && 'Thiếu mô tả ngắn',
        !r.description && 'Thiếu giới thiệu',
      ].filter(Boolean),
    }))
    .filter((x) => x.issues.length),
)

const myId = computed(() => users.value.find((u) => u.username === user.value?.username)?.id)

const priceRange = computed(() =>
  prices.value.length ? `Từ ${money(Math.min(...prices.value))} đến ${money(Math.max(...prices.value))}` : 'Chưa có giá',
)
</script>

<template>
  <div class="a-page">
    <AdminPageHeader title="Tổng quan" :subtitle="`${greeting}, ${user?.fullName ?? ''}. Hôm nay là ${today}.`">
      <template #actions>
        <a href="/" target="_blank" rel="noopener" :class="BTN_SECONDARY">
          <AdminIcon name="external" class="size-4" /> Xem website
        </a>
        <RouterLink :to="{ name: 'admin-room-new' }" :class="BTN_PRIMARY">
          <AdminIcon name="plus" class="size-4" /> Thêm phòng
        </RouterLink>
      </template>
    </AdminPageHeader>

    <AdminAlert v-if="error" type="error" class="mb-5">
      {{ error }}
      <button type="button" class="a-link ml-1" @click="load">Thử lại</button>
    </AdminAlert>

    <!-- Số liệu tóm tắt -->
    <section class="a-stats mb-6" aria-label="Tóm tắt">
      <component
        :is="s.to ? 'RouterLink' : 'div'"
        v-for="s in stats"
        :key="s.label"
        :to="s.to"
        class="a-stat"
      >
        <span class="a-stat-label">{{ s.label }}</span>
        <span :class="['a-stat-value', s.small && 'a-stat-value--sm']">{{ loading ? '–' : s.value }}</span>
        <span class="a-stat-note">{{ loading ? ' ' : s.note }}</span>
      </component>
    </section>

    <div class="grid items-start gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
      <!-- Phòng -->
      <section :class="[CARD, 'overflow-hidden']">
        <div class="a-block-head">
          <h2 :class="CARD_TITLE">Các phòng</h2>
          <RouterLink :to="{ name: 'admin-rooms' }" class="a-link text-[13px]">Xem tất cả</RouterLink>
        </div>
        <div class="overflow-x-auto">
          <table class="a-table min-w-[460px]">
            <thead>
              <tr>
                <th>Phòng</th>
                <th>Loại</th>
                <th class="num">Khách</th>
                <th class="num">Giá / đêm</th>
              </tr>
            </thead>
            <tbody>
              <template v-if="loading">
                <tr v-for="i in 4" :key="i">
                  <td colspan="4"><div class="a-skel h-7" /></td>
                </tr>
              </template>
              <tr v-else-if="!rooms.length">
                <td colspan="4">
                  <div class="a-empty">
                    <p class="a-empty-title">Chưa có phòng nào</p>
                    <RouterLink :to="{ name: 'admin-room-new' }" :class="[BTN_PRIMARY, 'mt-4']">Thêm phòng</RouterLink>
                  </div>
                </td>
              </tr>
              <tr v-for="r in rooms.slice(0, 6)" v-else :key="r.id">
                <td>
                  <RouterLink :to="{ name: 'admin-room-edit', params: { id: r.id } }" class="flex items-center gap-3 text-ink no-underline hover:underline">
                    <img v-if="r.images[0]" :src="r.images[0]" alt="" class="h-8 w-11 shrink-0 rounded-[5px] bg-surface-2 object-cover" />
                    <span v-else class="h-8 w-11 shrink-0 rounded-[5px] bg-surface-2" />
                    <span class="truncate font-medium">{{ r.name }}</span>
                  </RouterLink>
                </td>
                <td class="text-ink-2">{{ r.type?.name ?? '—' }}</td>
                <td class="num">{{ r.maxGuests }}</td>
                <td class="num font-medium">{{ money(r.price) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <div class="space-y-5">
        <!-- Cần bổ sung -->
        <section :class="[CARD, 'overflow-hidden']">
          <div class="a-block-head">
            <h2 :class="CARD_TITLE">Cần bổ sung</h2>
            <span v-if="!loading" :class="['a-badge', todo.length ? 'tone-warn' : 'tone-ok']">
              {{ todo.length ? `${todo.length} phòng` : 'Đã đủ' }}
            </span>
          </div>
          <div v-if="loading" class="p-4"><div class="a-skel h-14" /></div>
          <p v-else-if="!todo.length" class="px-[18px] py-4 text-[13px] text-ink-2">Tất cả phòng đã đủ ảnh và mô tả.</p>
          <ul v-else>
            <li v-for="t in todo" :key="t.room.id" class="border-b border-line last:border-0">
              <RouterLink
                :to="{ name: 'admin-room-edit', params: { id: t.room.id } }"
                class="block px-[18px] py-2.5 text-ink no-underline hover:bg-surface-2"
              >
                <span class="block font-medium">{{ t.room.name }}</span>
                <span class="mt-1 flex flex-wrap gap-1">
                  <span v-for="i in t.issues" :key="i" class="a-badge tone-warn h-[22px]! text-xs!">{{ i }}</span>
                </span>
              </RouterLink>
            </li>
          </ul>
        </section>

        <!-- Tài khoản -->
        <section :class="[CARD, 'overflow-hidden']">
          <div class="a-block-head">
            <h2 :class="CARD_TITLE">Tài khoản quản trị</h2>
            <RouterLink :to="{ name: 'admin-users' }" class="a-link text-[13px]">Quản lý</RouterLink>
          </div>
          <ul class="px-[18px] py-2">
            <li v-for="u in users.slice(0, 5)" :key="u.id" class="flex items-center gap-3 py-2">
              <span class="flex size-8 shrink-0 items-center justify-center rounded-full border border-line bg-surface-2 text-xs font-semibold text-ink-2">
                {{ initials(u.fullName) }}
              </span>
              <span class="min-w-0 leading-tight">
                <span class="block truncate font-medium">{{ u.fullName }}</span>
                <span class="block truncate text-[12.5px] text-muted">@{{ u.username }}</span>
              </span>
            </li>
          </ul>
          <div class="flex gap-2 border-t border-line px-[18px] py-3">
            <RouterLink :to="{ name: 'admin-user-new' }" :class="[BTN_SECONDARY, 'flex-1']">
              <AdminIcon name="plus" class="size-4" /> Thêm
            </RouterLink>
            <RouterLink v-if="myId" :to="{ name: 'admin-user-edit', params: { id: myId } }" :class="[BTN_SECONDARY, 'flex-1']">
              <AdminIcon name="key" class="size-4" /> Đổi mật khẩu
            </RouterLink>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

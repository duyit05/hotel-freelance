<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import AdminDetailPanel from '@/components/admin/AdminDetailPanel.vue'
import { BTN_DANGER, BTN_DANGER_SOLID, BTN_PRIMARY, BTN_SECONDARY, CARD, INPUT } from '@/components/admin/styles'
import { userApi } from '@/api/users'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'
import { initials, shortDate } from '@/utils/format'

const users = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)
const query = ref('')
const { user: me } = useAuth()
const { toast } = useToast()

const roleLabel = (role) => (role === 'ADMIN' ? 'Quản trị viên' : role)
const isMe = (u) => u.username === me.value?.username

const normalize = (s) => (s ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/gi, 'd').toLowerCase()

/* Lọc nhanh khi bấm ô số liệu */
const RECENT_DAYS = 30
const isRecent = (u) => u.createdAt && Date.now() - new Date(u.createdAt) < RECENT_DAYS * 864e5
const FILTERS = { all: () => true, admin: (u) => u.role === 'ADMIN', recent: isRecent }
const filter = ref('all')

/* Sắp xếp khi bấm tiêu đề cột */
const SORT_FIELDS = { fullName: (u) => u.fullName ?? '', role: (u) => roleLabel(u.role), createdAt: (u) => new Date(u.createdAt || 0).getTime() }
const sortKey = ref('createdAt')
const sortDir = ref('asc')
function sortBy(key) {
  if (sortKey.value === key) sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  else [sortKey.value, sortDir.value] = [key, 'asc']
}
const ariaSort = (key) => (sortKey.value !== key ? 'none' : sortDir.value === 'asc' ? 'ascending' : 'descending')
const sortMark = (key) => (sortKey.value !== key ? '' : sortDir.value === 'asc' ? '↑' : '↓')

const rows = computed(() => {
  const q = normalize(query.value.trim())
  const get = SORT_FIELDS[sortKey.value]
  return users.value
    .filter(FILTERS[filter.value])
    .filter((u) => !q || normalize(`${u.fullName} ${u.username}`).includes(q))
    .sort((a, b) => {
      const [x, y] = [get(a), get(b)]
      const d = typeof x === 'string' ? x.localeCompare(y, 'vi') : x - y
      return sortDir.value === 'asc' ? d : -d
    })
})

const stats = computed(() => [
  { key: 'all', label: 'Tài khoản', value: users.value.length, note: 'Được vào trang quản trị' },
  { key: 'admin', label: 'Quản trị viên', value: users.value.filter(FILTERS.admin).length, note: 'Quản lý phòng và tài khoản' },
  { key: 'recent', label: 'Mới tạo', value: users.value.filter(isRecent).length, note: `Trong ${RECENT_DAYS} ngày qua` },
])

function resetFilters() {
  query.value = ''
  filter.value = 'all'
}

/* ----- Panel chi tiết ----- */
const selectedId = ref(null)
const selected = computed(() => users.value.find((u) => u.id === selectedId.value) ?? null)
const confirmingDelete = ref(false)
watch(selectedId, () => (confirmingDelete.value = false))
const select = (u) => (selectedId.value = selectedId.value === u.id ? null : u.id)

async function load() {
  loading.value = true
  error.value = ''
  try {
    users.value = await userApi.list()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function remove(u) {
  deleting.value = u.id
  try {
    await userApi.remove(u.id)
    users.value = users.value.filter((x) => x.id !== u.id)
    selectedId.value = null
    toast(`Đã xóa tài khoản "${u.fullName}"`)
  } catch (e) {
    toast(`Không xóa được tài khoản: ${e.message}`, { tone: 'error' })
  } finally {
    deleting.value = null
    confirmingDelete.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="a-split">
    <div class="a-page">
      <AdminPageHeader title="Tài khoản" subtitle="Những người có quyền đăng nhập trang quản trị.">
        <template #actions>
          <RouterLink :to="{ name: 'admin-user-new' }" :class="BTN_PRIMARY">
            <AdminIcon name="plus" class="size-4" /> Thêm tài khoản
          </RouterLink>
        </template>
      </AdminPageHeader>

      <AdminAlert v-if="error" type="error">
        {{ error }}
        <button type="button" class="a-link ml-1" @click="load">Thử lại</button>
      </AdminAlert>

      <template v-else>
        <section class="a-stats mb-5" aria-label="Tóm tắt">
          <button
            v-for="s in stats"
            :key="s.key"
            type="button"
            class="a-stat"
            :aria-pressed="filter === s.key"
            @click="filter = filter === s.key ? 'all' : s.key"
          >
            <span class="a-stat-label">{{ s.label }}</span>
            <span class="a-stat-value">{{ loading ? '–' : s.value }}</span>
            <span class="a-stat-note">{{ s.note }}</span>
          </button>
        </section>

        <div class="mb-3 flex flex-wrap items-center gap-2.5">
          <div class="relative min-w-[200px] flex-1 sm:max-w-[320px]">
            <AdminIcon name="search" class="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted" />
            <input v-model="query" type="search" :class="[INPUT, 'pl-9']" placeholder="Tìm theo họ tên, tên đăng nhập" aria-label="Tìm tài khoản" />
          </div>
        </div>

        <div :class="[CARD, 'overflow-x-auto']">
          <table class="a-table min-w-[560px]">
            <thead>
              <tr>
                <th :aria-sort="ariaSort('fullName')">
                  <button type="button" class="a-sort" @click="sortBy('fullName')">Người dùng <span class="a-sort-ind">{{ sortMark('fullName') }}</span></button>
                </th>
                <th :aria-sort="ariaSort('role')">
                  <button type="button" class="a-sort" @click="sortBy('role')">Vai trò <span class="a-sort-ind">{{ sortMark('role') }}</span></button>
                </th>
                <th class="num" :aria-sort="ariaSort('createdAt')">
                  <button type="button" class="a-sort" @click="sortBy('createdAt')"><span class="a-sort-ind">{{ sortMark('createdAt') }}</span>Ngày tạo</button>
                </th>
              </tr>
            </thead>
            <tbody>
              <template v-if="loading">
                <tr v-for="i in 3" :key="i">
                  <td colspan="3"><div class="a-skel h-8" /></td>
                </tr>
              </template>
              <tr v-else-if="!users.length">
                <td colspan="3">
                  <div class="a-empty">
                    <p class="a-empty-title">Chưa có tài khoản nào</p>
                    <RouterLink :to="{ name: 'admin-user-new' }" :class="[BTN_PRIMARY, 'mt-4']">Thêm tài khoản</RouterLink>
                  </div>
                </td>
              </tr>
              <tr v-else-if="!rows.length">
                <td colspan="3">
                  <div class="a-empty">
                    <p class="a-empty-title">Không có tài khoản nào khớp bộ lọc</p>
                    <button type="button" :class="[BTN_SECONDARY, 'mt-4']" @click="resetFilters">Xóa bộ lọc</button>
                  </div>
                </td>
              </tr>
              <template v-else>
                <tr
                  v-for="u in rows"
                  :key="u.id"
                  class="is-clickable"
                  tabindex="0"
                  :aria-selected="selectedId === u.id"
                  @click="select(u)"
                  @keydown.enter.prevent="select(u)"
                  @keydown.space.prevent="select(u)"
                >
                  <td>
                    <div class="flex items-center gap-3">
                      <span
                        class="flex size-8 shrink-0 items-center justify-center rounded-full border border-line bg-surface-2 text-xs font-semibold text-ink-2"
                        aria-hidden="true"
                      >
                        {{ initials(u.fullName) }}
                      </span>
                      <span class="min-w-0 leading-tight">
                        <span class="flex items-center gap-2 font-medium">
                          {{ u.fullName }}
                          <span v-if="isMe(u)" class="a-badge a-badge--plain h-5! px-1.5! text-[11.5px]!">Bạn</span>
                        </span>
                        <span class="block text-[12.5px] text-muted">@{{ u.username }}</span>
                      </span>
                    </div>
                  </td>
                  <td><span class="a-badge tone-c1">{{ roleLabel(u.role) }}</span></td>
                  <td class="num text-ink-2">{{ shortDate(u.createdAt) }}</td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </template>
    </div>

    <AdminDetailPanel :open="!!selected" :title="selected?.fullName" @close="selectedId = null">
      <template v-if="selected">
        <div class="mb-5 flex items-center gap-3">
          <span class="flex size-11 shrink-0 items-center justify-center rounded-full border border-line bg-surface-2 text-sm font-semibold text-ink-2">
            {{ initials(selected.fullName) }}
          </span>
          <div class="min-w-0 leading-tight">
            <p class="truncate font-semibold">{{ selected.fullName }}</p>
            <p class="truncate text-[13px] text-muted">@{{ selected.username }}</p>
          </div>
        </div>
        <dl class="a-kv">
          <dt>Vai trò</dt>
          <dd>{{ roleLabel(selected.role) }}</dd>
          <dt>Tên đăng nhập</dt>
          <dd>{{ selected.username }}</dd>
          <dt>Ngày tạo</dt>
          <dd>{{ shortDate(selected.createdAt) }}</dd>
        </dl>
        <p v-if="isMe(selected)" class="mt-5 border-t border-line pt-4 text-[13px] text-ink-2">
          Đây là tài khoản bạn đang dùng nên không thể tự xóa.
        </p>
      </template>

      <template #empty>
        <p class="text-[13px] text-ink-2">Chọn một tài khoản trong bảng để xem chi tiết, sửa hoặc xóa.</p>
      </template>

      <template #footer>
        <template v-if="!confirmingDelete">
          <RouterLink :to="{ name: 'admin-user-edit', params: { id: selected.id } }" :class="[BTN_PRIMARY, 'flex-1']">
            <AdminIcon name="edit" class="size-4" /> {{ isMe(selected) ? 'Sửa / đổi mật khẩu' : 'Sửa tài khoản' }}
          </RouterLink>
          <button v-if="!isMe(selected)" type="button" :class="BTN_DANGER" @click="confirmingDelete = true">Xóa</button>
        </template>
        <template v-else>
          <p class="w-full text-[13px] text-ink-2">
            Xóa tài khoản "{{ selected.fullName }}" ({{ selected.username }})? Thao tác này không thể hoàn tác.
          </p>
          <button type="button" :class="[BTN_DANGER_SOLID, 'flex-1']" :disabled="deleting === selected.id" @click="remove(selected)">
            {{ deleting === selected.id ? 'Đang xóa...' : 'Xác nhận xóa' }}
          </button>
          <button type="button" :class="BTN_SECONDARY" @click="confirmingDelete = false">Hủy</button>
        </template>
      </template>
    </AdminDetailPanel>
  </div>
</template>

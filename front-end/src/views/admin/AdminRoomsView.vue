<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import AdminDetailPanel from '@/components/admin/AdminDetailPanel.vue'
import AppPagination from '@/components/ui/AppPagination.vue'
import { BTN_DANGER, BTN_DANGER_SOLID, BTN_PRIMARY, BTN_SECONDARY, CARD, INPUT, toneAt } from '@/components/admin/styles'
import { roomApi } from '@/api/rooms'
import { roomTypeApi } from '@/api/roomTypes'
import { useToast } from '@/composables/useToast'
import { useRooms } from '@/composables/useRooms'
import { prefersReducedMotion } from '@/composables/useReducedMotion'
import { money } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const rooms = ref([])
const types = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)
const query = ref('')
const { toast } = useToast()
const { invalidateRooms } = useRooms()

/** Lọc theo loại phòng, giữ trên URL (?loai=3) để trang Loại phòng có thể link sang */
const typeFilter = computed({
  get: () => (route.query.loai ? Number(route.query.loai) : null),
  set: (id) => router.replace({ query: { ...route.query, loai: id ?? undefined } }),
})

const MIN_IMAGES = 3

const normalize = (s) => (s ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/gi, 'd').toLowerCase()

/* ----- Mức độ đầy đủ nội dung ----- */
const missingImages = (r) => r.images.length < MIN_IMAGES
const missingText = (r) => !r.shortDescription || !r.description
const isComplete = (r) => !missingImages(r) && !missingText(r)
function issuesOf(r) {
  return [
    missingImages(r) && `${r.images.length ? 'Chỉ có ' + r.images.length : 'Chưa có'} ảnh (nên có ít nhất ${MIN_IMAGES})`,
    !r.shortDescription && 'Thiếu mô tả ngắn',
    !r.description && 'Thiếu giới thiệu chi tiết',
  ].filter(Boolean)
}

/** Lọc theo nội dung khi bấm vào ô số liệu */
const CONTENT_FILTERS = {
  all: () => true,
  complete: isComplete,
  images: missingImages,
  text: missingText,
}
const contentFilter = ref('all')

/* ----- Sắp xếp: chọn trong ô "Sắp xếp" hoặc bấm tiêu đề cột trong bảng ----- */
const SORT_FIELDS = {
  id: (r) => r.id,
  name: (r) => r.name,
  type: (r) => r.type?.name ?? '',
  price: (r) => r.price ?? 0,
  maxGuests: (r) => r.maxGuests ?? 0,
  images: (r) => r.images.length,
}
const SORT_PRESETS = [
  { value: 'id:asc', label: 'Mới thêm sau cùng' },
  { value: 'price:asc', label: 'Giá thấp đến cao' },
  { value: 'price:desc', label: 'Giá cao đến thấp' },
  { value: 'name:asc', label: 'Tên A-Z' },
]
const sortKey = ref('id')
const sortDir = ref('asc')
const sortPreset = computed({
  get: () => `${sortKey.value}:${sortDir.value}`,
  set: (v) => ([sortKey.value, sortDir.value] = v.split(':')),
})
const isPreset = computed(() => SORT_PRESETS.some((p) => p.value === sortPreset.value))

function sortBy(key) {
  if (sortKey.value === key) sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  else [sortKey.value, sortDir.value] = [key, 'asc']
}
const ariaSort = (key) => (sortKey.value !== key ? 'none' : sortDir.value === 'asc' ? 'ascending' : 'descending')
const sortMark = (key) => (sortKey.value !== key ? '' : sortDir.value === 'asc' ? '↑' : '↓')

function compare(a, b) {
  const get = SORT_FIELDS[sortKey.value]
  const [x, y] = [get(a), get(b)]
  const d = typeof x === 'string' ? x.localeCompare(y, 'vi') : x - y
  return sortDir.value === 'asc' ? d : -d
}

const countByType = computed(() => {
  const map = {}
  for (const r of rooms.value) if (r.type) map[r.type.id] = (map[r.type.id] ?? 0) + 1
  return map
})

/** Màu phân loại theo thứ tự loại phòng */
const typeTone = (id) => toneAt(types.value.findIndex((t) => t.id === id))

/** Tìm theo tên / đường dẫn / tầm nhìn / loại phòng, gõ không dấu vẫn ra */
const filtered = computed(() => {
  const q = normalize(query.value.trim())
  return rooms.value
    .filter((r) => !typeFilter.value || r.type?.id === typeFilter.value)
    .filter(CONTENT_FILTERS[contentFilter.value])
    .filter((r) => !q || normalize(`${r.name} ${r.slug} ${r.view} ${r.type?.name}`).includes(q))
    .sort(compare)
})

const hasFilters = computed(() => !!(query.value.trim() || typeFilter.value || contentFilter.value !== 'all'))

const stats = computed(() => [
  { key: 'all', label: 'Tất cả phòng', value: rooms.value.length, note: `${types.value.length} loại phòng` },
  { key: 'complete', label: 'Đủ nội dung', value: rooms.value.filter(isComplete).length, tone: 'var(--ok)', note: 'Ảnh và mô tả đầy đủ' },
  { key: 'images', label: 'Thiếu ảnh', value: rooms.value.filter(missingImages).length, tone: 'var(--warn)', note: `Dưới ${MIN_IMAGES} ảnh` },
  { key: 'text', label: 'Thiếu mô tả', value: rooms.value.filter(missingText).length, tone: 'var(--warn)', note: 'Mô tả ngắn hoặc chi tiết' },
])
const priceRange = computed(() => {
  const prices = rooms.value.map((r) => r.price).filter((p) => p != null)
  return prices.length
    ? { from: money(Math.min(...prices)), to: `đến ${money(Math.max(...prices))}` }
    : { from: '—', to: 'Chưa có giá' }
})

/* ----- Kiểu hiển thị (ghi nhớ trên trình duyệt) ----- */
const view = ref(localStorage.getItem('admin-rooms-view') === 'table' ? 'table' : 'grid')
watch(view, (v) => localStorage.setItem('admin-rooms-view', v))

/* ----- Panel chi tiết ----- */
const selectedId = ref(null)
const selected = computed(() => rooms.value.find((r) => r.id === selectedId.value) ?? null)
const confirmingDelete = ref(false)
watch(selectedId, () => (confirmingDelete.value = false))
const select = (room) => (selectedId.value = selectedId.value === room.id ? null : room.id)

async function load() {
  loading.value = true
  error.value = ''
  try {
    ;[rooms.value, types.value] = await Promise.all([roomApi.list(), roomTypeApi.list()])
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function remove(room) {
  deleting.value = room.id
  try {
    await roomApi.remove(room.id)
    rooms.value = rooms.value.filter((r) => r.id !== room.id)
    invalidateRooms()
    selectedId.value = null
    toast(`Đã xóa phòng "${room.name}"`)
  } catch (e) {
    toast(`Không xóa được phòng: ${e.message}`, { tone: 'error' })
  } finally {
    deleting.value = null
    confirmingDelete.value = false
  }
}

/* ----- Phân trang ----- */
const PAGE_SIZE = 12
const page = ref(1)
const paged = computed(() => filtered.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))
// Đổi từ khóa / bộ lọc / cách sắp xếp -> về trang 1
watch([query, typeFilter, contentFilter, sortKey, sortDir], () => (page.value = 1))
// Xóa phòng làm trang hiện tại trống -> lùi về trang cuối còn phòng
watch(
  () => Math.max(1, Math.ceil(filtered.value.length / PAGE_SIZE)),
  (n) => page.value > n && (page.value = n),
)

function changePage(p) {
  page.value = p
  window.scrollTo({ top: 0, behavior: prefersReducedMotion() ? 'auto' : 'smooth' })
}

function resetFilters() {
  query.value = ''
  typeFilter.value = null
  contentFilter.value = 'all'
}

// Loại phòng trên URL không còn tồn tại (vd. đã bị xóa) -> bỏ lọc
watch(types, (list) => {
  if (typeFilter.value && !list.some((t) => t.id === typeFilter.value)) typeFilter.value = null
})

onMounted(load)
</script>

<template>
  <div class="a-split">
    <div class="a-page">
      <AdminPageHeader title="Phòng" subtitle="Các phòng đang hiển thị trên trang web, giá và mức độ đầy đủ nội dung.">
        <template #actions>
          <RouterLink :to="{ name: 'admin-room-types' }" :class="BTN_SECONDARY">Loại phòng</RouterLink>
          <RouterLink :to="{ name: 'admin-room-new' }" :class="BTN_PRIMARY">
            <AdminIcon name="plus" class="size-4" /> Thêm phòng
          </RouterLink>
        </template>
      </AdminPageHeader>

      <AdminAlert v-if="error" type="error">
        {{ error }}
        <button type="button" class="a-link ml-1" @click="load">Thử lại</button>
      </AdminAlert>

      <template v-else>
        <!-- Số liệu tóm tắt: bấm để lọc -->
        <section class="a-stats mb-5" aria-label="Tóm tắt">
          <button
            v-for="s in stats"
            :key="s.key"
            type="button"
            class="a-stat"
            :aria-pressed="contentFilter === s.key"
            @click="contentFilter = contentFilter === s.key && s.key !== 'all' ? 'all' : s.key"
          >
            <span class="a-stat-label">
              <span v-if="s.tone" class="a-dot" :style="{ background: s.tone }" aria-hidden="true" />
              {{ s.label }}
            </span>
            <span class="a-stat-value">{{ loading ? '–' : s.value }}</span>
            <span class="a-stat-note">{{ s.note }}</span>
          </button>
          <div class="a-stat">
            <span class="a-stat-label">Giá / đêm từ</span>
            <span class="a-stat-value a-stat-value--sm">{{ loading ? '–' : priceRange.from }}</span>
            <span class="a-stat-note">{{ loading ? ' ' : priceRange.to }}</span>
          </div>
        </section>

        <!-- Thanh công cụ -->
        <div class="mb-3 flex flex-wrap items-center gap-2.5">
          <div class="relative min-w-[200px] flex-1 sm:max-w-[320px]">
            <AdminIcon name="search" class="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted" />
            <input
              v-model="query"
              type="search"
              :class="[INPUT, 'pl-9']"
              placeholder="Tìm theo tên, loại, tầm nhìn"
              aria-label="Tìm phòng"
            />
          </div>
          <label class="ml-auto flex items-center gap-2 text-[13px] text-ink-2">
            Sắp xếp
            <select v-model="sortPreset" :class="[INPUT, 'w-auto']">
              <option v-if="!isPreset" :value="sortPreset" disabled>Theo cột trong bảng</option>
              <option v-for="p in SORT_PRESETS" :key="p.value" :value="p.value">{{ p.label }}</option>
            </select>
          </label>
          <div class="a-seg" role="group" aria-label="Kiểu hiển thị">
            <button type="button" :aria-pressed="view === 'grid'" @click="view = 'grid'">
              <AdminIcon name="grid" class="size-4" /> Lưới
            </button>
            <button type="button" :aria-pressed="view === 'table'" @click="view = 'table'">
              <AdminIcon name="list" class="size-4" /> Bảng
            </button>
          </div>
        </div>

        <div v-if="types.length" class="-mx-1 mb-5 flex gap-2 overflow-x-auto px-1 pb-1" role="group" aria-label="Lọc theo loại phòng">
          <button type="button" class="a-chip" :aria-pressed="!typeFilter" @click="typeFilter = null">
            Tất cả loại <span class="a-chip-n">{{ rooms.length }}</span>
          </button>
          <button
            v-for="(t, i) in types"
            :key="t.id"
            type="button"
            class="a-chip"
            :aria-pressed="typeFilter === t.id"
            @click="typeFilter = typeFilter === t.id ? null : t.id"
          >
            <span :class="['a-dot', toneAt(i)]" :style="{ background: 'var(--tone)' }" aria-hidden="true" />
            {{ t.name }}
            <span class="a-chip-n">{{ countByType[t.id] ?? 0 }}</span>
          </button>
        </div>

        <!-- Đang tải -->
        <div v-if="loading" :class="[CARD, 'space-y-3 p-4']" aria-busy="true" aria-label="Đang tải">
          <div v-for="i in 5" :key="i" class="a-skel h-9" />
        </div>

        <!-- Không có kết quả -->
        <div v-else-if="!filtered.length" :class="[CARD, 'a-empty']">
          <template v-if="rooms.length">
            <p class="a-empty-title">Không có phòng nào khớp bộ lọc</p>
            <p>Thử từ khóa khác hoặc bỏ bớt điều kiện lọc.</p>
            <button type="button" :class="[BTN_SECONDARY, 'mt-4']" @click="resetFilters">Xóa bộ lọc</button>
          </template>
          <template v-else>
            <p class="a-empty-title">Chưa có phòng nào</p>
            <p>Thêm phòng đầu tiên để hiển thị trên trang web.</p>
            <RouterLink :to="{ name: 'admin-room-new' }" :class="[BTN_PRIMARY, 'mt-4']">
              <AdminIcon name="plus" class="size-4" /> Thêm phòng
            </RouterLink>
          </template>
        </div>

        <template v-else>
          <!-- Dạng lưới -->
          <ul v-if="view === 'grid'" class="grid gap-3 [grid-template-columns:repeat(auto-fill,minmax(232px,1fr))]">
            <li v-for="room in paged" :key="room.id">
              <button
                type="button"
                :class="[
                  'room-card flex h-full w-full flex-col overflow-hidden rounded-xl border bg-surface text-left',
                  selectedId === room.id ? 'is-selected' : 'border-line',
                ]"
                :aria-pressed="selectedId === room.id"
                @click="select(room)"
              >
                <img v-if="room.images[0]" :src="room.images[0]" alt="" class="aspect-16/10 w-full bg-surface-2 object-cover" />
                <span v-else class="flex aspect-16/10 w-full items-center justify-center bg-surface-2 text-[13px] text-muted">
                  Chưa có ảnh
                </span>
                <span class="flex flex-1 flex-col gap-2 border-t border-line p-3.5">
                  <span class="min-w-0">
                    <span class="block truncate font-semibold">{{ room.name }}</span>
                    <span class="block truncate text-[12.5px] text-muted">{{ room.view || 'Chưa nhập tầm nhìn' }}</span>
                  </span>
                  <span class="flex flex-wrap gap-1.5">
                    <span v-if="room.type" :class="['a-badge', typeTone(room.type.id)]">{{ room.type.name }}</span>
                    <span v-else class="a-badge a-badge--plain">Chưa phân loại</span>
                    <span v-if="!isComplete(room)" class="a-badge tone-warn">Thiếu nội dung</span>
                  </span>
                  <span class="mt-auto flex items-baseline justify-between gap-2 pt-1 text-[13px] whitespace-nowrap">
                    <span><span class="font-semibold text-ink">{{ money(room.price) }}</span><span class="text-muted"> / đêm</span></span>
                    <span class="text-ink-2" :title="`Tối đa ${room.maxGuests} khách`">{{ room.maxGuests }} khách</span>
                  </span>
                </span>
              </button>
            </li>
          </ul>

          <!-- Dạng bảng -->
          <div v-else :class="[CARD, 'overflow-x-auto']">
            <table class="a-table min-w-[760px]">
              <thead>
                <tr>
                  <th :aria-sort="ariaSort('name')">
                    <button type="button" class="a-sort" @click="sortBy('name')">Phòng <span class="a-sort-ind">{{ sortMark('name') }}</span></button>
                  </th>
                  <th :aria-sort="ariaSort('type')">
                    <button type="button" class="a-sort" @click="sortBy('type')">Loại <span class="a-sort-ind">{{ sortMark('type') }}</span></button>
                  </th>
                  <th class="num" :aria-sort="ariaSort('price')">
                    <button type="button" class="a-sort" @click="sortBy('price')"><span class="a-sort-ind">{{ sortMark('price') }}</span>Giá / đêm</button>
                  </th>
                  <th class="num" :aria-sort="ariaSort('maxGuests')">
                    <button type="button" class="a-sort" @click="sortBy('maxGuests')"><span class="a-sort-ind">{{ sortMark('maxGuests') }}</span>Khách</button>
                  </th>
                  <th class="num" :aria-sort="ariaSort('images')">
                    <button type="button" class="a-sort" @click="sortBy('images')"><span class="a-sort-ind">{{ sortMark('images') }}</span>Ảnh</button>
                  </th>
                  <th>Nội dung</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="room in paged"
                  :key="room.id"
                  class="is-clickable"
                  tabindex="0"
                  :aria-selected="selectedId === room.id"
                  @click="select(room)"
                  @keydown.enter.prevent="select(room)"
                  @keydown.space.prevent="select(room)"
                >
                  <td>
                    <div class="flex items-center gap-3">
                      <img v-if="room.images[0]" :src="room.images[0]" alt="" class="h-8 w-11 shrink-0 rounded-[5px] bg-surface-2 object-cover" />
                      <span v-else class="h-8 w-11 shrink-0 rounded-[5px] bg-surface-2" />
                      <span class="min-w-0 leading-tight">
                        <span class="block truncate font-medium">{{ room.name }}</span>
                        <span class="block truncate text-[12.5px] text-muted">/phong/{{ room.slug }}</span>
                      </span>
                    </div>
                  </td>
                  <td>
                    <span v-if="room.type" :class="['a-badge', typeTone(room.type.id)]">{{ room.type.name }}</span>
                    <span v-else class="text-muted">—</span>
                  </td>
                  <td class="num font-medium">{{ money(room.price) }}</td>
                  <td class="num">{{ room.maxGuests }}</td>
                  <td :class="['num', missingImages(room) && 'font-semibold text-warn']">{{ room.images.length }}</td>
                  <td>
                    <span v-if="isComplete(room)" class="a-badge tone-ok">Đủ</span>
                    <span v-else class="a-badge tone-warn">Thiếu {{ issuesOf(room).length }} mục</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="mt-5 flex flex-wrap items-center justify-between gap-3">
            <p class="text-[13px] text-muted">
              Hiển thị {{ (page - 1) * PAGE_SIZE + 1 }}–{{ Math.min(page * PAGE_SIZE, filtered.length) }} trên
              {{ filtered.length }} phòng
              <button v-if="hasFilters" type="button" class="a-link ml-2" @click="resetFilters">Xóa bộ lọc</button>
            </p>
            <AppPagination
              :model-value="page"
              :total="filtered.length"
              :page-size="PAGE_SIZE"
              @update:model-value="changePage"
            />
          </div>
        </template>
      </template>
    </div>

    <!-- Panel chi tiết phòng -->
    <AdminDetailPanel :open="!!selected" :title="selected?.name" @close="selectedId = null">
      <template v-if="selected">
        <img
          v-if="selected.images[0]"
          :src="selected.images[0]"
          alt=""
          class="mb-4 aspect-16/10 w-full rounded-lg border border-line bg-surface-2 object-cover"
        />
        <div class="mb-5 flex flex-wrap gap-1.5">
          <span v-if="selected.type" :class="['a-badge', typeTone(selected.type.id)]">{{ selected.type.name }}</span>
          <span v-else class="a-badge a-badge--plain">Chưa phân loại</span>
          <span v-if="isComplete(selected)" class="a-badge tone-ok">Đủ nội dung</span>
          <span v-else class="a-badge tone-warn">Thiếu nội dung</span>
        </div>

        <dl class="a-kv">
          <dt>Giá / đêm</dt>
          <dd>{{ money(selected.price) }}</dd>
          <dt>Khách tối đa</dt>
          <dd>{{ selected.maxGuests }}</dd>
          <dt>Diện tích</dt>
          <dd>{{ selected.size || '—' }}</dd>
          <dt>Sức chứa</dt>
          <dd>{{ selected.guests || '—' }}</dd>
          <dt>Giường</dt>
          <dd>{{ selected.bed || '—' }}</dd>
          <dt>Tầm nhìn</dt>
          <dd>{{ selected.view || '—' }}</dd>
          <dt>Số ảnh</dt>
          <dd :class="missingImages(selected) && 'text-warn'">{{ selected.images.length }}</dd>
          <dt>Tiện nghi</dt>
          <dd>{{ selected.features.length }}</dd>
          <dt>Đường dẫn</dt>
          <dd>/phong/{{ selected.slug }}</dd>
        </dl>

        <div class="mt-6 border-t border-line pt-4">
          <h3 class="mb-2 text-[13px] font-semibold">Nội dung cần bổ sung</h3>
          <p v-if="isComplete(selected)" class="text-[13px] text-ink-2">Không có. Phòng đã đủ ảnh và mô tả.</p>
          <ul v-else class="space-y-1.5 text-[13px]">
            <li v-for="issue in issuesOf(selected)" :key="issue" class="flex items-center gap-2">
              <span class="a-dot" style="background: var(--warn)" aria-hidden="true" /> {{ issue }}
            </li>
          </ul>
        </div>
      </template>

      <template #empty>
        <p class="text-[13px] text-ink-2">Chọn một phòng trong danh sách để xem chi tiết, sửa hoặc xóa.</p>
      </template>

      <template #footer>
        <template v-if="!confirmingDelete">
          <RouterLink :to="{ name: 'admin-room-edit', params: { id: selected.id } }" :class="[BTN_PRIMARY, 'flex-1']">
            <AdminIcon name="edit" class="size-4" /> Sửa phòng
          </RouterLink>
          <a :href="`/phong/${selected.slug}`" target="_blank" rel="noopener" :class="BTN_SECONDARY">Xem trên web</a>
          <button type="button" :class="BTN_DANGER" @click="confirmingDelete = true">Xóa</button>
        </template>
        <template v-else>
          <p class="w-full text-[13px] text-ink-2">Xóa "{{ selected.name }}"? Thao tác này không thể hoàn tác.</p>
          <button
            type="button"
            :class="[BTN_DANGER_SOLID, 'flex-1']"
            :disabled="deleting === selected.id"
            @click="remove(selected)"
          >
            {{ deleting === selected.id ? 'Đang xóa...' : 'Xác nhận xóa' }}
          </button>
          <button type="button" :class="BTN_SECONDARY" @click="confirmingDelete = false">Hủy</button>
        </template>
      </template>
    </AdminDetailPanel>
  </div>
</template>

<style scoped>
.room-card:hover { border-color: color-mix(in srgb, var(--ink) 24%, var(--line)); }
.room-card.is-selected { border-color: var(--brand); box-shadow: inset 0 0 0 1px var(--brand); }
</style>

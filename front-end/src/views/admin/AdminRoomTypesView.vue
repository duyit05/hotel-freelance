<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import AdminField from '@/components/admin/AdminField.vue'
import AdminDetailPanel from '@/components/admin/AdminDetailPanel.vue'
import { BTN_DANGER, BTN_DANGER_SOLID, BTN_PRIMARY, BTN_SECONDARY, CARD, INPUT, toneAt } from '@/components/admin/styles'
import { roomTypeApi } from '@/api/roomTypes'
import { useRooms } from '@/composables/useRooms'
import { useToast } from '@/composables/useToast'

const types = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)
const { invalidateRooms } = useRooms()
const { toast } = useToast()

const totalRooms = computed(() => types.value.reduce((sum, t) => sum + t.roomCount, 0))
const emptyTypes = computed(() => types.value.filter((t) => !t.roomCount).length)

/* Lọc nhanh khi bấm ô số liệu: tất cả / đang dùng / chưa có phòng */
const usageFilter = ref('all')
const FILTERS = { all: () => true, used: (t) => t.roomCount > 0, empty: (t) => !t.roomCount }

/* Sắp xếp khi bấm tiêu đề cột (mặc định theo thứ tự tạo, cũng là thứ tự nút lọc ở trang chủ) */
const SORT_FIELDS = { sortOrder: (t) => t.sortOrder, name: (t) => t.name, roomCount: (t) => t.roomCount }
const sortKey = ref('sortOrder')
const sortDir = ref('asc')
function sortBy(key) {
  if (sortKey.value === key) sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  else [sortKey.value, sortDir.value] = [key, 'asc']
}
const ariaSort = (key) => (sortKey.value !== key ? 'none' : sortDir.value === 'asc' ? 'ascending' : 'descending')
const sortMark = (key) => (sortKey.value !== key ? '' : sortDir.value === 'asc' ? '↑' : '↓')

const rows = computed(() => {
  const get = SORT_FIELDS[sortKey.value]
  return types.value
    .map((t, i) => ({ ...t, tone: toneAt(i) }))
    .filter(FILTERS[usageFilter.value])
    .sort((a, b) => {
      const [x, y] = [get(a), get(b)]
      const d = typeof x === 'string' ? x.localeCompare(y, 'vi') : x - y
      return sortDir.value === 'asc' ? d : -d
    })
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    types.value = await roomTypeApi.list()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

/* ----- Panel thêm / sửa ----- */
const panelOpen = ref(false)
const editing = ref(null) // loại đang sửa, null = thêm mới
const form = reactive({ name: '', description: '', sortOrder: 0 })
const saving = ref(false)
const formError = ref('')
const errors = ref({})
const nameInput = ref(null)
const confirmingDelete = ref(false)

function openPanel(type = null) {
  editing.value = type
  // Loại mới xếp cuối; khi sửa giữ nguyên thứ tự cũ
  const nextOrder = Math.max(0, ...types.value.map((t) => t.sortOrder ?? 0)) + 10
  Object.assign(form, {
    name: type?.name ?? '',
    description: type?.description ?? '',
    sortOrder: type?.sortOrder ?? nextOrder,
  })
  formError.value = ''
  errors.value = {}
  confirmingDelete.value = false
  panelOpen.value = true
  nextTick(() => nameInput.value?.focus())
}

const closePanel = () => !saving.value && (panelOpen.value = false)

async function save() {
  saving.value = true
  formError.value = ''
  errors.value = {}
  try {
    const data = { ...form, sortOrder: Number(form.sortOrder) || 0 }
    const saved = editing.value
      ? await roomTypeApi.update(editing.value.id, data)
      : await roomTypeApi.create(data)
    await load()
    invalidateRooms()
    toast(editing.value ? `Đã lưu thay đổi cho "${saved.name}"` : `Đã thêm loại phòng "${saved.name}"`)
    panelOpen.value = false
  } catch (e) {
    formError.value = e.message
    errors.value = e.errors ?? {}
  } finally {
    saving.value = false
  }
}

async function remove(type) {
  deleting.value = type.id
  try {
    await roomTypeApi.remove(type.id)
    types.value = types.value.filter((t) => t.id !== type.id)
    invalidateRooms()
    panelOpen.value = false
    toast(`Đã xóa loại phòng "${type.name}"`)
  } catch (e) {
    toast(`Không xóa được loại phòng: ${e.message}`, { tone: 'error' })
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
      <AdminPageHeader title="Loại phòng" subtitle="Nhóm phòng hiển thị ở bộ lọc trang chủ và trên thẻ phòng.">
        <template #actions>
          <button type="button" :class="BTN_PRIMARY" @click="openPanel()">
            <AdminIcon name="plus" class="size-4" /> Thêm loại phòng
          </button>
        </template>
      </AdminPageHeader>

      <AdminAlert v-if="error" type="error">
        {{ error }}
        <button type="button" class="a-link ml-1" @click="load">Thử lại</button>
      </AdminAlert>

      <template v-else>
        <!-- Số liệu tóm tắt -->
        <section class="a-stats mb-5" aria-label="Tóm tắt">
          <button type="button" class="a-stat" :aria-pressed="usageFilter === 'all'" @click="usageFilter = 'all'">
            <span class="a-stat-label">Loại phòng</span>
            <span class="a-stat-value">{{ loading ? '–' : types.length }}</span>
            <span class="a-stat-note">Tất cả</span>
          </button>
          <button type="button" class="a-stat" :aria-pressed="usageFilter === 'used'" @click="usageFilter = usageFilter === 'used' ? 'all' : 'used'">
            <span class="a-stat-label">Phòng đã phân loại</span>
            <span class="a-stat-value">{{ loading ? '–' : totalRooms }}</span>
            <span class="a-stat-note">Xem các loại đang có phòng</span>
          </button>
          <button type="button" class="a-stat" :aria-pressed="usageFilter === 'empty'" @click="usageFilter = usageFilter === 'empty' ? 'all' : 'empty'">
            <span class="a-stat-label">
              <span class="a-dot" style="background: var(--warn)" aria-hidden="true" /> Chưa có phòng
            </span>
            <span class="a-stat-value">{{ loading ? '–' : emptyTypes }}</span>
            <span class="a-stat-note">Không hiện trên trang chủ</span>
          </button>
        </section>

        <!-- Đang tải -->
        <div v-if="loading" :class="[CARD, 'space-y-3 p-4']" aria-busy="true" aria-label="Đang tải">
          <div v-for="i in 4" :key="i" class="a-skel h-9" />
        </div>

        <!-- Chưa có loại nào -->
        <div v-else-if="!types.length" :class="[CARD, 'a-empty']">
          <p class="a-empty-title">Chưa có loại phòng nào</p>
          <p>Tạo loại phòng như "Phòng tiêu chuẩn", "Phòng gia đình" để phân nhóm các phòng.</p>
          <button type="button" :class="[BTN_PRIMARY, 'mt-4']" @click="openPanel()">
            <AdminIcon name="plus" class="size-4" /> Thêm loại phòng đầu tiên
          </button>
        </div>

        <!-- Bộ lọc không có kết quả -->
        <div v-else-if="!rows.length" :class="[CARD, 'a-empty']">
          <p class="a-empty-title">Không có loại phòng nào khớp bộ lọc</p>
          <button type="button" :class="[BTN_SECONDARY, 'mt-4']" @click="usageFilter = 'all'">Xóa bộ lọc</button>
        </div>

        <!-- Bảng -->
        <div v-else :class="[CARD, 'overflow-x-auto']">
          <table class="a-table min-w-[620px]">
            <thead>
              <tr>
                <th :aria-sort="ariaSort('name')">
                  <button type="button" class="a-sort" @click="sortBy('name')">Tên loại <span class="a-sort-ind">{{ sortMark('name') }}</span></button>
                </th>
                <th>Mô tả</th>
                <th class="num" :aria-sort="ariaSort('roomCount')">
                  <button type="button" class="a-sort" @click="sortBy('roomCount')"><span class="a-sort-ind">{{ sortMark('roomCount') }}</span>Số phòng</button>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="type in rows"
                :key="type.id"
                class="is-clickable"
                tabindex="0"
                :aria-selected="panelOpen && editing?.id === type.id"
                @click="openPanel(type)"
                @keydown.enter.prevent="openPanel(type)"
                @keydown.space.prevent="openPanel(type)"
              >
                <td><span :class="['a-badge', type.tone]">{{ type.name }}</span></td>
                <td class="max-w-[320px] truncate text-ink-2">{{ type.description || '—' }}</td>
                <td class="num">
                  <RouterLink
                    v-if="type.roomCount"
                    :to="{ name: 'admin-rooms', query: { loai: type.id } }"
                    class="a-link"
                    :title="`Xem các phòng thuộc ${type.name}`"
                    @click.stop
                  >
                    {{ type.roomCount }}
                  </RouterLink>
                  <span v-else class="text-warn">0</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>

    <!-- Panel thêm / sửa -->
    <AdminDetailPanel
      :open="panelOpen"
      :title="editing ? 'Sửa loại phòng' : 'Thêm loại phòng'"
      @close="closePanel"
    >
      <form id="room-type-form" novalidate @submit.prevent="save">
        <AdminAlert v-if="formError" type="error" class="mb-4">{{ formError }}</AdminAlert>

        <div class="space-y-4">
          <AdminField id="type-name" label="Tên loại phòng" required :error="errors.name">
            <input
              id="type-name"
              ref="nameInput"
              v-model="form.name"
              :class="INPUT"
              :aria-invalid="!!errors.name"
              maxlength="100"
              placeholder="vd. Phòng gia đình"
            />
          </AdminField>
          <AdminField id="type-desc" label="Mô tả ngắn" :error="errors.description" hint="Hiện khi rê chuột lên nút lọc.">
            <textarea
              id="type-desc"
              v-model="form.description"
              rows="3"
              :class="INPUT"
              maxlength="500"
              placeholder="vd. Nhiều giường, đủ chỗ cho gia đình có trẻ nhỏ."
            />
          </AdminField>
        </div>

        <dl v-if="editing" class="a-kv mt-6 border-t border-line pt-4">
          <dt>Số phòng thuộc loại này</dt>
          <dd>{{ editing.roomCount }}</dd>
        </dl>
        <p v-if="editing?.roomCount" class="mt-2 text-[12.5px] text-muted">
          Còn phòng thuộc loại này nên chưa thể xóa. Hãy chuyển các phòng sang loại khác trước.
        </p>
      </form>

      <template #footer>
        <template v-if="!confirmingDelete">
          <button type="submit" form="room-type-form" :class="[BTN_PRIMARY, 'flex-1']" :disabled="saving">
            {{ saving ? 'Đang lưu...' : editing ? 'Lưu thay đổi' : 'Thêm loại phòng' }}
          </button>
          <button type="button" :class="BTN_SECONDARY" @click="closePanel">Hủy</button>
          <button
            v-if="editing"
            type="button"
            :class="BTN_DANGER"
            :disabled="editing.roomCount > 0"
            :title="editing.roomCount ? 'Còn phòng thuộc loại này' : 'Xóa loại phòng'"
            @click="confirmingDelete = true"
          >
            Xóa
          </button>
        </template>
        <template v-else>
          <p class="w-full text-[13px] text-ink-2">Xóa loại phòng "{{ editing.name }}"?</p>
          <button type="button" :class="[BTN_DANGER_SOLID, 'flex-1']" :disabled="deleting === editing.id" @click="remove(editing)">
            {{ deleting === editing.id ? 'Đang xóa...' : 'Xác nhận xóa' }}
          </button>
          <button type="button" :class="BTN_SECONDARY" @click="confirmingDelete = false">Hủy</button>
        </template>
      </template>

      <template #empty>
        <p class="text-[13px] text-ink-2">Chọn một loại phòng trong bảng để sửa, hoặc bấm "Thêm loại phòng".</p>
      </template>
    </AdminDetailPanel>
  </div>
</template>

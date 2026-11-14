<script setup>
// Dùng chung cho thêm phòng (không có id) và sửa phòng (có id)
import { computed, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import AdminField from '@/components/admin/AdminField.vue'
import ListEditor from '@/components/admin/ListEditor.vue'
import ImageListEditor from '@/components/admin/ImageListEditor.vue'
import { BTN_PRIMARY, BTN_SECONDARY, CARD, INPUT, toneAt } from '@/components/admin/styles'
import { roomApi } from '@/api/rooms'
import { roomTypeApi } from '@/api/roomTypes'
import { useToast } from '@/composables/useToast'
import { useRooms } from '@/composables/useRooms'
import { prefersReducedMotion } from '@/composables/useReducedMotion'
import { money } from '@/utils/format'

const props = defineProps({
  id: { type: String, default: null },
})

const router = useRouter()
const { toast } = useToast()
const { invalidateRooms } = useRooms()
const isEdit = computed(() => !!props.id)

const form = reactive({
  slug: '',
  name: '',
  typeId: null,
  price: null,
  size: '',
  guests: '',
  bed: '',
  view: '',
  shortDescription: '',
  description: '',
  maxGuests: 2,
  features: [],
  highlights: [],
  reviews: [],
  images: [],
})

const types = ref([])
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const errors = ref({})

/* Theo dõi thay đổi chưa lưu để hỏi lại khi rời trang */
const snapshot = ref('')
const takeSnapshot = () => (snapshot.value = JSON.stringify(form))
const dirty = computed(() => !loading.value && JSON.stringify(form) !== snapshot.value)

Promise.all([
  roomTypeApi.list().then((data) => (types.value = data)),
  isEdit.value &&
    roomApi.getById(props.id).then((room) => {
      const { id: _id, type, ...rest } = room
      Object.assign(form, rest, { typeId: type?.id ?? null })
    }),
])
  .catch((e) => (error.value = e.message))
  .finally(() => {
    loading.value = false
    takeSnapshot()
  })

const selectedType = computed(() => types.value.find((t) => t.id === form.typeId))
/** Màu phân loại của loại đang chọn (cùng màu với trang danh sách phòng) */
const typeTone = computed(() => toneAt(types.value.findIndex((t) => t.id === form.typeId)))

/** Bỏ các dòng trống trước khi gửi */
function payload() {
  const filled = (obj) => Object.values(obj).some((v) => v?.trim())
  return {
    ...form,
    slug: undefined, // đường dẫn do hệ thống tự tạo khi thêm mới, giữ nguyên khi sửa
    name: form.name.trim(),
    features: form.features.map((f) => f.trim()).filter(Boolean),
    highlights: form.highlights.filter(filled),
    reviews: form.reviews.filter(filled),
  }
}

async function save() {
  saving.value = true
  error.value = ''
  errors.value = {}
  try {
    const data = payload()
    const room = isEdit.value ? await roomApi.update(props.id, data) : await roomApi.create(data)
    invalidateRooms()
    takeSnapshot()
    toast(isEdit.value ? `Đã lưu thay đổi cho "${room.name}"` : `Đã thêm phòng "${room.name}"`)
    router.push({ name: 'admin-rooms' })
  } catch (e) {
    error.value = e.message
    errors.value = e.errors ?? {}
    window.scrollTo({ top: 0, behavior: prefersReducedMotion() ? 'auto' : 'smooth' })
  } finally {
    saving.value = false
  }
}

onBeforeRouteLeave(() => !dirty.value || confirm('Bạn có thay đổi chưa lưu. Rời trang mà không lưu?'))

/** Lỗi của danh sách, vd. "highlights[0].title" -> hiện dưới mục Điểm nổi bật */
const listError = (key) =>
  Object.entries(errors.value).find(([k]) => k.startsWith(key + '['))?.[1]
    ? 'Có mục chưa hợp lệ, vui lòng kiểm tra lại (tiêu đề/nội dung không được để trống).'
    : ''

/* ----- Mức độ hoàn thiện (gợi ý, không bắt buộc) ----- */
const checklist = computed(() => [
  { label: 'Tên, loại phòng và giá', done: !!(form.name.trim() && form.typeId && form.price), href: '#co-ban' },
  { label: 'Ít nhất 3 ảnh', done: form.images.length >= 3, href: '#hinh-anh' },
  { label: 'Mô tả ngắn và giới thiệu', done: !!(form.shortDescription?.trim() && form.description?.trim()), href: '#mo-ta' },
  { label: 'Thông số (diện tích, giường...)', done: !!(form.size && form.bed && form.view), href: '#thong-so' },
  { label: '3 điểm nổi bật', done: form.highlights.length >= 3, href: '#noi-bat' },
  { label: 'Tiện nghi trong phòng', done: form.features.some((f) => f.trim()), href: '#tien-nghi' },
])
const progress = computed(() => Math.round((checklist.value.filter((c) => c.done).length / checklist.value.length) * 100))

/** Khối nội dung của form */
const SECTION = 'a-block scroll-mt-20 p-5 sm:p-6'
const SECTION_HEAD = 'mb-5'
const SECTION_DESC = 'mt-0.5 text-[13px] text-ink-2'

const typeOption = (active) => [
  'flex cursor-pointer items-start gap-3 rounded-lg border p-3',
  active ? 'border-brand shadow-[inset_0_0_0_1px_var(--brand)]' : 'border-line hover:bg-surface-2',
]
</script>

<template>
  <div class="a-page pb-24 lg:pb-10">
    <AdminPageHeader
      :title="isEdit ? form.name || 'Sửa phòng' : 'Thêm phòng mới'"
      :subtitle="isEdit ? 'Chỉnh sửa thông tin hiển thị trên trang phòng.' : 'Điền thông tin để thêm phòng mới lên trang web.'"
      :back="{ name: 'admin-rooms' }"
      back-label="Danh sách phòng"
    >
      <template v-if="dirty" #actions>
        <span class="a-badge tone-warn">Có thay đổi chưa lưu</span>
      </template>
    </AdminPageHeader>

    <div v-if="loading" class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_320px]" aria-busy="true" aria-label="Đang tải">
      <div class="space-y-5">
        <div class="a-skel h-80 rounded-xl" />
        <div class="a-skel h-48 rounded-xl" />
      </div>
      <div class="a-skel h-96 rounded-xl" />
    </div>

    <form v-else class="grid items-start gap-5 lg:grid-cols-[minmax(0,1fr)_320px]" novalidate @submit.prevent="save">
      <div class="min-w-0 space-y-5">
        <AdminAlert v-if="error" type="error">{{ error }}</AdminAlert>

        <!-- Thông tin cơ bản -->
        <section id="co-ban" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Thông tin cơ bản</h2>
            <p :class="SECTION_DESC">Tên và loại phòng hiển thị cho khách.</p>
          </header>

          <div class="space-y-5">
            <AdminField id="name" label="Tên phòng" required :error="errors.name">
              <input
                id="name"
                v-model="form.name"
                :class="INPUT"
                :aria-invalid="!!errors.name"
                maxlength="150"
                placeholder="vd. Phòng hướng biển"
              />
            </AdminField>


            <fieldset>
              <div class="mb-1.5 flex items-center justify-between gap-3">
                <legend class="text-[13px] font-medium text-ink-2">Loại phòng<span class="text-danger"> *</span></legend>
                <RouterLink
                  :to="{ name: 'admin-room-types' }"
                  target="_blank"
                  class="inline-flex items-center gap-1 text-[13px] text-ink-2 no-underline hover:text-ink"
                >
                  Quản lý loại phòng <AdminIcon name="external" class="size-3.5" />
                </RouterLink>
              </div>
              <div v-if="!types.length" class="rounded-lg border border-dashed border-line p-4 text-[13px] text-ink-2">
                Chưa có loại phòng nào. Hãy tạo ở trang
                <RouterLink :to="{ name: 'admin-room-types' }" target="_blank" class="a-link">Loại phòng</RouterLink> rồi tải lại trang này.
              </div>
              <div v-else class="grid gap-2 sm:grid-cols-2 xl:grid-cols-3" role="radiogroup">
                <label v-for="(t, i) in types" :key="t.id" :class="typeOption(form.typeId === t.id)">
                  <input v-model="form.typeId" type="radio" name="typeId" :value="t.id" class="type-radio mt-0.5 size-4 shrink-0" />
                  <span class="min-w-0">
                    <span class="flex items-center gap-2 font-medium">
                      <span :class="['a-dot', toneAt(i)]" :style="{ background: 'var(--tone)' }" aria-hidden="true" />
                      {{ t.name }}
                    </span>
                    <span v-if="t.description" class="line-clamp-2 text-[12.5px] text-muted">{{ t.description }}</span>
                  </span>
                </label>
              </div>
              <p v-if="errors.typeId" class="mt-1.5 text-[12.5px] text-danger">{{ errors.typeId }}</p>
            </fieldset>
          </div>
        </section>

        <!-- Giá & sức chứa -->
        <section id="gia" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Giá và sức chứa</h2>
            <p :class="SECTION_DESC">Giá đã gồm bữa sáng và thuế phí.</p>
          </header>
          <div class="grid gap-4 sm:grid-cols-2">
            <AdminField
              id="price"
              label="Giá một đêm"
              required
              :error="errors.price"
              :hint="form.price ? money(Number(form.price)) : 'Nhập số, vd. 1200000'"
            >
              <div class="relative">
                <input
                  id="price"
                  v-model.number="form.price"
                  type="number"
                  min="0"
                  step="50000"
                  :class="[INPUT, 'pr-14 text-right']"
                  :aria-invalid="!!errors.price"
                />
                <span class="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-[13px] text-muted">VNĐ</span>
              </div>
            </AdminField>
            <AdminField id="maxGuests" label="Số khách tối đa" required :error="errors.maxGuests" hint="Dùng để lọc phòng theo số khách.">
              <div class="flex items-stretch gap-1.5">
                <button
                  type="button"
                  :class="[BTN_SECONDARY, 'w-[38px] shrink-0 px-0 text-base']"
                  :disabled="form.maxGuests <= 1"
                  aria-label="Bớt 1 khách"
                  @click="form.maxGuests = Math.max(1, (form.maxGuests || 1) - 1)"
                >
                  −
                </button>
                <input
                  id="maxGuests"
                  v-model.number="form.maxGuests"
                  type="number"
                  min="1"
                  max="20"
                  :class="[INPUT, 'text-center']"
                  :aria-invalid="!!errors.maxGuests"
                />
                <button
                  type="button"
                  :class="[BTN_SECONDARY, 'w-[38px] shrink-0 px-0 text-base']"
                  :disabled="form.maxGuests >= 20"
                  aria-label="Thêm 1 khách"
                  @click="form.maxGuests = Math.min(20, (form.maxGuests || 0) + 1)"
                >
                  +
                </button>
              </div>
            </AdminField>
          </div>
        </section>

        <!-- Thông số -->
        <section id="thong-so" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Thông số phòng</h2>
            <p :class="SECTION_DESC">Hiện trong bảng thông số ở trang chi tiết phòng.</p>
          </header>
          <div class="grid gap-4 sm:grid-cols-2">
            <AdminField id="size" label="Diện tích" :error="errors.size">
              <input id="size" v-model="form.size" :class="INPUT" placeholder="vd. 28 m²" />
            </AdminField>
            <AdminField id="guests" label="Sức chứa (chữ hiển thị)" :error="errors.guests">
              <input id="guests" v-model="form.guests" :class="INPUT" placeholder="vd. 2 người lớn, 1 trẻ em" />
            </AdminField>
            <AdminField id="bed" label="Giường" :error="errors.bed">
              <input id="bed" v-model="form.bed" :class="INPUT" placeholder="vd. King hoặc 2 đơn" />
            </AdminField>
            <AdminField id="view" label="Tầm nhìn" :error="errors.view">
              <input id="view" v-model="form.view" :class="INPUT" placeholder="vd. Hướng biển" />
            </AdminField>
          </div>
        </section>

        <!-- Mô tả -->
        <section id="mo-ta" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Mô tả</h2>
            <p :class="SECTION_DESC">Giới thiệu ngắn gọn, dễ đọc trên điện thoại.</p>
          </header>
          <div class="space-y-4">
            <AdminField id="shortDescription" label="Mô tả ngắn" :error="errors.shortDescription">
              <input id="shortDescription" v-model="form.shortDescription" :class="INPUT" maxlength="500" />
              <p class="mt-1.5 flex justify-between text-[12.5px] text-muted">
                <span>Hiện trên thẻ phòng ở trang chủ (1 câu).</span>
                <span>{{ form.shortDescription?.length ?? 0 }}/500</span>
              </p>
            </AdminField>
            <AdminField id="description" label="Giới thiệu chi tiết" :error="errors.description">
              <textarea id="description" v-model="form.description" rows="6" :class="INPUT" />
            </AdminField>
          </div>
        </section>

        <!-- Ảnh -->
        <section id="hinh-anh" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Hình ảnh <span class="font-normal text-muted">{{ form.images.length }}</span></h2>
            <p :class="SECTION_DESC">Ảnh đầu tiên là ảnh bìa. Nên có ít nhất 3 ảnh.</p>
          </header>
          <ImageListEditor v-model="form.images" />
        </section>

        <!-- Điểm nổi bật -->
        <section id="noi-bat" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Điểm nổi bật</h2>
            <p :class="SECTION_DESC">Nên có 3 mục, hiện ngay dưới phần giới thiệu.</p>
          </header>
          <ListEditor
            v-model="form.highlights"
            :fields="[
              { key: 'title', label: 'Tiêu đề' },
              { key: 'text', label: 'Mô tả ngắn', multiline: true },
            ]"
            add-label="Thêm điểm nổi bật"
          />
          <p v-if="listError('highlights')" class="mt-2 text-[13px] text-danger">{{ listError('highlights') }}</p>
        </section>

        <!-- Tiện nghi -->
        <section id="tien-nghi" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Tiện nghi trong phòng</h2>
            <p :class="SECTION_DESC">Mỗi dòng một tiện nghi, vd. "Máy pha cà phê".</p>
          </header>
          <ListEditor v-model="form.features" add-label="Thêm tiện nghi" placeholder="vd. Máy pha cà phê" />
        </section>

        <!-- Đánh giá -->
        <section id="danh-gia" :class="SECTION">
          <header :class="SECTION_HEAD">
            <h2 class="a-h2">Đánh giá của khách</h2>
            <p :class="SECTION_DESC">Trích dẫn ngắn từ khách đã ở.</p>
          </header>
          <ListEditor
            v-model="form.reviews"
            :fields="[
              { key: 'quote', label: 'Nội dung đánh giá', multiline: true },
              { key: 'author', label: 'Tên khách, vd. Chị Hà, Hà Nội' },
            ]"
            add-label="Thêm đánh giá"
          />
          <p v-if="listError('reviews')" class="mt-2 text-[13px] text-danger">{{ listError('reviews') }}</p>
        </section>
      </div>

      <!-- Cột phải: xem trước, mức độ hoàn thiện, lưu -->
      <aside class="space-y-4 lg:sticky lg:top-[76px]">
        <div :class="[CARD, 'overflow-hidden']">
          <img v-if="form.images[0]" :src="form.images[0]" alt="" class="aspect-16/10 w-full bg-surface-2 object-cover" />
          <div v-else class="flex aspect-16/10 w-full items-center justify-center bg-surface-2 text-[13px] text-muted">Chưa có ảnh bìa</div>
          <div class="border-t border-line p-4">
            <p class="truncate font-semibold">{{ form.name || 'Tên phòng' }}</p>
            <div class="mt-2">
              <span v-if="selectedType" :class="['a-badge', typeTone]">{{ selectedType.name }}</span>
              <span v-else class="a-badge a-badge--plain">Chưa chọn loại</span>
            </div>
            <dl class="a-kv mt-3 border-t border-line pt-3">
              <dt>Giá / đêm</dt>
              <dd>{{ form.price ? money(Number(form.price)) : '—' }}</dd>
              <dt>Khách tối đa</dt>
              <dd>{{ form.maxGuests || '—' }}</dd>
              <dt>Diện tích</dt>
              <dd>{{ form.size || '—' }}</dd>
              <dt>Tầm nhìn</dt>
              <dd>{{ form.view || '—' }}</dd>
            </dl>
          </div>
        </div>

        <div :class="[CARD, 'p-4']">
          <div class="mb-2 flex items-center justify-between text-[13px]">
            <span class="font-semibold">Mức độ hoàn thiện</span>
            <span :class="progress === 100 ? 'font-semibold text-ok' : 'text-ink-2'">{{ progress }}%</span>
          </div>
          <div
            class="mb-3 h-1.5 overflow-hidden rounded-full bg-surface-2"
            role="progressbar"
            :aria-valuenow="progress"
            aria-valuemin="0"
            aria-valuemax="100"
            aria-label="Mức độ hoàn thiện"
          >
            <div :class="['h-full rounded-full', progress === 100 ? 'bg-ok' : 'bg-ink-2']" :style="{ width: `${progress}%` }" />
          </div>
          <ul class="space-y-1.5 text-[13px]">
            <li v-for="c in checklist" :key="c.label">
              <a :href="c.href" class="flex items-center gap-2 text-ink no-underline hover:underline">
                <span
                  :class="[
                    'flex size-4 shrink-0 items-center justify-center rounded-full',
                    c.done ? 'bg-ok text-white' : 'border border-line text-transparent',
                  ]"
                >
                  <AdminIcon name="check" class="size-3" />
                </span>
                <span>{{ c.label }}</span>
              </a>
            </li>
          </ul>
        </div>

        <!-- Nút lưu: trong cột phải trên máy tính, thanh dưới cùng trên điện thoại -->
        <div
          class="fixed inset-x-0 bottom-0 z-30 flex gap-2 border-t border-line bg-surface p-3 md:left-[232px] lg:static lg:flex-col lg:border-0 lg:bg-transparent lg:p-0"
        >
          <button type="submit" :class="[BTN_PRIMARY, 'flex-1 lg:w-full lg:flex-none']" :disabled="saving">
            {{ saving ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm phòng' }}
          </button>
          <RouterLink :to="{ name: 'admin-rooms' }" :class="[BTN_SECONDARY, 'lg:w-full']">Hủy</RouterLink>
          <a
            v-if="isEdit"
            :href="`/phong/${form.slug}`"
            target="_blank"
            rel="noopener"
            class="hidden items-center justify-center gap-1.5 pt-1 text-[13px] text-ink-2 no-underline hover:text-ink lg:flex"
          >
            <AdminIcon name="eye" class="size-4" /> Xem trang phòng
          </a>
        </div>
      </aside>
    </form>
  </div>
</template>

<style scoped>
.type-radio { accent-color: var(--brand); }
</style>

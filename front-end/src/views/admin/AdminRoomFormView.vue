<script setup>
// Dùng chung cho thêm phòng (không có id) và sửa phòng (có id)
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import AdminField from '@/components/admin/AdminField.vue'
import ListEditor from '@/components/admin/ListEditor.vue'
import ImageListEditor from '@/components/admin/ImageListEditor.vue'
import { BTN_PRIMARY, BTN_SECONDARY, CARD, CARD_TITLE, INPUT } from '@/components/admin/styles'
import { roomApi } from '@/api/rooms'
import { useFlash } from '@/composables/useFlash'
import { useRooms } from '@/composables/useRooms'
import { money } from '@/utils/format'
import { slugify } from '@/utils/slugify'

const props = defineProps({
  id: { type: String, default: null },
})

const router = useRouter()
const { flash } = useFlash()
const { invalidateRooms } = useRooms()
const isEdit = computed(() => !!props.id)

const form = reactive({
  slug: '',
  name: '',
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

const loading = ref(isEdit.value)
const saving = ref(false)
const error = ref('')
const errors = ref({})

/* Khi thêm mới: tự sinh đường dẫn từ tên phòng, cho đến khi admin tự sửa đường dẫn */
const slugTouched = ref(false)
watch(
  () => form.name,
  (name) => {
    if (!isEdit.value && !slugTouched.value) form.slug = slugify(name)
  },
)

if (isEdit.value) {
  roomApi
    .getById(props.id)
    .then((room) => Object.assign(form, room))
    .catch((e) => (error.value = e.message))
    .finally(() => (loading.value = false))
}

/** Bỏ các dòng trống trước khi gửi */
function payload() {
  const filled = (obj) => Object.values(obj).some((v) => v?.trim())
  return {
    ...form,
    slug: form.slug.trim(),
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
    flash(`Đã lưu "${room.name}".`)
    router.push({ name: 'admin-rooms' })
  } catch (e) {
    error.value = e.message
    errors.value = e.errors ?? {}
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } finally {
    saving.value = false
  }
}

/** Lỗi của danh sách, vd. "highlights[0].title" -> hiện dưới mục Điểm nổi bật */
const listError = (key) =>
  Object.entries(errors.value).find(([k]) => k.startsWith(key + '['))?.[1]
    ? 'Có mục chưa hợp lệ, vui lòng kiểm tra lại (tiêu đề/nội dung không được để trống).'
    : ''
</script>

<template>
  <div>
    <AdminPageHeader
      :title="isEdit ? form.name || 'Sửa phòng' : 'Thêm phòng mới'"
      :subtitle="isEdit ? 'Chỉnh sửa thông tin hiển thị trên trang phòng.' : 'Điền thông tin để thêm loại phòng mới lên website.'"
      :back="{ name: 'admin-rooms' }"
      back-label="Danh sách phòng"
    />

    <div v-if="loading" class="grid gap-6 lg:grid-cols-[1fr_300px]">
      <div class="space-y-6">
        <div class="h-80 animate-pulse rounded-2xl bg-soft" />
        <div class="h-48 animate-pulse rounded-2xl bg-soft" />
      </div>
      <div class="h-64 animate-pulse rounded-2xl bg-soft" />
    </div>

    <form v-else class="grid items-start gap-6 lg:grid-cols-[1fr_300px]" novalidate @submit.prevent="save">
      <div class="space-y-6">
        <AdminAlert v-if="error" type="error">{{ error }}</AdminAlert>

        <!-- Thông tin chính -->
        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-5']">Thông tin chính</h2>
          <div class="grid gap-4 sm:grid-cols-2">
            <AdminField id="name" label="Tên phòng" required :error="errors.name" class="sm:col-span-2">
              <input id="name" v-model="form.name" :class="INPUT" :aria-invalid="!!errors.name" maxlength="150" />
            </AdminField>
            <AdminField
              id="slug"
              label="Đường dẫn"
              required
              :error="errors.slug"
              :hint="`Trang phòng: /phong/${form.slug || '...'}`"
              class="sm:col-span-2"
            >
              <input
                id="slug"
                v-model="form.slug"
                :class="INPUT"
                :aria-invalid="!!errors.slug"
                placeholder="vd. phong-huong-bien"
                @input="slugTouched = true"
              />
            </AdminField>
            <AdminField
              id="price"
              label="Giá một đêm (VNĐ)"
              required
              :error="errors.price"
              :hint="form.price ? money(Number(form.price)) : ''"
            >
              <input
                id="price"
                v-model.number="form.price"
                type="number"
                min="0"
                step="50000"
                :class="INPUT"
                :aria-invalid="!!errors.price"
              />
            </AdminField>
            <AdminField id="maxGuests" label="Số khách tối đa" required :error="errors.maxGuests">
              <input
                id="maxGuests"
                v-model.number="form.maxGuests"
                type="number"
                min="1"
                max="20"
                :class="INPUT"
                :aria-invalid="!!errors.maxGuests"
              />
            </AdminField>
            <AdminField id="size" label="Diện tích" :error="errors.size">
              <input id="size" v-model="form.size" :class="INPUT" placeholder="vd. 28 m²" />
            </AdminField>
            <AdminField id="guests" label="Sức chứa (hiển thị)" :error="errors.guests">
              <input id="guests" v-model="form.guests" :class="INPUT" placeholder="vd. 2 khách" />
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
        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-5']">Mô tả</h2>
          <div class="space-y-4">
            <AdminField
              id="shortDescription"
              label="Mô tả ngắn"
              hint="Hiện trên thẻ phòng ở trang chủ (1 câu)."
              :error="errors.shortDescription"
            >
              <input id="shortDescription" v-model="form.shortDescription" :class="INPUT" maxlength="500" />
            </AdminField>
            <AdminField id="description" label="Giới thiệu chi tiết" :error="errors.description">
              <textarea id="description" v-model="form.description" rows="5" :class="INPUT" />
            </AdminField>
          </div>
        </section>

        <!-- Ảnh -->
        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-5']">Hình ảnh</h2>
          <ImageListEditor v-model="form.images" />
        </section>

        <!-- Danh sách -->
        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-1']">Điểm nổi bật</h2>
          <p class="mb-4 text-sm text-muted">Nên có 3 mục, hiện ngay dưới phần giới thiệu.</p>
          <ListEditor
            v-model="form.highlights"
            :fields="[
              { key: 'title', label: 'Tiêu đề' },
              { key: 'text', label: 'Mô tả ngắn', multiline: true },
            ]"
            add-label="Thêm điểm nổi bật"
          />
          <p v-if="listError('highlights')" class="mt-2 text-sm text-red-600">{{ listError('highlights') }}</p>
        </section>

        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-5']">Tiện nghi trong phòng</h2>
          <ListEditor v-model="form.features" add-label="Thêm tiện nghi" placeholder="vd. Máy pha cà phê" />
        </section>

        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-5']">Đánh giá của khách</h2>
          <ListEditor
            v-model="form.reviews"
            :fields="[
              { key: 'quote', label: 'Nội dung đánh giá', multiline: true },
              { key: 'author', label: 'Tên khách, vd. Chị Hà, Hà Nội' },
            ]"
            add-label="Thêm đánh giá"
          />
          <p v-if="listError('reviews')" class="mt-2 text-sm text-red-600">{{ listError('reviews') }}</p>
        </section>
      </div>

      <!-- Cột lưu -->
      <aside :class="[CARD, 'overflow-hidden p-0 lg:sticky lg:top-24']">
        <!-- Xem trước thẻ phòng -->
        <div class="aspect-16/10 bg-soft">
          <img v-if="form.images[0]" :src="form.images[0]" alt="" class="size-full object-cover" />
          <div v-else class="flex size-full flex-col items-center justify-center gap-1 text-sm text-muted">
            <AdminIcon name="image" class="size-8 text-muted/50" />
            Chưa có ảnh bìa
          </div>
        </div>
        <div class="p-5">
          <p class="truncate font-serif text-lg">{{ form.name || 'Tên phòng' }}</p>
          <p class="text-sm text-muted">
            <span class="font-semibold text-ink">{{ form.price ? money(Number(form.price)) : '—' }}</span> / đêm ·
            tối đa {{ form.maxGuests || '?' }} khách
          </p>

          <div class="mt-5 space-y-2">
            <button type="submit" :class="[BTN_PRIMARY, 'w-full']" :disabled="saving">
              <AdminIcon name="save" class="size-[18px]" />
              {{ saving ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm phòng' }}
            </button>
            <RouterLink :to="{ name: 'admin-rooms' }" :class="[BTN_SECONDARY, 'w-full']">Hủy</RouterLink>
          </div>
          <a
            v-if="isEdit"
            :href="`/phong/${form.slug}`"
            target="_blank"
            rel="noopener"
            class="mt-4 flex items-center justify-center gap-1.5 text-sm text-muted no-underline hover:text-primary"
          >
            <AdminIcon name="eye" class="size-4" /> Xem trang phòng
          </a>
        </div>
      </aside>
    </form>
  </div>
</template>

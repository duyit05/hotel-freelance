<script setup>
import { computed, ref, watch } from 'vue'
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import AppPagination from '@/components/ui/AppPagination.vue'
import RoomCard from '@/components/room/RoomCard.vue'
import { useRooms } from '@/composables/useRooms'
import { roomTypeApi } from '@/api/roomTypes'
import { smoothScrollTo } from '@/utils/smoothScroll'

/** Số phòng mỗi trang (6 = 2 hàng x 3 cột trên máy tính) */
const PAGE_SIZE = 6

const { rooms, loading, error, loadRooms, typeFilter: selected, roomsPage: page } = useRooms()
loadRooms()

/** Loại phòng theo thứ tự admin đặt; chỉ hiện nút lọc cho loại đang có phòng */
const allTypes = ref([])
roomTypeApi
  .list()
  .then((data) => (allTypes.value = data))
  .catch(() => {}) // không tải được thì chỉ ẩn bộ lọc
const types = computed(() => allTypes.value.filter((t) => rooms.value.some((r) => r.type?.id === t.id)))
const shown = computed(() =>
  selected.value ? rooms.value.filter((r) => r.type?.id === selected.value) : rooms.value,
)
const pageCount = computed(() => Math.max(1, Math.ceil(shown.value.length / PAGE_SIZE)))
const paged = computed(() => shown.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))

// Đổi loại phòng -> về trang 1; số phòng giảm (vd. admin xóa phòng) -> không đứng ở trang trống
watch(selected, () => (page.value = 1))
watch(pageCount, (n) => page.value > n && (page.value = n))

const section = ref(null)
function changePage(p) {
  page.value = p
  smoothScrollTo(section.value?.$el ?? document.getElementById('phong'))
}

const chip = (active) => [
  'rounded-full border px-4 py-1.5 text-sm transition-colors',
  active ? 'border-primary bg-primary text-white' : 'border-line bg-surface text-ink hover:border-primary hover:text-primary',
]
</script>

<template>
  <PageSection id="phong" ref="section" soft>
    <SectionHeading
      title="Các loại phòng"
      lead="Giá đã gồm bữa sáng và thuế phí. Chọn một phòng để xem chi tiết."
    />

    <p v-if="error && !rooms.length" class="text-muted">
      Chưa tải được danh sách phòng. {{ error }}
      <button type="button" class="ml-1 font-semibold text-primary underline" @click="loadRooms({ force: true })">
        Thử lại
      </button>
    </p>

    <template v-else>
      <div v-if="types.length > 1" class="mb-8 flex flex-wrap gap-2" role="group" aria-label="Lọc theo loại phòng">
        <button type="button" :class="chip(!selected)" :aria-pressed="!selected" @click="selected = null">Tất cả</button>
        <button
          v-for="t in types"
          :key="t.id"
          type="button"
          :class="chip(selected === t.id)"
          :aria-pressed="selected === t.id"
          :title="t.description || undefined"
          @click="selected = t.id"
        >
          {{ t.name }}
        </button>
      </div>

      <div class="grid gap-7 sm:grid-cols-2 md:grid-cols-3">
        <template v-if="loading && !rooms.length">
          <div v-for="n in 3" :key="n" class="h-[420px] animate-pulse rounded-card bg-surface" />
        </template>
        <RoomCard v-for="(room, i) in paged" :key="room.id" v-reveal="i % 3" :room="room" />
      </div>

      <div v-if="pageCount > 1" class="mt-10 flex flex-col items-center gap-2">
        <AppPagination
          :model-value="page"
          :total="shown.length"
          :page-size="PAGE_SIZE"
          label="Phân trang danh sách phòng"
          @update:model-value="changePage"
        />
        <p class="text-sm text-muted">
          Phòng {{ (page - 1) * PAGE_SIZE + 1 }}–{{ Math.min(page * PAGE_SIZE, shown.length) }} / {{ shown.length }}
        </p>
      </div>
    </template>
  </PageSection>
</template>

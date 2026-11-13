<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { BTN_ICON, BTN_ICON_DANGER, BTN_PRIMARY, CARD, INPUT, TABLE_HEAD } from '@/components/admin/styles'
import { roomApi } from '@/api/rooms'
import { useFlash } from '@/composables/useFlash'
import { useRooms } from '@/composables/useRooms'
import { money } from '@/utils/format'

const rooms = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)
const query = ref('')
const { message, clear } = useFlash()
const { invalidateRooms } = useRooms()

const normalize = (s) =>
  (s ?? '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/gi, 'd')
    .toLowerCase()

/** Tìm theo tên / đường dẫn, gõ không dấu vẫn ra */
const filtered = computed(() => {
  const q = normalize(query.value.trim())
  return q ? rooms.value.filter((r) => normalize(`${r.name} ${r.slug} ${r.view}`).includes(q)) : rooms.value
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    rooms.value = await roomApi.list()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function remove(room) {
  if (!confirm(`Xóa "${room.name}"? Thao tác này không thể hoàn tác.`)) return
  deleting.value = room.id
  try {
    await roomApi.remove(room.id)
    rooms.value = rooms.value.filter((r) => r.id !== room.id)
    invalidateRooms()
    message.value = `Đã xóa "${room.name}".`
  } catch (e) {
    alert(e.message)
  } finally {
    deleting.value = null
  }
}

onMounted(load)
onBeforeUnmount(clear)
</script>

<template>
  <div>
    <AdminPageHeader title="Phòng" :subtitle="`${rooms.length} loại phòng đang hiển thị trên trang web`">
      <template #actions>
        <RouterLink :to="{ name: 'admin-room-new' }" :class="BTN_PRIMARY">
          <AdminIcon name="plus" class="size-[18px]" /> Thêm phòng
        </RouterLink>
      </template>
    </AdminPageHeader>

    <AdminAlert v-if="message" closable class="mb-5" @close="clear">{{ message }}</AdminAlert>

    <AdminAlert v-if="error" type="error">
      {{ error }}
      <button type="button" class="ml-1 font-semibold underline" @click="load">Thử lại</button>
    </AdminAlert>

    <section v-else :class="[CARD, 'overflow-hidden p-0']">
      <div class="flex flex-wrap items-center gap-3 border-b border-line/70 px-5 py-4">
        <div class="relative w-full sm:max-w-xs">
          <AdminIcon
            name="search"
            class="pointer-events-none absolute top-1/2 left-3.5 size-[18px] -translate-y-1/2 text-muted"
          />
          <input v-model="query" type="search" :class="[INPUT, 'py-2 pl-10']" placeholder="Tìm theo tên phòng..." aria-label="Tìm phòng" />
        </div>
        <p v-if="query" class="text-sm text-muted">{{ filtered.length }} kết quả</p>
      </div>

      <div class="overflow-x-auto">
        <table class="w-full min-w-[760px] text-left text-[15px]">
          <thead :class="TABLE_HEAD">
            <tr>
              <th class="px-5 py-3 font-medium">Phòng</th>
              <th class="px-5 py-3 font-medium">Đường dẫn</th>
              <th class="px-5 py-3 text-right font-medium">Giá / đêm</th>
              <th class="px-5 py-3 text-center font-medium">Khách</th>
              <th class="px-5 py-3 text-center font-medium">Ảnh</th>
              <th class="w-28 px-5 py-3"><span class="sr-only">Thao tác</span></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-line/70">
            <template v-if="loading">
              <tr v-for="i in 4" :key="i">
                <td colspan="6" class="px-5 py-3.5">
                  <div class="flex items-center gap-3">
                    <span class="h-12 w-16 animate-pulse rounded-lg bg-soft" />
                    <span class="h-4 w-48 animate-pulse rounded bg-soft" />
                  </div>
                </td>
              </tr>
            </template>
            <tr v-else-if="!filtered.length">
              <td colspan="6" class="px-5 py-14 text-center">
                <AdminIcon name="bed" class="mx-auto mb-3 size-10 text-line" />
                <p class="font-medium">{{ query ? 'Không tìm thấy phòng phù hợp' : 'Chưa có phòng nào' }}</p>
                <p class="text-sm text-muted">
                  {{ query ? 'Thử từ khóa khác.' : 'Bấm "Thêm phòng" để tạo loại phòng đầu tiên.' }}
                </p>
              </td>
            </tr>
            <template v-else>
              <tr v-for="room in filtered" :key="room.id" class="group transition-colors hover:bg-soft/30">
                <td class="px-5 py-3">
                  <RouterLink
                    :to="{ name: 'admin-room-edit', params: { id: room.id } }"
                    class="flex items-center gap-3 no-underline"
                  >
                    <img
                      v-if="room.images[0]"
                      :src="room.images[0]"
                      alt=""
                      class="h-12 w-16 shrink-0 rounded-lg bg-img object-cover"
                    />
                    <span v-else class="flex h-12 w-16 shrink-0 items-center justify-center rounded-lg bg-soft">
                      <AdminIcon name="image" class="size-5 text-muted/60" />
                    </span>
                    <span class="min-w-0">
                      <span class="block font-medium group-hover:text-primary">{{ room.name }}</span>
                      <span v-if="room.view" class="block text-sm text-muted">{{ room.view }}</span>
                    </span>
                  </RouterLink>
                </td>
                <td class="px-5 py-3">
                  <a
                    :href="`/phong/${room.slug}`"
                    target="_blank"
                    rel="noopener"
                    class="inline-flex items-center gap-1 rounded-md bg-soft/60 px-2 py-0.5 font-mono text-[13px] text-muted no-underline hover:text-primary"
                  >
                    /{{ room.slug }}
                    <AdminIcon name="external" class="size-3.5" />
                  </a>
                </td>
                <td class="px-5 py-3 text-right font-semibold">{{ money(room.price) }}</td>
                <td class="px-5 py-3 text-center">{{ room.maxGuests }}</td>
                <td class="px-5 py-3 text-center">
                  <span
                    :class="[
                      'inline-flex min-w-8 justify-center rounded-full px-2 py-0.5 text-sm font-medium',
                      room.images.length >= 3 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700',
                    ]"
                  >
                    {{ room.images.length }}
                  </span>
                </td>
                <td class="px-5 py-3">
                  <div class="flex justify-end gap-1">
                    <RouterLink
                      :to="{ name: 'admin-room-edit', params: { id: room.id } }"
                      :class="BTN_ICON"
                      title="Sửa"
                      :aria-label="`Sửa ${room.name}`"
                    >
                      <AdminIcon name="edit" class="size-[18px]" />
                    </RouterLink>
                    <button
                      type="button"
                      :class="BTN_ICON_DANGER"
                      :disabled="deleting === room.id"
                      title="Xóa"
                      :aria-label="`Xóa ${room.name}`"
                      @click="remove(room)"
                    >
                      <AdminIcon name="trash" class="size-[18px]" />
                    </button>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>

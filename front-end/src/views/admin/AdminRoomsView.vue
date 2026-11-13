<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { roomApi } from '@/api/rooms'
import { useFlash } from '@/composables/useFlash'
import { useRooms } from '@/composables/useRooms'
import { money } from '@/utils/format'

const rooms = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)
const { message, clear } = useFlash()
const { invalidateRooms } = useRooms()

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
    <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
      <div>
        <h1 class="text-[30px]">Phòng</h1>
        <p class="text-muted">{{ rooms.length }} loại phòng đang hiển thị trên trang web</p>
      </div>
      <BaseButton :to="{ name: 'admin-room-new' }" size="sm">+ Thêm phòng</BaseButton>
    </div>

    <p
      v-if="message"
      class="mb-5 flex items-center justify-between rounded-lg bg-green-50 px-4 py-3 text-green-800"
      role="status"
    >
      {{ message }}
      <button type="button" class="text-green-700 hover:text-green-900" aria-label="Đóng" @click="clear">✕</button>
    </p>

    <div v-if="error" class="rounded-card border border-red-200 bg-red-50 p-5 text-red-700">
      {{ error }}
      <button type="button" class="ml-2 font-semibold underline" @click="load">Thử lại</button>
    </div>

    <div v-else class="overflow-x-auto rounded-card border border-line bg-surface">
      <table class="w-full min-w-[720px] text-left text-[15px]">
        <thead class="border-b border-line bg-soft text-sm text-muted">
          <tr>
            <th class="px-4 py-3 font-medium">Phòng</th>
            <th class="px-4 py-3 font-medium">Đường dẫn</th>
            <th class="px-4 py-3 text-right font-medium">Giá / đêm</th>
            <th class="px-4 py-3 text-center font-medium">Tối đa</th>
            <th class="px-4 py-3 text-center font-medium">Ảnh</th>
            <th class="px-4 py-3" />
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="px-4 py-10 text-center text-muted">Đang tải...</td>
          </tr>
          <tr v-else-if="!rooms.length">
            <td colspan="6" class="px-4 py-10 text-center text-muted">Chưa có phòng nào.</td>
          </tr>
          <template v-else>
          <tr v-for="room in rooms" :key="room.id" class="border-b border-line last:border-0 hover:bg-bg">
            <td class="px-4 py-3">
              <div class="flex items-center gap-3">
                <img
                  v-if="room.images[0]"
                  :src="room.images[0]"
                  alt=""
                  class="h-12 w-16 shrink-0 rounded-md bg-img object-cover"
                />
                <div v-else class="h-12 w-16 shrink-0 rounded-md bg-soft" />
                <span class="font-medium">{{ room.name }}</span>
              </div>
            </td>
            <td class="px-4 py-3 text-muted">
              <a :href="`/phong/${room.slug}`" target="_blank" rel="noopener" class="hover:text-primary">/{{ room.slug }}</a>
            </td>
            <td class="px-4 py-3 text-right">{{ money(room.price) }}</td>
            <td class="px-4 py-3 text-center">{{ room.maxGuests }}</td>
            <td class="px-4 py-3 text-center">{{ room.images.length }}</td>
            <td class="px-4 py-3">
              <div class="flex justify-end gap-2">
                <RouterLink
                  :to="{ name: 'admin-room-edit', params: { id: room.id } }"
                  class="rounded-lg border border-line px-3 py-1.5 text-sm no-underline hover:border-primary hover:text-primary"
                >
                  Sửa
                </RouterLink>
                <button
                  type="button"
                  class="rounded-lg border border-line px-3 py-1.5 text-sm hover:border-red-500 hover:text-red-600 disabled:opacity-50"
                  :disabled="deleting === room.id"
                  @click="remove(room)"
                >
                  {{ deleting === room.id ? 'Đang xóa...' : 'Xóa' }}
                </button>
              </div>
            </td>
          </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

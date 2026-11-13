<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { BTN_ICON, BTN_ICON_DANGER, BTN_PRIMARY, CARD, TABLE_HEAD } from '@/components/admin/styles'
import { userApi } from '@/api/users'
import { useAuth } from '@/composables/useAuth'
import { useFlash } from '@/composables/useFlash'
import { initials, shortDate } from '@/utils/format'

const users = ref([])
const loading = ref(true)
const error = ref('')
const deleting = ref(null)
const { user: me } = useAuth()
const { message, clear } = useFlash()

/** Màu ảnh đại diện cố định theo id để dễ phân biệt */
const AVATAR = [
  'bg-[#efe6d8] text-primary',
  'bg-emerald-50 text-emerald-700',
  'bg-sky-50 text-sky-700',
  'bg-violet-50 text-violet-700',
  'bg-rose-50 text-rose-700',
]

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
  if (!confirm(`Xóa tài khoản "${u.fullName}" (${u.username})? Thao tác này không thể hoàn tác.`)) return
  deleting.value = u.id
  try {
    await userApi.remove(u.id)
    users.value = users.value.filter((x) => x.id !== u.id)
    message.value = `Đã xóa tài khoản "${u.fullName}".`
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
    <AdminPageHeader title="Tài khoản" :subtitle="`${users.length} tài khoản có quyền vào trang quản trị`">
      <template #actions>
        <RouterLink :to="{ name: 'admin-user-new' }" :class="BTN_PRIMARY">
          <AdminIcon name="plus" class="size-[18px]" /> Thêm tài khoản
        </RouterLink>
      </template>
    </AdminPageHeader>

    <AdminAlert v-if="message" closable class="mb-5" @close="clear">{{ message }}</AdminAlert>

    <AdminAlert v-if="error" type="error">
      {{ error }}
      <button type="button" class="ml-1 font-semibold underline" @click="load">Thử lại</button>
    </AdminAlert>

    <section v-else :class="[CARD, 'overflow-hidden p-0']">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[680px] text-left text-[15px]">
          <thead :class="TABLE_HEAD">
            <tr>
              <th class="px-5 py-3 font-medium">Người dùng</th>
              <th class="px-5 py-3 font-medium">Vai trò</th>
              <th class="px-5 py-3 font-medium">Ngày tạo</th>
              <th class="w-28 px-5 py-3"><span class="sr-only">Thao tác</span></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-line/70">
            <template v-if="loading">
              <tr v-for="i in 3" :key="i">
                <td colspan="4" class="px-5 py-4">
                  <div class="flex items-center gap-3">
                    <span class="size-10 animate-pulse rounded-full bg-soft" />
                    <span class="h-4 w-48 animate-pulse rounded bg-soft" />
                  </div>
                </td>
              </tr>
            </template>
            <tr v-else-if="!users.length">
              <td colspan="4" class="px-5 py-14 text-center text-muted">Chưa có tài khoản nào.</td>
            </tr>
            <template v-else>
              <tr v-for="u in users" :key="u.id" class="transition-colors hover:bg-soft/30">
                <td class="px-5 py-3">
                  <div class="flex items-center gap-3">
                    <span
                      :class="[
                        'flex size-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold',
                        AVATAR[u.id % AVATAR.length],
                      ]"
                      aria-hidden="true"
                    >
                      {{ initials(u.fullName) }}
                    </span>
                    <div class="min-w-0 leading-tight">
                      <p class="flex items-center gap-2 font-medium">
                        {{ u.fullName }}
                        <span
                          v-if="u.username === me?.username"
                          class="rounded-full bg-primary px-2 py-px text-[11px] font-medium text-white"
                        >
                          Bạn
                        </span>
                      </p>
                      <p class="text-sm text-muted">@{{ u.username }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-5 py-3">
                  <span
                    class="inline-flex items-center gap-1.5 rounded-full border border-line bg-white px-2.5 py-0.5 text-sm"
                  >
                    <AdminIcon name="shield" class="size-3.5 text-accent" />
                    {{ u.role === 'ADMIN' ? 'Quản trị viên' : u.role }}
                  </span>
                </td>
                <td class="px-5 py-3 text-muted">{{ shortDate(u.createdAt) }}</td>
                <td class="px-5 py-3">
                  <div class="flex justify-end gap-1">
                    <RouterLink
                      :to="{ name: 'admin-user-edit', params: { id: u.id } }"
                      :class="BTN_ICON"
                      title="Sửa"
                      :aria-label="`Sửa ${u.fullName}`"
                    >
                      <AdminIcon name="edit" class="size-[18px]" />
                    </RouterLink>
                    <button
                      v-if="u.username !== me?.username"
                      type="button"
                      :class="BTN_ICON_DANGER"
                      :disabled="deleting === u.id"
                      title="Xóa"
                      :aria-label="`Xóa ${u.fullName}`"
                      @click="remove(u)"
                    >
                      <AdminIcon name="trash" class="size-[18px]" />
                    </button>
                    <span v-else class="size-9" />
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

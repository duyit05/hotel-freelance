<script setup>
// Dùng chung cho thêm tài khoản (không có id) và sửa tài khoản (có id)
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminField from '@/components/admin/AdminField.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPasswordInput from '@/components/admin/AdminPasswordInput.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { BTN_PRIMARY, BTN_SECONDARY, CARD, CARD_TITLE, INPUT } from '@/components/admin/styles'
import { initials } from '@/utils/format'
import { userApi } from '@/api/users'
import { useAuth } from '@/composables/useAuth'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  id: { type: String, default: null },
})

const router = useRouter()
const { toast } = useToast()
const { user: me } = useAuth()
const isEdit = computed(() => !!props.id)

const form = reactive({ username: '', fullName: '', password: '', confirm: '' })
const loading = ref(isEdit.value)
const saving = ref(false)
const error = ref('')
const errors = ref({})

const isSelf = computed(() => isEdit.value && form.username === me.value?.username)

if (isEdit.value) {
  userApi
    .getById(props.id)
    .then((u) => Object.assign(form, { username: u.username, fullName: u.fullName ?? '' }))
    .catch((e) => (error.value = e.message))
    .finally(() => (loading.value = false))
}

async function save() {
  error.value = ''
  errors.value = {}
  if (form.password && form.password !== form.confirm) {
    errors.value = { confirm: 'Mật khẩu nhập lại không khớp' }
    return
  }

  saving.value = true
  try {
    const data = { fullName: form.fullName.trim(), password: form.password || null }
    if (!isEdit.value) data.username = form.username.trim()
    const u = isEdit.value ? await userApi.update(props.id, data) : await userApi.create(data)
    toast(
      !isEdit.value
        ? `Đã thêm tài khoản "${u.fullName}"`
        : isSelf.value
          ? 'Đã lưu thay đổi. Họ tên mới hiển thị từ lần đăng nhập tiếp theo'
          : `Đã lưu thay đổi cho "${u.fullName}"`,
      { ms: isSelf.value ? 6000 : undefined },
    )
    router.push({ name: 'admin-users' })
  } catch (e) {
    error.value = e.message
    errors.value = e.errors ?? {}
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="a-page">
    <AdminPageHeader
      :title="isEdit ? (isSelf ? 'Tài khoản của bạn' : 'Sửa tài khoản') : 'Thêm tài khoản'"
      :subtitle="isEdit ? 'Cập nhật họ tên hoặc đặt lại mật khẩu.' : 'Tạo tài khoản mới để đăng nhập trang quản trị.'"
      :back="{ name: 'admin-users' }"
      back-label="Danh sách tài khoản"
    />

    <div v-if="loading" class="grid max-w-[1000px] gap-5 lg:grid-cols-[minmax(0,1fr)_300px]" aria-busy="true" aria-label="Đang tải">
      <div class="a-skel h-72 rounded-xl" />
      <div class="a-skel h-48 rounded-xl" />
    </div>

    <form v-else class="grid max-w-[1000px] items-start gap-5 lg:grid-cols-[minmax(0,1fr)_300px]" novalidate @submit.prevent="save">
      <div class="space-y-5">
        <AdminAlert v-if="error" type="error">{{ error }}</AdminAlert>

        <section :class="[CARD, 'p-5 sm:p-6']">
          <h2 :class="[CARD_TITLE, 'mb-5']">Thông tin tài khoản</h2>
          <div class="grid gap-4">
            <AdminField id="fullName" label="Họ và tên" required :error="errors.fullName">
              <input
                id="fullName"
                v-model="form.fullName"
                :class="INPUT"
                :aria-invalid="!!errors.fullName"
                maxlength="100"
                placeholder="vd. Nguyễn Văn A"
                autocomplete="name"
              />
            </AdminField>
            <AdminField
              id="username"
              label="Tên đăng nhập"
              :required="!isEdit"
              :error="errors.username"
              :hint="isEdit ? 'Không thể đổi tên đăng nhập sau khi tạo.' : 'Chữ không dấu, số, dấu . _ -'"
            >
              <input
                id="username"
                v-model="form.username"
                :class="INPUT"
                :aria-invalid="!!errors.username"
                :readonly="isEdit"
                maxlength="50"
                autocomplete="off"
              />
            </AdminField>
          </div>
        </section>

        <section :class="[CARD, 'p-5 sm:p-6']">
          <h2 :class="CARD_TITLE">{{ isEdit ? 'Đổi mật khẩu' : 'Mật khẩu' }}</h2>
          <p class="mt-0.5 mb-5 text-[13px] text-ink-2">
            {{ isEdit ? 'Để trống nếu muốn giữ mật khẩu hiện tại.' : 'Ít nhất 8 ký tự, nên có cả chữ và số.' }}
          </p>
          <div class="grid gap-4 sm:grid-cols-2">
            <AdminField id="password" :label="isEdit ? 'Mật khẩu mới' : 'Mật khẩu'" :required="!isEdit" :error="errors.password">
              <AdminPasswordInput
                id="password"
                v-model="form.password"
                :aria-invalid="!!errors.password"
                autocomplete="new-password"
              />
            </AdminField>
            <AdminField id="confirm" label="Nhập lại mật khẩu" :required="!isEdit" :error="errors.confirm">
              <AdminPasswordInput
                id="confirm"
                v-model="form.confirm"
                :aria-invalid="!!errors.confirm"
                autocomplete="new-password"
              />
            </AdminField>
          </div>
        </section>
      </div>

      <aside :class="[CARD, 'overflow-hidden lg:sticky lg:top-[76px]']">
        <!-- Xem trước -->
        <div class="flex items-center gap-3 border-b border-line p-4">
          <span class="flex size-10 shrink-0 items-center justify-center rounded-full border border-line bg-surface-2 text-[13px] font-semibold text-ink-2">
            {{ initials(form.fullName) }}
          </span>
          <div class="min-w-0 leading-tight">
            <p class="truncate font-semibold">{{ form.fullName || 'Họ và tên' }}</p>
            <p class="truncate text-[13px] text-muted">@{{ form.username || 'ten-dang-nhap' }}</p>
          </div>
        </div>

        <dl class="a-kv border-b border-line p-4">
          <dt>Vai trò</dt>
          <dd>Quản trị viên</dd>
          <dt>Quyền</dt>
          <dd>Phòng và tài khoản</dd>
        </dl>

        <div class="space-y-2 p-4">
          <button type="submit" :class="[BTN_PRIMARY, 'w-full']" :disabled="saving">
            {{ saving ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm tài khoản' }}
          </button>
          <RouterLink :to="{ name: 'admin-users' }" :class="[BTN_SECONDARY, 'w-full']">Hủy</RouterLink>
        </div>
      </aside>
    </form>
  </div>
</template>

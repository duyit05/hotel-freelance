<script setup>
// Dùng chung cho thêm tài khoản (không có id) và sửa tài khoản (có id)
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminField from '@/components/admin/AdminField.vue'
import AdminIcon from '@/components/admin/AdminIcon.vue'
import AdminAlert from '@/components/admin/AdminAlert.vue'
import AdminPageHeader from '@/components/admin/AdminPageHeader.vue'
import { BTN_PRIMARY, BTN_SECONDARY, CARD, CARD_TITLE, INPUT } from '@/components/admin/styles'
import { initials } from '@/utils/format'
import { userApi } from '@/api/users'
import { useAuth } from '@/composables/useAuth'
import { useFlash } from '@/composables/useFlash'

const props = defineProps({
  id: { type: String, default: null },
})

const router = useRouter()
const { flash } = useFlash()
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
    flash(
      isSelf.value
        ? `Đã lưu tài khoản của bạn. Họ tên mới sẽ hiển thị ở góc trên sau lần đăng nhập tiếp theo.`
        : `Đã lưu tài khoản "${u.fullName}".`,
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
  <div>
    <AdminPageHeader
      :title="isEdit ? (isSelf ? 'Tài khoản của bạn' : 'Sửa tài khoản') : 'Thêm tài khoản'"
      :subtitle="isEdit ? 'Cập nhật họ tên hoặc đặt lại mật khẩu.' : 'Tạo tài khoản mới để đăng nhập trang quản trị.'"
      :back="{ name: 'admin-users' }"
      back-label="Danh sách tài khoản"
    />

    <div v-if="loading" class="grid gap-6 lg:grid-cols-[1fr_300px]">
      <div class="h-72 animate-pulse rounded-2xl bg-soft" />
      <div class="h-48 animate-pulse rounded-2xl bg-soft" />
    </div>

    <form v-else class="grid items-start gap-6 lg:grid-cols-[1fr_300px]" novalidate @submit.prevent="save">
      <div class="space-y-6">
        <AdminAlert v-if="error" type="error">{{ error }}</AdminAlert>

        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-5']">Thông tin tài khoản</h2>
          <div class="grid gap-4 sm:grid-cols-2">
            <AdminField id="fullName" label="Họ và tên" required :error="errors.fullName" class="sm:col-span-2">
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
              class="sm:col-span-2"
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

        <section :class="CARD">
          <h2 :class="[CARD_TITLE, 'mb-1']">{{ isEdit ? 'Đổi mật khẩu' : 'Mật khẩu' }}</h2>
          <p class="mb-5 text-sm text-muted">
            {{ isEdit ? 'Để trống nếu muốn giữ mật khẩu hiện tại.' : 'Ít nhất 6 ký tự, nên có cả chữ và số.' }}
          </p>
          <div class="grid gap-4 sm:grid-cols-2">
            <AdminField
              id="password"
              :label="isEdit ? 'Mật khẩu mới' : 'Mật khẩu'"
              :required="!isEdit"
              :error="errors.password"
            >
              <input
                id="password"
                v-model="form.password"
                type="password"
                :class="INPUT"
                :aria-invalid="!!errors.password"
                autocomplete="new-password"
              />
            </AdminField>
            <AdminField id="confirm" label="Nhập lại mật khẩu" :required="!isEdit" :error="errors.confirm">
              <input
                id="confirm"
                v-model="form.confirm"
                type="password"
                :class="INPUT"
                :aria-invalid="!!errors.confirm"
                autocomplete="new-password"
              />
            </AdminField>
          </div>
        </section>
      </div>

      <aside :class="[CARD, 'lg:sticky lg:top-24']">
        <!-- Xem trước -->
        <div class="mb-5 flex items-center gap-3 border-b border-line/70 pb-5">
          <span
            class="flex size-12 shrink-0 items-center justify-center rounded-full bg-[#efe6d8] font-semibold text-primary"
          >
            {{ initials(form.fullName) }}
          </span>
          <div class="min-w-0 leading-tight">
            <p class="truncate font-medium">{{ form.fullName || 'Họ và tên' }}</p>
            <p class="truncate text-sm text-muted">@{{ form.username || 'ten-dang-nhap' }}</p>
          </div>
        </div>

        <div class="space-y-2">
          <button type="submit" :class="[BTN_PRIMARY, 'w-full']" :disabled="saving">
            <AdminIcon name="save" class="size-[18px]" />
            {{ saving ? 'Đang lưu...' : isEdit ? 'Lưu thay đổi' : 'Thêm tài khoản' }}
          </button>
          <RouterLink :to="{ name: 'admin-users' }" :class="[BTN_SECONDARY, 'w-full']">Hủy</RouterLink>
        </div>

        <p class="mt-5 flex gap-2 rounded-xl bg-soft/60 p-3 text-[13px] text-muted">
          <AdminIcon name="shield" class="mt-px size-4 shrink-0 text-accent" />
          <span>Vai trò <strong class="text-ink">Quản trị viên</strong>: quản lý phòng và các tài khoản khác.</span>
        </p>
      </aside>
    </form>
  </div>
</template>

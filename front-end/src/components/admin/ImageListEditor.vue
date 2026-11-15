<script setup>
// Quản lý ảnh phòng: upload nhiều ảnh, xóa, chọn ảnh bìa (ảnh đầu tiên)
import { ref } from 'vue'
import { uploadImage } from '@/api/rooms'
import AdminIcon from './AdminIcon.vue'
import { BTN_SMALL } from './styles'

const images = defineModel({ type: Array, required: true })
const uploading = ref(0)
const error = ref('')
const input = ref(null)

async function onFiles(e) {
  const files = [...e.target.files]
  e.target.value = ''
  error.value = ''
  uploading.value += files.length
  // Upload lần lượt để giữ đúng thứ tự ảnh đã chọn
  for (const file of files) {
    try {
      images.value.push(await uploadImage(file))
    } catch (err) {
      error.value = `${file.name}: ${err.message}`
    } finally {
      uploading.value--
    }
  }
}

function remove(i) {
  images.value.splice(i, 1)
}
function makeCover(i) {
  const [img] = images.value.splice(i, 1)
  images.value.unshift(img)
}
</script>

<template>
  <div>
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
      <figure
        v-for="(src, i) in images"
        :key="src + i"
        class="overflow-hidden rounded-lg border border-line bg-surface"
      >
        <img :src="src" alt="" class="aspect-4/3 w-full bg-surface-2 object-cover" />
        <figcaption class="flex items-center justify-between gap-1 border-t border-line px-2 py-1.5">
          <span v-if="i === 0" class="a-badge tone-c1 h-[22px]! px-2! text-xs!">Ảnh bìa</span>
          <button v-else type="button" :class="[BTN_SMALL, 'h-[26px]! px-2!']" @click="makeCover(i)">Đặt làm bìa</button>
          <button type="button" class="a-btn a-btn--icon a-btn--danger size-[26px]!" :aria-label="`Xóa ảnh ${i + 1}`" title="Xóa ảnh" @click="remove(i)">
            <AdminIcon name="trash" class="size-4" />
          </button>
        </figcaption>
      </figure>

      <button
        type="button"
        class="flex min-h-[120px] flex-col items-center justify-center gap-1.5 rounded-lg border border-dashed border-line text-[13px] font-medium text-ink-2 hover:bg-surface-2 hover:text-ink disabled:cursor-wait disabled:opacity-60"
        :disabled="uploading > 0"
        @click="input.click()"
      >
        <AdminIcon name="upload" class="size-5" />
        {{ uploading ? `Đang tải lên ${uploading} ảnh...` : 'Thêm ảnh' }}
      </button>
    </div>
    <input ref="input" type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" @change="onFiles" />
    <p v-if="error" class="mt-2 text-[13px] text-danger">{{ error }}</p>
    <p v-else class="mt-2 text-[13px] text-muted">JPG, PNG hoặc WEBP, tối đa 10MB mỗi ảnh (tự động nén khi tải lên). Ảnh đầu tiên là ảnh bìa.</p>
  </div>
</template>

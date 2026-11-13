<script setup>
// Quản lý ảnh phòng: upload nhiều ảnh, xóa, chọn ảnh bìa (ảnh đầu tiên)
import { ref } from 'vue'
import { uploadImage } from '@/api/rooms'
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
        class="group relative aspect-4/3 overflow-hidden rounded-lg border border-line bg-img"
      >
        <img :src="src" alt="" class="size-full object-cover" />
        <span v-if="i === 0" class="absolute top-2 left-2 rounded-full bg-primary px-2 py-0.5 text-xs text-white">
          Ảnh bìa
        </span>
        <div class="absolute inset-x-2 bottom-2 flex justify-end gap-1">
          <button v-if="i > 0" type="button" :class="BTN_SMALL" @click="makeCover(i)">Đặt làm bìa</button>
          <button type="button" :class="[BTN_SMALL, 'hover:border-red-500! hover:text-red-600!']" @click="remove(i)">
            Xóa
          </button>
        </div>
      </figure>

      <button
        type="button"
        class="flex aspect-4/3 flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed border-line text-sm text-muted hover:border-primary hover:text-primary disabled:opacity-60"
        :disabled="uploading > 0"
        @click="input.click()"
      >
        <span class="text-2xl leading-none">+</span>
        {{ uploading ? `Đang tải lên ${uploading} ảnh...` : 'Thêm ảnh' }}
      </button>
    </div>
    <input ref="input" type="file" accept="image/jpeg,image/png,image/webp" multiple class="hidden" @change="onFiles" />
    <p v-if="error" class="mt-2 text-sm text-red-600">{{ error }}</p>
    <p v-else class="mt-2 text-sm text-muted">JPG, PNG hoặc WEBP, tối đa 5MB mỗi ảnh. Ảnh đầu tiên là ảnh bìa.</p>
  </div>
</template>

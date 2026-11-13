<script setup>
// Sửa một danh sách: thêm, xóa, đổi thứ tự.
// - Không truyền `fields`: mỗi mục là một chuỗi (vd. tiện nghi).
// - Có `fields`: mỗi mục là object, vd. [{ key: 'title', label: 'Tiêu đề' }, { key: 'text', label: 'Mô tả', multiline: true }]
import { INPUT, BTN_SMALL } from './styles'

const items = defineModel({ type: Array, required: true })
const props = defineProps({
  fields: { type: Array, default: null },
  addLabel: { type: String, default: 'Thêm' },
  placeholder: { type: String, default: '' },
})

function add() {
  items.value.push(props.fields ? Object.fromEntries(props.fields.map((f) => [f.key, ''])) : '')
}
function remove(i) {
  items.value.splice(i, 1)
}
function move(i, step) {
  const j = i + step
  if (j < 0 || j >= items.value.length) return
  const list = items.value
  ;[list[i], list[j]] = [list[j], list[i]]
}
</script>

<template>
  <div class="space-y-2.5">
    <div v-for="(item, i) in items" :key="i" class="flex items-start gap-2">
      <span class="w-6 shrink-0 pt-2 text-right text-sm text-muted">{{ i + 1 }}.</span>

      <input v-if="!fields" v-model="items[i]" :class="INPUT" :placeholder="placeholder" />
      <div v-else class="grid flex-1 gap-2 rounded-lg border border-line bg-bg p-3">
        <template v-for="f in fields" :key="f.key">
          <textarea
            v-if="f.multiline"
            v-model="item[f.key]"
            rows="2"
            :class="INPUT"
            :placeholder="f.label"
            :aria-label="f.label"
          />
          <input v-else v-model="item[f.key]" :class="INPUT" :placeholder="f.label" :aria-label="f.label" />
        </template>
      </div>

      <div class="flex shrink-0 gap-1">
        <button type="button" :class="BTN_SMALL" :disabled="i === 0" aria-label="Đưa lên" @click="move(i, -1)">↑</button>
        <button type="button" :class="BTN_SMALL" :disabled="i === items.length - 1" aria-label="Đưa xuống" @click="move(i, 1)">↓</button>
        <button type="button" :class="[BTN_SMALL, 'hover:border-red-500! hover:text-red-600!']" aria-label="Xóa" @click="remove(i)">✕</button>
      </div>
    </div>

    <button type="button" :class="BTN_SMALL" @click="add">+ {{ addLabel }}</button>
  </div>
</template>

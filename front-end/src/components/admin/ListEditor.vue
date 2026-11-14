<script setup>
// Sửa một danh sách: thêm, xóa, đổi thứ tự.
// - Không truyền `fields`: mỗi mục là một chuỗi (vd. tiện nghi).
// - Có `fields`: mỗi mục là object, vd. [{ key: 'title', label: 'Tiêu đề' }, { key: 'text', label: 'Mô tả', multiline: true }]
import AdminIcon from './AdminIcon.vue'
import { INPUT, BTN_ICON, BTN_ICON_DANGER, BTN_SMALL } from './styles'

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
  <div class="space-y-2">
    <div v-for="(item, i) in items" :key="i" class="flex items-start gap-2">
      <span class="mt-2.5 w-5 shrink-0 text-right text-[12.5px] text-muted">{{ i + 1 }}</span>

      <input v-if="!fields" v-model="items[i]" :class="INPUT" :placeholder="placeholder" />
      <div v-else class="grid flex-1 gap-2 rounded-lg border border-line p-2.5">
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

      <div class="flex shrink-0 gap-0.5 pt-0.5">
        <button type="button" :class="BTN_ICON" :disabled="i === 0" title="Đưa lên" aria-label="Đưa lên" @click="move(i, -1)"><AdminIcon name="chevronUp" class="size-4" /></button>
        <button type="button" :class="BTN_ICON" :disabled="i === items.length - 1" title="Đưa xuống" aria-label="Đưa xuống" @click="move(i, 1)"><AdminIcon name="chevronDown" class="size-4" /></button>
        <button type="button" :class="BTN_ICON_DANGER" title="Xóa" aria-label="Xóa" @click="remove(i)"><AdminIcon name="trash" class="size-4" /></button>
      </div>
    </div>

    <button type="button" :class="[BTN_SMALL, 'ml-7 border-dashed']" @click="add"><AdminIcon name="plus" class="size-3.5" /> {{ addLabel }}</button>
  </div>
</template>

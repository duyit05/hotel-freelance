import { computed, reactive } from 'vue'

// Trạng thái dùng chung cho hộp xem ảnh (LightboxViewer đặt trong App.vue)
const state = reactive({ open: false, list: [], index: 0 })
const current = computed(() => state.list[state.index])

function show(pos) {
  state.index = (pos + state.list.length) % state.list.length
}

/** list: [{ src, alt }] */
function open(list, index = 0) {
  state.list = list
  show(index)
  state.open = true
}

export function useLightbox() {
  return {
    state,
    current,
    open,
    close: () => (state.open = false),
    next: () => show(state.index + 1),
    prev: () => show(state.index - 1),
  }
}

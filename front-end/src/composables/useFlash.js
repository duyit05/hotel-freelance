// Thông báo ngắn hiển thị ở trang kế tiếp (vd. "Đã lưu phòng" sau khi chuyển về danh sách)
import { ref } from 'vue'

const message = ref('')

export function useFlash() {
  return {
    message,
    flash: (text) => (message.value = text),
    clear: () => (message.value = ''),
  }
}

// Toast sau mỗi thao tác, dùng thư viện vue-sonner (<Toaster> đặt ở AdminLayout nên vẫn còn khi chuyển trang).
// Dùng cùng động từ với nút: "Lưu thay đổi" -> "Đã lưu thay đổi".
//   toast('Đã xóa phòng "A"')
//   toast('Không xóa được phòng: ...', { tone: 'error' })
//   toast('Đang tải ảnh...', { tone: 'info', description: 'Vui lòng chờ' })
import { toast as sonner } from 'vue-sonner'

function toast(text, { tone = 'ok', ms, description } = {}) {
  const opts = { description, duration: ms ?? (tone === 'error' ? 6000 : 4000) }
  if (tone === 'error') return sonner.error(text, opts)
  if (tone === 'info') return sonner.info(text, opts)
  if (tone === 'warning') return sonner.warning(text, opts)
  return sonner.success(text, opts)
}

export function useToast() {
  return { toast, dismiss: sonner.dismiss }
}

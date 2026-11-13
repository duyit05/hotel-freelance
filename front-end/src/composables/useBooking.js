import { ref } from 'vue'
import { addDays, todayISO } from '@/utils/format'

// Ngày khách chọn ở trang chủ, được dùng lại ở trang chi tiết phòng
const checkin = ref(todayISO())
const checkout = ref(addDays(todayISO(), 1))

export function useBooking() {
  return { checkin, checkout }
}

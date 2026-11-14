// Danh sách phòng lấy từ API, dùng chung cho các trang (chỉ tải một lần)
import { ref } from 'vue'
import { roomApi } from '@/api/rooms'

const rooms = ref([])
const loading = ref(false)
const error = ref('')
let pending = null

async function loadRooms({ force = false } = {}) {
  if (pending) return pending
  if (rooms.value.length && !force) return rooms.value
  loading.value = true
  error.value = ''
  pending = roomApi
    .list()
    .then((data) => (rooms.value = data))
    .catch((e) => {
      error.value = e.message
      return rooms.value
    })
    .finally(() => {
      loading.value = false
      pending = null
    })
  return pending
}

/** Gọi sau khi admin thêm/sửa/xóa để trang công khai thấy dữ liệu mới */
const invalidateRooms = () => (rooms.value = [])

/** Loại phòng đang lọc ở mục "Các loại phòng" trang chủ (null = tất cả), dùng chung để thanh tìm kiếm có thể bỏ lọc */
const typeFilter = ref(null)
/** Trang đang xem ở mục "Các loại phòng" trang chủ */
const roomsPage = ref(1)

export function useRooms() {
  return { rooms, loading, error, loadRooms, invalidateRooms, typeFilter, roomsPage }
}

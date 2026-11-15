// Trạng thái đăng nhập admin.
// - Access token (JWT ~15 phút) chỉ giữ trong bộ nhớ, không lưu localStorage để XSS khó lấy được.
// - Refresh token nằm trong cookie HttpOnly do back-end đặt; F5 hoặc token hết hạn thì gọi
//   /api/auth/refresh để lấy access token mới.
import { computed, ref } from 'vue'
import { request } from '@/api/http'

// Chỉ là cờ "đã từng đăng nhập" để biết có nên thử làm mới khi tải trang, không chứa token
const SESSION_KEY = 'duynhat_admin_session'
// Làm mới sớm hơn hạn thật một chút để request không bị 401 giữa đường
const EXPIRY_MARGIN_MS = 30_000

localStorage.removeItem('duynhat_admin_token') // token cũ từ phiên bản trước

const token = ref(null)
let refreshing = null
let onUnauthorized = null

/** Đọc phần payload của JWT (không kiểm tra chữ ký, việc đó do back-end làm) */
function decode(jwt) {
  try {
    const part = jwt.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    // atob trả về từng byte; giải mã lại UTF-8 để tên tiếng Việt không bị lỗi font
    const bytes = Uint8Array.from(atob(part), (c) => c.charCodeAt(0))
    return JSON.parse(new TextDecoder().decode(bytes))
  } catch {
    return null
  }
}

const payload = computed(() => (token.value ? decode(token.value) : null))
const isExpired = (margin = 0) => !payload.value || payload.value.exp * 1000 <= Date.now() + margin

function setToken(value) {
  token.value = value
  if (value) localStorage.setItem(SESSION_KEY, '1')
  else localStorage.removeItem(SESSION_KEY)
}

// Đăng xuất ở tab khác -> tab này cũng đăng xuất
window.addEventListener('storage', (e) => {
  if (e.key === SESSION_KEY && !e.newValue) {
    token.value = null
    onUnauthorized?.()
  }
})

/** Lấy access token mới bằng cookie refresh. Nhiều request cùng lúc chỉ gọi một lần. */
export function refreshSession() {
  refreshing ??= request('/api/auth/refresh', { method: 'POST' })
    .then((res) => {
      setToken(res.token)
      return true
    })
    .catch((e) => {
      // Mất mạng thì giữ nguyên, chỉ xóa phiên khi back-end trả lời từ chối
      if (e.status !== 0) setToken(null)
      return false
    })
    .finally(() => {
      refreshing = null
    })
  return refreshing
}

/** Access token còn hạn (tự làm mới nếu sắp hết hạn), null nếu chưa đăng nhập */
export async function getAccessToken() {
  if (token.value && !isExpired(EXPIRY_MARGIN_MS)) return token.value
  if (!token.value && !localStorage.getItem(SESSION_KEY)) return null
  await refreshSession()
  return token.value
}

/** Gọi khi API vẫn trả 401 sau khi đã thử làm mới */
export function handleUnauthorized() {
  setToken(null)
  onUnauthorized?.()
}

/** Router đăng ký hàm này để chuyển về trang đăng nhập khi phiên hết hạn */
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn
}

export function useAuth() {
  const user = computed(() =>
    payload.value
      ? {
          username: payload.value.sub,
          fullName: payload.value.name ?? payload.value.sub,
          roles: payload.value.roles ?? [],
        }
      : null,
  )
  const isAdmin = () => !isExpired() && (payload.value?.roles ?? []).includes('ADMIN')

  /** Dùng ở router: khôi phục phiên từ cookie refresh (vd. sau khi F5) rồi kiểm tra quyền */
  async function ensureSession() {
    if (isAdmin()) return true
    await getAccessToken()
    return isAdmin()
  }

  async function login(username, password) {
    const res = await request('/api/auth/login', { method: 'POST', body: { username, password } })
    setToken(res.token)
    return res
  }

  async function logout() {
    try {
      await request('/api/auth/logout', { method: 'POST' })
    } catch {
      // Không gọi được back-end vẫn xóa phiên ở trình duyệt
    }
    setToken(null)
  }

  return { user, isAdmin, ensureSession, login, logout }
}

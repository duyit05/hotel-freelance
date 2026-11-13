// Trạng thái đăng nhập admin. JWT được lưu trong localStorage để F5 không bị đăng xuất.
import { computed, ref } from 'vue'
import { request } from '@/api/http'

const STORAGE_KEY = 'moclan_admin_token'
const token = ref(localStorage.getItem(STORAGE_KEY))
let onUnauthorized = null

/** Đọc phần payload của JWT (không kiểm tra chữ ký, việc đó do back-end làm) */
function decode(jwt) {
  try {
    const part = jwt.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(part))
  } catch {
    return null
  }
}

const payload = computed(() => (token.value ? decode(token.value) : null))
const isExpired = () => !payload.value || payload.value.exp * 1000 <= Date.now()

function setToken(value) {
  token.value = value
  if (value) localStorage.setItem(STORAGE_KEY, value)
  else localStorage.removeItem(STORAGE_KEY)
}

export const getToken = () => (token.value && !isExpired() ? token.value : null)

/** Gọi khi API trả 401 (token hết hạn hoặc không hợp lệ) */
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
    payload.value ? { username: payload.value.sub, roles: payload.value.roles ?? [] } : null,
  )
  const isAdmin = () => !isExpired() && (payload.value?.roles ?? []).includes('ADMIN')

  async function login(username, password) {
    const res = await request('/api/auth/login', { method: 'POST', body: { username, password } })
    setToken(res.token)
    return res
  }

  const logout = () => setToken(null)

  return { user, isAdmin, login, logout }
}

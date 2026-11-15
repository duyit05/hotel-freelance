// Hàm gọi API dùng chung. Lỗi từ back-end có dạng { detail, errors: { field: message } }
import { getAccessToken, handleUnauthorized, refreshSession } from '@/composables/useAuth'

const BASE = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  constructor(status, message, errors = {}) {
    super(message)
    this.status = status
    this.errors = errors
  }
}

/**
 * @param {string} path  vd. "/api/rooms"
 * @param {{ method?: string, body?: any, auth?: boolean }} options
 *   auth: true để gửi kèm JWT (chỉ dùng cho API của admin).
 *   Token hết hạn giữa chừng (401) thì tự làm mới một lần rồi gửi lại.
 */
export async function request(path, { method = 'GET', body, auth = false, retried = false } = {}) {
  // X-Requested-With: back-end bắt buộc với /api/auth/refresh và /logout (chống CSRF)
  const headers = { 'X-Requested-With': 'XMLHttpRequest' }
  if (auth) {
    const token = await getAccessToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }
  let payload = body
  if (body !== undefined && !(body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
    payload = JSON.stringify(body)
  }

  let res
  try {
    // credentials: gửi cookie refresh kể cả khi API ở origin khác (VITE_API_URL)
    res = await fetch(BASE + path, { method, headers, body: payload, credentials: 'include' })
  } catch {
    throw new ApiError(0, 'Không kết nối được máy chủ. Vui lòng thử lại.')
  }

  if (res.status === 401 && auth) {
    if (!retried && (await refreshSession())) {
      return request(path, { method, body, auth, retried: true })
    }
    handleUnauthorized()
  }
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new ApiError(res.status, data?.detail || `Có lỗi xảy ra (mã ${res.status})`, data?.errors)
  }
  return res.status === 204 ? null : res.json()
}

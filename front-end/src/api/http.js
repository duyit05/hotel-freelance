// Hàm gọi API dùng chung. Lỗi từ back-end có dạng { detail, errors: { field: message } }
import { getToken, handleUnauthorized } from '@/composables/useAuth'

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
 *   auth: true để gửi kèm JWT (chỉ dùng cho API của admin)
 */
export async function request(path, { method = 'GET', body, auth = false } = {}) {
  const headers = {}
  if (auth) {
    const token = getToken()
    if (token) headers.Authorization = `Bearer ${token}`
  }
  if (body !== undefined && !(body instanceof FormData)) {
    headers['Content-Type'] = 'application/json'
    body = JSON.stringify(body)
  }

  let res
  try {
    res = await fetch(BASE + path, { method, headers, body })
  } catch {
    throw new ApiError(0, 'Không kết nối được máy chủ. Vui lòng thử lại.')
  }

  if (res.status === 401 && auth) handleUnauthorized()
  if (!res.ok) {
    const data = await res.json().catch(() => null)
    throw new ApiError(res.status, data?.detail || `Có lỗi xảy ra (mã ${res.status})`, data?.errors)
  }
  return res.status === 204 ? null : res.json()
}

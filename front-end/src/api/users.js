import { request } from './http'

/** Quản lý tài khoản quản trị (đều cần đăng nhập ADMIN) */
export const userApi = {
  list: () => request('/api/users', { auth: true }),
  getById: (id) => request(`/api/users/${id}`, { auth: true }),
  create: (user) => request('/api/users', { method: 'POST', body: user, auth: true }),
  update: (id, user) => request(`/api/users/${id}`, { method: 'PUT', body: user, auth: true }),
  remove: (id) => request(`/api/users/${id}`, { method: 'DELETE', auth: true }),
}

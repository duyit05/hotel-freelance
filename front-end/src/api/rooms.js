import { request } from './http'

export const roomApi = {
  list: () => request('/api/rooms'),
  getBySlug: (slug) => request(`/api/rooms/slug/${encodeURIComponent(slug)}`),
  getById: (id) => request(`/api/rooms/${id}`),
  create: (room) => request('/api/rooms', { method: 'POST', body: room, auth: true }),
  update: (id, room) => request(`/api/rooms/${id}`, { method: 'PUT', body: room, auth: true }),
  remove: (id) => request(`/api/rooms/${id}`, { method: 'DELETE', auth: true }),
}

/** Upload một ảnh, trả về đường dẫn ảnh, vd. "/uploads/abc.jpg" */
export async function uploadImage(file) {
  const form = new FormData()
  form.append('file', file)
  const { url } = await request('/api/uploads', { method: 'POST', body: form, auth: true })
  return url
}

import { request } from './http'

export const roomTypeApi = {
  /** [{ id, name, description, sortOrder, roomCount }] theo thứ tự hiển thị */
  list: () => request('/api/room-types'),
  create: (type) => request('/api/room-types', { method: 'POST', body: type, auth: true }),
  update: (id, type) => request(`/api/room-types/${id}`, { method: 'PUT', body: type, auth: true }),
  remove: (id) => request(`/api/room-types/${id}`, { method: 'DELETE', auth: true }),
}

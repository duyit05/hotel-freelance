export const money = (n) => (n ?? 0).toLocaleString('vi-VN') + 'đ'

/** "28 m², 2 khách, hướng phố" (bỏ qua các ô admin để trống) */
export const roomMeta = (room, withView = true) =>
  [room.size, room.guests, withView && room.view?.toLowerCase()].filter(Boolean).join(', ')

const DAY = 864e5

export const toISO = (d) => d.toISOString().slice(0, 10)
export const todayISO = () => toISO(new Date())
export const addDays = (iso, n) => toISO(new Date(new Date(iso).getTime() + n * DAY))
export const nightsBetween = (from, to) => Math.round((new Date(to) - new Date(from)) / DAY)

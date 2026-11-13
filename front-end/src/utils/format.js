export const money = (n) => n.toLocaleString('vi-VN') + 'đ'

const DAY = 864e5

export const toISO = (d) => d.toISOString().slice(0, 10)
export const todayISO = () => toISO(new Date())
export const addDays = (iso, n) => toISO(new Date(new Date(iso).getTime() + n * DAY))
export const nightsBetween = (from, to) => Math.round((new Date(to) - new Date(from)) / DAY)

// Ảnh nằm trong src/assets/images. Muốn thay ảnh: chép file mới đè lên cùng tên
// (hoặc thêm file mới và dùng tên file, không có đuôi, làm key).
const files = import.meta.glob('@/assets/images/*.{jpg,jpeg,png,webp}', { eager: true, import: 'default' })

export const IMAGES = Object.fromEntries(
  Object.entries(files).map(([path, url]) => [path.split('/').pop().replace(/\.\w+$/, ''), url]),
)

export const img = (key) => IMAGES[key]

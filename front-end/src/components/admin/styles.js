// Tên class dùng chung cho trang quản trị (định nghĩa trong assets/admin.css)
export const INPUT = 'a-input'

/** Khối nội dung: nền trắng, viền 1px, bo 12px, không đổ bóng */
export const CARD = 'a-block'

/** Tiêu đề trong khối */
export const CARD_TITLE = 'a-h2'

export const BTN_PRIMARY = 'a-btn a-btn--primary'
export const BTN_SECONDARY = 'a-btn'
export const BTN_DANGER = 'a-btn a-btn--danger'
export const BTN_DANGER_SOLID = 'a-btn a-btn--danger-solid'
export const BTN_SMALL = 'a-btn a-btn--sm'

/** Nút chỉ có icon (sửa / xóa / đóng) */
export const BTN_ICON = 'a-btn a-btn--icon'
export const BTN_ICON_DANGER = 'a-btn a-btn--icon a-btn--danger'

/** Màu phân loại theo thứ tự (loại phòng thứ 1 -> c1, ...) */
export const TONES = ['tone-c1', 'tone-c2', 'tone-c3', 'tone-c4']
export const toneAt = (i) => (i < 0 ? '' : TONES[i % TONES.length])

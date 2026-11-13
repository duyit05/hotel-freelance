// Class Tailwind dùng chung cho trang quản trị
export const INPUT =
  'w-full rounded-xl border border-line bg-white px-3.5 py-2.5 text-ink outline-none transition-[border-color,box-shadow] placeholder:text-muted/50 focus:border-primary focus:ring-4 focus:ring-primary/10 focus-visible:outline-none aria-invalid:border-red-400 aria-invalid:ring-red-100 read-only:cursor-not-allowed read-only:bg-soft/60 read-only:text-muted'

export const CARD = 'rounded-2xl border border-line/70 bg-surface p-6 shadow-[0_1px_2px_rgba(43,40,36,.04)]'

/** Tiêu đề trong thẻ */
export const CARD_TITLE = 'font-sans text-base font-semibold'

const BTN_BASE =
  'inline-flex items-center justify-center gap-2 rounded-xl font-medium no-underline transition-[background-color,border-color,color,box-shadow] disabled:cursor-not-allowed disabled:opacity-60'

export const BTN_PRIMARY = `${BTN_BASE} bg-primary px-4 py-2.5 text-white shadow-[0_8px_18px_-10px_rgba(91,70,54,.9)] hover:bg-primary-hover`

export const BTN_SECONDARY = `${BTN_BASE} border border-line bg-white px-4 py-2.5 text-ink hover:border-primary/40 hover:bg-soft/50`

export const BTN_SMALL =
  'inline-flex items-center gap-1 rounded-lg border border-line bg-white px-2.5 py-1.5 text-sm text-ink transition-colors hover:border-primary/50 hover:text-primary disabled:cursor-not-allowed disabled:opacity-40'

/** Nút chỉ có icon trong bảng (sửa / xóa) */
export const BTN_ICON =
  'inline-flex size-9 items-center justify-center rounded-lg text-muted transition-colors hover:bg-soft hover:text-primary disabled:opacity-40'

export const BTN_ICON_DANGER =
  'inline-flex size-9 items-center justify-center rounded-lg text-muted transition-colors hover:bg-red-50 hover:text-red-600 disabled:opacity-40'

export const TABLE_HEAD = 'border-b border-line bg-soft/40 text-xs font-medium tracking-wide text-muted uppercase'

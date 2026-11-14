/** Nhanh dần lúc đầu, chậm dần lúc cuối */
const easeInOutCubic = (t) => (t < 0.5 ? 4 * t * t * t : 1 - (-2 * t + 2) ** 3 / 2)

let frame = 0

/**
 * Cuộn mượt tới phần tử (tự làm bằng requestAnimationFrame, không phụ thuộc
 * hiệu ứng cuộn của trình duyệt hay cài đặt "giảm chuyển động" của hệ điều hành).
 * offset: chừa khoảng trống phía trên, vd. chiều cao thanh menu.
 */
export function smoothScrollTo(el, { offset = 80, duration = 800 } = {}) {
  if (!el) return
  cancelAnimationFrame(frame)
  const start = window.scrollY
  const target = Math.max(0, el.getBoundingClientRect().top + start - offset)
  const distance = target - start
  if (Math.abs(distance) < 2) return

  // Khách tự cuộn chuột / chạm màn hình thì dừng hiệu ứng, không "giành" lại thanh cuộn
  const stop = () => cancelAnimationFrame(frame)
  window.addEventListener('wheel', stop, { once: true, passive: true })
  window.addEventListener('touchstart', stop, { once: true, passive: true })

  const begin = performance.now()
  const step = (now) => {
    const t = Math.min(1, (now - begin) / duration)
    window.scrollTo(0, start + distance * easeInOutCubic(t))
    if (t < 1) {
      frame = requestAnimationFrame(step)
    } else {
      window.removeEventListener('wheel', stop)
      window.removeEventListener('touchstart', stop)
    }
  }
  frame = requestAnimationFrame(step)
}

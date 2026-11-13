import { prefersReducedMotion } from '@/composables/useReducedMotion'

// v-reveal hoặc v-reveal="1|2|3" (độ trễ): phần tử hiện dần khi cuộn tới
const HIDDEN = ['opacity-0', 'translate-y-3.5']
const BASE = ['transition-[opacity,translate]', 'duration-500', 'ease-soft']
const DELAYS = ['', 'delay-50', 'delay-100', 'delay-150']

let observer
function getObserver() {
  observer ??= new IntersectionObserver(
    (entries) =>
      entries.forEach((e) => {
        if (!e.isIntersecting) return
        e.target.classList.remove(...HIDDEN)
        observer.unobserve(e.target)
      }),
    { threshold: 0, rootMargin: '0px 0px -40px 0px' },
  )
  return observer
}

export default {
  mounted(el, { value }) {
    if (prefersReducedMotion()) return
    el.classList.add(...BASE, ...HIDDEN)
    if (DELAYS[value]) el.classList.add(DELAYS[value])
    getObserver().observe(el)
  },
  unmounted(el) {
    observer?.unobserve(el)
  },
}

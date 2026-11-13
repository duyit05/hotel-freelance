<script setup>
// Số chạy từ 0 đến `end` khi cuộn tới
import { onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  end: { type: Number, required: true },
  suffix: { type: String, default: '' },
  duration: { type: Number, default: 1400 },
})

const el = ref(null)
const decimals = String(props.end).includes('.') ? 1 : 0
const text = ref(format(0))
let observer, frame

function format(v) {
  return v.toFixed(decimals).replace('.', ',') + props.suffix
}

function run() {
  const t0 = performance.now()
  const step = (t) => {
    const p = Math.min(1, (t - t0) / props.duration)
    text.value = format(props.end * (1 - Math.pow(1 - p, 3)))
    if (p < 1) frame = requestAnimationFrame(step)
  }
  frame = requestAnimationFrame(step)
}

onMounted(() => {
  observer = new IntersectionObserver(([e]) => {
    if (!e.isIntersecting) return
    observer.disconnect()
    run()
  })
  observer.observe(el.value)
})
onBeforeUnmount(() => {
  observer?.disconnect()
  cancelAnimationFrame(frame)
})
</script>

<template>
  <strong ref="el">{{ text }}</strong>
</template>

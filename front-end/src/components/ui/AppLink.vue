<script setup>
// Link nội bộ: như RouterLink, nhưng bấm lại vào link đang mở (vd. "/#phong") vẫn cuộn tới mục đó
import { useRoute, useRouter } from 'vue-router'
import { prefersReducedMotion } from '@/composables/useReducedMotion'

const props = defineProps({ to: { type: [String, Object], required: true } })
const route = useRoute()
const router = useRouter()

function onClick(e, navigate) {
  const target = router.resolve(props.to)
  if (target.fullPath !== route.fullPath) return navigate(e)
  e.preventDefault()
  const behavior = prefersReducedMotion() ? 'instant' : 'smooth'
  const el = target.hash && document.querySelector(target.hash)
  if (el) window.scrollTo({ top: el.getBoundingClientRect().top + window.scrollY - 80, behavior })
  else window.scrollTo({ top: 0, behavior })
}
</script>

<template>
  <RouterLink v-slot="{ href, navigate }" :to="to" custom>
    <a :href="href" @click="onClick($event, navigate)"><slot /></a>
  </RouterLink>
</template>

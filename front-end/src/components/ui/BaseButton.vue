<script setup>
// Nút bo tròn. Truyền `to` (link nội bộ), `href` (link ngoài) hoặc để trống (thẻ <button>)
import { computed } from 'vue'
import AppLink from './AppLink.vue'

const props = defineProps({
  to: { type: [String, Object], default: null },
  href: { type: String, default: null },
  variant: { type: String, default: 'primary' }, // primary | ghost
  size: { type: String, default: 'md' }, // md | sm
  type: { type: String, default: 'button' },
})

const tag = computed(() => (props.to ? AppLink : props.href ? 'a' : 'button'))
const attrs = computed(() => (props.to ? { to: props.to } : props.href ? { href: props.href } : { type: props.type }))

const VARIANTS = {
  primary: 'bg-primary text-white hover:bg-primary-hover',
  ghost: 'border-[1.5px] border-primary text-primary hover:bg-primary hover:text-white',
}
</script>

<template>
  <component
    :is="tag"
    v-bind="attrs"
    :class="[
      'inline-flex items-center justify-center rounded-full font-semibold no-underline transition-[background-color,color,translate] duration-200 hover:-translate-y-0.5',
      size === 'sm' ? 'px-[22px] py-3' : 'px-7 py-3.5',
      VARIANTS[variant],
    ]"
  >
    <slot />
  </component>
</template>

<script setup>
import AppImage from '@/components/ui/AppImage.vue'
import AppLink from '@/components/ui/AppLink.vue'

defineProps({
  image: { type: String, required: true },
  alt: { type: String, default: '' },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
})
</script>

<template>
  <section class="relative flex h-[64vh] min-h-[440px] items-end overflow-hidden text-white">
    <AppImage :src="image" :alt="alt" class="absolute inset-0 animate-slow-zoom" />
    <div
      class="absolute inset-0 bg-[linear-gradient(to_top,rgba(32,25,18,.72),rgba(32,25,18,.15)_60%,rgba(32,25,18,.4))]"
    />
    <div class="wrap relative z-[2] w-full animate-rise pb-14 [animation-delay:.2s]">
      <h1 class="mb-2.5 text-[clamp(36px,5.5vw,64px)]">{{ title }}</h1>
      <p v-if="subtitle" class="max-w-[50ch] text-lg opacity-95">{{ subtitle }}</p>
    </div>
  </section>
</template>

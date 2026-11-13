<script setup>
defineProps({
  items: { type: Array, required: true }, // [{ q, a }]
})
</script>

<template>
  <div class="max-w-[760px]">
    <details v-for="item in items" :key="item.q" class="group border-b border-line">
      <summary
        class="flex cursor-pointer list-none justify-between gap-4 py-5 text-[17px] font-medium [&::-webkit-details-marker]:hidden after:text-[22px] after:leading-none after:text-primary after:transition-transform after:duration-300 after:content-['+'] group-open:after:rotate-45"
      >
        {{ item.q }}
      </summary>
      <p class="animate-view-in-fast pb-5 text-muted">{{ item.a }}</p>
    </details>
  </div>
</template>

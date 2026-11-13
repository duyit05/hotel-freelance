<script setup>
defineProps({
  items: { type: Array, required: true },
})
</script>

<template>
  <ul class="grid gap-x-6 gap-y-2.5 sm:grid-cols-2">
    <li
      v-for="item in items"
      :key="item"
      class="relative pl-[22px] before:absolute before:top-[.65em] before:left-0 before:size-2 before:rounded-full before:bg-accent before:content-['']"
    >
      {{ item }}
    </li>
  </ul>
</template>

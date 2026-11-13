<script setup>
defineProps({
  title: { type: String, required: true },
  items: { type: Array, required: true },
})
</script>

<template>
  <div class="rounded-card bg-surface p-7">
    <h3 class="mb-3.5 text-[22px]">{{ title }}</h3>
    <ul class="text-muted">
      <li
        v-for="item in items"
        :key="item"
        class="relative py-1.5 pl-5 before:absolute before:top-[1.05em] before:left-0 before:h-0.5 before:w-2 before:bg-accent before:content-['']"
      >
        {{ item }}
      </li>
    </ul>
  </div>
</template>

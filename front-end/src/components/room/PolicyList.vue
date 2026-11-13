<script setup>
defineProps({
  items: { type: Array, required: true }, // [{ label, value }]
})
</script>

<template>
  <dl class="grid gap-0.5 sm:grid-cols-[130px_1fr] sm:gap-x-4 sm:gap-y-3">
    <template v-for="p in items" :key="p.label">
      <dt class="text-muted">{{ p.label }}</dt>
      <dd class="mb-2.5 sm:mb-0">{{ p.value }}</dd>
    </template>
  </dl>
</template>

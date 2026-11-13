<script setup>
defineProps({
  title: { type: String, required: true },
  text: { type: String, required: true },
})
</script>

<template>
  <div>
    <h3 class="mb-1.5 border-l-[3px] border-accent pl-3.5 font-sans text-[17px] font-semibold">{{ title }}</h3>
    <p class="pl-[17px] text-[15px] text-muted">{{ text }}</p>
  </div>
</template>

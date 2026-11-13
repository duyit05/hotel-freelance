<script setup>
import { computed } from 'vue'

const props = defineProps({
  room: { type: Object, required: true },
})

const specs = computed(() => [
  { label: 'Diện tích', value: props.room.size },
  { label: 'Sức chứa', value: props.room.guests },
  { label: 'Giường', value: props.room.bed },
  { label: 'Tầm nhìn', value: props.room.view },
])
</script>

<template>
  <div class="mt-7 grid grid-cols-2 gap-px overflow-hidden rounded-card border border-line bg-line md:grid-cols-4">
    <div v-for="s in specs" :key="s.label" class="bg-surface p-4">
      <span class="block text-[13px] text-muted">{{ s.label }}</span>
      <b class="font-semibold">{{ s.value || '—' }}</b>
    </div>
  </div>
</template>

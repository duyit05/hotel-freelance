<script setup>
import SectionTitle from './SectionTitle.vue'
import LeadText from './LeadText.vue'

defineProps({
  title: { type: String, required: true },
  lead: { type: String, default: '' },
})
</script>

<template>
  <div v-reveal class="mb-11">
    <SectionTitle>{{ title }}</SectionTitle>
    <LeadText v-if="lead">{{ lead }}</LeadText>
  </div>
</template>

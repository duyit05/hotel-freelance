<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import ReviewCard from '@/components/ui/ReviewCard.vue'
import { REVIEWS } from '@/data/hotel'
</script>

<template>
  <PageSection>
    <SectionHeading title="Khách nói gì về chúng tôi" />
    <div class="grid gap-7 sm:grid-cols-2 md:grid-cols-3">
      <ReviewCard v-for="(r, i) in REVIEWS" :key="r.author" v-reveal="i" v-bind="r" />
    </div>
  </PageSection>
</template>

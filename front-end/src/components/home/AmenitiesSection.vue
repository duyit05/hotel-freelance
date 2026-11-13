<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import AmenityItem from '@/components/ui/AmenityItem.vue'
import { AMENITIES } from '@/data/hotel'
</script>

<template>
  <PageSection id="tien-ich" soft>
    <SectionHeading title="Tiện ích" lead="Tất cả tiện ích dưới đây đều miễn phí cho khách lưu trú." />
    <div class="grid gap-x-12 gap-y-9 sm:grid-cols-2 md:grid-cols-3">
      <AmenityItem v-for="(a, i) in AMENITIES" :key="a.title" v-reveal="i % 3" v-bind="a" />
    </div>
  </PageSection>
</template>

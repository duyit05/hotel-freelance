<script setup>
// Khối 2 cột: ảnh + tiêu đề, đoạn giới thiệu và danh sách "tên ... giá trị"
import AppImage from '@/components/ui/AppImage.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'

defineProps({
  image: { type: String, required: true },
  alt: { type: String, default: '' },
  title: { type: String, required: true },
  lead: { type: String, default: '' },
  items: { type: Array, default: () => [] }, // [{ label, value }]
  imageRight: { type: Boolean, default: false },
})
</script>

<template>
  <div class="grid items-center gap-9 md:grid-cols-2 md:gap-16">
    <div v-reveal="imageRight ? 1 : 0" :class="['aspect-5/4 overflow-hidden rounded-card', imageRight && 'md:order-2']">
      <AppImage :src="image" :alt="alt" />
    </div>
    <div v-reveal="imageRight ? 0 : 1">
      <SectionTitle>{{ title }}</SectionTitle>
      <LeadText v-if="lead">{{ lead }}</LeadText>
      <ul class="mt-5">
        <li v-for="item in items" :key="item.label" class="flex justify-between gap-4 border-b border-line py-2.5">
          <span>{{ item.label }}</span>
          <span class="text-muted">{{ item.value }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>

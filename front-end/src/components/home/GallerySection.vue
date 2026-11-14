<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import AppLink from '@/components/ui/AppLink.vue'
import AppImage from '@/components/ui/AppImage.vue'
import { GALLERY } from '@/data/hotel'
import { img } from '@/data/images'

// Độ trễ hiệu ứng theo thứ tự trong lưới gốc
const DELAYS = [0, 1, 2, 3, 1, 2]
</script>

<template>
  <PageSection id="hinh-anh">
    <SectionHeading title="Hình ảnh" lead="Một vòng quanh khách sạn và bờ biển Quy Nhơn." />
    <div class="grid auto-rows-[180px] grid-cols-2 gap-3.5 md:auto-rows-[220px] md:grid-cols-4">
      <AppLink
        v-for="(item, i) in GALLERY"
        :key="item.image"
        v-reveal="DELAYS[i]"
        to="/#phong"
        :class="['group block overflow-hidden rounded-card', item.span]"
      >
        <AppImage
          :src="img(item.image)"
          :alt="item.alt"
          class="transition-transform duration-800 ease-soft group-hover:scale-107"
        />
      </AppLink>
    </div>
  </PageSection>
</template>

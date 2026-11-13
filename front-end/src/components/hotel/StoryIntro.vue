<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'
import { STATS } from '@/data/hotel'
</script>

<template>
  <PageSection>
    <div class="grid items-start gap-9 md:grid-cols-[1.3fr_1fr] md:gap-[72px]">
      <div v-reveal>
        <SectionTitle>Câu chuyện của chúng tôi</SectionTitle>
        <LeadText>
          Mộc Lan bắt đầu từ một nhà nghỉ gia đình 12 phòng vào năm 2015. Sau gần mười năm, khách sạn có 48 phòng,
          hồ bơi tầng thượng và nhà hàng riêng, nhưng vẫn giữ cách đón khách như người nhà.
        </LeadText>
        <LeadText class="mt-3.5">
          Đội ngũ 40 nhân viên đều là người Đà Nẵng. Họ có thể chỉ cho bạn quán bún chả cá ngon nhất phố hay giờ
          đẹp nhất để ngắm bình minh trên biển.
        </LeadText>
      </div>
      <div v-reveal="1" class="grid grid-cols-2 gap-px overflow-hidden rounded-card border border-line bg-line">
        <div v-for="s in STATS" :key="s.label" class="bg-surface p-[22px]">
          <strong class="block font-serif text-[28px] text-primary">{{ s.value }}</strong>
          <span class="text-sm text-muted">{{ s.label }}</span>
        </div>
      </div>
    </div>
  </PageSection>
</template>

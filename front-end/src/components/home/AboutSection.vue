<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'
import AppImage from '@/components/ui/AppImage.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import CountUp from '@/components/ui/CountUp.vue'
import { FACTS } from '@/data/hotel'
import { img } from '@/data/images'
</script>

<template>
  <PageSection id="gioi-thieu">
    <div class="grid items-center gap-9 md:grid-cols-[1.1fr_1fr] md:gap-[72px]">
      <div v-reveal class="relative pr-10 pb-10 sm:pr-[60px] sm:pb-[60px]">
        <div class="aspect-4/5 overflow-hidden rounded-card">
          <AppImage :src="img('resort')" alt="Sảnh khách sạn" />
        </div>
        <div class="absolute right-0 bottom-0 aspect-square w-[52%] overflow-hidden rounded-card border-6 border-bg">
          <AppImage :src="img('dining')" alt="Nhà hàng" />
        </div>
      </div>

      <div v-reveal="1">
        <SectionTitle>Một khách sạn nhỏ, chăm chút từng chi tiết</SectionTitle>
        <LeadText>
          Mộc Lan mở cửa từ năm 2015 trên đường Võ Nguyên Giáp. Chúng tôi giữ quy mô vừa phải để mỗi vị khách
          đều được phục vụ chu đáo, từ lúc nhận phòng đến khi ra sân bay.
        </LeadText>
        <div class="mt-8 flex flex-wrap gap-10 border-t border-line pt-6">
          <div v-for="fact in FACTS" :key="fact.label">
            <CountUp :end="fact.value" :suffix="fact.suffix" class="block font-serif text-[30px] text-primary" />
            <span class="text-sm text-muted">{{ fact.label }}</span>
          </div>
        </div>
        <BaseButton to="/khach-san" variant="ghost" class="mt-8">Xem chi tiết khách sạn</BaseButton>
      </div>
    </div>
  </PageSection>
</template>

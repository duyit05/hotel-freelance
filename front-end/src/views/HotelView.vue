<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import PageHero from '@/components/hotel/PageHero.vue'
import StoryIntro from '@/components/hotel/StoryIntro.vue'
import PhotoLibrary from '@/components/hotel/PhotoLibrary.vue'
import InfoSplit from '@/components/hotel/InfoSplit.vue'
import FacilityCard from '@/components/hotel/FacilityCard.vue'
import FaqList from '@/components/hotel/FaqList.vue'
import { DINING_HOURS, FACILITIES, FAQS, NEARBY, PHONE } from '@/data/hotel'
import { img } from '@/data/images'
</script>

<template>
  <main>
    <PageHero
      :image="img('resort')"
      alt="Khách sạn Mộc Lan về đêm"
      title="Về Mộc Lan Hotel"
      subtitle="Khách sạn 4 sao trên đường Võ Nguyên Giáp, nhìn ra biển Mỹ Khê, Đà Nẵng."
    />

    <StoryIntro />

    <PageSection soft class="pt-[88px]!">
      <SectionHeading title="Thư viện ảnh" lead="Bấm vào ảnh để xem cỡ lớn." />
      <PhotoLibrary />
    </PageSection>

    <PageSection>
      <InfoSplit
        :image="img('restaurant')"
        alt="Nhà hàng Mộc Lan"
        title="Nhà hàng và quầy bar"
        lead="Nhà hàng tầng 2 phục vụ hải sản tươi lấy từ chợ Sơn Trà mỗi sáng, cùng các món Âu đơn giản. Quầy bar tầng thượng mở đến khuya, cạnh hồ bơi."
        :items="DINING_HOURS"
      />
    </PageSection>

    <PageSection soft>
      <SectionHeading title="Tiện ích và dịch vụ" />
      <div class="grid gap-7 md:grid-cols-3">
        <FacilityCard v-for="(f, i) in FACILITIES" :key="f.title" v-reveal="i" v-bind="f" />
      </div>
    </PageSection>

    <PageSection>
      <InfoSplit
        :image="img('beach2')"
        alt="Bãi biển gần khách sạn"
        title="Xung quanh khách sạn"
        lead="Khoảng cách tính từ sảnh khách sạn."
        :items="NEARBY"
        image-right
      />
    </PageSection>

    <PageSection soft>
      <SectionHeading title="Câu hỏi thường gặp" />
      <FaqList v-reveal :items="FAQS" />
    </PageSection>

    <PageSection class="text-center">
      <div v-reveal>
        <SectionTitle>Sẵn sàng cho chuyến đi?</SectionTitle>
        <LeadText class="mx-auto mb-7">
          Xem các loại phòng và giá, hoặc gọi {{ PHONE.label }} để được tư vấn.
        </LeadText>
        <BaseButton to="/#phong">Xem các loại phòng</BaseButton>
      </div>
    </PageSection>
  </main>
</template>

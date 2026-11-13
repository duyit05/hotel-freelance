<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'
import { CONTACT, MAP_EMBED } from '@/data/hotel'
</script>

<template>
  <PageSection id="lien-he" soft>
    <div class="grid gap-9 md:grid-cols-2 md:gap-16">
      <div v-reveal>
        <SectionTitle>Liên hệ đặt phòng</SectionTitle>
        <LeadText>Gọi hoặc nhắn Zalo, chúng tôi phản hồi trong vòng 15 phút.</LeadText>
        <dl class="mt-6 grid grid-cols-[110px_1fr] gap-x-4 gap-y-3.5">
          <template v-for="item in CONTACT" :key="item.label">
            <dt class="text-[15px] text-muted">{{ item.label }}</dt>
            <dd class="font-medium">
              <a v-if="item.href" :href="item.href">{{ item.value }}</a>
              <template v-else>{{ item.value }}</template>
            </dd>
          </template>
        </dl>
      </div>
      <div v-reveal="1" class="min-h-[340px] overflow-hidden rounded-card border border-line">
        <!-- Thay MAP_EMBED trong src/data/hotel.js bằng link nhúng Google Maps của khách sạn -->
        <iframe :src="MAP_EMBED" loading="lazy" title="Bản đồ vị trí khách sạn" class="size-full min-h-[340px] border-0" />
      </div>
    </div>
  </PageSection>
</template>

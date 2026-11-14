<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { ADDRESS, CONTACT, MAP_EMBED, MAP_URL, PHONES, ZALO_URL } from '@/data/hotel'

// Địa chỉ và số điện thoại đã nằm trên thẻ, phần dưới chỉ hiện các mục còn lại (email, giờ nhận/trả phòng)
const extra = CONTACT.filter((item) => item.label !== 'Địa chỉ' && !item.href?.startsWith('tel:'))
</script>

<template>
  <PageSection id="lien-he" soft>
    <div class="grid gap-9 md:grid-cols-2 md:gap-16">
      <div v-reveal>
        <SectionTitle>Liên hệ đặt phòng</SectionTitle>
        <LeadText>Gọi hoặc nhắn Zalo, chúng tôi phản hồi trong vòng 15 phút.</LeadText>

        <!-- Thẻ thông tin theo mẫu thẻ phòng của khách sạn -->
        <div
          class="relative mt-7 overflow-hidden rounded-[18px] bg-[linear-gradient(150deg,#E6C877_0%,#D6B25A_45%,#C99F45_100%)] px-7 pt-8 pb-24 text-center text-[#4A2F17] shadow-[0_24px_44px_-26px_rgba(74,47,23,.75)] sm:px-10"
        >
          <p class="font-serif text-[clamp(30px,4.2vw,40px)] leading-none font-semibold tracking-[.04em] uppercase">
            Duy Nhất
          </p>
          <p class="mt-3 flex items-center justify-center gap-4 text-sm font-semibold tracking-[.4em]">
            <span class="h-px w-12 bg-current opacity-60" aria-hidden="true" />
            HOTEL
            <span class="h-px w-12 bg-current opacity-60" aria-hidden="true" />
          </p>

          <a
            :href="MAP_URL"
            target="_blank"
            rel="noopener"
            class="mt-7 flex flex-col items-center gap-1.5 font-semibold text-inherit no-underline hover:underline"
          >
            <svg viewBox="0 0 24 24" class="size-5" fill="currentColor" aria-hidden="true">
              <path d="M12 2a7 7 0 0 0-7 7c0 5.2 7 13 7 13s7-7.8 7-13a7 7 0 0 0-7-7m0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5" />
            </svg>
            <span>{{ ADDRESS.street }},<br />{{ ADDRESS.area }}</span>
          </a>

          <ul class="mt-4 inline-flex flex-col gap-1 text-left font-semibold">
            <li v-for="(p, i) in PHONES" :key="p.tel" class="flex items-center gap-2.5">
              <svg
                viewBox="0 0 24 24"
                :class="['size-[18px]', i ? 'invisible' : '']"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  d="M6.6 10.8a15.1 15.1 0 0 0 6.6 6.6l2.2-2.2a1 1 0 0 1 1-.25 11.4 11.4 0 0 0 3.6.57 1 1 0 0 1 1 1V20a1 1 0 0 1-1 1A17 17 0 0 1 3 4a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1c0 1.25.2 2.45.57 3.57a1 1 0 0 1-.25 1z"
                />
              </svg>
              <a :href="`tel:${p.tel}`" class="text-inherit no-underline hover:underline">{{ p.label }}</a>
            </li>
          </ul>

          <!-- Họa tiết sóng biển -->
          <svg
            viewBox="0 0 400 110"
            preserveAspectRatio="none"
            class="pointer-events-none absolute inset-x-0 bottom-0 h-24 w-full text-[#5A3A1C]"
            fill="none"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path d="M0 40c60-30 120-30 170 5s90 40 130 15 70-30 100-20" stroke-width="2.2" opacity=".75" />
            <path d="M0 55c55-28 115-26 165 6s95 38 135 14 70-28 100-18" stroke-width="1.6" opacity=".55" />
            <path d="M0 70c50-25 110-22 160 8s100 36 140 12 70-25 100-15" stroke-width="1.3" opacity=".45" />
            <path d="M0 85c50-22 105-18 155 10s105 32 145 10 70-22 100-12" stroke-width="1" opacity=".35" />
            <path d="M0 100c50-18 100-14 150 12s110 28 150 8 70-18 100-8" stroke-width=".8" opacity=".3" />
          </svg>
        </div>

        <div class="mt-5 flex flex-wrap gap-2.5">
          <BaseButton :href="`tel:${PHONES[1].tel}`" size="sm">Gọi {{ PHONES[1].label }}</BaseButton>
          <BaseButton :href="ZALO_URL" variant="ghost" size="sm" target="_blank" rel="noopener">Nhắn Zalo</BaseButton>
        </div>
      </div>
      <div v-reveal="1" class="min-h-[340px] overflow-hidden rounded-card border border-line">
        <!-- Đổi địa chỉ bản đồ ở MAP_EMBED trong src/data/hotel.js -->
        <iframe :src="MAP_EMBED" loading="lazy" title="Bản đồ vị trí khách sạn" class="size-full min-h-[340px] border-0" />
      </div>
    </div>
  </PageSection>
</template>

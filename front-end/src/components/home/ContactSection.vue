<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionTitle from '@/components/ui/SectionTitle.vue'
import LeadText from '@/components/ui/LeadText.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { ADDRESS, CONTACT, EMAIL, MAP_EMBED, MAP_URL, PHONES, ZALO_URL } from '@/data/hotel'

// Danh sách thông tin liên hệ — sửa nội dung trong src/data/hotel.js
const info = [
  { label: 'Địa chỉ', values: [{ text: ADDRESS.full, href: MAP_URL, external: true }] },
  { label: 'Điện thoại', values: PHONES.map((p) => ({ text: p.label })) },
  { label: 'Email', values: [{ text: EMAIL }] },
  // Giờ nhận/trả phòng
  ...CONTACT.filter((c) => !c.href).map((c) => ({ label: c.label, values: [{ text: c.value }] })),
]
</script>

<template>
  <PageSection id="lien-he" soft>
    <div class="grid gap-9 md:grid-cols-2 md:gap-16">
      <div v-reveal>
        <SectionTitle>Liên hệ đặt phòng</SectionTitle>
        <LeadText>Gọi hoặc nhắn Zalo, chúng tôi phản hồi trong vòng 15 phút.</LeadText>

        <!--
          Mỗi thông tin luôn nằm trên 1 dòng: chữ tự co theo bề rộng khung (container query).
          Khung hẹp (< 34rem): nhãn nằm trên, nội dung bên dưới; khung rộng: nhãn bên trái.
        -->
        <dl class="@container mt-7 divide-y divide-line border-y border-line">
          <div
            v-for="item in info"
            :key="item.label"
            class="grid gap-1 py-3.5 @min-[34rem]:grid-cols-[120px_1fr] @min-[34rem]:gap-4"
          >
            <dt class="text-sm text-muted">{{ item.label }}</dt>
            <dd
              class="font-medium whitespace-nowrap text-ink text-[clamp(12px,calc(100cqw/26),16px)] @min-[34rem]:text-[clamp(14px,calc((100cqw-136px)/26),16px)]"
            >
              <template v-for="v in item.values" :key="v.text">
                <a
                  v-if="v.href"
                  :href="v.href"
                  :target="v.external ? '_blank' : undefined"
                  :rel="v.external ? 'noopener' : undefined"
                  class="block text-inherit no-underline hover:underline"
                  >{{ v.text }}</a
                >
                <span v-else class="block">{{ v.text }}</span>
              </template>
            </dd>
          </div>
        </dl>

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

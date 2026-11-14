<script setup>
import AppLink from '@/components/ui/AppLink.vue'
import AppImage from '@/components/ui/AppImage.vue'
import PriceTag from '@/components/ui/PriceTag.vue'
import { roomMeta } from '@/utils/format'

defineProps({
  room: { type: Object, required: true },
  compact: { type: Boolean, default: false }, // bản rút gọn ở mục "Các phòng khác"
})
</script>

<template>
  <AppLink
    :to="`/phong/${room.slug}`"
    class="group flex flex-col overflow-hidden rounded-card bg-surface no-underline transition-[translate,box-shadow] duration-350 ease-soft hover:-translate-y-1.5 hover:shadow-[0_24px_40px_-24px_rgba(30,35,45,.18)]"
  >
    <div class="aspect-4/3 overflow-hidden">
      <AppImage
        :src="room.images[0]"
        :alt="room.name"
        class="transition-transform duration-800 ease-soft group-hover:scale-107"
      />
    </div>
    <div class="flex flex-1 flex-col gap-2 px-6 pt-[22px] pb-[26px]">
      <span v-if="room.type" class="text-xs font-semibold tracking-wide text-primary uppercase">
        {{ room.type.name }}
      </span>
      <h3 class="text-[23px]">{{ room.name }}</h3>
      <div class="text-sm text-muted">{{ roomMeta(room, !compact) }}</div>
      <p v-if="!compact" class="text-sm text-muted">{{ room.shortDescription }}</p>
      <div class="mt-auto flex items-center justify-between border-t border-line pt-3.5">
        <PriceTag :price="room.price" />
        <span class="text-[15px] font-semibold text-primary">Xem chi tiết</span>
      </div>
    </div>
  </AppLink>
</template>

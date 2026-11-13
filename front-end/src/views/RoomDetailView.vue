<script setup>
import { computed, ref } from 'vue'
import AppLink from '@/components/ui/AppLink.vue'
import AmenityItem from '@/components/ui/AmenityItem.vue'
import PriceTag from '@/components/ui/PriceTag.vue'
import ReviewCard from '@/components/ui/ReviewCard.vue'
import RoomCard from '@/components/room/RoomCard.vue'
import RoomViewer from '@/components/room/RoomViewer.vue'
import RoomSpecs from '@/components/room/RoomSpecs.vue'
import FeatureList from '@/components/room/FeatureList.vue'
import PolicyList from '@/components/room/PolicyList.vue'
import RoomBookBox from '@/components/room/RoomBookBox.vue'
import MobileBookBar from '@/components/room/MobileBookBar.vue'
import { POLICIES, ROOMS, findRoom } from '@/data/rooms'
import { img } from '@/data/images'

const props = defineProps({
  id: { type: String, required: true },
})

// Route đã kiểm tra id hợp lệ (xem router/index.js)
const room = computed(() => findRoom(props.id))
// Chỉ gợi ý 3 phòng khác để lưới gọn một hàng
const others = computed(() => ROOMS.filter((r) => r.id !== props.id).slice(0, 3))
const photos = computed(() => room.value.images.map((k, i) => ({ src: img(k), alt: `${room.value.name}, ảnh ${i + 1}` })))

const bookBox = ref(null)
const h2 = 'mt-12 mb-4 text-[26px] first:mt-0'
</script>

<template>
  <main class="pt-[104px] pb-24">
    <div class="wrap animate-view-in">
      <nav aria-label="Đường dẫn" class="mb-3.5 flex flex-wrap gap-2 text-sm text-muted">
        <AppLink to="/" class="no-underline underline-offset-4 hover:text-primary hover:underline">Trang chủ</AppLink>
        <span aria-hidden="true">/</span>
        <AppLink to="/#phong" class="no-underline underline-offset-4 hover:text-primary hover:underline">Phòng</AppLink>
        <span aria-hidden="true">/</span>
        <span aria-current="page" class="text-ink">{{ room.name }}</span>
      </nav>

      <div class="mb-7 flex flex-wrap items-end justify-between gap-5">
        <div>
          <h1 class="text-[clamp(32px,4.5vw,52px)]">{{ room.name }}</h1>
          <p class="text-muted">{{ room.size }}, {{ room.guests }}, {{ room.view.toLowerCase() }}</p>
        </div>
        <PriceTag :price="room.price" />
      </div>

      <RoomViewer :photos="photos" />

      <div class="grid items-start gap-9 md:grid-cols-[1.6fr_1fr] md:gap-16">
        <div>
          <h2 :class="h2">Giới thiệu phòng</h2>
          <p class="max-w-[62ch] text-[17px] text-muted">{{ room.desc }}</p>
          <RoomSpecs :room="room" />
          <div class="mt-8 grid gap-4 md:grid-cols-3 md:gap-6">
            <AmenityItem v-for="h in room.highlights" :key="h.title" v-bind="h" />
          </div>

          <h2 :class="h2">Tiện nghi trong phòng</h2>
          <FeatureList :items="room.features" />

          <h2 :class="h2">Khách đã ở phòng này</h2>
          <div class="grid gap-4 md:grid-cols-2 md:gap-5">
            <ReviewCard v-for="r in room.reviews" :key="r.author" v-bind="r" compact />
          </div>

          <h2 :class="h2">Chính sách</h2>
          <PolicyList :items="POLICIES" />
        </div>

        <RoomBookBox ref="bookBox" :room="room" />
      </div>

      <h2 class="mt-[88px] mb-7 text-[30px]">Các phòng khác</h2>
      <div class="grid grid-cols-[repeat(auto-fit,minmax(260px,1fr))] gap-7">
        <RoomCard v-for="o in others" :key="o.id" :room="o" compact />
      </div>
    </div>

    <MobileBookBar :price="room.price" @book="bookBox.focus()" />
  </main>
</template>

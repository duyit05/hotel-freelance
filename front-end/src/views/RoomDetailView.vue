<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
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
import { roomApi } from '@/api/rooms'
import { useRooms } from '@/composables/useRooms'
import { POLICIES } from '@/data/hotel'
import { roomMeta } from '@/utils/format'

const props = defineProps({
  slug: { type: String, required: true },
})

const router = useRouter()
const room = ref(null)
const error = ref('')
const { rooms, loadRooms } = useRooms()

roomApi
  .getBySlug(props.slug)
  .then((data) => (room.value = data))
  .catch((e) => {
    if (e.status === 404) router.replace('/')
    else error.value = e.message
  })
loadRooms()

// Chỉ gợi ý 3 phòng khác để lưới gọn một hàng
const others = computed(() => rooms.value.filter((r) => r.slug !== props.slug).slice(0, 3))
const photos = computed(() =>
  (room.value?.images ?? []).map((src, i) => ({ src, alt: `${room.value.name}, ảnh ${i + 1}` })),
)

const bookBox = ref(null)
const h2 = 'mt-12 mb-4 text-[26px] first:mt-0'
</script>

<template>
  <main class="pt-[104px] pb-24">
    <div v-if="error" class="wrap py-20 text-center text-muted">
      <p class="mb-4">{{ error }}</p>
      <AppLink to="/#phong" class="font-semibold text-primary">Quay lại danh sách phòng</AppLink>
    </div>

    <div v-else-if="!room" class="wrap">
      <div class="mb-7 h-12 w-2/3 animate-pulse rounded-card bg-soft" />
      <div class="h-[540px] animate-pulse rounded-card bg-soft" />
    </div>

    <template v-else>
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
            <p class="text-muted">{{ roomMeta(room) }}</p>
          </div>
          <PriceTag :price="room.price" />
        </div>

        <RoomViewer v-if="photos.length" :photos="photos" />

        <div class="grid items-start gap-9 md:grid-cols-[1.6fr_1fr] md:gap-16">
          <div>
            <h2 :class="h2">Giới thiệu phòng</h2>
            <p class="max-w-[62ch] text-[17px] whitespace-pre-line text-muted">{{ room.description }}</p>
            <RoomSpecs :room="room" />
            <div v-if="room.highlights.length" class="mt-8 grid gap-4 md:grid-cols-3 md:gap-6">
              <AmenityItem v-for="(h, i) in room.highlights" :key="i" v-bind="h" />
            </div>

            <template v-if="room.features.length">
              <h2 :class="h2">Tiện nghi trong phòng</h2>
              <FeatureList :items="room.features" />
            </template>

            <template v-if="room.reviews.length">
              <h2 :class="h2">Khách đã ở phòng này</h2>
              <div class="grid gap-4 md:grid-cols-2 md:gap-5">
                <ReviewCard v-for="(r, i) in room.reviews" :key="i" v-bind="r" compact />
              </div>
            </template>

            <h2 :class="h2">Chính sách</h2>
            <PolicyList :items="POLICIES" />
          </div>

          <RoomBookBox ref="bookBox" :room="room" />
        </div>

        <template v-if="others.length">
          <h2 class="mt-[88px] mb-7 text-[30px]">Các phòng khác</h2>
          <div class="grid grid-cols-[repeat(auto-fit,minmax(260px,1fr))] gap-7">
            <RoomCard v-for="o in others" :key="o.id" :room="o" compact />
          </div>
        </template>
      </div>

      <MobileBookBar :price="room.price" @book="bookBox.focus()" />
    </template>
  </main>
</template>

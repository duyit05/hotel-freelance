<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import RoomCard from '@/components/room/RoomCard.vue'
import { ROOMS } from '@/data/rooms'
</script>

<template>
  <PageSection id="phong" soft>
    <SectionHeading
      title="Các loại phòng"
      lead="Giá đã gồm bữa sáng và thuế phí. Chọn một phòng để xem chi tiết."
    />
    <div class="grid gap-7 sm:grid-cols-2 md:grid-cols-3">
      <RoomCard v-for="(room, i) in ROOMS" :key="room.id" v-reveal="i % 3" :room="room" />
    </div>
  </PageSection>
</template>

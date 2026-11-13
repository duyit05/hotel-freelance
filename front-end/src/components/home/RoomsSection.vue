<script setup>
import PageSection from '@/components/ui/PageSection.vue'
import SectionHeading from '@/components/ui/SectionHeading.vue'
import RoomCard from '@/components/room/RoomCard.vue'
import { useRooms } from '@/composables/useRooms'

const { rooms, loading, error, loadRooms } = useRooms()
loadRooms()
</script>

<template>
  <PageSection id="phong" soft>
    <SectionHeading
      title="Các loại phòng"
      lead="Giá đã gồm bữa sáng và thuế phí. Chọn một phòng để xem chi tiết."
    />

    <p v-if="error && !rooms.length" class="text-muted">
      Chưa tải được danh sách phòng. {{ error }}
      <button type="button" class="ml-1 font-semibold text-primary underline" @click="loadRooms({ force: true })">
        Thử lại
      </button>
    </p>

    <div v-else class="grid gap-7 sm:grid-cols-2 md:grid-cols-3">
      <template v-if="loading && !rooms.length">
        <div v-for="n in 3" :key="n" class="h-[420px] animate-pulse rounded-card bg-surface" />
      </template>
      <RoomCard v-for="(room, i) in rooms" :key="room.id" v-reveal="i % 3" :room="room" />
    </div>
  </PageSection>
</template>

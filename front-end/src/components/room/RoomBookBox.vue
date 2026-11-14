<script setup>
// Hộp đặt phòng: chọn ngày, số khách và tự tính tổng tiền
import { computed, ref, watch } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormField from '@/components/ui/FormField.vue'
import PriceTag from '@/components/ui/PriceTag.vue'
import { useBooking } from '@/composables/useBooking'
import { PHONE, ZALO_URL } from '@/data/hotel'
import { addDays, money, nightsBetween, todayISO } from '@/utils/format'

const props = defineProps({
  room: { type: Object, required: true },
})

// Lấy ngày khách đã chọn ở trang chủ nếu hợp lệ
const booking = useBooking()
const today = todayISO()
const valid = booking.checkout.value > booking.checkin.value && booking.checkin.value >= today
const checkin = ref(valid ? booking.checkin.value : today)
const checkout = ref(valid ? booking.checkout.value : addDays(today, 1))
const guests = ref(Math.min(2, props.room.maxGuests))

const minCheckout = computed(() => addDays(checkin.value, 1))
watch(checkin, () => {
  if (!checkout.value || checkout.value <= checkin.value) checkout.value = minCheckout.value
})

const nights = computed(() => Math.max(1, nightsBetween(checkin.value, checkout.value) || 1))
const total = computed(() => nights.value * props.room.price)

const root = ref(null)
const firstInput = ref(null)
defineExpose({
  focus() {
    root.value.scrollIntoView({ behavior: 'smooth', block: 'center' })
    firstInput.value.focus({ preventScroll: true })
  },
})

const field = 'mt-3 rounded-lg border border-line px-3.5 py-2.5 focus-within:border-primary'
</script>

<template>
  <aside
    ref="root"
    aria-label="Đặt phòng"
    class="rounded-card border border-line bg-surface p-7 md:sticky md:top-24"
  >
    <PriceTag :price="room.price" large />

    <FormField id="dIn" v-slot="{ inputClass }" label="Nhận phòng" :class="field">
      <input id="dIn" ref="firstInput" v-model="checkin" type="date" :min="today" :class="inputClass" />
    </FormField>
    <FormField id="dOut" v-slot="{ inputClass }" label="Trả phòng" :class="field">
      <input id="dOut" v-model="checkout" type="date" :min="minCheckout" :class="inputClass" />
    </FormField>
    <FormField id="dGuests" v-slot="{ inputClass }" label="Số khách" :class="field">
      <select id="dGuests" v-model="guests" :class="inputClass">
        <option v-for="n in room.maxGuests" :key="n" :value="n">{{ n }} người lớn</option>
      </select>
    </FormField>

    <div class="mt-5 grid gap-2 text-[15px] text-muted">
      <div class="flex justify-between gap-3">
        <span>{{ money(room.price) }} × {{ nights }} đêm</span>
        <span>{{ money(total) }}</span>
      </div>
      <div class="flex justify-between gap-3">
        <span>Bữa sáng, thuế và phí</span>
        <span>Đã gồm</span>
      </div>
    </div>
    <div class="mt-4 mb-5 flex justify-between border-t border-line pt-4 text-lg font-semibold">
      <span>Tổng cộng</span>
      <span>{{ money(total) }}</span>
    </div>

    <BaseButton :href="`tel:${PHONE.tel}`" variant="ghost" class="w-full">Gọi đặt phòng</BaseButton>
    <BaseButton :href="ZALO_URL" variant="ghost" class="mt-2.5 w-full" target="_blank" rel="noopener">
      Nhắn Zalo
    </BaseButton>
    <small class="mt-3.5 block text-center text-sm text-muted">Lễ tân phản hồi trong vòng 15 phút</small>
  </aside>
</template>

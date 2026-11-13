<script setup>
import { ref } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import FormField from '@/components/ui/FormField.vue'
import { useBooking } from '@/composables/useBooking'
import { GUEST_OPTIONS, PHONE } from '@/data/hotel'
import { addDays, nightsBetween, todayISO } from '@/utils/format'

const { checkin, checkout } = useBooking()
const guests = ref(GUEST_OPTIONS[1])
const note = ref('')
const today = todayISO()

function check() {
  const n = nightsBetween(checkin.value, checkout.value)
  note.value =
    n < 1
      ? 'Ngày trả phòng phải sau ngày nhận phòng. Vui lòng chọn lại.'
      : `Còn phòng cho ${n} đêm. Xem các loại phòng bên dưới hoặc gọi ${PHONE.label} để giữ phòng.`
}

const field = 'px-4 py-2 border-line'
</script>

<template>
  <div>
    <div
      role="group"
      aria-label="Kiểm tra phòng trống"
      class="grid grid-cols-2 items-center gap-1 rounded-card bg-surface p-2 text-ink shadow-[0_20px_40px_-20px_rgba(0,0,0,.35)] md:grid-cols-[repeat(3,1fr)_auto]"
    >
      <FormField id="checkin" v-slot="{ inputClass }" label="Nhận phòng" :class="[field, 'border-r']">
        <input id="checkin" v-model="checkin" type="date" :min="today" :class="inputClass" />
      </FormField>
      <FormField id="checkout" v-slot="{ inputClass }" label="Trả phòng" :class="[field, 'md:border-r']">
        <input id="checkout" v-model="checkout" type="date" :min="addDays(today, 1)" :class="inputClass" />
      </FormField>
      <FormField
        id="guests"
        v-slot="{ inputClass }"
        label="Số khách"
        :class="[field, 'col-span-full border-t md:col-span-1 md:border-t-0 md:border-r']"
      >
        <select id="guests" v-model="guests" :class="inputClass">
          <option v-for="opt in GUEST_OPTIONS" :key="opt">{{ opt }}</option>
        </select>
      </FormField>
      <BaseButton class="col-span-full md:col-span-1" @click="check">Kiểm tra phòng</BaseButton>
    </div>
    <p class="mt-3 min-h-6 text-[15px]" aria-live="polite">{{ note }}</p>
  </div>
</template>

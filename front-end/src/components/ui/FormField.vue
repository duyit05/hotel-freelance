<script setup>
// Ô nhập có nhãn nhỏ phía trên. Đặt <input>/<select> vào slot và dùng `inputClass` từ slot props.
defineProps({
  label: { type: String, required: true },
  id: { type: String, required: true },
})

const inputClass = 'w-full border-none bg-transparent font-medium text-ink outline-none focus-visible:outline-none'
</script>

<template>
  <div>
    <label :for="id" class="block text-[13px] text-muted">{{ label }}</label>
    <slot :input-class="inputClass" />
  </div>
</template>

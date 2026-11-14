<script setup>
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LightboxViewer from '@/components/ui/LightboxViewer.vue'
import FloatingContact from '@/components/layout/FloatingContact.vue'

const route = useRoute()
</script>

<template>
  <!-- Chờ router xác định trang xong mới hiển thị, tránh nháy giao diện sai -->
  <template v-if="route.matched.length">
    <!-- Khu quản trị có bố cục riêng -->
    <RouterView v-if="route.meta.admin" />

    <template v-else>
      <AppHeader />
      <RouterView :key="route.path" />
      <!-- Chừa chỗ cho thanh "Đặt phòng" cố định ở đáy màn hình điện thoại -->
      <AppFooter :class="route.name === 'room' && 'max-md:pb-24'" />
      <LightboxViewer />
      <FloatingContact />
    </template>
  </template>
</template>

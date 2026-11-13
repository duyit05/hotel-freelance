<script setup>
import { useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import LightboxViewer from '@/components/ui/LightboxViewer.vue'

const route = useRoute()
</script>

<template>
  <AppHeader />
  <RouterView :key="route.path" />
  <!-- Chừa chỗ cho thanh "Đặt phòng" cố định ở đáy màn hình điện thoại -->
  <AppFooter :class="route.name === 'room' && 'max-md:pb-24'" />
  <LightboxViewer />
</template>

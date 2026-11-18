<script setup>
// Nút Gọi điện + Zalo + Facebook nổi ở góc phải dưới, hiện trên mọi trang khách.
// Trang chi tiết phòng trên điện thoại có thanh "Đặt phòng" ở đáy -> đẩy nút lên cao hơn.
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { FACEBOOK_URL, PHONES, ZALO_URL } from '@/data/hotel'

const route = useRoute()
const raised = computed(() => route.name === 'room')

// Số di động (cùng số với nút "Gọi" ở mục Liên hệ)
const CALL_PHONE = PHONES[1]

const ITEMS = [
  {
    key: 'phone',
    href: `tel:${CALL_PHONE.tel}`,
    label: `Gọi ${CALL_PHONE.label}`,
    aria: `Gọi điện cho khách sạn: ${CALL_PHONE.label}`,
    color: '#0068FF',
  },
  { key: 'zalo', href: ZALO_URL, label: 'Chat Zalo', aria: 'Nhắn Zalo cho khách sạn', color: '#0068FF' },
  { key: 'facebook', href: FACEBOOK_URL, label: 'Facebook', aria: 'Trang Facebook của khách sạn', color: '#0866FF' },
]
</script>

<template>
  <div :class="['fc', raised && 'fc--raised']">
    <a
      v-for="item in ITEMS"
      :key="item.key"
      :href="item.href"
      :target="item.key === 'phone' ? undefined : '_blank'"
      :rel="item.key === 'phone' ? undefined : 'noopener'"
      :class="['fc-item', `fc-item--${item.key}`]"
      :style="{ '--brand-c': item.color }"
      :aria-label="item.aria"
    >
      <span class="fc-label" aria-hidden="true">{{ item.label }}</span>
      <span class="fc-btn">
        <span class="fc-ring" aria-hidden="true" />

        <!-- Icon điện thoại: ống nghe trắng, rung nhẹ -->
        <svg v-if="item.key === 'phone'" viewBox="0 0 24 24" class="fc-icon fc-icon--phone" aria-hidden="true">
          <path
            fill="#fff"
            d="M6.6 10.8a15.1 15.1 0 0 0 6.6 6.6l2.2-2.2c.3-.3.7-.4 1-.2 1.1.4 2.3.6 3.6.6.6 0 1 .4 1 1V20c0 .6-.4 1-1 1A17 17 0 0 1 3 4c0-.6.4-1 1-1h3.5c.6 0 1 .4 1 1 0 1.2.2 2.4.6 3.6.1.3 0 .7-.2 1z"
          />
        </svg>

        <!-- Logo Zalo: bong bóng chat trắng, chữ Zalo xanh -->
        <svg v-else-if="item.key === 'zalo'" viewBox="0 0 48 48" class="fc-icon" aria-hidden="true">
          <path
            fill="#fff"
            d="M19 11h10c6.1 0 11 4.6 11 10.3S35.1 31.6 29 31.6h-6.6l-6.9 4.9c-.7.5-1.6-.1-1.4-.9l1.1-4.6C11.4 29.4 8 25.7 8 21.3 8 15.6 12.9 11 19 11Z"
          />
          <text
            x="24"
            y="25.2"
            text-anchor="middle"
            fill="#0068FF"
            font-family="Arial, Helvetica, sans-serif"
            font-size="10.5"
            font-weight="700"
            letter-spacing="-.3"
          >Zalo</text>
        </svg>

        <!-- Logo Facebook: chữ f trắng -->
        <svg v-else viewBox="0 0 24 24" class="fc-icon fc-icon--fb" aria-hidden="true">
          <path
            fill="#fff"
            d="M13.6 22v-8.2h2.8l.4-3.3h-3.2V8.4c0-.9.3-1.6 1.6-1.6h1.7V3.9c-.3 0-1.3-.1-2.5-.1-2.5 0-4.2 1.5-4.2 4.3v2.4H7.4v3.3h2.8V22z"
          />
        </svg>
      </span>
    </a>
  </div>
</template>

<style scoped>
.fc {
  position: fixed;
  right: 18px;
  bottom: 22px;
  z-index: 30;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 14px;
}
/* Trang phòng trên điện thoại: chừa chỗ cho thanh "Đặt phòng" */
@media (max-width: 919.98px) {
  .fc--raised { bottom: 92px; }
}
@media (max-width: 559.98px) {
  .fc { right: 14px; gap: 12px; }
}

.fc-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
}

.fc-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 999px;
  background: var(--brand-c);
  border: 3px solid #fff;
  box-shadow:
    0 8px 20px -8px color-mix(in srgb, var(--brand-c) 70%, transparent),
    0 2px 6px rgb(0 0 0 / 0.12);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.fc-item:hover .fc-btn,
.fc-item:focus-visible .fc-btn {
  transform: scale(1.08);
}
.fc-item:focus-visible { outline: none; }
.fc-item:focus-visible .fc-btn { outline: 2px solid var(--brand-c); outline-offset: 3px; }

.fc-icon { width: 46px; height: 46px; }
.fc-icon--fb { width: 30px; height: 30px; }

/* Ống nghe rung nhẹ theo nhịp để gây chú ý */
.fc-icon--phone {
  width: 26px;
  height: 26px;
  animation: fc-shake 2.2s ease-in-out infinite;
}
@keyframes fc-shake {
  0%, 60%, 100% { transform: rotate(0); }
  65%, 75%, 85% { transform: rotate(-14deg); }
  70%, 80%, 90% { transform: rotate(14deg); }
}
@media (prefers-reduced-motion: reduce) {
  .fc-icon--phone,
  .fc-ring { animation: none; }
}

/* Vòng sóng lan ra quanh mọi nút để gây chú ý */
.fc-ring {
  position: absolute;
  inset: -3px;
  border-radius: inherit;
  border: 2px solid var(--brand-c);
  animation: fc-pulse 2.2s ease-out infinite;
  pointer-events: none;
}
@keyframes fc-pulse {
  0% { transform: scale(1); opacity: 0.7; }
  80%, 100% { transform: scale(1.55); opacity: 0; }
}

/* Nhãn hiện khi rê chuột (chỉ trên máy tính) */
.fc-label {
  padding: 7px 12px;
  border-radius: 999px;
  background: #fff;
  color: #1f2937;
  font-size: 13.5px;
  font-weight: 600;
  white-space: nowrap;
  box-shadow: 0 6px 18px -8px rgb(0 0 0 / 0.3), 0 0 0 1px rgb(0 0 0 / 0.04);
  opacity: 0;
  transform: translateX(8px);
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
}
@media (hover: hover) {
  .fc-item:hover .fc-label,
  .fc-item:focus-visible .fc-label {
    opacity: 1;
    transform: none;
  }
}
@media (hover: none) {
  .fc-label { display: none; }
}
</style>

<script setup>
// Phân trang: <AppPagination v-model="page" :total="items.length" :page-size="6" />
// Ẩn khi chỉ có 1 trang. Nhiều trang thì rút gọn: 1 … 4 5 6 … 12
import { computed } from 'vue'

const page = defineModel({ type: Number, required: true })
const props = defineProps({
  total: { type: Number, required: true },
  pageSize: { type: Number, required: true },
  label: { type: String, default: 'Phân trang' },
})

const pageCount = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)))

const items = computed(() => {
  const n = pageCount.value
  const cur = page.value
  if (n <= 7) return Array.from({ length: n }, (_, i) => i + 1)
  const pages = new Set([1, n, cur - 1, cur, cur + 1].filter((p) => p >= 1 && p <= n))
  if (cur <= 3) [2, 3, 4].forEach((p) => pages.add(p))
  if (cur >= n - 2) [n - 3, n - 2, n - 1].forEach((p) => pages.add(p))
  const sorted = [...pages].sort((a, b) => a - b)
  // Chèn "…" vào chỗ bị ngắt quãng
  return sorted.flatMap((p, i) => (i && p - sorted[i - 1] > 1 ? [`gap-${p}`, p] : [p]))
})

const go = (p) => (page.value = Math.min(pageCount.value, Math.max(1, p)))

const BTN =
  'inline-flex h-10 min-w-10 items-center justify-center rounded-full px-3 text-[15px] transition-colors disabled:pointer-events-none disabled:opacity-35'
</script>

<template>
  <nav v-if="pageCount > 1" :aria-label="label" class="flex flex-wrap items-center justify-center gap-1.5">
    <button type="button" :class="[BTN, 'gap-1 hover:bg-soft']" :disabled="page <= 1" @click="go(page - 1)">
      <span aria-hidden="true">‹</span> Trước
    </button>
    <template v-for="item in items" :key="item">
      <span v-if="typeof item === 'string'" class="px-1 text-muted" aria-hidden="true">…</span>
      <button
        v-else
        type="button"
        :class="[BTN, item === page ? 'bg-primary font-semibold text-white' : 'text-ink hover:bg-soft']"
        :aria-current="item === page ? 'page' : undefined"
        :aria-label="`Trang ${item}`"
        @click="go(item)"
      >
        {{ item }}
      </button>
    </template>
    <button type="button" :class="[BTN, 'gap-1 hover:bg-soft']" :disabled="page >= pageCount" @click="go(page + 1)">
      Sau <span aria-hidden="true">›</span>
    </button>
  </nav>
</template>

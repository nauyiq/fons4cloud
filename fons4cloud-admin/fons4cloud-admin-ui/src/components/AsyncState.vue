<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    loading?: boolean
    empty?: boolean
    forbidden?: boolean
    dependencyUnavailable?: boolean
    title?: string
    description?: string
    requestId?: string
  }>(),
  {
    loading: false,
    empty: false,
    forbidden: false,
    dependencyUnavailable: false,
    title: '',
    description: '',
    requestId: '',
  },
)

const state = computed(() => {
  if (props.forbidden) return { status: '403', title: props.title || '无权访问', description: props.description || '当前账号缺少所需权限。' }
  if (props.dependencyUnavailable) return { status: '500', title: props.title || '依赖暂不可用', description: props.description || '无法确认真实数据，请稍后重试。' }
  if (props.empty) return { status: 'info', title: props.title || '暂无数据', description: props.description || '当前筛选条件下没有记录。' }
  return null
})
</script>

<template>
  <div v-if="loading" class="async-state async-state--loading" aria-busy="true">
    <a-skeleton active :paragraph="{ rows: 6 }" />
  </div>
  <a-result
    v-else-if="state"
    class="async-state"
    :status="state.status"
    :title="state.title"
    :sub-title="requestId ? `${state.description} · requestId: ${requestId}` : state.description"
  >
    <template #extra><slot name="action" /></template>
  </a-result>
  <slot v-else />
</template>

<style scoped>
.async-state { min-height: 280px; }
.async-state--loading { padding: 24px; }
</style>

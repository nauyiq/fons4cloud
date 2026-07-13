<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http, { unwrapResult } from '../api/client'
import { AdminApiError, type ApiResult } from '../api/result'
import AsyncState from '../components/AsyncState.vue'

interface ActionItem { type: string; severity: 'ERROR' | 'WARNING' | 'INFO'; count: number; title: string; route: string }
interface Overview { actions: ActionItem[]; statistics: Record<string, number>; dependencies: Record<string, 'UP' | 'DOWN' | 'UNKNOWN'> }

const router = useRouter()
const loading = ref(true)
const error = ref<AdminApiError | null>(null)
const overview = ref<Overview>({ actions: [], statistics: {}, dependencies: {} })

async function load() {
  loading.value = true
  error.value = null
  try {
    const response = await http.get<ApiResult<Overview>>('/admin/api/overview')
    overview.value = unwrapResult(response.data)
  } catch (caught) {
    error.value = caught instanceof AdminApiError ? caught : new AdminApiError({ code: 'UNKNOWN', message: '概览加载失败', retryable: true, category: 'UNKNOWN' })
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="workspace-page">
    <header class="page-heading"><div><p class="eyebrow">OPERATIONS</p><h1>运行概览</h1><p>优先处理异常实例、配置漂移和失败发布。</p></div><a-button @click="load">刷新</a-button></header>
    <AsyncState :loading="loading" :dependency-unavailable="error?.category === 'DEPENDENCY'" :forbidden="error?.category === 'PERMISSION'" :title="error?.message" :request-id="error?.requestId">
      <template #action><a-button v-if="error?.retryable" type="primary" @click="load">重试</a-button></template>
      <div class="overview-grid">
        <section class="workspace-panel action-panel">
          <div class="panel-title"><h2>行动队列</h2><span>{{ overview.actions.length }} 类待处理</span></div>
          <a-empty v-if="overview.actions.length === 0" description="当前没有待处理异常" />
          <button v-for="item in overview.actions" :key="item.type" class="action-row" @click="router.push(item.route)">
            <span :class="['severity', `severity--${item.severity.toLowerCase()}`]">{{ item.severity }}</span>
            <strong>{{ item.title }}</strong><b>{{ item.count }}</b><span>查看处理 →</span>
          </button>
        </section>
        <aside class="workspace-panel status-panel">
          <div class="panel-title"><h2>能力状态</h2></div>
          <div v-for="(state, name) in overview.dependencies" :key="name" class="dependency-row"><span>{{ name }}</span><a-tag :color="state === 'UP' ? 'green' : state === 'DOWN' ? 'red' : 'default'">{{ state }}</a-tag></div>
          <a-divider />
          <div v-for="(count, name) in overview.statistics" :key="name" class="stat-row"><span>{{ name }}</span><strong>{{ count }}</strong></div>
        </aside>
      </div>
    </AsyncState>
  </section>
</template>

<style scoped>
.overview-grid { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 20px; }
.action-panel, .status-panel { min-height: 430px; }
.panel-title { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 18px; }
.panel-title h2 { margin: 0; font-size: 16px; }.panel-title span { color: #667085; font-size: 12px; }
.action-row { width: 100%; display: grid; grid-template-columns: 84px 1fr 60px 90px; align-items: center; gap: 12px; padding: 15px 4px; color: #344054; text-align: left; background: none; border: 0; border-bottom: 1px solid #eaecf0; cursor: pointer; }
.action-row:hover { background: #f8fafc; }.action-row b { color: #101828; font-size: 18px; }.action-row > span:last-child { color: #2563eb; font-size: 12px; }
.severity { width: fit-content; padding: 3px 7px; border-radius: 4px; font-size: 10px; font-weight: 800; }.severity--error { color: #b42318; background: #fee4e2; }.severity--warning { color: #b54708; background: #fef0c7; }.severity--info { color: #175cd3; background: #dbeafe; }
.dependency-row, .stat-row { display: flex; justify-content: space-between; align-items: center; padding: 11px 0; border-bottom: 1px solid #f2f4f7; text-transform: capitalize; }.stat-row strong { font-size: 18px; }
</style>

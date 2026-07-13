<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import http, { unwrapResult } from '../api/client'
import { AdminApiError, type ApiResult } from '../api/result'
import AsyncState from '../components/AsyncState.vue'

interface Instance { serviceName: string; instanceId: string; host: string; port: number; healthy: boolean; metadata: Record<string, string> }
interface ProbeResult { serviceName: string; endpointPath: string; available: boolean; status: string; unavailableReason?: string }

const services = ref<string[]>([])
const instances = ref<Instance[]>([])
const selectedService = ref('')
const selectedInstanceId = ref('')
const endpointPath = ref('/actuator/health')
const keyword = ref('')
const loading = ref(true)
const instanceLoading = ref(false)
const probing = ref(false)
const error = ref<AdminApiError | null>(null)
const probeResult = ref<ProbeResult | null>(null)

const filteredServices = computed(() => services.value.filter((name) => name.toLowerCase().includes(keyword.value.toLowerCase())))
const columns = [
  { title: '实例 ID', dataIndex: 'instanceId', key: 'instanceId' },
  { title: '地址', key: 'address' },
  { title: '健康', dataIndex: 'healthy', key: 'healthy', width: 100 },
  { title: '元数据', dataIndex: 'metadata', key: 'metadata' },
  { title: '操作', key: 'action', width: 100 },
]

async function loadServices() {
  loading.value = true
  error.value = null
  try {
    const response = await http.get<ApiResult<string[]>>('/admin/services')
    services.value = unwrapResult(response.data)
    if (!selectedService.value && services.value.length) await selectService(services.value[0])
  } catch (caught) {
    error.value = caught instanceof AdminApiError ? caught : null
  } finally { loading.value = false }
}

async function selectService(name: string) {
  selectedService.value = name
  selectedInstanceId.value = ''
  probeResult.value = null
  instanceLoading.value = true
  try {
    const response = await http.get<ApiResult<Instance[]>>(`/admin/services/${encodeURIComponent(name)}/instances`)
    instances.value = unwrapResult(response.data)
  } finally { instanceLoading.value = false }
}

async function probe() {
  if (!selectedInstanceId.value) return
  probing.value = true
  try {
    const response = await http.post<ApiResult<ProbeResult>>('/admin/observability/actuator/probe', {
      serviceName: selectedService.value,
      instanceId: selectedInstanceId.value,
      endpointPath: endpointPath.value,
    })
    probeResult.value = unwrapResult(response.data)
  } finally { probing.value = false }
}

onMounted(loadServices)
</script>

<template>
  <section class="workspace-page">
    <header class="page-heading"><div><p class="eyebrow">CAPABILITY WORKSPACE</p><h1>服务治理</h1><p>查看注册实例、健康状态并执行受控只读探测。</p></div><a-button @click="loadServices">刷新</a-button></header>
    <AsyncState :loading="loading" :empty="!error && services.length === 0" :dependency-unavailable="error?.category === 'DEPENDENCY'" :forbidden="error?.category === 'PERMISSION'" :title="error?.message" :request-id="error?.requestId">
      <div class="services-grid">
        <aside class="workspace-panel service-list">
          <a-input-search v-model:value="keyword" placeholder="搜索服务" allow-clear />
          <button v-for="service in filteredServices" :key="service" :class="['service-item', { active: service === selectedService }]" @click="selectService(service)"><span class="service-dot" />{{ service }}</button>
        </aside>
        <div class="workspace-panel instance-panel">
          <div class="panel-heading"><div><h2>{{ selectedService || '请选择服务' }}</h2><span>{{ instances.length }} 个实例</span></div></div>
          <a-table :columns="columns" :data-source="instances" :loading="instanceLoading" row-key="instanceId" size="middle" :pagination="false">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'address'"><code>{{ record.host }}:{{ record.port }}</code></template>
              <template v-else-if="column.key === 'healthy'"><a-badge :status="record.healthy ? 'success' : 'error'" :text="record.healthy ? '健康' : '异常'" /></template>
              <template v-else-if="column.key === 'metadata'"><span class="metadata">{{ Object.entries(record.metadata || {}).map(([k,v]) => `${k}=${v}`).join(' · ') || '-' }}</span></template>
              <template v-else-if="column.key === 'action'"><a-button type="link" size="small" @click="selectedInstanceId = record.instanceId">探测</a-button></template>
            </template>
          </a-table>
          <section v-if="selectedInstanceId" class="probe-panel">
            <div><strong>只读探测</strong><span>{{ selectedInstanceId }}</span></div>
            <a-select v-model:value="endpointPath" :options="[{value:'/actuator/health'},{value:'/actuator/info'}]" style="width:180px" />
            <a-button type="primary" :loading="probing" @click="probe">执行探测</a-button>
            <a-tag v-if="probeResult" :color="probeResult.available ? 'green' : 'red'">{{ probeResult.status }}</a-tag>
            <span v-if="probeResult?.unavailableReason" class="probe-error">{{ probeResult.unavailableReason }}</span>
          </section>
        </div>
      </div>
    </AsyncState>
  </section>
</template>

<style scoped>
.services-grid { display:grid; grid-template-columns:280px minmax(0,1fr); gap:20px; }.service-list,.instance-panel{min-height:560px}.service-list{padding:16px}.service-item{width:100%;display:flex;align-items:center;gap:10px;padding:11px 10px;margin-top:4px;color:#344054;text-align:left;background:none;border:0;border-radius:6px;cursor:pointer}.service-item:hover,.service-item.active{color:#175cd3;background:#eff6ff}.service-dot{width:7px;height:7px;border-radius:50%;background:#12b76a}.panel-heading{display:flex;justify-content:space-between;margin-bottom:16px}.panel-heading h2{margin:0;font-size:17px}.panel-heading span{color:#667085;font-size:12px}.metadata{display:block;max-width:380px;color:#667085;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.probe-panel{display:flex;align-items:center;gap:14px;margin-top:20px;padding:16px;background:#f8fafc;border:1px solid #e5e7eb;border-radius:8px}.probe-panel>div{margin-right:auto}.probe-panel strong,.probe-panel span{display:block}.probe-panel span{color:#667085;font-size:12px}.probe-error{color:#b42318!important}
</style>

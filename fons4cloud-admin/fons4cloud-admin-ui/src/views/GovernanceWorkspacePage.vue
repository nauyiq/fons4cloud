<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http, { unwrapResult } from '../api/client'
import { AdminApiError, type ApiResult } from '../api/result'
import AsyncState from '../components/AsyncState.vue'
import GovernanceStructuredEditor from '../components/GovernanceStructuredEditor.vue'

interface ResourceItem { registeredResourceId?: number; domain: string; resourceType: string; resourceKey: string; displayName: string; targetRefSummary: string; currentHash: string; safeContent: string; status: string; allowedActions: string[]; registered: boolean }
interface Page<T> { items: T[]; total: number; offset: number; limit: number }
interface DiffEntry { path: string; operation: string; before: unknown; after: unknown; sensitive: boolean }

const props = defineProps<{ title: string; description: string; domain: string; resourceType: string; listEndpoint: string; detailEndpoint: string; draftEndpoint: string }>()
const router = useRouter()
const items = ref<ResourceItem[]>([])
const selected = ref<ResourceItem | null>(null)
const keyword = ref('')
const loading = ref(true)
const detailLoading = ref(false)
const error = ref<AdminApiError | null>(null)
const editorMode = ref('structured')
const editorContent = ref('')
const description = ref('')
const diffEntries = ref<DiffEntry[]>([])
const diffVisible = ref(false)
const saving = ref(false)
const columns = [{title:'资源',dataIndex:'displayName',key:'displayName'},{title:'状态',dataIndex:'status',key:'status',width:110},{title:'登记',dataIndex:'registered',key:'registered',width:100},{title:'目标摘要',dataIndex:'targetRefSummary',key:'targetRefSummary'},{title:'操作',key:'action',width:100}]
const changed = computed(() => selected.value && normalize(editorContent.value) !== normalize(selected.value.safeContent))

async function load() {
  loading.value = true; error.value = null
  try {
    const response = await http.get<ApiResult<Page<ResourceItem>>>(props.listEndpoint, { params: { keyword: keyword.value, offset: 0, limit: 50 } })
    items.value = unwrapResult(response.data).items
    if (items.value.length) await selectResource(items.value[0])
  } catch (caught) { error.value = caught instanceof AdminApiError ? caught : null }
  finally { loading.value = false }
}
async function selectResource(item: ResourceItem) {
  detailLoading.value = true
  try {
    const response = await http.get<ApiResult<ResourceItem>>(`${props.detailEndpoint}/${encodeURIComponent(item.resourceKey)}`)
    selected.value = unwrapResult(response.data); editorContent.value = selected.value.safeContent; diffEntries.value = []
  } finally { detailLoading.value = false }
}
async function previewDiff() {
  if (!selected.value) return
  const response = await http.post<ApiResult<{changed:boolean;entries:DiffEntry[]}>>('/admin/api/governance/config/diff', { domain: props.domain, resourceType: props.resourceType, beforeContent: selected.value.safeContent, afterContent: editorContent.value })
  diffEntries.value = unwrapResult(response.data).entries; diffVisible.value = true
}
async function saveDraft() {
  if (!selected.value) return
  saving.value = true
  try {
    await http.post(props.draftEndpoint, { resourceKey: selected.value.resourceKey, baseHash: selected.value.currentHash, content: editorContent.value, changeType: 'UPDATE', description: description.value })
    diffVisible.value = false
    await router.push({ name: 'changes' })
  } finally { saving.value = false }
}
function normalize(value:string){try{return JSON.stringify(JSON.parse(value))}catch{return value.trim()}}
onMounted(load)
</script>

<template>
  <section class="workspace-page">
    <header class="page-heading"><div><p class="eyebrow">CAPABILITY WORKSPACE</p><h1>{{ title }}</h1><p>{{ description }}</p></div><a-space><a-input-search v-model:value="keyword" placeholder="搜索资源" @search="load" /><a-button @click="load">刷新</a-button></a-space></header>
    <AsyncState :loading="loading" :empty="!error && items.length===0" :dependency-unavailable="error?.category==='DEPENDENCY'" :forbidden="error?.category==='PERMISSION'" :title="error?.message" :request-id="error?.requestId">
      <div class="governance-grid">
        <section class="workspace-panel resource-table"><a-table :columns="columns" :data-source="items" row-key="resourceKey" size="middle" :pagination="false" :custom-row="(record:ResourceItem)=>({onClick:()=>selectResource(record)})"><template #bodyCell="{column,record}"><template v-if="column.key==='status'"><a-badge :status="record.status==='ACTIVE'?'success':'default'" :text="record.status" /></template><template v-else-if="column.key==='registered'"><a-tag>{{ record.registered?'已登记':'目标现存' }}</a-tag></template><template v-else-if="column.key==='action'"><a-button type="link" size="small">编辑</a-button></template></template></a-table></section>
        <section class="workspace-panel editor-panel"><a-spin :spinning="detailLoading"><template v-if="selected"><div class="editor-heading"><div><h2>{{ selected.displayName }}</h2><code>{{ selected.currentHash?.slice(0,12) }}</code></div><a-tag color="blue">{{ selected.resourceType }}</a-tag></div><a-tabs v-model:active-key="editorMode"><a-tab-pane key="structured" tab="结构化编辑"><GovernanceStructuredEditor v-model="editorContent" :resource-type="resourceType" :resource-key="selected.resourceKey" /></a-tab-pane><a-tab-pane key="json" tab="高级 JSON"><a-textarea v-model:value="editorContent" :rows="24" class="json-editor" /></a-tab-pane></a-tabs><a-form-item label="变更说明"><a-input v-model:value="description" placeholder="说明本次变更目的和影响" /></a-form-item><div class="editor-actions"><span v-if="changed">存在未保存修改</span><a-button :disabled="!changed" @click="previewDiff">预览差异</a-button></div></template><a-empty v-else description="请选择治理资源" /></a-spin></section>
      </div>
    </AsyncState>
    <a-modal v-model:open="diffVisible" title="语义差异确认" width="860px" :confirm-loading="saving" ok-text="创建草稿" @ok="saveDraft"><a-alert type="warning" show-icon message="创建草稿不会直接写入目标系统；发布需在变更中心再次确认。" /><a-table :data-source="diffEntries" :pagination="false" size="small" row-key="path" :columns="[{title:'路径',dataIndex:'path'},{title:'操作',dataIndex:'operation',width:100},{title:'变更前',dataIndex:'before'},{title:'变更后',dataIndex:'after'}]" /></a-modal>
  </section>
</template>

<style scoped>
.governance-grid{display:grid;grid-template-columns:minmax(480px,.85fr) minmax(560px,1.15fr);gap:20px}.resource-table,.editor-panel{min-height:620px}.resource-table{padding:0}.editor-heading{display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #eaecf0;padding-bottom:14px}.editor-heading h2{margin:0 0 4px;font-size:17px}.editor-heading code{color:#667085}.json-editor{font-family:"Cascadia Code",Consolas,monospace;font-size:12px}.editor-actions{display:flex;align-items:center;justify-content:flex-end;gap:14px}.editor-actions span{color:#b54708;font-size:12px}
</style>

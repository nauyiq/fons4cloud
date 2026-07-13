<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import http, { unwrapResult } from '../api/client'
import { AdminApiError, type ApiResult } from '../api/result'
import AsyncState from '../components/AsyncState.vue'
import { useSessionStore } from '../stores/session'

interface Change { id:number; resourceId:number; changeNo:string; changeType:string; status:string; baseHash:string; contentHash:string; content:string; validationResult?:string; description?:string; createdBy:string; updatedBy:string }
interface Release { id:number; releaseNo:string; releaseType:string; status:string; beforeHash?:string; afterHash?:string; errorCode?:string; errorMessage?:string; startedAt?:string; finishedAt?:string }
interface Snapshot { id:number; snapshotType:string; contentHash:string; createdAt:string }
interface Detail { change:Change; releases:Release[]; snapshots:Snapshot[]; allowedActions:string[] }
interface Page<T> { items:T[]; total:number; offset:number; limit:number }

const session = useSessionStore()
const changes = ref<Change[]>([])
const detail = ref<Detail | null>(null)
const loading = ref(true)
const detailLoading = ref(false)
const actionLoading = ref(false)
const error = ref<AdminApiError | null>(null)
const status = ref<string>()
const rollbackOpen = ref(false)
const rollbackSnapshotId = ref<number>()
const rollbackReason = ref('')
const columns = [{title:'变更单',dataIndex:'changeNo',key:'changeNo'},{title:'类型',dataIndex:'changeType',key:'changeType',width:100},{title:'状态',dataIndex:'status',key:'status',width:150},{title:'资源 ID',dataIndex:'resourceId',key:'resourceId',width:100},{title:'说明',dataIndex:'description',key:'description'},{title:'操作',key:'action',width:90}]

async function load(){loading.value=true;error.value=null;try{const response=await http.get<ApiResult<Page<Change>>>('/admin/changes/page',{params:{status:status.value,offset:0,limit:100}});changes.value=unwrapResult(response.data).items;if(changes.value.length&&!detail.value)await select(changes.value[0])}catch(caught){error.value=caught instanceof AdminApiError?caught:null}finally{loading.value=false}}
async function select(change:Change){detailLoading.value=true;try{const response=await http.get<ApiResult<Detail>>(`/admin/changes/${change.id}/detail`);detail.value=unwrapResult(response.data)}finally{detailLoading.value=false}}
async function validateChange(){if(!detail.value)return;actionLoading.value=true;try{await http.post(`/admin/changes/${detail.value.change.id}/validate`);message.success('校验完成');await refreshDetail()}finally{actionLoading.value=false}}
function publishChange(){if(!detail.value)return;const current=detail.value.change;Modal.confirm({title:'确认发布治理变更',content:`环境：${session.environmentName}\n变更：${current.changeNo}\n目标摘要：${current.contentHash?.slice(0,12)}\n发布后将写入权威目标。`,okText:'确认发布',okType:'danger',cancelText:'取消',async onOk(){actionLoading.value=true;try{await http.post(`/admin/changes/${current.id}/publish`,{expectedBaseHash:current.baseHash,publishReason:current.description||'控制台发布'});message.success('发布请求已受理');await refreshDetail()}finally{actionLoading.value=false}}})}
async function rollback(){if(!detail.value||!rollbackSnapshotId.value)return;actionLoading.value=true;try{await http.post(`/admin/changes/${detail.value.change.id}/rollback`,{snapshotId:rollbackSnapshotId.value,expectedCurrentHash:detail.value.change.contentHash,rollbackReason:rollbackReason.value});rollbackOpen.value=false;message.success('回滚请求已受理');await refreshDetail()}finally{actionLoading.value=false}}
async function recover(){const release=detail.value?.releases.find(item=>item.status==='PENDING_CONFIRM');if(!release)return;actionLoading.value=true;try{await http.post(`/admin/changes/releases/${release.id}/recover`);message.success('已完成目标回读');await refreshDetail()}finally{actionLoading.value=false}}
async function refreshDetail(){if(detail.value){await select(detail.value.change);await load()}}
function statusColor(value:string){if(value.includes('FAILED'))return'red';if(value.includes('PUBLISH')||value.includes('ROLLBACK'))return'blue';if(value==='PUBLISHED'||value==='ROLLED_BACK')return'green';if(value==='DRIFT_DETECTED')return'orange';return'default'}
onMounted(load)
</script>

<template>
  <section class="workspace-page"><header class="page-heading"><div><p class="eyebrow">CHANGE WORKBENCH</p><h1>变更中心</h1><p>统一校验、差异确认、发布、状态查询和回滚。</p></div><a-space><a-select v-model:value="status" allow-clear placeholder="全部状态" style="width:180px" :options="['DRAFT','VALIDATED','PUBLISHING','PUBLISHED','PUBLISH_FAILED','DRIFT_DETECTED','ROLLBACKING','ROLLED_BACK'].map(value=>({value}))" @change="load"/><a-button @click="load">刷新</a-button></a-space></header>
    <AsyncState :loading="loading" :empty="!error&&changes.length===0" :dependency-unavailable="error?.category==='DEPENDENCY'" :forbidden="error?.category==='PERMISSION'" :title="error?.message" :request-id="error?.requestId">
      <div class="change-grid"><section class="workspace-panel change-list"><a-table :columns="columns" :data-source="changes" row-key="id" size="middle" :pagination="{pageSize:20}" :custom-row="(record:Change)=>({onClick:()=>select(record)})"><template #bodyCell="{column,record}"><template v-if="column.key==='status'"><a-tag :color="statusColor(record.status)">{{record.status}}</a-tag></template><template v-else-if="column.key==='action'"><a-button type="link" size="small">详情</a-button></template></template></a-table></section>
        <section class="workspace-panel change-detail"><a-spin :spinning="detailLoading"><template v-if="detail"><div class="detail-heading"><div><h2>{{detail.change.changeNo}}</h2><span>{{detail.change.description||'无变更说明'}}</span></div><a-tag :color="statusColor(detail.change.status)">{{detail.change.status}}</a-tag></div><a-descriptions :column="2" size="small" bordered><a-descriptions-item label="资源 ID">{{detail.change.resourceId}}</a-descriptions-item><a-descriptions-item label="类型">{{detail.change.changeType}}</a-descriptions-item><a-descriptions-item label="基线 Hash"><code>{{detail.change.baseHash}}</code></a-descriptions-item><a-descriptions-item label="目标 Hash"><code>{{detail.change.contentHash}}</code></a-descriptions-item></a-descriptions><a-collapse class="detail-section"><a-collapse-panel key="content" header="目标配置（受控正文）"><pre>{{detail.change.content}}</pre></a-collapse-panel><a-collapse-panel key="validation" header="校验结果"><pre>{{detail.change.validationResult||'尚未校验'}}</pre></a-collapse-panel></a-collapse><h3>执行记录</h3><a-timeline><a-timeline-item v-for="release in detail.releases" :key="release.id" :color="statusColor(release.status)"><strong>{{release.releaseType}} · {{release.status}}</strong><p>{{release.releaseNo}}<span v-if="release.errorMessage"> · {{release.errorMessage}}</span></p></a-timeline-item></a-timeline><div class="detail-actions"><a-button v-if="detail.allowedActions.includes('VALIDATE')" :loading="actionLoading" @click="validateChange">校验</a-button><a-button v-if="detail.allowedActions.includes('PUBLISH')" type="primary" danger :loading="actionLoading" @click="publishChange">发布</a-button><a-button v-if="detail.allowedActions.includes('ROLLBACK')" danger @click="rollbackOpen=true">回滚</a-button><a-button v-if="detail.releases.some(item=>item.status==='PENDING_CONFIRM')" :loading="actionLoading" @click="recover">回读确认</a-button><a-tag v-if="detail.allowedActions.includes('QUERY_STATUS')" color="processing">执行中，不会自动重复写入</a-tag></div></template><a-empty v-else description="请选择变更" /></a-spin></section></div>
    </AsyncState>
    <a-modal v-model:open="rollbackOpen" title="回滚确认" ok-text="确认回滚" ok-type="danger" :confirm-loading="actionLoading" @ok="rollback"><a-alert type="warning" show-icon :message="`将在 ${session.environmentName} 创建新的回滚变更，不覆盖原历史。`"/><a-form layout="vertical"><a-form-item label="来源快照"><a-select v-model:value="rollbackSnapshotId" :options="detail?.snapshots.map(item=>({value:item.id,label:`${item.snapshotType} · ${item.contentHash.slice(0,12)}`}))"/></a-form-item><a-form-item label="回滚原因"><a-input v-model:value="rollbackReason"/></a-form-item></a-form></a-modal>
  </section>
</template>

<style scoped>
.change-grid{display:grid;grid-template-columns:minmax(600px,1.1fr) minmax(500px,.9fr);gap:20px}.change-list,.change-detail{min-height:650px}.change-list{padding:0}.detail-heading{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:18px}.detail-heading h2{margin:0;font-size:18px}.detail-heading span{color:#667085;font-size:12px}.detail-section{margin:18px 0}.detail-section pre{max-height:280px;margin:0;overflow:auto;font:12px/1.6 "Cascadia Code",Consolas,monospace}.change-detail h3{margin-top:22px;font-size:14px}.change-detail p{margin:3px 0;color:#667085;font-size:12px}.detail-actions{position:sticky;bottom:0;display:flex;justify-content:flex-end;gap:10px;padding-top:16px;background:#fff;border-top:1px solid #eaecf0}
</style>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = defineProps<{ resourceType: string; resourceKey: string; modelValue: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const documentValue = ref<any>({})
const activeRoute = ref<any>({})
const whiteIpsText = ref('')
const blockedIpsText = ref('[]')
const resourcesText = ref('[]')
const ignoredUrisText = ref('')
const identifierUrisText = ref('')
const parseError = ref('')

const isRoute = computed(() => props.resourceType === 'ROUTE')
const isTraffic = computed(() => props.resourceType === 'IP_LIST')
const isAccess = computed(() => props.resourceType === 'AUTH_RESOURCE')
const isClient = computed(() => props.resourceType === 'OAUTH_CLIENT')

watch(() => [props.modelValue, props.resourceKey], load, { immediate: true })

function load() {
  parseError.value = ''
  try {
    documentValue.value = JSON.parse(props.modelValue || (isRoute.value ? '[]' : '{}'))
    if (isRoute.value) activeRoute.value = documentValue.value.find((item: any) => item.id === props.resourceKey) ?? documentValue.value[0] ?? {}
    if (isTraffic.value) {
      whiteIpsText.value = (documentValue.value.whiteIps ?? []).join('\n')
      blockedIpsText.value = JSON.stringify(documentValue.value.manualBlockedIps ?? [], null, 2)
    }
    if (isAccess.value) {
      resourcesText.value = JSON.stringify(documentValue.value.authorizationResources ?? [], null, 2)
      ignoredUrisText.value = (documentValue.value.ignoredAccessTokenUris ?? []).join('\n')
      identifierUrisText.value = (documentValue.value.identifierTokenUris ?? []).join('\n')
    }
  } catch { parseError.value = '当前 JSON 无法转换为结构化表单，请先在高级模式修正。' }
}

function commit() {
  parseError.value = ''
  try {
    if (isTraffic.value) {
      documentValue.value.whiteIps = lines(whiteIpsText.value)
      documentValue.value.manualBlockedIps = JSON.parse(blockedIpsText.value || '[]')
    }
    if (isAccess.value) {
      documentValue.value.authorizationResources = JSON.parse(resourcesText.value || '[]')
      documentValue.value.ignoredAccessTokenUris = lines(ignoredUrisText.value)
      documentValue.value.identifierTokenUris = lines(identifierUrisText.value)
    }
    emit('update:modelValue', JSON.stringify(documentValue.value, null, 2))
  } catch { parseError.value = '结构化字段包含无效 JSON。' }
}

function lines(value: string): string[] {
  return value.split(/\r?\n/).map((item) => item.trim()).filter(Boolean)
}
</script>

<template>
  <a-alert v-if="parseError" type="error" show-icon :message="parseError" />
  <a-form v-else layout="vertical" class="structured-editor" @change="commit">
    <template v-if="isRoute">
      <div class="form-grid"><a-form-item label="路由 ID"><a-input v-model:value="activeRoute.id" @change="commit" /></a-form-item><a-form-item label="目标 URI"><a-input v-model:value="activeRoute.uri" @change="commit" /></a-form-item><a-form-item label="顺序"><a-input-number v-model:value="activeRoute.order" style="width:100%" @change="commit" /></a-form-item></div>
      <a-form-item label="Predicates（结构化数组）"><a-textarea v-model:value="activeRoute.predicatesText" placeholder="复杂谓词请在高级 JSON 模式编辑" disabled /></a-form-item>
      <a-alert type="info" show-icon message="路由 ID、URI 和顺序可直接编辑；predicates/filters 保持原结构并可在高级 JSON 模式调整。" />
    </template>
    <template v-else-if="isTraffic">
      <a-form-item label="白名单 IP（每行一个）"><a-textarea v-model:value="whiteIpsText" :rows="6" @change="commit" /></a-form-item>
      <a-form-item label="人工黑名单（ip / blockSeconds 数组）"><a-textarea v-model:value="blockedIpsText" :rows="10" class="code-input" @change="commit" /></a-form-item>
    </template>
    <template v-else-if="isAccess">
      <a-form-item label="授权资源（id / authorities 数组）"><a-textarea v-model:value="resourcesText" :rows="12" class="code-input" @change="commit" /></a-form-item>
      <div class="form-grid"><a-form-item label="忽略 Access Token URI"><a-textarea v-model:value="ignoredUrisText" :rows="6" @change="commit" /></a-form-item><a-form-item label="幂等标识 URI"><a-textarea v-model:value="identifierUrisText" :rows="6" @change="commit" /></a-form-item></div>
    </template>
    <template v-else-if="isClient">
      <div class="form-grid"><a-form-item label="操作"><a-select v-model:value="documentValue.operation" :options="['UPDATE','STATUS','ROTATE_SECRET'].map(value=>({value}))" @change="commit" /></a-form-item><a-form-item label="Client ID"><a-input v-model:value="documentValue.clientId" disabled /></a-form-item><a-form-item label="状态"><a-switch v-model:checked="documentValue.status" @change="commit" /></a-form-item></div>
      <div class="form-grid"><a-form-item label="Scope"><a-input v-model:value="documentValue.scope" @change="commit" /></a-form-item><a-form-item label="Grant Types"><a-input v-model:value="documentValue.authorizedGrantTypes" @change="commit" /></a-form-item><a-form-item label="Authorities"><a-input v-model:value="documentValue.authorities" @change="commit" /></a-form-item></div>
      <a-alert type="warning" show-icon message="控制台不会读取或保存原 Secret；轮换密钥由认证服务生成一次性结果。" />
    </template>
  </a-form>
</template>

<style scoped>
.structured-editor{padding:4px}.form-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}.code-input{font-family:"Cascadia Code",Consolas,monospace;font-size:12px}
</style>

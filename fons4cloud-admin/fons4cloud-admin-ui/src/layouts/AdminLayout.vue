<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const collapsed = ref(false)

const navigation = [
  { key: 'overview', label: '运行概览', permission: '' },
  { key: 'services', label: '服务治理', permission: 'services:view' },
  { key: 'gateway', label: '网关治理', permission: 'gateway:view' },
  { key: 'traffic', label: '流量治理', permission: 'traffic:view' },
  { key: 'access', label: '访问控制', permission: 'access:view' },
  { key: 'clients', label: '认证客户端', permission: 'clients:view' },
  { key: 'changes', label: '变更中心', permission: 'changes:view' },
  { key: 'audits', label: '审计追踪', permission: 'audits:view' },
]

const visibleNavigation = computed(() =>
  navigation.filter((item) => !item.permission || session.can(item.permission)),
)
const selectedKeys = computed(() => [String(route.name ?? 'overview')])

function navigate({ key }: { key: string }) {
  void router.push({ name: key })
}
</script>

<template>
  <a-layout class="admin-layout">
    <a-layout-sider v-model:collapsed="collapsed" collapsible :width="232" class="admin-sider">
      <div class="brand">
        <span class="brand__mark">F4</span>
        <div v-if="!collapsed"><strong>Fons4Cloud</strong><small>运维控制台</small></div>
      </div>
      <a-menu theme="dark" mode="inline" :selected-keys="selectedKeys" :items="visibleNavigation" @click="navigate" />
    </a-layout-sider>
    <a-layout>
      <a-layout-header class="admin-header">
        <div class="environment">
          <span class="status-dot" />
          <div><small>当前环境</small><strong>{{ session.environmentName }}</strong></div>
        </div>
        <div class="header-actions">
          <span class="operator">{{ session.username || '管理员' }}</span>
          <a-button type="text" @click="session.logout">退出</a-button>
        </div>
      </a-layout-header>
      <a-layout-content class="admin-content"><router-view /></a-layout-content>
    </a-layout>
  </a-layout>
</template>

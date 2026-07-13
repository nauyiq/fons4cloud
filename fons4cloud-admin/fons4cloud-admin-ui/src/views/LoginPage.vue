<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AdminApiError } from '../api/result'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const session = useSessionStore()
const form = reactive({ accessAccount: '', accessSecret: '' })
const submitting = ref(false)
const errorMessage = ref(route.query.reason === 'expired' ? '会话已失效，请重新登录。' : '')

async function submit() {
  submitting.value = true
  errorMessage.value = ''
  try {
    await session.login(form)
    await router.replace({ name: 'overview' })
  } catch (error) {
    errorMessage.value = error instanceof AdminApiError ? error.message : '登录失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-panel">
      <header><span class="brand-mark">F4</span><div><h1>Fons4Cloud</h1><p>框架运维控制台</p></div></header>
      <a-alert v-if="errorMessage" type="error" show-icon :message="errorMessage" />
      <a-form layout="vertical" :model="form" @finish="submit">
        <a-form-item label="账号" name="accessAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="form.accessAccount" autocomplete="username" size="large" />
        </a-form-item>
        <a-form-item label="密码" name="accessSecret" :rules="[{ required: true, message: '请输入密码' }]">
          <a-input-password v-model:value="form.accessSecret" autocomplete="current-password" size="large" />
        </a-form-item>
        <a-button type="primary" html-type="submit" size="large" block :loading="submitting">登录控制台</a-button>
      </a-form>
      <footer>单环境内部运维服务 · 所有高风险操作均记录审计</footer>
    </section>
  </main>
</template>

import { defineStore } from 'pinia'
import router from '../router'
import http, {
  refreshSessionToken,
  registerSessionExpiredHandler,
  unwrapResult,
  type AdminTokenPayload,
} from '../api/client'
import { clearAccessToken, getAccessToken, setAccessToken } from '../api/token-vault'
import type { ApiResult } from '../api/result'

export interface SessionContext {
  userId: number
  username: string
  environmentName: string
  permissions: string[]
  dependencies: Record<string, 'UP' | 'DOWN' | 'UNKNOWN'>
}

interface LoginPayload {
  accessAccount: string
  accessSecret: string
}

export const useSessionStore = defineStore('admin-session', {
  state: () => ({
    username: '',
    userId: 0,
    environmentName: '环境未加载',
    permissions: [] as string[],
    dependencies: {} as SessionContext['dependencies'],
    restoring: false,
  }),
  getters: {
    authenticated: () => Boolean(getAccessToken()),
    can: (state) => (permission: string) => state.permissions.includes(permission),
  },
  actions: {
    applyToken(payload: AdminTokenPayload) {
      setAccessToken(payload.accessToken)
      this.username = payload.username
      this.userId = payload.userId
      this.permissions = payload.scopes ?? []
    },
    applyContext(context: SessionContext) {
      this.username = context.username
      this.userId = context.userId
      this.environmentName = context.environmentName
      this.permissions = context.permissions ?? []
      this.dependencies = context.dependencies ?? {}
    },
    async login(payload: LoginPayload) {
      // 重新登录必须先废弃内存中的旧令牌，避免失败后继续使用过期会话。
      clearAccessToken()
      const response = await http.post<ApiResult<AdminTokenPayload>>('/admin/auth/login', {
        ...payload,
        grantType: 'PASSWORD',
        scopes: [],
      })
      this.applyToken(unwrapResult(response.data))
      await this.loadContext().catch(() => undefined)
    },
    async refresh() {
      this.applyToken(await refreshSessionToken())
    },
    async loadContext() {
      const response = await http.get<ApiResult<SessionContext>>('/admin/api/session/context')
      this.applyContext(unwrapResult(response.data))
    },
    async restore() {
      if (this.restoring) return
      this.restoring = true
      try {
        if (!getAccessToken()) await this.refresh()
        await this.loadContext()
      } finally {
        this.restoring = false
      }
    },
    async logout() {
      try {
        if (getAccessToken()) await http.delete('/admin/auth/logout')
      } finally {
        this.clear()
        await router.replace({ name: 'login' })
      }
    },
    clear() {
      clearAccessToken()
      this.$reset()
    },
  },
})

registerSessionExpiredHandler(() => {
  const session = useSessionStore()
  session.clear()
  void router.replace({ name: 'login', query: { reason: 'expired' } })
})

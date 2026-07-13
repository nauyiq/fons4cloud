import axios, { AxiosError, type AxiosRequestConfig } from 'axios'
import { AdminApiError, classifyApiError, type ApiResult } from './result'
import { clearAccessToken, getAccessToken, setAccessToken } from './token-vault'

export interface AdminTokenPayload {
  accessToken: string
  tokenType: string
  expiresIn: number
  scopes: string[]
  userId: number
  username: string
}

interface RetryableRequestConfig extends AxiosRequestConfig {
  __adminRetried?: boolean
}

const http = axios.create({
  baseURL: '/',
  timeout: 15_000,
  withCredentials: true,
})

const refreshHttp = axios.create({
  baseURL: '/',
  timeout: 15_000,
  withCredentials: true,
})

const ANONYMOUS_AUTH_PATHS = new Set(['/admin/auth/login', '/admin/auth/refresh-token'])

let refreshPromise: Promise<AdminTokenPayload> | null = null
let sessionExpiredHandler: (() => void) | null = null

export function registerSessionExpiredHandler(handler: () => void): void {
  sessionExpiredHandler = handler
}

export async function refreshSessionToken(): Promise<AdminTokenPayload> {
  if (!refreshPromise) {
    refreshPromise = refreshHttp
      .post<ApiResult<AdminTokenPayload>>('/admin/auth/refresh-token')
      .then(({ data }) => {
        if (!data.success || !data.data?.accessToken) {
          throw toAdminError(data)
        }
        setAccessToken(data.data.accessToken)
        return data.data
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

http.interceptors.request.use((config) => {
  const requestPath = config.url?.split(/[?#]/, 1)[0]
  if (requestPath && ANONYMOUS_AUTH_PATHS.has(requestPath)) {
    // 匿名认证接口不能携带旧 Bearer Token，否则网关会在进入接口前拒绝请求。
    config.headers.delete('Authorization')
    return config
  }
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const result = response.data as ApiResult<unknown>
    if (result && typeof result.success === 'boolean' && !result.success) {
      throw toAdminError(result, response.status, response.headers['x-request-id'])
    }
    return response
  },
  async (error: AxiosError<ApiResult<unknown>>) => {
    const status = error.response?.status
    const request = error.config as RetryableRequestConfig | undefined
    if (status === 401 && request && !request.__adminRetried && !request.url?.includes('/admin/auth/')) {
      request.__adminRetried = true
      try {
        await refreshSessionToken()
        return http.request(request)
      } catch {
        clearAccessToken()
        sessionExpiredHandler?.()
      }
    }
    throw toAdminError(
      error.response?.data,
      status,
      error.response?.headers?.['x-request-id'],
      error.message,
    )
  },
)

export function unwrapResult<T>(result: ApiResult<T>): T {
  if (!result.success) throw toAdminError(result)
  return result.data
}

function toAdminError(
  result?: Partial<ApiResult<unknown>>,
  status?: number,
  requestId?: string,
  fallbackMessage = '请求失败，请稍后重试',
): AdminApiError {
  const code = result?.code ?? `HTTP_${status ?? 'UNKNOWN'}`
  const category = classifyApiError(code, status)
  return new AdminApiError({
    code,
    message: result?.message || fallbackMessage,
    requestId,
    status,
    retryable: category === 'DEPENDENCY' || (status !== undefined && status >= 500),
    category,
  })
}

export default http

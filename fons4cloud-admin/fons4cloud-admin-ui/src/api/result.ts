export interface ApiResult<T> {
  success: boolean
  code: string
  message: string
  data: T
}

export interface AdminApiErrorShape {
  code: string
  message: string
  requestId?: string
  status?: number
  retryable: boolean
  category: 'AUTH' | 'PERMISSION' | 'DEPENDENCY' | 'CONFLICT' | 'VALIDATION' | 'UNKNOWN'
}

export class AdminApiError extends Error implements AdminApiErrorShape {
  code: string
  requestId?: string
  status?: number
  retryable: boolean
  category: AdminApiErrorShape['category']

  constructor(shape: AdminApiErrorShape) {
    super(shape.message)
    this.name = 'AdminApiError'
    this.code = shape.code
    this.requestId = shape.requestId
    this.status = shape.status
    this.retryable = shape.retryable
    this.category = shape.category
  }
}

export function classifyApiError(code: string, status?: number): AdminApiErrorShape['category'] {
  if (status === 403 || code === 'AD100005') return 'PERMISSION'
  if (code === 'AD200004' || code === 'AD100004') return 'DEPENDENCY'
  if (status === 401 || code.startsWith('AD10000')) return 'AUTH'
  if (code === 'AD200003' || status === 409) return 'CONFLICT'
  if (code === 'AD200002' || status === 400) return 'VALIDATION'
  return 'UNKNOWN'
}

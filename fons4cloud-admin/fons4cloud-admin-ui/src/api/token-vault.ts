let accessToken = ''

/** Access Token 只保存在模块内存中，禁止写入浏览器持久化存储。 */
export function getAccessToken(): string {
  return accessToken
}

export function setAccessToken(token?: string | null): void {
  accessToken = token?.trim() ?? ''
}

export function clearAccessToken(): void {
  accessToken = ''
}

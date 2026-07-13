import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginPage from '../views/LoginPage.vue'
import { useSessionStore } from '../stores/session'

const OverviewPage = () => import('../views/OverviewPage.vue')
const ServicesPage = () => import('../views/ServicesPage.vue')
const GovernanceWorkspacePage = () => import('../views/GovernanceWorkspacePage.vue')
const ChangesPage = () => import('../views/ChangesPage.vue')
const AuditsPage = () => import('../views/AuditsPage.vue')

const capabilityRoutes: RouteRecordRaw[] = [
  { path: '', redirect: { name: 'overview' } },
  { path: 'overview', name: 'overview', component: OverviewPage },
  { path: 'services', name: 'services', component: ServicesPage, meta: { permission: 'services:view' } },
  { path: 'gateway', name: 'gateway', component: GovernanceWorkspacePage, props: { title: '网关治理', description: '管理路由草稿、差异和发布。', domain:'GATEWAY', resourceType:'ROUTE', listEndpoint:'/admin/gateway/routes', detailEndpoint:'/admin/gateway/routes', draftEndpoint:'/admin/gateway/routes/drafts' }, meta: { permission: 'gateway:view' } },
  { path: 'traffic', name: 'traffic', component: GovernanceWorkspacePage, props: { title: '流量治理', description: '管理 IP 名单和流量访问边界。', domain:'TRAFFIC', resourceType:'IP_LIST', listEndpoint:'/admin/traffic/ip-lists', detailEndpoint:'/admin/traffic/ip-lists', draftEndpoint:'/admin/traffic/ip-lists/drafts' }, meta: { permission: 'traffic:view' } },
  { path: 'access', name: 'access', component: GovernanceWorkspacePage, props: { title: '访问控制', description: '管理授权资源、忽略 Token 与幂等规则。', domain:'ACCESS', resourceType:'AUTH_RESOURCE', listEndpoint:'/admin/access/resources', detailEndpoint:'/admin/access/resources', draftEndpoint:'/admin/access/resources/drafts' }, meta: { permission: 'access:view' } },
  { path: 'clients', name: 'clients', component: GovernanceWorkspacePage, props: { title: '认证客户端', description: '管理 OAuth Client 和安全轮换。', domain:'CLIENTS', resourceType:'OAUTH_CLIENT', listEndpoint:'/admin/clients', detailEndpoint:'/admin/clients', draftEndpoint:'/admin/clients/drafts' }, meta: { permission: 'clients:view' } },
  { path: 'changes', name: 'changes', component: ChangesPage, meta: { permission: 'changes:view' } },
  { path: 'audits', name: 'audits', component: AuditsPage, meta: { permission: 'audits:view' } },
]

const router = createRouter({
  history: createWebHistory('/admin-ui/'),
  routes: [
    { path: '/login', name: 'login', component: LoginPage, meta: { public: true } },
    { path: '/', component: AdminLayout, children: capabilityRoutes },
    { path: '/:pathMatch(.*)*', redirect: { name: 'overview' } },
  ],
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true
  const session = useSessionStore()
  if (!session.authenticated) {
    try {
      await session.restore()
    } catch {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }
  const permission = to.meta.permission as string | undefined
  if (permission && !session.can(permission)) return { name: 'overview' }
  return true
})

export default router

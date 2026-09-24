<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { api, getSession, type Session } from './api'
import { resources } from './resources'
import ResourceView from './ResourceView.vue'
const session = ref<Session | null>(null), ready = ref(false), busy = ref(false), error = ref(''), notice = ref('')
const username = ref(''), password = ref(''), section = ref(location.hash.slice(1) || 'overview'), overview = ref<any>(null)
const logs = ref<any[]>([]), logTotal = ref(0), logPage = ref(1), pipelineFilter = ref(''), settings = ref('')
const admin = computed(() => session.value?.user.role === 'ADMIN')
const resource = computed(() => resources.find(item => item.key === section.value))
const navigation = computed(() => resources.filter(item => admin.value || item.key !== 'users'))
function navigate(key: string) { location.hash = key }
async function loadSection() {
  error.value = ''; notice.value = ''
  if (!session.value?.authenticated) return
  busy.value = true
  try {
    if (section.value === 'overview') overview.value = await api('/overview')
    if (section.value === 'logs') { const data = await api(`/logs?page=${logPage.value}&size=20${pipelineFilter.value ? `&pipelineId=${encodeURIComponent(pipelineFilter.value)}` : ''}`); logs.value = data.items; logTotal.value = data.total }
    if (section.value === 'settings' && admin.value) settings.value = JSON.stringify(await api('/settings'), null, 2)
  } catch (e: any) { error.value = e.message } finally { busy.value = false }
}
function route() { section.value = location.hash.slice(1) || 'overview'; loadSection() }
async function restore() { try { session.value = await getSession(); await loadSection() } catch (e: any) { error.value = e.message } finally { ready.value = true } }
async function login() {
  busy.value = true; error.value = ''
  try { await getSession(); await api('/login', 'POST', new URLSearchParams({ username: username.value, password: password.value })); password.value = ''; session.value = await getSession(); await loadSection() }
  catch (e: any) { error.value = e.message } finally { busy.value = false }
}
async function logout() { try { await api('/logout', 'POST'); session.value = await getSession(); overview.value = null } catch (e: any) { error.value = e.message } }
async function saveSettings() { busy.value = true; error.value = ''; try { await api('/settings', 'PUT', JSON.parse(settings.value)); notice.value = '系统设置已保存' } catch (e: any) { error.value = e.message } finally { busy.value = false } }
function expired() { session.value = null; error.value = '会话已过期，请重新登录' }
onMounted(() => { window.addEventListener('hashchange', route); window.addEventListener('session-expired', expired); restore() })
onUnmounted(() => { window.removeEventListener('hashchange', route); window.removeEventListener('session-expired', expired) })
</script>

<template>
  <div v-if="!ready" class="loading-screen">正在连接 Otter 管理服务…</div>
  <div v-else-if="!session?.authenticated" class="login-page"><section class="login-story"><a class="brand" href="#"><span class="brand-symbol">O</span>otter<span class="brand-next">NEXT</span></a><div><p class="eyebrow">数据连接业务</p><h1>让数据流动，<br>让每一步清晰可见。</h1><p>统一管理同步任务、执行节点与数据配置。<br>从采集到加载，掌握数据同步的运行状态。</p><div class="flow-illustration" aria-hidden="true"><span>数据源</span><i></i><span class="flow-center">Otter</span><i></i><span>目标端</span></div></div><small>OTTER DATA SYNCHRONIZATION</small></section><section class="login-form"><form @submit.prevent="login"><p class="eyebrow">管理控制台</p><h2>登录工作空间</h2><p class="muted">使用管理账号继续</p><label>用户名<input v-model="username" required autocomplete="username" autofocus placeholder="请输入用户名"></label><label>密码<input v-model="password" required type="password" autocomplete="current-password" placeholder="请输入密码"></label><p v-if="error" class="alert error" role="alert">{{ error }}</p><button class="primary" :disabled="busy">{{ busy ? '正在登录…' : '登录 →' }}</button></form></section></div>
  <div v-else class="workspace"><aside class="sidebar"><a class="brand" href="#overview"><span class="brand-symbol">O</span>otter<span class="brand-next">NEXT</span></a><p class="nav-heading">工作空间</p><nav aria-label="主导航"><a href="#overview" :class="{ selected: section === 'overview' }"><span>◈</span>运行总览</a><p class="nav-heading">同步管理</p><a v-for="item in navigation" :key="item.key" :href="`#${item.key}`" :class="{ selected: section === item.key }"><span>{{ ({ channels: '⇄', pipelines: '⤳', nodes: '▦', sources: '◉', tables: '▤', mappings: '⇢', canals: '⌁', alarms: '♧', clusters: '⌘', matrices: '▧', users: '♙' } as Record<string, string>)[item.key] || '·' }}</span>{{ item.title }}</a><p class="nav-heading">运维</p><a href="#logs" :class="{ selected: section === 'logs' }"><span>☷</span>运行日志</a><a v-if="admin" href="#settings" :class="{ selected: section === 'settings' }"><span>⚙</span>系统设置</a></nav><div class="sidebar-foot"><span class="connection-dot"></span>管理服务已连接</div></aside>
    <main><header class="topbar"><div><span class="muted">工作空间</span><span class="breadcrumb">/</span>{{ resource?.title || ({ overview: '运行总览', logs: '运行日志', settings: '系统设置' } as Record<string, string>)[section] || '页面' }}</div><div class="account"><span class="avatar">{{ session.user.name?.slice(0, 1).toUpperCase() }}</span><span>{{ session.user.name }}<small>{{ admin ? '管理员' : '操作员' }}</small></span><button class="text-button" @click="logout">退出</button></div></header>
      <div class="content"><p v-if="error" class="alert error" role="alert">{{ error }}</p><p v-if="notice" class="alert success" role="status">{{ notice }}</p>
        <ResourceView v-if="resource && (resource.key !== 'users' || admin)" :resource="resource" :admin="admin" />
        <template v-else-if="section === 'overview'"><section class="page-heading"><div><p class="eyebrow">运行概况</p><h1>同步工作空间</h1><p>查看同步任务的整体状态，快速进入配置与运维</p></div><button @click="loadSection" :disabled="busy">{{ busy ? '更新中…' : '刷新数据' }}</button></section><div class="metrics"><article v-for="metric in [{ key: 'channels', title: '同步通道', sub: '已配置通道', to: 'channels' }, { key: 'running', title: '运行中', sub: '当前启动的通道', to: 'channels' }, { key: 'pipelines', title: '流水线', sub: '采集与加载流程', to: 'pipelines' }, { key: 'nodes', title: '执行节点', sub: '已登记节点', to: 'nodes' }]" :key="metric.key" class="metric" @click="navigate(metric.to)"><p>{{ metric.title }}<span>↗</span></p><strong>{{ overview?.[metric.key] ?? '—' }}</strong><small>{{ metric.sub }}</small></article></div><div class="overview-grid"><section class="panel"><header class="panel-title"><div><h2>同步延迟</h2><p>延迟最高的 10 条流水线</p></div><span class="badge active">实时数据</span></header><table><thead><tr><th>流水线</th><th>延迟（毫秒）</th><th>最近记录</th></tr></thead><tbody><tr v-for="(item, index) in overview?.topDelay || []" :key="index"><td>{{ item.pipelineName || item.pipelineId }}</td><td>{{ item.delayTime ?? item.delayNumber ?? '—' }}</td><td>{{ item.lastUpdate || '—' }}</td></tr><tr v-if="!overview?.topDelay?.length"><td colspan="3" class="empty"><div class="empty-symbol">↔</div>暂无延迟记录<p>同步任务运行并上报统计后，将在这里显示</p></td></tr></tbody></table></section><section class="panel setup-guide"><p class="eyebrow">配置指引</p><h2>开始同步任务</h2><a href="#nodes"><span>01</span><div>登记执行节点<small>设置通信地址与下载端口</small></div>↗</a><a href="#sources"><span>02</span><div>连接数据库<small>添加数据源与数据表</small></div>↗</a><a href="#canals"><span>03</span><div>配置采集任务<small>设置 Canal 与源库连接</small></div>↗</a><a href="#channels"><span>04</span><div>创建同步通道<small>关联流水线与表映射后启动</small></div>↗</a></section></div></template>
        <template v-else-if="section === 'logs'"><section class="page-heading"><div><p class="eyebrow">运行诊断</p><h1>运行日志</h1><p>查看流水线上报的运行记录</p></div></section><section class="panel"><form class="toolbar" @submit.prevent="logPage = 1; loadSection()"><input v-model="pipelineFilter" type="number" placeholder="流水线 ID（可选）" aria-label="流水线 ID"><button>查询</button></form><div class="log-list"><article v-for="(log, index) in logs" :key="index"><div><span class="badge">流水线 {{ log.pipelineId }}</span><time>{{ log.gmtCreate }}</time></div><pre>{{ log.message || log.content || JSON.stringify(log, null, 2) }}</pre></article><div v-if="!logs.length" class="empty">暂无运行日志</div></div><footer class="pagination"><span>共 {{ logTotal }} 条</span><div><button :disabled="logPage <= 1" @click="logPage--; loadSection()">上一页</button><span>{{ logPage }}</span><button :disabled="logPage * 20 >= logTotal" @click="logPage++; loadSection()">下一页</button></div></footer></section></template>
        <template v-else-if="section === 'settings' && admin"><section class="page-heading"><div><p class="eyebrow">全局配置</p><h1>系统设置</h1><p>维护系统表、文件下载方式和告警接收人配置</p></div><button class="primary" :disabled="busy" @click="saveSettings">保存设置</button></section><section class="panel settings"><label>系统参数（JSON）<textarea v-model="settings" class="code-input" rows="28" spellcheck="false"></textarea></label></section></template>
        <section v-else class="empty">页面不存在或当前账号没有访问权限<a href="#overview">返回运行总览</a></section>
        <footer class="workspace-footer">Otter Next <span>数据同步管理平台</span></footer>
      </div>
    </main>
  </div>
</template>

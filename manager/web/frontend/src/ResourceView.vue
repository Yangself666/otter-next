<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { api } from "./api";
import { get, set, labels, type Resource, type Field } from "./resources";
const props = defineProps<{ resource: Resource; admin: boolean }>();
const rows = ref<any[]>([]),
  total = ref(0),
  page = ref(1),
  search = ref(""),
  loading = ref(false),
  saving = ref(false);
const error = ref(""),
  notice = ref(""),
  editing = ref(false),
  current = ref<any>({}),
  values = ref<Record<string, any>>({});
const choices = ref<Record<string, any[]>>({}),
  details = ref<any>(null),
  columns = ref<any[] | null>(null),
  mappingId = ref<number>(),
  groups = ref("[]");
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / 20)));
async function load() {
  loading.value = true;
  error.value = "";
  try {
    const data = await api(
      `/config/${props.resource.key}?page=${page.value}&size=20&searchKey=${encodeURIComponent(search.value)}`,
    );
    rows.value = data.items;
    total.value = data.total;
  } catch (e: any) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
function display(value: any) {
  if (value == null || value === "") return "—";
  return typeof value === "object"
    ? Array.isArray(value)
      ? value.join("，")
      : JSON.stringify(value)
    : String(value);
}
async function open(row?: any) {
  error.value = "";
  notice.value = "";
  try {
    current.value = row
      ? await api(`/config/${props.resource.key}/${row.id}`)
      : structuredClone(props.resource.defaults);
    values.value = {};
    for (const field of props.resource.fields) {
      const value = get(current.value, field.key);
      values.value[field.key] =
        field.type === "json"
          ? JSON.stringify(value ?? null, null, 2)
          : field.type === "multi"
            ? (value ?? []).map((item: any) => item.id)
            : (value ?? "");
      if (field.resource && !choices.value[field.resource]) {
        const items: any[] = [];
        let next = 1,
          count = 0;
        do {
          const data = await api(
            `/config/${field.resource}?page=${next++}&size=200`,
          );
          items.push(...data.items);
          count = data.total;
        } while (items.length < count);
        choices.value[field.resource] = items;
      }
    }
    editing.value = true;
  } catch (e: any) {
    error.value = e.message;
  }
}
function optionValue(field: Field, item: any) {
  return field.key === "parameters.destinationName" ? item.name : item.id;
}
function optionLabel(item: any) {
  return `${item.name || item.clusterName || item.groupKey || item.id}${item.namespace ? ` · ${item.namespace}` : ""} (#${item.id})`;
}
async function save() {
  saving.value = true;
  error.value = "";
  try {
    const body = JSON.parse(JSON.stringify(current.value));
    // 先合并高级参数，再应用独立表单字段，保持可见输入优先
    for (const field of [...props.resource.fields].sort(
      (a, b) => Number(b.type === "json") - Number(a.type === "json"),
    )) {
      let value = values.value[field.key];
      if (field.type === "json")
        value = value.trim() ? JSON.parse(value) : null;
      if (field.type === "number") value = value === "" ? null : Number(value);
      if (field.type === "multi") value = value.map((id: number) => ({ id }));
      set(body, field.key, value);
    }
    const id = current.value.id;
    await api(
      `/config/${props.resource.key}${id ? `/${id}` : ""}`,
      id ? "PUT" : "POST",
      body,
    );
    editing.value = false;
    choices.value = {};
    notice.value = "配置已保存";
    await load();
  } catch (e: any) {
    error.value =
      e instanceof SyntaxError ? "JSON 格式不正确，请检查高级参数" : e.message;
  } finally {
    saving.value = false;
  }
}
async function remove(row: any) {
  if (
    !window.confirm(
      `确认删除 ${row.name || row.id}？关联中的配置需要先解除关联。`,
    )
  )
    return;
  try {
    await api(`/config/${props.resource.key}/${row.id}`, "DELETE");
    notice.value = "配置已删除";
    await load();
  } catch (e: any) {
    error.value = e.message;
  }
}
async function operate(row: any, action: string) {
  if (
    !window.confirm(
      `确认${action === "start" ? "启动" : "停止"}通道「${row.name}」？`,
    )
  )
    return;
  saving.value = true;
  try {
    await api(`/channels/${row.id}/${action}`, "POST");
    notice.value = "任务状态已更新";
    await load();
  } catch (e: any) {
    error.value = e.message;
  } finally {
    saving.value = false;
  }
}
async function inspect(row: any) {
  try {
    details.value =
      props.resource.key === "pipelines"
        ? await api(`/pipelines/${row.id}/statistics`)
        : await api(`/config/${props.resource.key}/${row.id}`);
  } catch (e: any) {
    error.value = e.message;
  }
}
async function editColumns(row: any) {
  try {
    const data = await api(`/config/mappings/${row.id}/columns`);
    mappingId.value = row.id;
    columns.value = data.pairs;
    groups.value = JSON.stringify(data.groups, null, 2);
  } catch (e: any) {
    error.value = e.message;
  }
}
async function saveColumns() {
  saving.value = true;
  try {
    await api(`/config/mappings/${mappingId.value}/columns`, "PUT", {
      pairs: columns.value,
      groups: JSON.parse(groups.value),
    });
    columns.value = null;
    notice.value = "列映射已保存";
  } catch (e: any) {
    error.value = e.message;
  } finally {
    saving.value = false;
  }
}
watch(
  () => props.resource.key,
  () => {
    page.value = 1;
    search.value = "";
    editing.value = false;
    details.value = null;
    columns.value = null;
    choices.value = {};
    notice.value = "";
    load();
  },
);
onMounted(load);
</script>

<template>
  <section class="page-heading">
    <div>
      <p class="eyebrow">配置中心</p>
      <h1>{{ resource.title }}</h1>
      <p>{{ resource.description }}</p>
    </div>
    <button v-if="admin" class="primary" @click="open()">
      ＋ 新建{{ resource.title }}
    </button>
  </section>
  <p v-if="error" class="alert error" role="alert">{{ error }}</p>
  <p v-if="notice" class="alert success" role="status">{{ notice }}</p>
  <section class="panel">
    <form
      class="toolbar"
      @submit.prevent="
        page = 1;
        load();
      "
    >
      <div class="search">
        <span aria-hidden="true">⌕</span
        ><input
          v-model="search"
          :aria-label="`搜索${resource.title}`"
          placeholder="按名称或 ID 搜索"
        /><button type="submit">搜索</button>
      </div>
      <button type="button" @click="load()" :disabled="loading">
        {{ loading ? "加载中…" : "刷新" }}
      </button>
    </form>
    <div class="table-scroll">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th v-for="column in resource.columns" :key="column">
              {{ labels[column] || column }}
            </th>
            <th class="actions">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.id">
            <td class="muted mono">{{ row.id }}</td>
            <td v-for="column in resource.columns" :key="column">
              <span
                v-if="column === 'status'"
                class="badge"
                :class="{
                  active: row.status === 'START' || row.status === 'ENABLE',
                }"
                >{{ row.status }}</span
              ><span v-else :title="display(get(row, column))">{{
                display(get(row, column))
              }}</span>
            </td>
            <td class="actions">
              <button class="text-button" @click="inspect(row)">
                {{ resource.key === "pipelines" ? "监控" : "详情" }}</button
              ><template v-if="resource.key === 'channels'"
                ><button
                  class="text-button"
                  :disabled="saving"
                  @click="
                    operate(row, row.status === 'START' ? 'stop' : 'start')
                  "
                >
                  {{ row.status === "START" ? "停止" : "启动" }}
                </button></template
              ><template v-if="admin"
                ><button
                  v-if="resource.key === 'mappings'"
                  class="text-button"
                  @click="editColumns(row)"
                >
                  列映射</button
                ><button class="text-button" @click="open(row)">编辑</button
                ><button class="text-button danger" @click="remove(row)">
                  删除
                </button></template
              >
            </td>
          </tr>
          <tr v-if="!rows.length">
            <td :colspan="resource.columns.length + 2" class="empty">
              <div class="empty-symbol">⌘</div>
              {{ loading ? "正在读取配置…" : "暂无配置" }}
              <p v-if="!loading">
                {{
                  admin
                    ? "点击右上角按钮创建第一条配置"
                    : "配置创建后将在这里显示"
                }}
              </p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <footer class="pagination">
      <span>共 {{ total }} 条</span>
      <div>
        <button
          :disabled="page <= 1 || loading"
          @click="
            page--;
            load();
          "
        >
          上一页</button
        ><span>{{ page }} / {{ pageCount }}</span
        ><button
          :disabled="page >= pageCount || loading"
          @click="
            page++;
            load();
          "
        >
          下一页
        </button>
      </div>
    </footer>
  </section>
  <div v-if="editing" class="overlay" @click.self="editing = false">
    <section
      class="dialog"
      role="dialog"
      aria-modal="true"
      aria-labelledby="editor-title"
    >
      <header>
        <div>
          <p class="eyebrow">
            {{ current.id ? `配置 #${current.id}` : "新增配置" }}
          </p>
          <h2 id="editor-title">
            {{ current.id ? "编辑" : "新建" }}{{ resource.title }}
          </h2>
        </div>
        <button aria-label="关闭" @click="editing = false">✕</button>
      </header>
      <form @submit.prevent="save">
        <div class="form-grid">
          <label
            v-for="field in resource.fields"
            :key="field.key"
            :class="{ wide: field.type === 'json' }"
            ><span>{{ field.label }}<b v-if="field.required"> *</b></span>
            <textarea
              v-if="field.type === 'json'"
              v-model="values[field.key]"
              rows="7"
              spellcheck="false"
              :required="field.required"
              class="code-input"
            ></textarea>
            <select
              v-else-if="field.type === 'select' || field.type === 'multi'"
              v-model="values[field.key]"
              :multiple="field.type === 'multi'"
              :required="field.required"
            >
              <option v-if="field.type !== 'multi'" disabled value="">
                请选择
              </option>
              <template v-if="field.resource"
                ><option
                  v-for="item in choices[field.resource] || []"
                  :key="item.id"
                  :value="optionValue(field, item)"
                >
                  {{ optionLabel(item) }}
                </option></template
              ><template v-else
                ><option
                  v-for="item in field.options"
                  :key="item"
                  :value="item"
                >
                  {{ item }}
                </option></template
              >
            </select>
            <input
              v-else
              v-model="values[field.key]"
              :type="field.type || 'text'"
              :required="field.required"
              :autocomplete="field.type === 'password' ? 'new-password' : 'off'"
            />
            <small v-if="field.hint">{{ field.hint }}</small
            ><small v-if="field.type === 'multi'"
              >按住 Ctrl / Command 可选择多个节点</small
            >
          </label>
        </div>
        <p v-if="error" class="alert error" role="alert">{{ error }}</p>
        <footer>
          <button type="button" @click="editing = false">取消</button
          ><button type="submit" class="primary" :disabled="saving">
            {{ saving ? "保存中…" : "保存配置" }}
          </button>
        </footer>
      </form>
    </section>
  </div>
  <div v-if="details !== null" class="overlay" @click.self="details = null">
    <section
      class="dialog"
      role="dialog"
      aria-modal="true"
      aria-label="配置详情"
    >
      <header>
        <h2>
          {{ resource.key === "pipelines" ? "流水线实时监控" : "配置详情" }}
        </h2>
        <button @click="details = null" aria-label="关闭">✕</button>
      </header>
      <pre class="detail-json">{{ JSON.stringify(details, null, 2) }}</pre>
    </section>
  </div>
  <div v-if="columns !== null" class="overlay" @click.self="columns = null">
    <section class="dialog" role="dialog" aria-modal="true" aria-label="列映射">
      <header>
        <h2>列映射 · #{{ mappingId }}</h2>
        <button @click="columns = null" aria-label="关闭">✕</button>
      </header>
      <p class="muted">填写源端和目标端字段，保存前请停止所属通道</p>
      <div v-for="(column, index) in columns" :key="index" class="column-row">
        <input
          v-model="column.sourceColumn.name"
          aria-label="源字段"
          placeholder="源字段"
        /><span>→</span
        ><input
          v-model="column.targetColumn.name"
          aria-label="目标字段"
          placeholder="目标字段"
        /><button @click="columns.splice(index, 1)" aria-label="删除字段">
          ✕
        </button>
      </div>
      <button
        @click="
          columns.push({
            sourceColumn: { name: '' },
            targetColumn: { name: '' },
          })
        "
      >
        ＋ 添加字段</button
      ><label class="block-label"
        >列分组（JSON）<textarea
          v-model="groups"
          rows="6"
          class="code-input"
        ></textarea>
      </label>
      <p v-if="error" class="alert error">{{ error }}</p>
      <footer>
        <button class="primary" :disabled="saving" @click="saveColumns">
          保存列映射
        </button>
      </footer>
    </section>
  </div>
</template>

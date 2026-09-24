export interface Field {
  key: string;
  label: string;
  type?:
    "text" | "number" | "password" | "select" | "json" | "multi" | "checkbox";
  required?: boolean;
  options?: string[];
  resource?: string;
  hint?: string;
}
export interface Resource {
  key: string;
  title: string;
  description: string;
  fields: Field[];
  columns: string[];
  defaults: Record<string, any>;
}
const name: Field = { key: "name", label: "名称", required: true };
const description: Field = { key: "description", label: "说明" };
const parameters: Field = {
  key: "parameters",
  label: "高级参数（JSON）",
  type: "json",
  hint: "保留未修改的参数；请根据同步场景调整",
};
const ref = (
  key: string,
  label: string,
  resource: string,
  multi = false,
): Field => ({
  key,
  label,
  resource,
  type: multi ? "multi" : "select",
  required: true,
});
export const resources: Resource[] = [
  {
    key: "channels",
    title: "通道",
    description: "组织同步流水线，统一控制任务的运行状态",
    columns: ["name", "status", "description"],
    fields: [name, description, parameters],
    defaults: { parameters: {} },
  },
  {
    key: "pipelines",
    title: "流水线",
    description: "连接采集节点与加载节点，配置数据同步行为",
    columns: ["name", "channelId", "description"],
    fields: [
      name,
      ref("channelId", "所属通道", "channels"),
      ref("selectNodes", "采集节点", "nodes", true),
      ref("loadNodes", "加载节点", "nodes", true),
      {
        key: "parameters.destinationName",
        label: "采集配置名称",
        resource: "canals",
        type: "select",
        required: true,
      },
      description,
      parameters,
    ],
    defaults: {
      selectNodes: [],
      loadNodes: [],
      parameters: {
        destinationName: "",
        mainstemClientId: 1001,
        parallelism: 5,
        batchsize: 100,
        selectTimeout: 1000,
        ddlSync: true,
      },
    },
  },
  {
    key: "nodes",
    title: "节点",
    description: "维护执行节点的通信地址与文件下载端口",
    columns: ["name", "ip", "port", "status"],
    fields: [
      name,
      { key: "ip", label: "IP 地址", required: true },
      { key: "port", label: "通信端口", type: "number", required: true },
      {
        key: "parameters.downloadPort",
        label: "下载端口",
        type: "number",
        required: true,
      },
      ref("parameters.zkCluster.id", "协调集群", "clusters"),
      description,
      parameters,
    ],
    defaults: { port: 2088, parameters: { downloadPort: 8081 } },
  },
  {
    key: "sources",
    title: "数据源",
    description: "管理源端和目标端数据库连接",
    columns: ["name", "type", "url", "username"],
    fields: [
      name,
      {
        key: "type",
        label: "数据库类型",
        type: "select",
        options: ["MYSQL", "ORACLE"],
        required: true,
      },
      { key: "url", label: "JDBC 连接地址", required: true },
      { key: "username", label: "数据库用户名", required: true },
      {
        key: "password",
        label: "数据库密码",
        type: "password",
        hint: "编辑时留空保留已保存的密码",
      },
      { key: "encode", label: "字符集", required: true },
    ],
    defaults: {
      type: "MYSQL",
      encode: "UTF8",
      url: "jdbc:mysql://127.0.0.1:3306/database",
    },
  },
  {
    key: "tables",
    title: "数据表",
    description: "定义数据表名称、所属库与数据源",
    columns: ["name", "namespace", "source.name"],
    fields: [
      name,
      { key: "namespace", label: "数据库 / Schema", required: true },
      ref("source.id", "数据源", "sources"),
    ],
    defaults: { source: {} },
  },
  {
    key: "mappings",
    title: "表映射",
    description: "建立源表和目标表的同步关系，维护列映射",
    columns: ["pipelineId", "source.name", "target.name", "columnPairMode"],
    fields: [
      ref("pipelineId", "流水线", "pipelines"),
      ref("source.id", "源表", "tables"),
      ref("target.id", "目标表", "tables"),
      { key: "pullWeight", label: "采集权重", type: "number" },
      { key: "pushWeight", label: "加载权重", type: "number" },
      {
        key: "columnPairMode",
        label: "列映射模式",
        type: "select",
        options: ["INCLUDE", "EXCLUDE"],
      },
      { key: "filterData", label: "过滤扩展（JSON）", type: "json" },
      { key: "resolverData", label: "解析扩展（JSON）", type: "json" },
    ],
    defaults: {
      source: {},
      target: {},
      pullWeight: 1,
      pushWeight: 1,
      columnPairMode: "INCLUDE",
    },
  },
  {
    key: "canals",
    title: "采集配置",
    description: "配置 Canal 连接、账号与采集参数",
    columns: ["name", "desc"],
    fields: [
      name,
      { key: "desc", label: "说明" },
      ref("canalParameter.zkClusterId", "协调集群", "clusters"),
      {
        key: "canalParameter.dbUsername",
        label: "采集数据库用户名",
        required: true,
      },
      {
        key: "canalParameter.dbPassword",
        label: "采集数据库密码",
        type: "password",
        hint: "编辑时留空保留原密码",
      },
      {
        key: "canalParameter",
        label: "采集参数（JSON）",
        type: "json",
        required: true,
        hint: "groupDbAddresses 中 dbAddress 使用 host:port 格式；type 为 MYSQL 或 ORACLE",
      },
    ],
    defaults: {
      canalParameter: {
        runMode: "EMBEDDED",
        storageMode: "MEMORY",
        metaMode: "MIXED",
        indexMode: "MEMORY_META_FAILBACK",
        haMode: "HEARTBEAT",
        sourcingType: "MYSQL",
        dbUsername: "",
        dbPassword: "",
        groupDbAddresses: [[{ type: "MYSQL", dbAddress: "127.0.0.1:3306" }]],
        slaveId: 1234,
        detectingEnable: false,
      },
    },
  },
  {
    key: "alarms",
    title: "告警规则",
    description: "配置流水线监控条件、通知接收人与恢复策略",
    columns: ["pipelineId", "monitorName", "status", "matchValue"],
    fields: [
      ref("pipelineId", "流水线", "pipelines"),
      {
        key: "monitorName",
        label: "监控类型",
        type: "select",
        options: [
          "DELAYTIME",
          "EXCEPTION",
          "PIPELINETIMEOUT",
          "PROCESSTIMEOUT",
        ],
        required: true,
      },
      { key: "matchValue", label: "匹配值 / 阈值", required: true },
      { key: "receiverKey", label: "接收人组", required: true },
      { key: "intervalTime", label: "通知间隔（秒）", type: "number" },
      {
        key: "status",
        label: "规则状态",
        type: "select",
        options: ["ENABLE", "DISABLE"],
      },
      description,
    ],
    defaults: {
      monitorName: "DELAYTIME",
      status: "DISABLE",
      intervalTime: 1800,
      receiverKey: "otterteam",
    },
  },
  {
    key: "clusters",
    title: "协调集群",
    description: "维护 ZooKeeper 集群连接配置",
    columns: ["clusterName", "serverList", "description"],
    fields: [
      { key: "clusterName", label: "集群名称", required: true },
      {
        key: "serverList",
        label: "服务地址列表（JSON）",
        type: "json",
        required: true,
        hint: '例如 ["127.0.0.1:2181"]',
      },
      description,
    ],
    defaults: { serverList: ["127.0.0.1:2181"] },
  },
  {
    key: "matrices",
    title: "数据矩阵",
    description: "配置数据库分组及主备连接标识",
    columns: ["groupKey", "master", "slave"],
    fields: [
      { key: "groupKey", label: "分组标识", required: true },
      { key: "master", label: "主库地址", required: true },
      { key: "slave", label: "备库地址" },
      description,
    ],
    defaults: {},
  },
  {
    key: "users",
    title: "用户与权限",
    description: "管理员维护配置，操作员查看状态并启停通道",
    columns: ["name", "authorizeType", "department"],
    fields: [
      name,
      {
        key: "password",
        label: "密码",
        type: "password",
        hint: "新密码至少 8 个字符，编辑时留空保留",
      },
      {
        key: "authorizeType",
        label: "角色",
        type: "select",
        options: ["ADMIN", "OPERATOR"],
        required: true,
      },
      { key: "department", label: "部门" },
      { key: "realName", label: "姓名" },
    ],
    defaults: { authorizeType: "OPERATOR" },
  },
];
export const labels: Record<string, string> = {
  desc: "说明",
  id: "ID",
  name: "名称",
  status: "状态",
  description: "说明",
  channelId: "通道 ID",
  pipelineId: "流水线 ID",
  ip: "IP 地址",
  port: "通信端口",
  type: "类型",
  url: "连接地址",
  username: "用户名",
  namespace: "数据库 / Schema",
  "source.name": "源端",
  "target.name": "目标端",
  columnPairMode: "映射模式",
  monitorName: "监控类型",
  matchValue: "匹配值",
  clusterName: "集群名称",
  serverList: "服务地址",
  groupKey: "分组标识",
  master: "主库",
  slave: "备库",
  authorizeType: "角色",
  department: "部门",
};
export function get(object: any, path: string): any {
  return path.split(".").reduce((value, key) => value?.[key], object);
}
export function set(object: any, path: string, value: any) {
  const keys = path.split(".");
  const last = keys.pop()!;
  const target = keys.reduce((item, key) => (item[key] ??= {}), object);
  target[last] = value;
}

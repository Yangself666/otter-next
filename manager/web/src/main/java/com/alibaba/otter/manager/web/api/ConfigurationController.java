package com.alibaba.otter.manager.web.api;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToIntFunction;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import com.alibaba.otter.manager.biz.common.baseservice.GenericService;
import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.autokeeper.AutoKeeperClusterService;
import com.alibaba.otter.manager.biz.config.canal.CanalService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairGroupService;
import com.alibaba.otter.manager.biz.config.datacolumnpair.DataColumnPairService;
import com.alibaba.otter.manager.biz.config.datamatrix.DataMatrixService;
import com.alibaba.otter.manager.biz.config.datamedia.DataMediaService;
import com.alibaba.otter.manager.biz.config.datamediapair.DataMediaPairService;
import com.alibaba.otter.manager.biz.config.datamediasource.DataMediaSourceService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.user.UserService;
import com.alibaba.otter.canal.instance.manager.model.Canal;
import com.alibaba.otter.shared.common.model.autokeeper.AutoKeeperCluster;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.channel.Channel;
import com.alibaba.otter.shared.common.model.config.channel.ChannelStatus;
import com.alibaba.otter.shared.common.model.config.data.*;
import com.alibaba.otter.shared.common.model.config.data.db.DbMediaSource;
import com.alibaba.otter.shared.common.model.config.node.Node;
import com.alibaba.otter.shared.common.model.config.node.NodeStatus;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.user.User;

@RestController
@RequestMapping("/api/config")
public class ConfigurationController {

    @Resource private ChannelService channelService;
    @Resource private PipelineService pipelineService;
    @Resource private NodeService nodeService;
    @Resource private DataMediaSourceService dataMediaSourceService;
    @Resource private DataMediaService dataMediaService;
    @Resource private DataMediaPairService dataMediaPairService;
    @Resource private DataColumnPairService dataColumnPairService;
    @Resource private DataColumnPairGroupService dataColumnPairGroupService;
    @Resource private CanalService canalService;
    @Resource private DataMatrixService dataMatrixService;
    @Resource private AlarmRuleService alarmRuleService;
    @Resource private AutoKeeperClusterService autoKeeperClusterService;
    @Resource private UserService userService;
    @Resource private PasswordEncoder passwordEncoder;
    @Resource private ObjectMapper objectMapper;

    private final Map<String, ConfigResource> resources = new LinkedHashMap<>();

    private record ConfigResource(Class<?> type, Function<Map, List<?>> list, ToIntFunction<Map> count,
                                  LongFunction<?> find, Consumer<Object> create, Consumer<Object> update,
                                  Consumer<Long> delete) {
    }

    @PostConstruct
    public void initialize() {
        register("channels", Channel.class, channelService);
        register("pipelines", Pipeline.class, pipelineService);
        register("nodes", Node.class, nodeService);
        register("sources", DbMediaSource.class, dataMediaSourceService);
        register("tables", DataMedia.class, dataMediaService);
        register("mappings", DataMediaPair.class, dataMediaPairService);
        resources.put("canals", new ConfigResource(Canal.class, canalService::listByCondition, canalService::getCount,
            canalService::findById, value -> canalService.create((Canal) value),
            value -> canalService.modify((Canal) value), canalService::remove));
        resources.put("matrices", new ConfigResource(DataMatrix.class, dataMatrixService::listByCondition,
            dataMatrixService::getCount, dataMatrixService::findById, value -> dataMatrixService.create((DataMatrix) value),
            value -> dataMatrixService.modify((DataMatrix) value), dataMatrixService::remove));
        resources.put("alarms", new ConfigResource(AlarmRule.class, alarmRuleService::listAllAlarmRules,
            condition -> alarmRuleService.getCount(), alarmRuleService::getAlarmRuleById,
            value -> alarmRuleService.create((AlarmRule) value), value -> alarmRuleService.modify((AlarmRule) value),
            alarmRuleService::remove));
        resources.put("clusters", new ConfigResource(AutoKeeperCluster.class,
            condition -> autoKeeperClusterService.listAutoKeeperClusters(), condition -> autoKeeperClusterService.getCount(),
            autoKeeperClusterService::findAutoKeeperClusterById,
            value -> autoKeeperClusterService.createAutoKeeperCluster((AutoKeeperCluster) value),
            value -> autoKeeperClusterService.modifyAutoKeeperCluster((AutoKeeperCluster) value),
            autoKeeperClusterService::removeAutoKeeperCluster));
        resources.put("users", new ConfigResource(User.class, userService::listByCondition, userService::getCount,
            userService::findUserById, value -> userService.createUser((User) value),
            value -> userService.updataUser((User) value), userService::deleteUser));
    }

    private <T> void register(String name, Class<? extends T> type, GenericService<T> service) {
        resources.put(name, new ConfigResource(type, service::listByCondition, service::getCount, service::findById,
            value -> service.create((T) value), value -> service.modify((T) value), service::remove));
    }

    @GetMapping("/{resource}")
    public Object list(@PathVariable String resource, @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "") String searchKey,
                       @RequestParam(required = false) Long pipelineId, @RequestParam(required = false) Long channelId) {
        if (page < 1 || size < 1 || size > 200) {
            throw new IllegalArgumentException("Invalid pagination");
        }
        ConfigResource config = resource(resource);
        Map<String, Object> condition = new HashMap<>();
        condition.put("offset", Math.multiplyExact(page - 1, size));
        condition.put("length", size);
        condition.put("searchKey", searchKey);
        if (pipelineId != null) condition.put("pipelineId", pipelineId);
        if (channelId != null) condition.put("channelId", channelId);
        if (resource.equals("clusters") || resource.equals("alarms")) {
            List<?> items = config.list.apply(new HashMap<>()).stream().filter(item -> {
                BeanWrapper properties = new BeanWrapperImpl(item);
                boolean matchesPipeline = pipelineId == null || !properties.isReadableProperty("pipelineId")
                    || pipelineId.equals(properties.getPropertyValue("pipelineId"));
                return matchesPipeline && (searchKey.isBlank()
                    || sanitize(item).toString().toLowerCase().contains(searchKey.toLowerCase()));
            }).toList();
            return sanitize(Map.of("items", items.stream().skip((long) (page - 1) * size).limit(size).toList(),
                                   "total", items.size(), "page", page, "size", size));
        }
        return sanitize(Map.of("items", config.list.apply(condition), "total", config.count.applyAsInt(condition),
                               "page", page, "size", size));
    }

    @GetMapping("/{resource}/{id}")
    public Object detail(@PathVariable String resource, @PathVariable Long id) {
        return sanitize(resource(resource).find.apply(id));
    }

    @PostMapping("/{resource}")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@PathVariable String resource, @RequestBody Map<String, Object> body) {
        body.remove("id");
        ConfigResource config = resource(resource);
        Object value = model(config, body);
        prepare(resource, value, null);
        config.create.accept(value);
    }

    @PutMapping("/{resource}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable String resource, @PathVariable Long id, @RequestBody Map<String, Object> body,
                       Authentication authentication) {
        ConfigResource config = resource(resource);
        Object previous = config.find.apply(id);
        body.put("id", id);
        preserveSecrets(body, objectMapper.convertValue(previous, Map.class));
        Object value = model(config, body);
        prepare(resource, value, previous);
        if (previous instanceof User old && old.getName().equals(authentication.getName())) {
            User user = (User) value;
            require(old.getName().equals(user.getName()) && old.getAuthorizeType() == user.getAuthorizeType(),
                    "当前账号的名称和角色需要由其他管理员修改");
        }
        config.update.accept(value);
    }

    @DeleteMapping("/{resource}/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String resource, @PathVariable Long id, Authentication authentication) {
        ConfigResource config = resource(resource);
        switch (resource) {
            case "channels" -> {
                stopped(channelService.findById(id));
                require(pipelineService.listByChannelIds(id).isEmpty(), "请先删除通道下的流水线");
            }
            case "pipelines" -> {
                stopped(channelService.findByPipelineId(id));
                require(dataMediaPairService.listByPipelineId(id).isEmpty(), "请先删除流水线下的表映射");
            }
            case "mappings" -> stopped(channelService.findByPipelineId(dataMediaPairService.findById(id).getPipelineId()));
            case "nodes" -> require(!pipelineService.hasRelation(id) && !nodeService.findById(id).getStatus().isStart(),
                                     "请先停止节点并解除流水线关联");
            case "sources" -> require(dataMediaService.listByDataMediaSourceId(id).isEmpty(), "数据源仍被数据表引用");
            case "tables" -> require(dataMediaPairService.listByDataMediaId(id).isEmpty(), "数据表仍被映射引用");
            case "canals" -> require(pipelineService.listByDestinationWithoutOther(canalService.findById(id).getName()).isEmpty(),
                                      "采集配置仍被流水线引用");
            case "clusters" -> {
                require(nodeService.listAll().stream().noneMatch(node -> node.getParameters().getZkCluster() != null
                    && id.equals(node.getParameters().getZkCluster().getId())), "协调集群仍被节点引用");
                require(canalService.listAll().stream().noneMatch(canal -> id.equals(canal.getCanalParameter().getZkClusterId())),
                        "协调集群仍被采集配置引用");
            }
            case "users" -> require(!userService.findUserById(id).getName().equals(authentication.getName()),
                                     "当前登录账号不能删除自身");
            default -> { }
        }
        config.delete.accept(id);
    }

    private Object model(ConfigResource config, Map<String, Object> body) {
        BeanWrapperImpl properties = new BeanWrapperImpl(config.type);
        Map<String, Object> writable = new LinkedHashMap<>();
        body.forEach((key, value) -> {
            if (properties.isWritableProperty(key) && !key.startsWith("gmt")) writable.put(key, value);
        });
        return objectMapper.convertValue(writable, config.type);
    }

    private void prepare(String resource, Object value, Object previous) {
        if (value instanceof Channel channel) {
            require(channel.getName() != null && !channel.getName().isBlank(), "请输入通道名称");
            if (previous instanceof Channel old) {
                stopped(old);
                channel.setStatus(old.getStatus());
            } else channel.setStatus(ChannelStatus.STOP);
        } else if (value instanceof Pipeline pipeline) {
            require(pipeline.getName() != null && !pipeline.getName().isBlank() && pipeline.getChannelId() != null
                && pipeline.getParameters() != null && pipeline.getParameters().getDestinationName() != null
                && !pipeline.getParameters().getDestinationName().isBlank(), "请输入流水线名称并选择通道和采集配置");
            stopped(channelService.findById(pipeline.getChannelId()));
            if (previous instanceof Pipeline old) stopped(channelService.findById(old.getChannelId()));
            require(pipeline.getSelectNodes() != null && !pipeline.getSelectNodes().isEmpty(), "请选择采集节点");
            require(pipeline.getLoadNodes() != null && !pipeline.getLoadNodes().isEmpty(), "请选择加载节点");
            require(canalService.findByName(pipeline.getParameters().getDestinationName()) != null, "采集配置不存在");
            pipeline.getSelectNodes().forEach(node -> nodeService.findById(node.getId()));
            pipeline.getLoadNodes().forEach(node -> nodeService.findById(node.getId()));
            if (pipeline.getExtractNodes() == null || pipeline.getExtractNodes().isEmpty()) {
                pipeline.setExtractNodes(pipeline.getSelectNodes());
            }
            require(pipelineService.listByDestinationWithoutOther(pipeline.getParameters().getDestinationName()).stream()
                .allMatch(item -> item.getId().equals(pipeline.getId())), "采集配置已经关联其他流水线");
        } else if (value instanceof Node node) {
            require(node.getName() != null && node.getIp() != null && node.getPort() != null,
                    "请输入节点名称、地址和通信端口");
            require(node.getPort() > 0 && node.getPort() <= 65535 && node.getParameters() != null
                && node.getParameters().getDownloadPort() != null && node.getParameters().getDownloadPort() > 0
                && node.getParameters().getDownloadPort() <= 65535, "请输入有效的通信端口与下载端口");
            require(node.getParameters().getZkCluster() != null && node.getParameters().getZkCluster().getId() != null,
                    "请选择节点使用的协调集群");
            node.getParameters().setZkCluster(autoKeeperClusterService.findAutoKeeperClusterById(
                node.getParameters().getZkCluster().getId()));
            if (previous instanceof Node old) require(!old.getStatus().isStart(), "请先停止节点再修改配置");
            node.setStatus(previous instanceof Node old ? old.getStatus() : NodeStatus.STOP);
        } else if (value instanceof DbMediaSource source) {
            if (previous instanceof DbMediaSource old) {
                for (DataMedia media : dataMediaService.listByDataMediaSourceId(old.getId())) stoppedTable(media.getId());
            }
            require(source.getType() != null && (source.getType().isMysql() || source.getType().isOracle()),
                    "请选择 MySQL 或 Oracle 数据源");
            require(source.getName() != null && source.getUrl() != null, "请输入数据源名称和连接地址");
            source.setDriver(source.getType().isMysql() ? "com.mysql.jdbc.Driver" : "oracle.jdbc.OracleDriver");
            if (previous instanceof DbMediaSource old && (source.getPassword() == null || source.getPassword().isEmpty())) {
                source.setPassword(old.getPassword());
            }
        } else if (value instanceof DataMedia media) {
            if (previous instanceof DataMedia old) stoppedTable(old.getId());
            require(media.getSource() != null && media.getSource().getId() != null, "请选择数据源");
            require(media.getName() != null && !media.getName().isBlank()
                && media.getNamespace() != null && !media.getNamespace().isBlank(), "请输入表名和所属数据库");
            media.setSource(dataMediaSourceService.findById(media.getSource().getId()));
        } else if (value instanceof DataMediaPair pair) {
            stopped(channelService.findByPipelineId(pair.getPipelineId()));
            if (previous instanceof DataMediaPair old) stopped(channelService.findByPipelineId(old.getPipelineId()));
            require(pair.getSource() != null && pair.getTarget() != null, "请选择源表和目标表");
            pair.setSource(dataMediaService.findById(pair.getSource().getId()));
            pair.setTarget(dataMediaService.findById(pair.getTarget().getId()));
        } else if (value instanceof Canal canal) {
            require(canal.getName() != null && !canal.getName().isBlank() && canal.getCanalParameter() != null,
                    "请输入采集配置名称和采集参数");
            require(canal.getCanalParameter().getZkClusterId() != null, "请选择采集配置使用的协调集群");
            autoKeeperClusterService.findAutoKeeperClusterById(canal.getCanalParameter().getZkClusterId());
            if (previous instanceof Canal old) {
                List<Pipeline> linked = pipelineService.listByDestinationWithoutOther(old.getName());
                require(linked.isEmpty() || old.getName().equals(canal.getName()), "请先解除流水线关联再修改采集配置名称");
                for (Pipeline pipeline : linked) {
                    stopped(channelService.findById(pipeline.getChannelId()));
                }
            }
        } else if (value instanceof AlarmRule rule) {
            require(rule.getPipelineId() != null && rule.getMonitorName() != null && rule.getStatus() != null
                && rule.getMatchValue() != null && rule.getReceiverKey() != null, "请完整填写告警规则");
            pipelineService.findById(rule.getPipelineId());
        } else if (value instanceof AutoKeeperCluster cluster) {
            if (previous instanceof AutoKeeperCluster old) {
                require(nodeService.listAll().stream().noneMatch(node -> node.getStatus().isStart()
                    && node.getParameters().getZkCluster() != null && old.getId().equals(node.getParameters().getZkCluster().getId())),
                    "请先停止使用该协调集群的节点");
            }
            require(cluster.getClusterName() != null && !cluster.getClusterName().isBlank()
                && cluster.getServerList() != null && !cluster.getServerList().isEmpty(), "请输入集群名称和地址列表");
        } else if (value instanceof DataMatrix matrix) {
            require(matrix.getGroupKey() != null && !matrix.getGroupKey().isBlank() && matrix.getMaster() != null,
                    "请输入分组标识和主库地址");
        } else if (value instanceof User user) {
            if (user.getDepartment() == null) user.setDepartment("");
            if (user.getRealName() == null) user.setRealName("");
            require(user.getName() != null && !user.getName().isBlank() && user.getAuthorizeType() != null
                    && !user.getAuthorizeType().isAnonymous(),
                    "请输入账号名称并选择角色");
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                require(user.getPassword().length() >= 8
                    && user.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 72,
                    "密码至少需要 8 个字符，UTF-8 编码长度不超过 72 字节");
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                require(previous != null, "请输入初始密码");
                user.setPassword(null);
            }
        }
    }

    @GetMapping("/mappings/{id}/columns")
    public Object columns(@PathVariable Long id) {
        return Map.of("pairs", dataColumnPairService.listByDataMediaPairId(id),
                      "groups", dataColumnPairGroupService.listByDataMediaPairId(id));
    }

    @PutMapping("/mappings/{id}/columns")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @org.springframework.transaction.annotation.Transactional
    public void columns(@PathVariable Long id, @RequestBody ColumnMapping mapping) {
        stopped(channelService.findByPipelineId(dataMediaPairService.findById(id).getPipelineId()));
        require(mapping.pairs() != null && mapping.groups() != null, "请提供列映射及列分组");
        for (ColumnPair column : mapping.pairs()) {
            require(column.getSourceColumn() != null && column.getTargetColumn() != null
                && column.getSourceColumn().getName() != null && !column.getSourceColumn().getName().isBlank()
                && column.getTargetColumn().getName() != null && !column.getTargetColumn().getName().isBlank(),
                "源字段和目标字段不能为空");
            column.setId(null);
            column.setDataMediaPairId(id);
        }
        dataColumnPairGroupService.removeByDataMediaPairId(id);
        dataColumnPairService.removeByDataMediaPairId(id);
        dataColumnPairService.createBatch(mapping.pairs());
        for (ColumnGroup group : mapping.groups()) {
            group.setId(null);
            group.setDataMediaPairId(id);
            dataColumnPairGroupService.create(group);
        }
    }

    public record ColumnMapping(List<ColumnPair> pairs, List<ColumnGroup> groups) {
    }

    // 页面不返回密码，更新时保留未重新输入的数据库与采集账号凭据
    private void preserveSecrets(Map<String, Object> incoming, Map<String, Object> previous) {
        previous.forEach((key, value) -> {
            Object replacement = incoming.get(key);
            if (key.toLowerCase().contains("password") && (replacement == null || replacement.equals(""))) {
                if (!previous.containsKey("authorizeType")) incoming.put(key, value);
            } else if (value instanceof Map old && replacement instanceof Map current) {
                preserveSecrets(current, old);
            }
        });
    }

    public Object sanitize(Object value) {
        return redact(objectMapper.convertValue(value, Object.class));
    }

    private Object redact(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                if (!key.toString().toLowerCase().contains("password")) result.put(key.toString(), redact(item));
            });
            return result;
        }
        if (value instanceof List<?> list) return list.stream().map(this::redact).toList();
        return value;
    }

    private ConfigResource resource(String name) {
        ConfigResource resource = resources.get(name);
        if (resource == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "配置类型不存在");
        return resource;
    }

    private void stoppedTable(Long id) {
        for (DataMediaPair pair : dataMediaPairService.listByDataMediaId(id)) {
            stopped(channelService.findByPipelineId(pair.getPipelineId()));
        }
    }

    static void stopped(Channel channel) {
        require(channel != null && !channel.getStatus().isStart(), "请先停止所属通道再修改配置");
    }

    static void require(boolean condition, String message) {
        if (!condition) throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}

package com.alibaba.otter.manager.web.api;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.alibaba.otter.manager.biz.config.alarm.AlarmRuleService;
import com.alibaba.otter.manager.biz.config.channel.ChannelService;
import com.alibaba.otter.manager.biz.config.node.NodeService;
import com.alibaba.otter.manager.biz.config.parameter.SystemParameterService;
import com.alibaba.otter.manager.biz.config.pipeline.PipelineService;
import com.alibaba.otter.manager.biz.config.record.LogRecordService;
import com.alibaba.otter.manager.biz.statistics.delay.DelayStatService;
import com.alibaba.otter.manager.biz.statistics.stage.ProcessStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.ThroughputStatService;
import com.alibaba.otter.manager.biz.statistics.throughput.param.AnalysisType;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.shared.common.model.config.parameter.SystemParameter;
import com.alibaba.otter.shared.common.model.statistics.throughput.ThroughputType;

@RestController
@RequestMapping("/api")
public class OperationsController {

    @Resource private ChannelService channelService;
    @Resource private PipelineService pipelineService;
    @Resource private NodeService nodeService;
    @Resource private AlarmRuleService alarmRuleService;
    @Resource private SystemParameterService systemParameterService;
    @Resource private LogRecordService logRecordService;
    @Resource private DelayStatService delayStatService;
    @Resource private ThroughputStatService throughputStatService;
    @Resource private ProcessStatService processStatService;
    @Resource private ConfigurationController configurationController;

    @GetMapping("/overview")
    public Object overview() {
        var channels = channelService.listOnlyChannels();
        return Map.of("channels", channels.size(), "running", channels.stream().filter(item -> item.getStatus().isStart()).count(),
                      "pipelines", pipelineService.getCount(new HashMap<>()), "nodes", nodeService.getCount(new HashMap<>()),
                      "topDelay", delayStatService.listTopDelayStat("", 10));
    }

    @PostMapping("/channels/{id}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void start(@PathVariable Long id) {
        var pipelines = pipelineService.listByChannelIds(id);
        ConfigurationController.require(!pipelines.isEmpty(), "请先配置流水线");
        for (var pipeline : pipelines) {
            ConfigurationController.require(pipeline.getSelectNodes().stream().anyMatch(node -> node.getStatus().isStart())
                && pipeline.getLoadNodes().stream().anyMatch(node -> node.getStatus().isStart()),
                "请先启动流水线的采集节点和加载节点");
        }
        channelService.startChannel(id);
    }

    @PostMapping("/channels/{id}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stop(@PathVariable Long id) {
        channelService.stopChannel(id);
    }

    @PostMapping("/alarms/{id}/{operation:enable|disable}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void monitor(@PathVariable Long id, @PathVariable String operation) {
        if (operation.equals("enable")) alarmRuleService.enableMonitor(id);
        else alarmRuleService.disableMonitor(id);
    }

    @GetMapping("/pipelines/{id}/statistics")
    public Object statistics(@PathVariable Long id) {
        RealtimeThroughputCondition condition = new RealtimeThroughputCondition();
        condition.setPipelineId(id);
        condition.setType(ThroughputType.ROW);
        condition.setAnalysisType(List.of(AnalysisType.values()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("delay", delayStatService.findRealtimeDelayStat(id));
        result.put("throughput", throughputStatService.listRealtimeThroughput(condition));
        result.put("processes", processStatService.listRealtimeProcessStat(id));
        Date end = new Date();
        result.put("timeline", delayStatService.listTimelineDelayStat(id, new Date(end.getTime() - 3600000), end));
        return configurationController.sanitize(result);
    }

    @GetMapping("/logs")
    public Object logs(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) Long pipelineId) {
        if (page < 1 || size < 1 || size > 200) throw new IllegalArgumentException("Invalid pagination");
        Map<String, Object> condition = new HashMap<>();
        condition.put("offset", Math.multiplyExact(page - 1, size));
        condition.put("length", size);
        if (pipelineId != null) condition.put("pipelineId", pipelineId);
        return Map.of("items", logRecordService.listByCondition(condition), "total", logRecordService.getCount(condition));
    }

    @GetMapping("/settings")
    public Object settings() {
        return configurationController.sanitize(systemParameterService.find());
    }

    @PutMapping("/settings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void settings(@RequestBody SystemParameter parameters) {
        ConfigurationController.require(parameters.getSystemSchema() != null && !parameters.getSystemSchema().isBlank(),
                                        "请输入系统库名称");
        systemParameterService.createOrUpdate(parameters);
    }
}

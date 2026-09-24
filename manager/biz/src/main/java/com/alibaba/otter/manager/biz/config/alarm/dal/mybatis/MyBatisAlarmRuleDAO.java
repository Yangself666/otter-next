/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.manager.biz.config.alarm.dal.mybatis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.alarm.dal.AlarmRuleDAO;
import com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRuleStatus;

/**
 * @author simon
 */
public class MyBatisAlarmRuleDAO extends SqlSessionDaoSupport implements AlarmRuleDAO {

    public AlarmRuleDO insert(AlarmRuleDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.insertAlarmRule", entityObj);
        return entityObj;
    }

    public void update(AlarmRuleDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.updateAlarmRule", entityObj);
    }

    public void delete(Long id) {
        Assert.assertNotNull(id);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.deleteAlarmRuleById", id);
    }

    public AlarmRuleDO findById(Long alarmRuleId) {
        Assert.assertNotNull(alarmRuleId);
        AlarmRuleDO alarmRuleDo = (AlarmRuleDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.findByRuleId", alarmRuleId);
        return alarmRuleDo;
    }

    public List<AlarmRuleDO> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        List<AlarmRuleDO> alarmRuleDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.listAlarmByPipelineId", pipelineId);
        return alarmRuleDos;
    }

    public List<AlarmRuleDO> listByPipelineId(Long pipelineId, AlarmRuleStatus status) {
        List<AlarmRuleDO> alarmRuleDos = listByPipelineId(pipelineId);
        List<AlarmRuleDO> result = new ArrayList<AlarmRuleDO>();
        for (AlarmRuleDO alarmRuleDo : alarmRuleDos) {
            if (alarmRuleDo.getStatus().equals(status)) {
                result.add(alarmRuleDo);
            }
        }
        return result;
    }

    public List<AlarmRuleDO> listAll() {
        List<AlarmRuleDO> alarmRuleDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.listAllAlarmRule");
        return alarmRuleDos;
    }

    public List<AlarmRuleDO> listAllByPipeline(Map condition) {
        List<AlarmRuleDO> alarmRuleDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.listAllAlarmOrderByPipeline",
                                                                                condition);
        return alarmRuleDos;
    }

    public List<AlarmRuleDO> listByStatus(AlarmRuleStatus status) {
        List<AlarmRuleDO> alarmRuleDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.listAlarmByStatus", status);
        return alarmRuleDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.alarm.dal.dataobject.AlarmRuleDO.getAlarmRuleCount");
        return count.intValue();
    }

}

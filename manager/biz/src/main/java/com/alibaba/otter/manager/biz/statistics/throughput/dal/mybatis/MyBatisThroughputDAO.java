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

package com.alibaba.otter.manager.biz.statistics.throughput.dal.mybatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.manager.biz.statistics.throughput.dal.ThroughputDAO;
import com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO;
import com.alibaba.otter.manager.biz.statistics.throughput.param.RealtimeThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.ThroughputCondition;
import com.alibaba.otter.manager.biz.statistics.throughput.param.TimelineThroughputCondition;

/**
 * @author simon
 */
public class MyBatisThroughputDAO extends SqlSessionDaoSupport implements ThroughputDAO {

    @Override
    public void insertThroughputStat(ThroughputStatDO throughputStat) {

        getSqlSession().insert("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.insertThroughputStat", throughputStat);
    }

    @Override
    public void deleteThroughputStat(Long throughputStatId) {
        getSqlSession().delete("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.deleteThroughputStat", throughputStatId);
    }

    @Override
    public void modifyThroughputStat(ThroughputStatDO throughputStat) {
        getSqlSession().update("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.modifyThroughputStat", throughputStat);
    }

    @Override
    public ThroughputStatDO findThroughputStatById(Long throughputStatId) {

        return (ThroughputStatDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.findThroughputStatById", throughputStatId);
    }

    @Override
    public List<ThroughputStatDO> listRealtimeThroughputStat(RealtimeThroughputCondition condition) {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.listRealtimeThroughputStat", condition);
    }

    @Override
    public List<ThroughputStatDO> listTimelineThroughputStat(TimelineThroughputCondition condition) {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.listTimelineThroughputStat", condition);
    }

    @Override
    public List<ThroughputStatDO> listThroughputStatByPipelineId(Long pipelineId) {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.listThroughputStatByPipelineId",
                                                                               pipelineId);
    }

    @Override
    public ThroughputStatDO findRealtimeThroughputStat(ThroughputCondition condition) {

        return (ThroughputStatDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.findRealtimeThroughputStat", condition);
    }

    public List<ThroughputStatDO> listRealTimeThroughputStatByPipelineIds(List<Long> pipelineIds, int minute) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("pipelineIds", pipelineIds);
        param.put("minute", minute);
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.throughput.dal.dataobject.ThroughputStatDO.listRealtimeThroughputStatByPipelineIds",
                                                                               param);
    }
}

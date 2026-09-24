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

package com.alibaba.otter.manager.biz.config.pipeline.dal.mybatis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.manager.biz.config.pipeline.dal.PipelineDAO;
import com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO;
import com.alibaba.otter.shared.common.utils.Assert;

/**
 * Pipeline的DAO层，MyBatis 实现，提供配置持久化操作
 * 
 * @author simon
 */
public class MyBatisPipelineDAO extends SqlSessionDaoSupport implements PipelineDAO {

    public PipelineDO insert(PipelineDO pipelineDo) {
        Assert.assertNotNull(pipelineDo);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.insertPipeline", pipelineDo);
        return pipelineDo;
    }

    public void delete(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.deletePipelineById", pipelineId);
    }

    public void update(PipelineDO pipelineDO) {
        Assert.assertNotNull(pipelineDO);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.updatePipeline", pipelineDO);
    }

    public boolean checkUnique(PipelineDO pipelineDO) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.checkPipelineUnique", pipelineDO);
        return count == 0 ? true : false;
    }

    public PipelineDO findById(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        return (PipelineDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.findPipelineById", pipelineId);
    }

    public List<PipelineDO> listByChannelIds(Long... channelId) {
        Assert.assertNotNull(channelId);
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.listPipelinesByChannelIds", channelId);
    }

    public List<PipelineDO> listByCondition(Map condition) {
        List<PipelineDO> pipelineDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.listPipelines", condition);
        return pipelineDos;
    }

    public List<PipelineDO> listAll() {
        List<PipelineDO> pipelines = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.listPipelines");
        return pipelines;
    }

    public List<PipelineDO> listByMultiId(Long... identities) {
        List<PipelineDO> pipelineDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.listPipelineByIds", identities);
        return pipelineDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.getPipelineCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.getPipelineCount", condition);
        return count.intValue();
    }

    public List<PipelineDO> listByDestinationCondition(String canalName) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("searchKey", canalName);
        List<PipelineDO> pipelineDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.pipeline.dal.dataobject.PipelineDO.listByDestinationCondition", map);
        return pipelineDos;
    }

}

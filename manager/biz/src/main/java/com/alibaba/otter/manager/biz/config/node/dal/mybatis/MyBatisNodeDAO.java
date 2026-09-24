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

package com.alibaba.otter.manager.biz.config.node.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.node.dal.NodeDAO;
import com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO;

/**
 * Node的DAO层，MyBatis 实现，提供配置持久化操作
 * 
 * @author simon
 */
public class MyBatisNodeDAO extends SqlSessionDaoSupport implements NodeDAO {

    public NodeDO insert(NodeDO node) {
        Assert.assertNotNull(node);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.insertNode", node);
        return node;
    }

    public void delete(Long nodeId) {
        Assert.assertNotNull(nodeId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.deleteNodeById", nodeId);
    }

    public void update(NodeDO node) {
        Assert.assertNotNull(node);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.updateNode", node);
    }

    public boolean checkUnique(NodeDO node) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.checkNodeUnique", node);
        return count == 0 ? true : false;
    }

    public List<NodeDO> listByCondition(Map condition) {
        List<NodeDO> nodeDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.listNodes", condition);
        return nodeDos;
    }

    public NodeDO findById(Long nodeId) {
        Assert.assertNotNull(nodeId);
        return (NodeDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.findNodeById", nodeId);
    }

    public List<NodeDO> listAll() {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.listNodes");
    }

    public List<NodeDO> listByMultiId(Long... identities) {
        List<NodeDO> nodeDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.listNodeByIds", identities);
        return nodeDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.getNodeCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.node.dal.dataobject.NodeDO.getNodeCount", condition);
        return count.intValue();
    }

}

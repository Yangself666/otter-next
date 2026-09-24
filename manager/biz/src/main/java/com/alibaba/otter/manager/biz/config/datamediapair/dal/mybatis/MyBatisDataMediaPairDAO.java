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

package com.alibaba.otter.manager.biz.config.datamediapair.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.DataMediaPairDAO;
import com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO;
import com.alibaba.otter.shared.common.model.config.data.ColumnPair;

/**
 * DataMediaPair的DAO层，MyBatis 实现，提供配置持久化操作
 * 
 * @author simon
 */
public class MyBatisDataMediaPairDAO extends SqlSessionDaoSupport implements DataMediaPairDAO {

    public DataMediaPairDO insert(DataMediaPairDO dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.insertDataMediaPair", dataMediaPair);
        return dataMediaPair;
    }

    public void insertColumnPairs(List<ColumnPair> ColumnPairs) {
        Assert.assertNotNull(ColumnPairs);
        getSqlSession().insert("insertColumnPairs", ColumnPairs);
    }

    public void delete(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.deleteDataMediaPairById", dataMediaPairId);
    }

    public void update(DataMediaPairDO dataMediaPair) {
        Assert.assertNotNull(dataMediaPair);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.updateDataMediaPair", dataMediaPair);
    }

    public boolean checkUnique(DataMediaPairDO dataMediaPair) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.checkDataMediaPairUnique", dataMediaPair);
        return count == 0 ? true : false;
    }

    public DataMediaPairDO findById(Long dataMediaPairId) {
        Assert.assertNotNull(dataMediaPairId);
        return (DataMediaPairDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.findDataMediaPairById", dataMediaPairId);
    }

    public List<DataMediaPairDO> listAll() {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.listDataMediaPairs");
    }

    public List<DataMediaPairDO> listByPipelineId(Long pipelineId) {
        Assert.assertNotNull(pipelineId);
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.listDataMediaPairsByPipelineId",
                                                                              pipelineId);
    }

    public List<DataMediaPairDO> listByCondition(Map condition) {
        List<DataMediaPairDO> dataMediaPairDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.listDataMediaPairs", condition);
        return dataMediaPairDos;
    }

    public List<DataMediaPairDO> listByDataMediaId(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.listDataMediaPairsByDataMediaId",
                                                                              dataMediaId);
    }

    public List<DataMediaPairDO> listByMultiId(Long... identities) {
        List<DataMediaPairDO> dataMediaPairDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.listDataMediaPairByIds",
                                                                                        identities);
        return dataMediaPairDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.getDataMediaPairCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediapair.dal.dataobject.DataMediaPairDO.getDataMediaPairCount", condition);
        return count.intValue();
    }

}

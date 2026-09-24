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

package com.alibaba.otter.manager.biz.config.datamediasource.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamediasource.dal.DataMediaSourceDAO;
import com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO;

/**
 * DataMediaSource的DAO层，MyBatis 实现，提供配置持久化操作
 * 
 * @author simon
 */
public class MyBatisDataMediaSourceDAO extends SqlSessionDaoSupport implements DataMediaSourceDAO {

    public DataMediaSourceDO insert(DataMediaSourceDO dataMediaSourceDO) {
        Assert.assertNotNull(dataMediaSourceDO);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.insertDataMediaSource", dataMediaSourceDO);
        return dataMediaSourceDO;
    }

    public void delete(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.deleteDataMediaSourceById", dataMediaSourceId);
    }

    public void update(DataMediaSourceDO dataMediaSourceDO) {
        Assert.assertNotNull(dataMediaSourceDO);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.updateDataMediaSource", dataMediaSourceDO);
    }

    public boolean checkUnique(DataMediaSourceDO dataMediaSourceDO) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.checkDataMediaSourceUnique", dataMediaSourceDO);
        return count == 0 ? true : false;
    }

    public DataMediaSourceDO findById(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        return (DataMediaSourceDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.findDataMediaSourceById",
                                                                            dataMediaSourceId);
    }

    public List<DataMediaSourceDO> listByCondition(Map condition) {
        List<DataMediaSourceDO> dataMediaSourceDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.listDataMediaSources",
                                                                                            condition);
        return dataMediaSourceDos;
    }

    public List<DataMediaSourceDO> listAll() {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.listDataMediaSources");
    }

    public List<DataMediaSourceDO> listByMultiId(Long... identities) {
        List<DataMediaSourceDO> dataMediaSourceDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.listSourceByIds",
                                                                                            identities);
        return dataMediaSourceDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.getSourceCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamediasource.dal.dataobject.DataMediaSourceDO.getSourceCount", condition);
        return count.intValue();
    }

}

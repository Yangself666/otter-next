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

package com.alibaba.otter.manager.biz.config.datamedia.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamedia.dal.DataMediaDAO;
import com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO;

/**
 * DataMedia的DAO层，MyBatis 实现，提供配置持久化操作
 * 
 * @author simon
 */
public class MyBatisDataMediaDAO extends SqlSessionDaoSupport implements DataMediaDAO {

    public DataMediaDO insert(DataMediaDO dataMedia) {
        Assert.assertNotNull(dataMedia);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.insertDataMedia", dataMedia);
        return dataMedia;
    }

    public void delete(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.deleteDataMediaById", dataMediaId);
    }

    public void update(DataMediaDO dataMedia) {
        Assert.assertNotNull(dataMedia);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.updateDataMedia", dataMedia);
    }

    public boolean checkUnique(DataMediaDO dataMedia) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.checkDataMediaUnique", dataMedia);
        return count == 0 ? true : false;
    }

    public DataMediaDO checkUniqueAndReturnExist(DataMediaDO dataMedia) {
        return (DataMediaDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.checkDataMediaUniqueAndReturnTheExist",
                                                                      dataMedia);
    }

    public DataMediaDO findById(Long dataMediaId) {
        Assert.assertNotNull(dataMediaId);
        return (DataMediaDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.findDataMediaById", dataMediaId);
    }

    public List<DataMediaDO> listAll() {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.listDataMedias");
    }

    public List<DataMediaDO> listByDataMediaSourceId(Long dataMediaSourceId) {
        Assert.assertNotNull(dataMediaSourceId);
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.listDataMediasByDataMediaSourceId",
                                                                          dataMediaSourceId);
    }

    public List<DataMediaDO> listByCondition(Map condition) {
        List<DataMediaDO> dataMediaDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.listDataMedias", condition);
        return dataMediaDos;
    }

    public List<DataMediaDO> listByMultiId(Long... identities) {
        List<DataMediaDO> dataMediaDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.listDataMediaByIds", identities);
        return dataMediaDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.getDataMediaCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamedia.dal.dataobject.DataMediaDO.getDataMediaCount", condition);
        return count.intValue();
    }

}

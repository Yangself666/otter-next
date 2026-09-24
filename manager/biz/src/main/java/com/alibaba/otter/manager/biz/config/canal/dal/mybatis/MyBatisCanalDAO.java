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

package com.alibaba.otter.manager.biz.config.canal.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.canal.dal.CanalDAO;
import com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO;

/**
 * @author sarah.lij 2012-7-25 下午05:12:29
 */
public class MyBatisCanalDAO extends SqlSessionDaoSupport implements CanalDAO {

    public CanalDO insert(CanalDO canal) {
        Assert.assertNotNull(canal);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.insertCanal", canal);
        return canal;
    }

    public void delete(Long canalId) {
        Assert.assertNotNull(canalId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.deleteCanalById", canalId);
    }

    public void update(CanalDO canal) {
        Assert.assertNotNull(canal);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.updateCanal", canal);
    }

    public List<CanalDO> listAll() {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.listCanals");
    }

    public List<CanalDO> listByMultiId(Long... identities) {
        List<CanalDO> canalDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.listCanalByIds", identities);
        return canalDos;
    }

    public boolean checkUnique(CanalDO canal) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.checkCanalUnique", canal);
        return count == 0 ? true : false;
    }

    public CanalDO findByName(String name) {
        Assert.assertNotNull(name);
        return (CanalDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.findCanalByName", name);
    }

    public CanalDO findById(Long identity) {
        throw new UnsupportedOperationException();
    }

    public int getCount() {
        return 0;
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.getCanalCount", condition);
        return count.intValue();
    }

    public List<CanalDO> listByCondition(Map condition) {
        List<CanalDO> canalDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.canal.dal.dataobject.CanalDO.listCanals", condition);
        return canalDos;
    }
}

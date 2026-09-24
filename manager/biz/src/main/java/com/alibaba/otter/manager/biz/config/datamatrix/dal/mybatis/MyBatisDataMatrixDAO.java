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

package com.alibaba.otter.manager.biz.config.datamatrix.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.DataMatrixDAO;
import com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO;

public class MyBatisDataMatrixDAO extends SqlSessionDaoSupport implements DataMatrixDAO {

    public DataMatrixDO insert(DataMatrixDO matrixDo) {
        Assert.assertNotNull(matrixDo);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.insertDataMatrix", matrixDo);
        return matrixDo;
    }

    public void delete(Long matrixId) {
        Assert.assertNotNull(matrixId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.deleteDataMatrixById", matrixId);
    }

    public void update(DataMatrixDO matrixDo) {
        Assert.assertNotNull(matrixDo);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.updateDataMatrix", matrixDo);
    }

    public List<DataMatrixDO> listAll() {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.listDataMatrixs");
    }

    public List<DataMatrixDO> listByMultiId(Long... identities) {
        List<DataMatrixDO> DataMatrixDOs = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.listDataMatrixByIds", identities);
        return DataMatrixDOs;
    }

    public boolean checkUnique(DataMatrixDO matrixDo) {
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.checkDataMatrixUnique", matrixDo);
        return count == 0 ? true : false;
    }

    public DataMatrixDO findByGroupKey(String groupKey) {
        Assert.assertNotNull(groupKey);
        return (DataMatrixDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.findDataMatrixByGroupKey", groupKey);
    }

    public DataMatrixDO findById(Long identity) {
        throw new UnsupportedOperationException();
    }

    public int getCount() {
        return 0;
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.getDataMatrixCount", condition);
        return count.intValue();
    }

    public List<DataMatrixDO> listByCondition(Map condition) {
        List<DataMatrixDO> DataMatrixDOs = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.datamatrix.dal.dataobject.DataMatrixDO.listDataMatrixs", condition);
        return DataMatrixDOs;
    }
}

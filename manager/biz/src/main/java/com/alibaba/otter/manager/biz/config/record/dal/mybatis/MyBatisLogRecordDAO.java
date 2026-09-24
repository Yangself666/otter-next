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

package com.alibaba.otter.manager.biz.config.record.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.record.dal.LogRecordDAO;
import com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO;

/**
 * 类MyBatisLogRecordDAO.java的实现描述：TODO 类实现描述
 * 
 * @author simon 2012-6-15 下午1:52:15
 */
public class MyBatisLogRecordDAO extends SqlSessionDaoSupport implements LogRecordDAO {

    public LogRecordDO insert(LogRecordDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.insertLogRecord", entityObj);
        return entityObj;
    }

    public void delete(Long identity) {
        Assert.assertNotNull(identity);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.deleteLogRecordById", identity);

    }

    public void update(LogRecordDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.updateLogRecord", entityObj);

    }

    public List<LogRecordDO> listAll() {
        List<LogRecordDO> logRecordDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.listLogRecords");
        return logRecordDos;
    }

    public List<LogRecordDO> listByCondition(Map condition) {

        List<LogRecordDO> logRecordDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.listLogRecordsWithCondition",
                                                                                condition);
        return logRecordDos;
    }

    public List<LogRecordDO> listByMultiId(Long... identities) {
        // TODO Auto-generated method stub
        return null;
    }

    public LogRecordDO findById(Long identity) {
        Assert.assertNotNull(identity);
        return (LogRecordDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.findLogRecordById", identity);
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.getLogRecordCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.getLogRecordCountWithPIdAndSearchKey",
                                                                           condition);
        return count.intValue();
    }

    public boolean checkUnique(LogRecordDO entityObj) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<LogRecordDO> listByPipelineId(Long pipelineId) {
        List<LogRecordDO> logRecordDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.listLogRecordsByPipelineId",
                                                                                pipelineId);
        return logRecordDos;
    }

    public List<LogRecordDO> listByPipelineIdWithoutContent(Long pipelineId) {
        List<LogRecordDO> logRecordDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.record.dal.dataobject.LogRecordDO.listLogRecordsByPipelineIdWithoutContent",
                                                                                pipelineId);
        return logRecordDos;
    }

}

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

package com.alibaba.otter.manager.biz.statistics.table.dal.mybatis;

import java.util.List;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.manager.biz.statistics.table.dal.TableStatDAO;
import com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO;
import com.alibaba.otter.manager.biz.statistics.table.param.BehaviorHistoryCondition;

/**
 * @author simon
 */
public class MyBatisTableStatDAO extends SqlSessionDaoSupport implements TableStatDAO {

    @Override
    public void insertTableStat(TableStatDO tableStat) {

        getSqlSession().insert("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.insertTableStat", tableStat);
    }

    @Override
    public void deleteTableStat(Long tableStatId) {
        getSqlSession().delete("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.deleteTableStatById", tableStatId);
    }

    @Override
    public int modifyTableStat(TableStatDO tableStat) {
        return (getSqlSession().update("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.modifyTableStat", tableStat));
    }

    @Override
    public TableStatDO findTableStatById(Long tableStatId) {

        return (TableStatDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.findTableStatById", tableStatId);
    }

    @Override
    public TableStatDO findTableStatByPipelineIdAndPairId(Long pipelineId, Long dataMediaPairId) {
        TableStatDO tableStat = new TableStatDO();
        tableStat.setPipelineId(pipelineId);
        tableStat.setDataMediaPairId(dataMediaPairId);
        return (TableStatDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.findTableStatByPipelineIdAndDataMediaPairId",
                                                                      tableStat);
    }

    @Override
    public List<TableStatDO> listTableStatsByPipelineId(Long pipelineId) {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.listTableStatsByPipelineId", pipelineId);
    }

    @Override
    public List<TableStatDO> listTableStatsByPairId(Long dataMediaPairId) {

        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableStatDO.listTableStatsByDataMediaPairId",
                                                                          dataMediaPairId);
    }

    public List<TableStatDO> listTimelineTableStat(BehaviorHistoryCondition condition) {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.statistics.table.dal.dataobject.TableHistoryStatDO.listTimelineTableStat", condition);
    }

}

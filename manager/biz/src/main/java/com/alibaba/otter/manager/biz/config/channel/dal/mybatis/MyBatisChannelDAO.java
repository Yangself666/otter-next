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

package com.alibaba.otter.manager.biz.config.channel.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.config.channel.dal.ChannelDAO;
import com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO;

/**
 * Channel的DAO层，MyBatis 实现，提供配置持久化操作
 * 
 * @author simon
 */
public class MyBatisChannelDAO extends SqlSessionDaoSupport implements ChannelDAO {

    public ChannelDO insert(ChannelDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlSession().insert("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.insertChannel", entityObj);
        return entityObj;
    }

    public void delete(Long identity) {
        Assert.assertNotNull(identity);
        getSqlSession().delete("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.deleteChannelById", identity);
    }

    public void update(ChannelDO entityObj) {
        Assert.assertNotNull(entityObj);
        getSqlSession().update("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.updateChannel", entityObj);
    }

    public boolean checkUnique(ChannelDO entityObj) {
        Assert.assertNotNull(entityObj);
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.checkChannelUnique", entityObj);
        return count == 0 ? true : false;
    }

    public List<ChannelDO> listAll() {
        List<ChannelDO> channels = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.listChannels");
        return channels;
    }

    public List<ChannelDO> listChannelPks() {
        List<ChannelDO> channels = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.listChannelPks");
        return channels;
    }

    public List<ChannelDO> listByCondition(Map condition) {

        List<ChannelDO> channelDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.listChannels", condition);
        return channelDos;
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.getChannelCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.getChannelCount", condition);
        return count.intValue();
    }

    public List<ChannelDO> listByMultiId(Long... identities) {
        List<ChannelDO> channelDos = getSqlSession().selectList("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.listChannelByIds", identities);
        return channelDos;
    }

    public ChannelDO findById(Long identity) {
        Assert.assertNotNull(identity);
        return (ChannelDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.config.channel.dal.dataobject.ChannelDO.findChannelById", identity);
    }

}

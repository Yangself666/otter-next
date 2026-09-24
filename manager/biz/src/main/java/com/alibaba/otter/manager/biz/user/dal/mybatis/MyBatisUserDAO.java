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

package com.alibaba.otter.manager.biz.user.dal.mybatis;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.support.SqlSessionDaoSupport;

import com.alibaba.otter.shared.common.utils.Assert;
import com.alibaba.otter.manager.biz.user.dal.UserDAO;
import com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO;

/**
 * TODO Comment of MyBatisUserDAO
 * 
 * @author simon
 */
public class MyBatisUserDAO extends SqlSessionDaoSupport implements UserDAO {

    public UserDO findByName(String name) {
        return getSqlSession().selectOne("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.findUserByName", name);
    }

    public UserDO findUserById(Long userId) {
        Assert.assertNotNull(userId);
        return (UserDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.findUserById", userId);
    }

    public List<UserDO> listAllUsers() {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.listUsers");
    }

    public List<UserDO> listByCondition(Map condition) {
        return getSqlSession().selectList("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.listUsers", condition);
    }

    public UserDO insertUser(UserDO user) {
        Assert.assertNotNull(user);
        int inserted = getSqlSession().insert("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.insertUser", user);
        if (inserted == 0) user.setId(0L);
        return user;
    }

    public void updateUser(UserDO user) {
        Assert.assertNotNull(user);
        getSqlSession().update("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.updateUser", user);
    }

    public boolean chackUnique(UserDO user) {
        Assert.assertNotNull(user);
        int count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.checkUserUnique", user);
        return count == 0 ? true : false;
    }

    public void deleteUser(Long userId) {
        Assert.assertNotNull(userId);
        getSqlSession().delete("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.deleteUserById", userId);
    }

    public UserDO getAuthenticatedUser(String name, String password) {
        UserDO userDo = new UserDO();

        userDo.setName(name);
        userDo.setPassword(password);

        return (UserDO) getSqlSession().selectOne("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.getUserByNameAndPassword", userDo);
    }

    public int getCount() {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.getUserCount");
        return count.intValue();
    }

    public int getCount(Map condition) {
        Integer count = (Integer) getSqlSession().selectOne("com.alibaba.otter.manager.biz.user.dal.dataobject.UserDO.getUserCount", condition);
        return count.intValue();
    }

}

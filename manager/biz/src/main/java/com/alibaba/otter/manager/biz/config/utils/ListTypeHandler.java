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

package com.alibaba.otter.manager.biz.config.utils;

import java.util.List;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.otter.shared.common.utils.JsonUtils;

/**
 * 用于List数据结构的解析，MyBatis 字段转换
 *
 * @author simon
 */
public class ListTypeHandler extends JsonTypeHandler {

    @Override
    protected String serialize(Object parameter) {
        return JsonUtils.marshalToString(parameter);
    }

    @Override
    protected Object deserialize(String value) {
        return JsonUtils.unmarshalFromString(value, new TypeReference<List<Long>>() {
        });
    }
}

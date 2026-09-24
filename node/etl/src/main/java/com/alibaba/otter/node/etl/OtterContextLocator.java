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

package com.alibaba.otter.node.etl;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;


/**
 * Comment of OtterServiceLocator
 *
 * @author xiaoqing.zhouxq
 * @author zebin.xuzb 重写 customizeBeanFactory，防止重复id
 */
public class OtterContextLocator {

    private static volatile org.springframework.context.ConfigurableApplicationContext context;

    public static void initialize(org.springframework.context.ConfigurableApplicationContext applicationContext) {
        context = applicationContext;
    }

    private static ApplicationContext getApplicationContext() {
        if (context == null) {
            throw new IllegalStateException("Node application context has not been initialized");
        }
        return context;
    }

    public static void close() {
        if (context != null) {
            context.close();
        }
    }

    public static OtterController getOtterController() {
        return (OtterController) getApplicationContext().getBean("otterController");
    }

    public static <T> T getBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    /**
     * 根据当前spring容器的bean定义，解析对应的object并完成注入
     */
    public static void autowire(Object obj) {
        // 重新注入一下对象
        context.getAutowireCapableBeanFactory().autowireBeanProperties(obj,
                                                                       AutowireCapableBeanFactory.AUTOWIRE_BY_NAME,
                                                                       false);
    }

}

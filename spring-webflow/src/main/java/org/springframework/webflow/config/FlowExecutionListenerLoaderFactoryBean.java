/*
 * Copyright 2004-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.webflow.execution.FlowExecutionListener;
import org.springframework.webflow.execution.factory.ConditionalFlowExecutionListenerLoader;
import org.springframework.webflow.execution.factory.FlowExecutionListenerCriteriaFactory;
import org.springframework.webflow.execution.factory.FlowExecutionListenerLoader;

import java.util.Map;

/**
 * A factory for a flow execution listener loader. Is a Spring FactoryBean, for provision by the flow execution listener
 * loader bean definition parser. Is package-private, as people should not be using this class directly, but rather
 * through the higher-level webflow-config Spring 2.x configuration namespace.
 *
 * @author Keith Donald
 */
class FlowExecutionListenerLoaderFactoryBean implements FactoryBean<FlowExecutionListenerLoader>, InitializingBean {

    /**
     * The configured execution listeners and the criteria determining when they apply.
     */
    private Map<FlowExecutionListener, String> listenersWithCriteria;

    /**
     * The listener loader created by this factory. Is conditional, allowing listeners to apply to flow executions
     * selectively based on some criteria expression.
     */
    private ConditionalFlowExecutionListenerLoader listenerLoader;

    /**
     * A helper factory for converting string-encoded listener criteria to a FlowExecutionListenerCriteria object.
     */
    private FlowExecutionListenerCriteriaFactory listenerCriteriaFactory = new FlowExecutionListenerCriteriaFactory();

    /**
     * Sets the listeners eligible for loading, and the criteria for when they should be loaded.
     *
     * @param listenersWithCriteria the listener-to-criteria map
     */
    public void setListeners(Map<FlowExecutionListener, String> listenersWithCriteria) {
        this.listenersWithCriteria = listenersWithCriteria;
    }

    public void afterPropertiesSet() {
        listenerLoader = new ConditionalFlowExecutionListenerLoader();
        for (Map.Entry<FlowExecutionListener, String> entry : listenersWithCriteria.entrySet()) {
            FlowExecutionListener listener = entry.getKey();
            String criteria = entry.getValue();
            listenerLoader.addListener(listener, listenerCriteriaFactory.getListenerCriteria(criteria));
        }
    }

    public FlowExecutionListenerLoader getObject() throws Exception {
        return listenerLoader;
    }

    public Class<?> getObjectType() {
        return FlowExecutionListenerLoader.class;
    }

    public boolean isSingleton() {
        return true;
    }
}

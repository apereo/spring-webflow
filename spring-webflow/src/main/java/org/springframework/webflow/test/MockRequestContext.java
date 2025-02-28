/*
 * Copyright 2004-2014 the original author or authors.
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
package org.springframework.webflow.test;

import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.binding.message.MessageContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.core.collection.ParameterMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionContext;
import org.springframework.webflow.execution.FlowSession;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;

import java.util.List;

/**
 * Mock implementation of the <code>RequestContext</code> interface to facilitate standalone flow artifact (e.g. action)
 * unit tests.
 *
 * @author Keith Donald
 * @author Erwin Vervaet
 * @see org.springframework.webflow.execution.RequestContext
 * @see org.springframework.webflow.execution.Action
 */
public class MockRequestContext implements RequestContext {

    private FlowExecutionContext flowExecutionContext;

    private ExternalContext externalContext;

    private MessageContext messageContext;

    private MutableAttributeMap<Object> requestScope = new LocalAttributeMap<>();

    private MutableAttributeMap<Object> attributes = new LocalAttributeMap<>();

    private Event currentEvent;

    private Transition currentTransition;

    private View currentView;

    /**
     * Convenience constructor that creates a new mock request context with the following defaults:
     * <ul>
     * <li>A mock flow execution context with a active session of flow "mockFlow" in state "mockState".
     * <li>A mock external context with no request parameters set.
     * </ul>
     * To add request parameters to this request, use the {@link #putRequestParameter(String, String)} method.
     */
    public MockRequestContext() {
        this(new MockFlowExecutionContext());
    }

    /**
     * Convenience constructor that creates a new mock request context with the following defaults:
     * <ul>
     * <li>A mock flow execution context with an active session for the specified flow.
     * <li>A mock external context with no request parameters set.
     * </ul>
     * To add request parameters to this request, use the {@link #putRequestParameter(String, String)} method.
     *
     * @param flow the flow definition
     */
    public MockRequestContext(Flow flow) {
        this(new MockFlowExecutionContext(flow));
    }

    /**
     * Convenience constructor that creates a new mock request context with the following defaults:
     * <ul>
     * <li>A mock flow execution context with a active session of flow "mockFlow" in state "mockState".
     * <li>A mock external context with the provided parameters set.
     * </ul>
     *
     * @param requestParameterMap
     * @param requestParameterMap
     */
    public MockRequestContext(ParameterMap requestParameterMap) {
        this.flowExecutionContext = new MockFlowExecutionContext();
        this.externalContext = new MockExternalContext(requestParameterMap);
        this.messageContext = new DefaultMessageContext();
    }

    /**
     * Creates a new mock request context with the provided flow execution context. To add request parameters to this
     * request, use the {@link #putRequestParameter(String, String)} method.
     *
     * @param flowExecutionContext the flow execution context
     */
    public MockRequestContext(FlowExecutionContext flowExecutionContext) {
        this.flowExecutionContext = flowExecutionContext;
        this.externalContext = new MockExternalContext();
        this.messageContext = new DefaultMessageContext();
    }

    // implementing RequestContext

    public FlowDefinition getActiveFlow() {
        return getFlowExecutionContext().getActiveSession().getDefinition();
    }

    public StateDefinition getCurrentState() {
        return getFlowExecutionContext().getActiveSession().getState();
    }

    public TransitionDefinition getMatchingTransition(String eventId) throws IllegalStateException {
        TransitionableState state = (TransitionableState) getFlowExecutionContext().getActiveSession().getState();
        TransitionDefinition transition = state.getTransition(eventId);
        if (transition == null) {
            transition = getRootFlow().getGlobalTransition(eventId);
        }
        return transition;
    }

    public boolean inViewState() {
        return getFlowExecutionContext().isActive() && getCurrentState() != null && getCurrentState().isViewState();
    }

    public MutableAttributeMap<Object> getRequestScope() {
        return requestScope;
    }

    public MutableAttributeMap<Object> getFlashScope() {
        return getMockFlowExecutionContext().getFlashScope();
    }

    public MutableAttributeMap<Object> getViewScope() throws IllegalStateException {
        return getMockFlowExecutionContext().getActiveSession().getViewScope();
    }

    public MutableAttributeMap<Object> getFlowScope() {
        return getFlowExecutionContext().getActiveSession().getScope();
    }

    public MutableAttributeMap<Object> getConversationScope() {
        return getMockFlowExecutionContext().getConversationScope();
    }

    public ParameterMap getRequestParameters() {
        return externalContext.getRequestParameterMap();
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public ExternalContext getExternalContext() {
        return externalContext;
    }

    /**
     * Sets the external context.
     *
     * @param externalContext
     * @param externalContext
     */
    public void setExternalContext(ExternalContext externalContext) {
        this.externalContext = externalContext;
    }

    public FlowExecutionContext getFlowExecutionContext() {
        return flowExecutionContext;
    }

    /**
     * Sets the flow execution context.
     *
     * @param flowExecutionContext
     * @param flowExecutionContext
     */
    public void setFlowExecutionContext(FlowExecutionContext flowExecutionContext) {
        this.flowExecutionContext = flowExecutionContext;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Set the current event being processed by this flow.
     *
     * @param event the current event
     */
    public void setCurrentEvent(Event event) {
        this.currentEvent = event;
    }

    public TransitionDefinition getCurrentTransition() {
        return currentTransition;
    }

    /**
     * Set the current transition executing in this request context.
     *
     * @param transition the current transition to set
     */
    public void setCurrentTransition(Transition transition) {
        this.currentTransition = transition;
    }

    // mutators for configuring the mock

    public View getCurrentView() {
        return currentView;
    }

    /**
     * Set the current view in this request context.
     *
     * @param currentView the current view
     */
    public void setCurrentView(View currentView) {
        this.currentView = currentView;
    }

    public MutableAttributeMap<Object> getAttributes() {
        return attributes;
    }

    public String getFlowExecutionUrl() {
        if (flowExecutionContext.getKey() == null) {
            throw new IllegalStateException(
                "Flow execution key not yet assigned; unable to build the flow execution url");
        } else {
            String flowDefinitionId = flowExecutionContext.getDefinition().getId();
            return externalContext.getFlowExecutionUrl(flowDefinitionId, flowExecutionContext.getKey().toString());
        }
    }

    public void sendFlowExecutionRedirect() {
        if (flowExecutionContext.getKey() == null) {
            throw new IllegalStateException(
                "Flow execution key not yet assigned; unable to send a flow execution redirect request");
        } else {
            externalContext.requestFlowExecutionRedirect();
        }
    }

    /**
     * Sets the active flow session of the executing flow associated with this request. This will influence
     * {@link #getActiveFlow()} and {@link #getCurrentState()}, as well as {@link #getFlowScope()} and
     * {@link #getFlashScope()}.
     *
     * @param flowSession
     * @param flowSession
     */
    public void setActiveSession(FlowSession flowSession) {
        getMockFlowExecutionContext().setActiveSession(flowSession);
    }

    /**
     * Set a request context attribute.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     */
    public void setAttribute(String attributeName, Object attributeValue) {
        attributes.put(attributeName, attributeValue);
    }

    /**
     * Remove a request context attribute.
     *
     * @param attributeName the attribute name
     */
    public void removeAttribute(String attributeName) {
        attributes.remove(attributeName);
    }

    /**
     * Adds a request parameter to the configured external context.
     *
     * @param parameterName  the parameter name
     * @param parameterValue the parameter value
     */
    public void putRequestParameter(String parameterName, String parameterValue) {
        getMockExternalContext().putRequestParameter(parameterName, parameterValue);
    }

    /**
     * Adds a multi-valued request parameter to the configured external context.
     *
     * @param parameterName   the parameter name
     * @param parameterValues the parameter values
     */
    public void putRequestParameter(String parameterName, String[] parameterValues) {
        getMockExternalContext().putRequestParameter(parameterName, parameterValues);
    }

    /**
     * Puts a MultipartFile request parameter into the mock parameter map.
     *
     * @param parameterName  the parameter name
     * @param parameterValue the parameter value
     */
    public void putRequestParameter(String parameterName, MultipartFile parameterValue) {
        getMockExternalContext().putRequestParameter(parameterName, parameterValue);
    }

    /**
     * Puts a multi-valued MultipartFile request parameter into the mock parameter map.
     *
     * @param parameterName  the parameter name
     * @param parameterValue the parameter value
     */
    public void putRequestParameter(String parameterName, List<MultipartFile> parameterValue) {
        getMockExternalContext().putRequestParameter(parameterName, parameterValue);
    }

    // convenience accessors

    /**
     * Returns the root flow definition for this request context. Assumes a {@link Flow} implementation.
     *
     * @return
     */
    public Flow getRootFlow() {
        return getMockFlowExecutionContext().getDefinitionInternal();
    }

    /**
     * Returns the contained mutable context {@link AttributeMap attribute map} allowing setting of mock context
     * attributes.
     *
     * @return the attribute map
     */
    public MutableAttributeMap<Object> getAttributeMap() {
        return attributes;
    }

    /**
     * Returns the flow execution context as a {@link MockFlowExecutionContext}.
     *
     * @return
     */
    public MockFlowExecutionContext getMockFlowExecutionContext() {
        return (MockFlowExecutionContext) flowExecutionContext;
    }

    /**
     * Returns the external context as a {@link MockExternalContext}.
     *
     * @return
     */
    public MockExternalContext getMockExternalContext() {
        return (MockExternalContext) externalContext;
    }

}

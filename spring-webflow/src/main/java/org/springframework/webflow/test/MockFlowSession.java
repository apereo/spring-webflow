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
package org.springframework.webflow.test;

import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.definition.FlowDefinition;
import org.springframework.webflow.definition.StateDefinition;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.FlowSession;

/**
 * Mock implementation of the {@link FlowSession} interface.
 *
 * @author Erwin Vervaet
 * @see FlowSession
 */
public class MockFlowSession implements FlowSession {

    private static final String VIEW_MAP_ATTRIBUTE = "flowViewMap";

    private static final String EMBEDDED_MODE_ATTRIBUTE = "embeddedMode";

    private Flow definition;

    private State state;

    private MutableAttributeMap<Object> scope = new LocalAttributeMap<>();

    private FlowSession parent;

    /**
     * Creates a new mock flow session that sets a flow with id "mockFlow" as the 'active flow' in state "mockState".
     */
    public MockFlowSession() {
        setDefinition(new Flow("mockFlow"));
        State state = new TransitionableState(definition, "mockState") {
            protected void doEnter(RequestControlContext context) throws FlowExecutionException {
                // nothing to do
            }
        };
        setState(state);
    }

    /**
     * Creates a new mock session in a created state for the specified flow definition.
     *
     * @param flow
     * @param flow
     */
    public MockFlowSession(Flow flow) {
        setDefinition(flow);
    }

    /**
     * Creates a new mock session for the specified flow definition.
     *
     * @param flow  the flow definition for the session
     * @param input initial contents of 'flow scope'
     */
    public MockFlowSession(Flow flow, AttributeMap<?> input) {
        setDefinition(flow);
        scope.putAll(input);
    }

    // implementing FlowSession

    public FlowDefinition getDefinition() {
        return definition;
    }

    /**
     * Set the flow associated with this flow session.
     *
     * @param flow
     * @param flow
     */
    public void setDefinition(Flow flow) {
        this.definition = flow;
    }

    public StateDefinition getState() {
        return state;
    }

    /**
     * Set the currently active state.
     *
     * @param state
     * @param state
     */
    public void setState(State state) {
        if (this.state != null && this.state.isViewState()) {
            destroyViewScope();
        }
        this.state = state;
        if (this.state != null && this.state.isViewState()) {
            initViewScope();
        }
    }

    public MutableAttributeMap<Object> getScope() {
        return scope;
    }

    /**
     * Set the scope data maintained by this flow session. This will be the flow scope data of the ongoing flow
     * execution.
     *
     * @param scope
     * @param scope
     */
    public void setScope(MutableAttributeMap<Object> scope) {
        this.scope = scope;
    }

    @SuppressWarnings("unchecked")
    public MutableAttributeMap<Object> getViewScope() throws IllegalStateException {
        if (state == null) {
            throw new IllegalStateException("The current state of this flow '" + definition.getId()
                                            + "' is [null] - cannot access view scope");
        }
        if (!state.isViewState()) {
            throw new IllegalStateException("The current state '" + state.getId() + "' of this flow '"
                                            + definition.getId() + "' is not a view state - view scope not accessible");
        }
        return (MutableAttributeMap<Object>) scope.get(VIEW_MAP_ATTRIBUTE);
    }

    // mutators

    public boolean isEmbeddedMode() {
        return (Boolean) scope.get(EMBEDDED_MODE_ATTRIBUTE, false);
    }

    public FlowSession getParent() {
        return parent;
    }

    /**
     * Set the parent flow session of this flow session in the ongoing flow execution.
     *
     * @param parent
     * @param parent
     */
    public void setParent(FlowSession parent) {
        this.parent = parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    // convenience accessors

    /**
     * Returns the flow definition of this session.
     *
     * @return
     */
    public Flow getDefinitionInternal() {
        return definition;
    }

    /**
     * Returns the current state of this session.
     *
     * @return
     */
    public State getStateInternal() {
        return state;
    }

    /**
     * Set a flow session attribute to indicate the current session should execute in embedded mode.
     *
     * @see FlowSession#isEmbeddedMode()
     */
    void setEmbeddedMode() {
        this.scope.put(EMBEDDED_MODE_ATTRIBUTE, true);
    }

    // internal helpers

    private void initViewScope() {
        scope.put(VIEW_MAP_ATTRIBUTE, new LocalAttributeMap<>());
    }

    private void destroyViewScope() {
        scope.remove(VIEW_MAP_ATTRIBUTE);
    }
}

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
package org.springframework.webflow.engine;

import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.support.AbstractGetValueExpression;
import org.springframework.binding.expression.support.FluentParserContext;
import org.springframework.binding.mapping.impl.DefaultMapper;
import org.springframework.binding.mapping.impl.DefaultMapping;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.MockTransitionCriteria;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.expression.spel.WebFlowSpringELExpressionParser;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestControlContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests EndState behavior.
 *
 * @author Keith Donald
 */
public class EndStateTests {

    protected static TransitionCriteria on(String event) {
        return new MockTransitionCriteria(event);
    }

    protected static TargetStateResolver to(String stateId) {
        return new DefaultTargetStateResolver(stateId);
    }

    @Test
    public void testEnterEndStateTerminateFlowExecution() {
        Flow flow = new Flow("myFlow");
        EndState state = new EndState(flow, "end");
        MockRequestControlContext context = new MockRequestControlContext(flow);
        state.enter(context);
        assertFalse(context.getFlowExecutionContext().isActive(), "Active");
    }

    @Test
    public void testEnterEndStateWithFinalResponseRenderer() {
        Flow flow = new Flow("myFlow");
        EndState state = new EndState(flow, "end");
        StubFinalResponseAction action = new StubFinalResponseAction();
        state.setFinalResponseAction(action);
        MockRequestControlContext context = new MockRequestControlContext(flow);
        state.enter(context);
        assertTrue(action.executeCalled);
    }

    @Test
    public void testEnterEndStateWithFinalResponseRendererResponseAlreadyComplete() {
        Flow flow = new Flow("myFlow");
        EndState state = new EndState(flow, "end");
        StubFinalResponseAction action = new StubFinalResponseAction();
        state.setFinalResponseAction(action);
        MockRequestControlContext context = new MockRequestControlContext(flow);
        context.getExternalContext().recordResponseComplete();
        state.enter(context);
        assertFalse(action.executeCalled);
    }

    @Test
    public void testEnterEndStateWithOutputMapper() {
        Flow flow = new Flow("myFlow") {
            @SuppressWarnings("unused")
            public void end(RequestControlContext context, MutableAttributeMap<Object> output)
                throws FlowExecutionException {
                assertEquals("foo", output.get("y"));
            }
        };
        EndState state = new EndState(flow, "end");
        DefaultMapper mapper = new DefaultMapper();
        ExpressionParser parser = new WebFlowSpringELExpressionParser(new SpelExpressionParser());
        Expression x = parser.parseExpression("flowScope.x", new FluentParserContext().evaluate(RequestContext.class));
        Expression y = parser.parseExpression("y", new FluentParserContext().evaluate(MutableAttributeMap.class));
        mapper.addMapping(new DefaultMapping(x, y));
        state.setOutputMapper(mapper);
        MockRequestControlContext context = new MockRequestControlContext(flow);
        context.getFlowScope().put("x", "foo");
        state.enter(context);
    }

    @Test
    public void testEnterEndStateTerminateFlowSession() {
        final Flow subflow = new Flow("mySubflow");
        EndState state = new EndState(subflow, "end");
        MockFlowSession session = new MockFlowSession(subflow);

        Flow parent = new Flow("parent");
        SubflowState subflowState = new SubflowState(parent, "subflow", new AbstractGetValueExpression() {
            public Object getValue(Object context) throws EvaluationException {
                return subflow;
            }
        });
        subflowState.getTransitionSet().add(new Transition(on("end"), to("end")));
        new EndState(parent, "end");

        MockFlowSession parentSession = new MockFlowSession(parent);
        parentSession.setState(subflowState);

        session.setParent(parentSession);
        MockRequestControlContext context = new MockRequestControlContext(new MockFlowExecutionContext(session));
        state.enter(context);

        assertFalse(context.getFlowExecutionContext().isActive(), "Active");
    }

    private class StubFinalResponseAction implements Action {
        private boolean executeCalled;

        public Event execute(RequestContext context) {
            executeCalled = true;
            return new Event(this, "success");
        }
    }
}

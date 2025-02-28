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

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.View;
import org.springframework.webflow.execution.ViewFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A view state is a state that issues a response to the user, for example, for soliciting form input. To accomplish
 * this, a <code>ViewState</code> delegates to a {@link ViewFactory}.
 *
 * @author Keith Donald
 * @author Erwin Vervaet
 * @see ViewFactory
 */
public class ViewState extends TransitionableState {

    /**
     * The list of actions to be executed before the view is rendered.
     */
    private ActionList renderActionList = new ActionList();

    /**
     * A factory for creating and restoring the view rendered by this view state.
     */
    private ViewFactory viewFactory;

    /**
     * The set of view variables created by this view state.
     */
    private Map<String, ViewVariable> variables = new LinkedHashMap<>();

    /**
     * Whether or not a redirect should occur before the view is rendered.
     */
    private Boolean redirect;

    /**
     * Whether or not the view should render as a popup.
     */
    private boolean popup;

    /**
     * Create a new view state.
     *
     * @param flow        the owning flow
     * @param id          the state identifier (must be unique to the flow)
     * @param viewFactory the view factory
     * @throws IllegalArgumentException when this state cannot be added to given flow, e.g. because the id is not unique
     */
    public ViewState(Flow flow, String id, ViewFactory viewFactory) throws IllegalArgumentException {
        super(flow, id);
        Assert.notNull(viewFactory, "The view factory is required");
        this.viewFactory = viewFactory;
    }

    // implementing StateDefinition

    public boolean isViewState() {
        return true;
    }

    /**
     * Adds a view variable.
     *
     * @param variable the variable
     */
    public void addVariable(ViewVariable variable) {
        variables.put(variable.getName(), variable);
    }

    /**
     * Adds a set of view variables.
     *
     * @param variables the variables
     */
    public void addVariables(ViewVariable... variables) {
        for (ViewVariable variable : variables) {
            addVariable(variable);
        }
    }

    /**
     * Returns the view variable with the given name.
     *
     * @param name the name of the variable
     * @return
     */
    public ViewVariable getVariable(String name) {
        return variables.get(name);
    }

    /**
     * Returns the configured view variables.
     *
     * @return
     */
    public ViewVariable[] getVariables() {
        return variables.values().toArray(new ViewVariable[variables.size()]);
    }

    /**
     * Returns whether this view state should request a flow execution redirect when entered.
     *
     * @return
     */
    public boolean getRedirect() {
        return (redirect == null) ? false : redirect;
    }

    /**
     * Sets whether this view state should requests a flow execution redirect when entered.
     *
     * @param redirect the redirect flag
     */
    public void setRedirect(Boolean redirect) {
        this.redirect = redirect;
    }

    /**
     * Returns whether this view state should render as a popup.
     *
     * @return
     */
    public boolean getPopup() {
        return popup;
    }

    /**
     * Sets whether this view state should render as a popup.
     *
     * @param popup the popup flag
     */
    public void setPopup(boolean popup) {
        this.popup = popup;
    }

    /**
     * Returns the view factory.
     *
     * @return
     */
    public ViewFactory getViewFactory() {
        return viewFactory;
    }

    /**
     * Returns the list of actions executable by this view state on entry and on refresh. The returned list is mutable.
     *
     * @return the state action list
     */
    public ActionList getRenderActionList() {
        return renderActionList;
    }

    public void resume(RequestControlContext context) {
        restoreVariables(context);
        View view = viewFactory.getView(context);
        context.setCurrentView(view);
        if (view.userEventQueued()) {
            boolean stateExited = handleEvent(view, context);
            if (!stateExited) {
                ExternalContext externalContext = context.getExternalContext();
                if (externalContext.isResponseComplete()) {
                    if (externalContext.isResponseCompleteFlowExecutionRedirect()) {
                        context.getFlashScope().put(View.USER_EVENT_STATE_ATTRIBUTE, view.getUserEventState());
                    } else {
                        clearFlash(context);
                    }
                } else {
                    if (externalContext.isAjaxRequest()) {
                        render(context, view);
                    } else {
                        if (shouldRedirectInSameState(context)) {
                            context.getFlashScope().put(View.USER_EVENT_STATE_ATTRIBUTE, view.getUserEventState());
                            externalContext.requestFlowExecutionRedirect();
                        } else {
                            if (externalContext.isResponseAllowed()) {
                                render(context, view);
                            }
                        }
                    }
                }
            }
        } else {
            refresh(view, context);
        }
    }

    public void exit(RequestControlContext context) {
        super.exit(context);
        updateHistory(context);
        destroyVariables(context);
        context.setCurrentView(null);
    }

    protected void doPreEntryActions(RequestControlContext context) throws FlowExecutionException {
        createVariables(context);
    }

    protected void doEnter(RequestControlContext context) throws FlowExecutionException {
        context.assignFlowExecutionKey();
        ExternalContext externalContext = context.getExternalContext();
        if (externalContext.isResponseComplete()) {
            if (!externalContext.isResponseCompleteFlowExecutionRedirect()) {
                clearFlash(context);
            }
        } else {
            if (shouldRedirect(context)) {
                context.getExternalContext().requestFlowExecutionRedirect();
                if (popup) {
                    context.getExternalContext().requestRedirectInPopup();
                }
            } else {
                View view = viewFactory.getView(context);
                context.setCurrentView(view);
                render(context, view);
            }
        }
    }

    protected void appendToString(ToStringCreator creator) {
        super.appendToString(creator);
        creator.append("viewFactory", viewFactory).append("variables", variables).append("redirect", redirect)
            .append("popup", popup);
    }

    private boolean handleEvent(View view, RequestControlContext context) {
        view.processUserEvent();
        if (view.hasFlowEvent()) {
            Event event = view.getFlowEvent();
            if (logger.isDebugEnabled()) {
                logger.debug("Event '" + event.getId() + "' returned from view " + view);
            }
            return context.handleEvent(event);
        } else {
            return false;
        }
    }

    // internal helpers

    private void refresh(View view, RequestControlContext context) {
        ExternalContext externalContext = context.getExternalContext();
        if (externalContext.isResponseComplete()) {
            clearFlash(context);
        } else {
            render(context, view);
        }
    }

    private void createVariables(RequestContext context) {
        for (ViewVariable variable : variables.values()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Creating " + variable);
            }
            variable.create(context);
        }
    }

    private boolean shouldRedirect(RequestControlContext context) {
        if (redirect != null) {
            return redirect;
        }
        if (context.getExternalContext().isAjaxRequest() && context.getEmbeddedMode()) {
            return false;
        }
        return context.getRedirectOnPause();
    }

    private boolean shouldRedirectInSameState(RequestControlContext context) {
        if (redirect != null) {
            return redirect;
        }
        if (context.getExternalContext().isAjaxRequest() && context.getEmbeddedMode()) {
            return false;
        }
        return context.getRedirectInSameState();
    }

    private void render(RequestControlContext context, View view) throws ViewRenderingException {
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering + " + view);
            logger.debug("  Flash scope = " + context.getFlashScope());
            logger.debug("  Messages = " + context.getMessageContext());
        }
        context.viewRendering(view);
        renderActionList.execute(context);
        try {
            view.render();
        } catch (IOException e) {
            throw new ViewRenderingException(getOwner().getId(), getId(), view, e);
        }
        clearFlash(context);
        context.getExternalContext().recordResponseComplete();
        context.viewRendered(view);
    }

    private void clearFlash(RequestContext context) {
        context.getFlashScope().clear();
        context.getMessageContext().clearMessages();
    }

    private void restoreVariables(RequestContext context) {
        for (ViewVariable variable : variables.values()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Restoring " + variable);
            }
            variable.restore(context);
        }
    }

    private void updateHistory(RequestControlContext context) {
        TransitionDefinition transition = context.getCurrentTransition();
        History history = (History) transition.getAttributes().get("history");
        if (history == null || history == History.PRESERVE) {
            View currentView = context.getCurrentView();
            if (currentView != null && shouldRedirect(context)) {
                currentView.saveState();
            }
            context.updateCurrentFlowExecutionSnapshot();
        } else if (history == History.DISCARD) {
            context.removeCurrentFlowExecutionSnapshot();
        } else if (history == History.INVALIDATE) {
            context.removeAllFlowExecutionSnapshots();
        }
    }

    private void destroyVariables(RequestContext context) {
        for (ViewVariable variable : variables.values()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Destroying " + variable);
            }
            variable.destroy(context);
        }
    }

}

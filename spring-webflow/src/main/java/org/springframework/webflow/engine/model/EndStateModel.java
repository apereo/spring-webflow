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
package org.springframework.webflow.engine.model;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedList;

/**
 * Model support for end states.
 *
 * @author Scott Andrews
 */
public class EndStateModel extends AbstractStateModel {

    private String view;

    private String commit;

    private LinkedList<OutputModel> outputs;

    /**
     * Create an end state model
     *
     * @param id the state identifier
     */
    public EndStateModel(String id) {
        super(id);
    }

    public boolean isMergeableWith(Model model) {
        if (!(model instanceof EndStateModel)) {
            return false;
        }
        EndStateModel state = (EndStateModel) model;
        return ObjectUtils.nullSafeEquals(getId(), state.getId());
    }

    public void merge(Model model) {
        EndStateModel state = (EndStateModel) model;
        setParent(null);
        setAttributes(merge(getAttributes(), state.getAttributes()));
        setSecured((SecuredModel) merge(getSecured(), state.getSecured()));
        setOnEntryActions(merge(getOnEntryActions(), state.getOnEntryActions(), false));
        setExceptionHandlers(merge(getExceptionHandlers(), state.getExceptionHandlers()));
        setView(merge(getView(), state.getView()));
        setCommit(merge(getCommit(), state.getCommit()));
        setOutputs(merge(getOutputs(), state.getOutputs(), false));
    }

    public Model createCopy() {
        EndStateModel copy = new EndStateModel(getId());
        super.fillCopy(copy);
        copy.setView(view);
        copy.setCommit(commit);
        copy.setOutputs(outputs);
        return copy;
    }

    /**
     * @return the view
     */
    public String getView() {
        return view;
    }

    /**
     * @param view the view factory to set
     */
    public void setView(String view) {
        if (StringUtils.hasText(view)) {
            this.view = view;
        } else {
            this.view = null;
        }
    }

    /**
     * @return the commit
     */
    public String getCommit() {
        return commit;
    }

    /**
     * @param commit the commit to set
     */
    public void setCommit(String commit) {
        if (StringUtils.hasText(commit)) {
            this.commit = commit;
        } else {
            this.commit = null;
        }
    }

    /**
     * @return the outputs
     */
    public LinkedList<OutputModel> getOutputs() {
        return outputs;
    }

    /**
     * @param outputs the outputs to set
     */
    public void setOutputs(LinkedList<OutputModel> outputs) {
        this.outputs = outputs;
    }

}

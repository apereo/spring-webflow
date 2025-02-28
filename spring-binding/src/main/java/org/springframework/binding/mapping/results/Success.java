/*
 * Copyright 2004-2008 the original author or authors.
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
package org.springframework.binding.mapping.results;

import org.springframework.binding.mapping.Mapping;

/**
 * Indicates a successful mapping operation.
 *
 * @author Keith Donald
 */
public class Success extends AbstractMappingResult {

    private Object mappedValue;

    private Object originalValue;

    /**
     * Creates a new success result.
     *
     * @param mapping
     * @param mapping
     * @param mappedValue   the successfully mapped value
     * @param originalValue the original value
     */
    public Success(Mapping mapping, Object mappedValue, Object originalValue) {
        super(mapping);
        this.mappedValue = mappedValue;
        this.originalValue = originalValue;
    }

    public String getCode() {
        return "success";
    }

    public boolean isError() {
        return false;
    }

    public Throwable getErrorCause() {
        return null;
    }

    public Object getOriginalValue() {
        return originalValue;
    }

    public Object getMappedValue() {
        return mappedValue;
    }

}

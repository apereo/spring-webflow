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
package org.springframework.binding.convert.converters;

import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionService;

import java.lang.reflect.Array;

/**
 * Special two-way converter that converts an object to an single-element array. Mainly used internally by
 * {@link ConversionService} implementations.
 *
 * @author Keith Donald
 */
public class ObjectToArray implements Converter {

    private ConversionService conversionService;

    private ConversionExecutor elementConverter;

    /**
     * Creates a new object to array converter.
     *
     * @param conversionService the conversion service to resolve the converter to use to convert the object added to
     *                          the target array.
     */
    public ObjectToArray(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Creates a new object to array converter.
     *
     * @param elementConverter a specific converter to use to convert the object added to the target array.
     */
    public ObjectToArray(ConversionExecutor elementConverter) {
        this.elementConverter = elementConverter;
    }

    public Class<?> getSourceClass() {
        return Object.class;
    }

    public Class<?> getTargetClass() {
        return Object[].class;
    }

    public Object convertSourceToTargetClass(Object source, Class<?> targetClass) {
        if (source == null) {
            return null;
        }
        Class<?> componentType = targetClass.getComponentType();
        Object array = Array.newInstance(componentType, 1);
        ConversionExecutor converter;
        if (elementConverter != null) {
            converter = elementConverter;
        } else {
            converter = conversionService.getConversionExecutor(source.getClass(), componentType);
        }
        Array.set(array, 0, converter.execute(source));
        return array;
    }
}

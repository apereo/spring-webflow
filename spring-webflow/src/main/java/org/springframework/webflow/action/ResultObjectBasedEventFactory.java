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
package org.springframework.webflow.action;

import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Result object-to-event adapter interface that tries to do a sensible conversion of the result object into a web flow
 * event. It uses the following:
 * <table border="1">
 * <caption>Conversion Table</caption>
 * <tr>
 * <th>Result object type</th>
 * <th>Event id</th>
 * <th>Remarks</th>
 * </tr>
 * <tr>
 * <td>null</td>
 * <td>{@link org.springframework.webflow.action.EventFactorySupport#getNullEventId()}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>{@link java.lang.Boolean} or boolean</td>
 * <td>{@link org.springframework.webflow.action.EventFactorySupport#getYesEventId()}/
 * {@link org.springframework.webflow.action.EventFactorySupport#getNoEventId()}</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>{@link java.lang.Enum}</td>
 * <td>{@link java.lang.Enum#name()}</td>
 * <td>The result object will included in the event as an attribute named "result".</td>
 * </tr>
 * <tr>
 * <td>{@link java.lang.String}</td>
 * <td>The string.</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>{@link org.springframework.webflow.execution.Event}</td>
 * <td>The resulting event object.</td>
 * <td>&nbsp;</td>
 * </tr>
 * </table>
 *
 * @author Keith Donald
 * @author Erwin Vervaet
 */
public class ResultObjectBasedEventFactory extends EventFactorySupport implements ResultEventFactory {

    public Event createResultEvent(Object source, Object resultObject, RequestContext context) {
        if (resultObject == null) {
            // this handles the case where the declared result return type is mapped
            // by this class but the value is null
            return event(source, getNullEventId());
        } else if (isBoolean(resultObject.getClass())) {
            return event(source, ((Boolean) resultObject));
        } else if (isEnum(resultObject.getClass())) {
            String eventId = EnumUtils.getEnumName(resultObject);
            return event(source, eventId, getResultAttributeName(), resultObject);
        } else if (isString(resultObject.getClass())) {
            String resultString = (String) resultObject;
            if (StringUtils.hasText(resultString)) {
                return event(source, resultString);
            } else {
                // treat an empty string as the null event
                return event(source, getNullEventId());
            }
        } else if (isEvent(resultObject.getClass())) {
            return (Event) resultObject;
        } else {
            throw new IllegalArgumentException("Cannot deal with result object '" + resultObject + "' of type '"
                                               + resultObject.getClass() + "'");
        }
    }

    /**
     * Check whether or not given type is mapped to a corresponding event using special mapping rules.
     *
     * @param type
     * @param type
     * @return
     */
    public boolean isMappedValueType(Class<?> type) {
        return isBoolean(type) || isEnum(type) || isString(type) || isEvent(type);
    }

    // internal helpers to determine the 'type' of a class

    /**
     * Simple helper class with Java 5 specific code factored out to keep the containing class JDK 1.3 compatible.
     */
    private static class EnumUtils {
        public static String getEnumName(Object enumValue) {
            return ((java.lang.Enum<?>) enumValue).name();
        }

        public static boolean isEnum(Class<?> type) {
            return java.lang.Enum.class.isAssignableFrom(type);
        }
    }

    private boolean isBoolean(Class<?> type) {
        return Boolean.class.equals(type) || boolean.class.equals(type);
    }

    private boolean isEnum(Class<?> type) {
        return EnumUtils.isEnum(type);
    }

    private boolean isString(Class<?> type) {
        return String.class.equals(type);
    }

    private boolean isEvent(Class<?> type) {
        return Event.class.isAssignableFrom(type);
    }
}

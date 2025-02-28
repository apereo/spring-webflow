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

import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Converts a String to a Locale using {@link StringUtils#parseLocaleString(String)}.
 *
 * @author Keith Donald
 */
public class StringToLocale extends StringToObject {

    public StringToLocale() {
        super(Locale.class);
    }

    public Object toObject(String string, Class<?> objectClass) {
        return StringUtils.parseLocaleString(string);
    }

    public String toString(Object object) {
        Locale locale = (Locale) object;
        return locale.toString();
    }

}

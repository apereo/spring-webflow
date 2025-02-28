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
package org.springframework.binding.collection;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link org.springframework.binding.collection.SharedMapDecorator}.
 */
public class SharedMapDecoratorTests {

    private SharedMapDecorator<String, String> map = new SharedMapDecorator<>(
        new HashMap<>());

    @Test
    public void testGetPutRemove() {
        assertTrue(map.size() == 0);
        assertTrue(map.isEmpty());
        assertNull(map.get("foo"));
        assertFalse(map.containsKey("foo"));
        map.put("foo", "bar");
        assertTrue(map.size() == 1);
        assertFalse(map.isEmpty());
        assertNotNull(map.get("foo"));
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsValue("bar"));
        assertEquals("bar", map.get("foo"));
        map.remove("foo");
        assertTrue(map.size() == 0);
        assertNull(map.get("foo"));
    }

    @Test
    public void testPutAll() {
        Map<String, String> all = new HashMap<>();
        all.put("foo", "bar");
        all.put("bar", "baz");
        map.putAll(all);
        assertTrue(map.size() == 2);
    }

    @Test
    public void testEntrySet() {
        map.put("foo", "bar");
        map.put("bar", "baz");
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        assertTrue(entrySet.size() == 2);
    }

    @Test
    public void testKeySet() {
        map.put("foo", "bar");
        map.put("bar", "baz");
        Set<String> keySet = map.keySet();
        assertTrue(keySet.size() == 2);
    }

    @Test
    public void testValues() {
        map.put("foo", "bar");
        map.put("bar", "baz");
        Collection<String> values = map.values();
        assertTrue(values.size() == 2);
    }
}

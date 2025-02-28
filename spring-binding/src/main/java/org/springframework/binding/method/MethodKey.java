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
package org.springframework.binding.method;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper for resolving and caching a Java method by reflection.
 *
 * @author Keith Donald
 */
public class MethodKey implements Serializable {

    /**
     * Map with primitive wrapper type as key and corresponding primitive type as value, for example: Integer.class ->
     * int.class.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new HashMap<>(8);

    static {
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, boolean.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, byte.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, char.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, double.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, float.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, int.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, long.class);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, short.class);
    }

    /**
     * The class the method is a member of.
     */
    private Class<?> declaredType;

    /**
     * The method name.
     */
    private String methodName;

    /**
     * The method's actual parameter types. Could contain null values if the user did not specify a parameter type for
     * the corresponding parameter
     */
    private Class<?>[] parameterTypes;

    /**
     * A cached handle to the resolved method (may be null).
     */
    private transient Method method;

    /**
     * Create a new method key.
     *
     * @param declaredType   the class the method is a member of
     * @param methodName     the method name
     * @param parameterTypes the method's parameter types, or <code>null</code> if the method has no parameters
     */
    public MethodKey(Class<?> declaredType, String methodName, Class<?>... parameterTypes) {
        Assert.notNull(declaredType, "The method's declared type is required");
        Assert.notNull(methodName, "The method name is required");
        this.declaredType = declaredType;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    /**
     * Determine if the given target type is assignable from the given value type, assuming setting by reflection.
     * Considers primitive wrapper classes as assignable to the corresponding primitive types.
     * <p>
     * NOTE: Pulled from ClassUtils in Spring 2.0 for 1.2.8 compatability.
     *
     * @param targetType the target type
     * @param valueType  the value type that should be assigned to the target type
     * @return if the target type is assignable from the value type
     */
    private static boolean isAssignable(Class<?> targetType, Class<?> valueType) {
        return (targetType.isAssignableFrom(valueType) || targetType.equals(PRIMITIVE_WRAPPER_TYPE_MAP.get(valueType)));
    }

    /**
     * Return the class the method is a member of.
     *
     * @return
     */
    public Class<?> getDeclaredType() {
        return declaredType;
    }

    // internal helpers

    /**
     * Returns the method name.
     *
     * @return
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Returns the method parameter types. Could contain null values if no type was specified for the corresponding
     * parameter.
     *
     * @return
     */
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Returns the keyed method, resolving it if necessary via reflection.
     *
     * @return
     * @throws InvalidMethodKeyException
     * @throws InvalidMethodKeyException
     */
    public Method getMethod() throws InvalidMethodKeyException {
        if (method == null) {
            method = resolveMethod();
        }
        return method;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MethodKey)) {
            return false;
        }
        MethodKey other = (MethodKey) obj;
        return declaredType.equals(other.declaredType) && methodName.equals(other.methodName)
               && parameterTypesEqual(other.parameterTypes);
    }

    public int hashCode() {
        return declaredType.hashCode() + methodName.hashCode() + parameterTypesHash();
    }

    public String toString() {
        return methodName + "(" + parameterTypesString() + ")";
    }

    /**
     * Resolve the keyed method.
     */
    protected Method resolveMethod() throws InvalidMethodKeyException {
        try {
            return declaredType.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            Method method = findMethodConsiderAssignableParameterTypes();
            if (method != null) {
                return method;
            } else {
                throw new InvalidMethodKeyException(this, e);
            }
        }
    }

    /**
     * Find the keyed method using 'relaxed' typing.
     */
    protected Method findMethodConsiderAssignableParameterTypes() {
        Method[] candidateMethods = getDeclaredType().getMethods();
        for (Method candidateMethod : candidateMethods) {
            if (candidateMethod.getName().equals(methodName)) {
                // Check if the method has the correct number of parameters.
                Class<?>[] candidateParameterTypes = candidateMethod.getParameterTypes();
                if (candidateParameterTypes.length == parameterTypes.length) {
                    int numberOfCorrectArguments = 0;
                    for (int j = 0; j < candidateParameterTypes.length; j++) {
                        // Check if the candidate type is assignable to the sig
                        // parameter type.
                        Class<?> candidateType = candidateParameterTypes[j];
                        Class<?> parameterType = parameterTypes[j];
                        if (parameterType != null) {
                            if (isAssignable(candidateType, parameterType)) {
                                numberOfCorrectArguments++;
                            }
                        } else {
                            // just match on a null param type (effectively 'any')
                            numberOfCorrectArguments++;
                        }
                    }
                    if (numberOfCorrectArguments == parameterTypes.length) {
                        return candidateMethod;
                    }
                }
            }
        }
        return null;
    }

    // internal helpers

    private boolean parameterTypesEqual(Class<?>[] other) {
        if (parameterTypes == other) {
            return true;
        }
        if (parameterTypes.length != other.length) {
            return false;
        }
        for (int i = 0; i < this.parameterTypes.length; i++) {
            if (!ObjectUtils.nullSafeEquals(parameterTypes[i], other[i])) {
                return false;
            }
        }
        return true;
    }

    private int parameterTypesHash() {
        if (parameterTypes == null) {
            return 0;
        }
        int hash = 0;
        for (Class<?> parameterType : parameterTypes) {
            if (parameterType != null) {
                hash += parameterType.hashCode();
            }
        }
        return hash;
    }

    /**
     * Convenience method that returns the parameter types describing the signature of the method as a string.
     */
    private String parameterTypesString() {
        StringBuilder parameterTypesString = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == null) {
                parameterTypesString.append("<any>");
            } else {
                parameterTypesString.append(ClassUtils.getShortName(parameterTypes[i]));
            }
            if (i < parameterTypes.length - 1) {
                parameterTypesString.append(',');
            }
        }
        return parameterTypesString.toString();
    }
}

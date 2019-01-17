/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.util;

import com.powsybl.iidm.network.Identifiable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public final class ImmutableTestHelper {

    static final Set<String> RXGB_SETTERS = new HashSet<>(Arrays.asList("setR", "setX", "setG", "setB"));

    static final Set<String> NEW_REACTIVE = new HashSet<>(Arrays.asList("newReactiveCapabilityCurve", "newMinMaxReactiveLimits"));

    private static final String EXPECTED_MESSAGE = "Unmodifiable identifiable";

    public static void testInvalidMethods(Object sut, Set<String> expectedInvalidMethods) {
        Set<String> testedInvalidMethods = new HashSet<>();
        Stream<Method> allMethods = Arrays.asList(sut.getClass().getMethods()).stream();
        Stream<Method> mutableMethods = allMethods.filter(ImmutableTestHelper::isMutableMethods);
        mutableMethods
                .forEach(m -> {
                    try {
                        testedInvalidMethods.add(m.getName());
                        m.setAccessible(true);
                        Class[] parameterTypes = m.getParameterTypes();
                        if (m.getParameterCount() == 0) {
                            m.invoke(sut);
                        } else {
                            Object[] mocks = new Object[parameterTypes.length];
                            for (int i = 0; i < parameterTypes.length; i++) {
                                mocks[i] = any(parameterTypes[i]);
                            }
                            m.invoke(sut, mocks);
                        }
                        fail(m.getName() + " should throw exception.");
                    } catch (InvocationTargetException e) {
                        assertEquals(EXPECTED_MESSAGE, e.getCause().getMessage());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        fail(m.getName() + " not tested");
                    }
                });
        assertEquals(expectedInvalidMethods, testedInvalidMethods);
    }

    private static boolean isMutableMethods(Method m) {
        String name = m.getName();
        return name.startsWith("set") || name.startsWith("new") || name.equals("remove") || name.equals("connect") || name.equals("disconnect");
    }

    public static <T extends Identifiable> void assertElementType(Class expectedClazz, Iterable<T> iterable, Stream<T> stream) {
        Iterator<T> iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(expectedClazz, iterator.next().getClass());
        stream.findAny().ifPresent(e -> assertEquals(expectedClazz, e.getClass()));
    }

    private ImmutableTestHelper() {
    }
}

/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExecutionEnvironmentTest {

    @Test
    public void test() {
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(ImmutableMap.of("var1", "value1"), "test", true);
        assertNotNull(executionEnvironment.getVariables());
        assertEquals(ImmutableMap.of("var1", "value1"), executionEnvironment.getVariables());
        assertEquals("test", executionEnvironment.getWorkingDirPrefix());
        assertTrue(executionEnvironment.isDebug());
        executionEnvironment.setVariables(ImmutableMap.of("var2", "value2"));
        assertNotNull(executionEnvironment.getVariables());
        assertEquals(ImmutableMap.of("var2", "value2"), executionEnvironment.getVariables());
        executionEnvironment.setWorkingDirPrefix("test2");
        assertEquals("test2", executionEnvironment.getWorkingDirPrefix());
        executionEnvironment.setDebug(false);
        assertFalse(executionEnvironment.isDebug());
    }
}

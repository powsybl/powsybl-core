/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CommandExecutionTest {

    private Command command;

    @Before
    public void setUp() {
        command = Mockito.mock(Command.class);
    }

    @Test
    public void getExecutionVariablesTest1() {
        // variables cannot be null
        try {
            CommandExecution.getExecutionVariables(null, new CommandExecution(command, 1, 0, null, null));
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getExecutionVariablesTest2() {
        // overloaded variables can be null
        Map<String, String> executionVariables = CommandExecution.getExecutionVariables(ImmutableMap.of("a", "1", "b", "2"),
                                                                                        new CommandExecution(command, 1, 0, null, null));
        assertEquals(ImmutableMap.of("a", "1", "b", "2"), executionVariables);
    }

    @Test
    public void getExecutionVariablesTest3() {
        // variables and overloadedVariables merge
        Map<String, String> executionVariables = CommandExecution.getExecutionVariables(ImmutableMap.of("a", "1", "b", "2"),
                                                                                        new CommandExecution(command, 1, 0, null, ImmutableMap.of("c", "3")));
        assertEquals(ImmutableMap.of("a", "1", "b", "2",  "c", "3"), executionVariables);
    }

}

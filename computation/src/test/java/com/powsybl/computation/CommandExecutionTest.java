/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class CommandExecutionTest {

    private Command command;

    @BeforeEach
    void setUp() {
        command = Mockito.mock(Command.class);
    }

    @Test
    void getExecutionVariablesTest1() {
        // variables cannot be null
        try {
            CommandExecution.getExecutionVariables(null, new CommandExecution(command, 1, 0, null, null));
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    void getExecutionVariablesTest2() {
        // overloaded variables can be null
        Map<String, String> executionVariables = CommandExecution.getExecutionVariables(ImmutableMap.of("a", "1", "b", "2"),
                                                                                        new CommandExecution(command, 1, 0, null, null));
        assertEquals(ImmutableMap.of("a", "1", "b", "2"), executionVariables);
    }

    @Test
    void getExecutionVariablesTest3() {
        // variables and overloadedVariables merge
        Map<String, String> executionVariables = CommandExecution.getExecutionVariables(ImmutableMap.of("a", "1", "b", "2"),
                                                                                        new CommandExecution(command, 1, 0, null, ImmutableMap.of("c", "3")));
        assertEquals(ImmutableMap.of("a", "1", "b", "2", "c", "3"), executionVariables);
    }

}

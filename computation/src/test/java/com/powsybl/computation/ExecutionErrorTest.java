/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExecutionErrorTest {

    @Test
    public void test() {
        Command command = Mockito.mock(Command.class);
        Mockito.when(command.getId())
                .thenReturn("cmd");
        ExecutionError executionError = new ExecutionError(command, 0, -1);
        assertSame(command, executionError.getCommand());
        assertEquals(0, executionError.getIndex());
        assertEquals(-1, executionError.getExitCode());
        assertEquals("cmd[0]=-1", executionError.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testErrorIndex() {
        Command command = Mockito.mock(Command.class);
        new ExecutionError(command, -1, -1);
    }
}

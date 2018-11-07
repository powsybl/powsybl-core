/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.statistic;

import org.junit.Test;

import java.util.function.Consumer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class StringBasedComputationDetailTest {

    @Test
    public void testStarted() {
        StringBasedComputationDetail sut = new StringBasedComputationDetail();
        Consumer<ProgramStartedLog> startedLogConsumer = mock(Consumer.class);

        assertTrue(sut.updateProgramStarted("S 1231006505 foo 0 2"));
        assertTrue(sut.updateProgramStarted("S 1231006507 foo 1 2"));
        assertFalse(sut.updateProgramStarted("S 1231006500 bar 0 1")); // an older log detected
        assertEquals("foo", sut.getProgramStartedLog().getProgramId());
        assertNull(sut.getErrorOccuredLog());
        assertTrue(sut.updateProgramError("S 1231006510 foo 0 2 127"));
        assertTrue(sut.getErrorOccuredLog().getExecutions()[0]);
        assertFalse(sut.getErrorOccuredLog().getExecutions()[1]);
        assertEquals(127, sut.getErrorOccuredLog().getExitCode());
    }

}

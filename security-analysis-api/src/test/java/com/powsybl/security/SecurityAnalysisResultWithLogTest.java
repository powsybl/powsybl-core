/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class SecurityAnalysisResultWithLogTest {

    @Test
    public void baseTest() {
        SecurityAnalysisResult sar = mock(SecurityAnalysisResult.class);
        SecurityAnalysisResultWithLog sut = new SecurityAnalysisResultWithLog(sar);
        assertSame(sar, sut.getResult());
        assertFalse(sut.getLogBytes().isPresent());
        byte[] logs = new byte[10];
        SecurityAnalysisResultWithLog sut1 = new SecurityAnalysisResultWithLog(sar, logs);
        assertSame(logs, sut1.getLogBytes().orElse(new byte[1]));
        try {
            new SecurityAnalysisResultWithLog(null);
            fail();
        } catch (NullPointerException e) {
            // ignored
        }
    }
}

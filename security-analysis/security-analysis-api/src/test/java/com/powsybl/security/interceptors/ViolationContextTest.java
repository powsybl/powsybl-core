/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.security.LimitViolation;
import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ViolationContextTest {

    @Test
    public void test() {
        RunningContext runningContext = mock(RunningContext.class);
        LimitViolation limitViolation = mock(LimitViolation.class);
        ViolationContext sut = new ViolationContext(runningContext, limitViolation);
        assertSame(runningContext, sut.getRunningContext());
        assertSame(limitViolation, sut.getLimitViolation());
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.interceptors;

import com.powsybl.contingency.Contingency;
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
        Contingency contingency = mock(Contingency.class);
        ViolationContext sut = new ViolationContext(runningContext, contingency);
        assertSame(runningContext, sut.getRunningContext());
        assertSame(contingency, sut.getContingency().orElseGet(() -> mock(Contingency.class)));

    }
}

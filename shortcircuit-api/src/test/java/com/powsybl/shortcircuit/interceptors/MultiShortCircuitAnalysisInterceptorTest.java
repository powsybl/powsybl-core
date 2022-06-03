/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.interceptors;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class MultiShortCircuitAnalysisInterceptorTest {
    @Test
    public void test() {
        Assert.assertEquals(Collections.singleton("ShortCircuitInterceptorExtensionMock"), ShortCircuitAnalysisInterceptors.getExtensionNames());

        ShortCircuitAnalysisInterceptor interceptor = ShortCircuitAnalysisInterceptors.createInterceptor("ShortCircuitInterceptorExtensionMock");
        assertNotNull(interceptor);
        assertEquals(ShortCircuitAnalysisInterceptorMock.class, interceptor.getClass());

        try {
            interceptor = ShortCircuitAnalysisInterceptors.createInterceptor(null);
            fail();
        } catch (NullPointerException e) {
            // Nothing to do
        }

        try {
            interceptor = ShortCircuitAnalysisInterceptors.createInterceptor("unknown-shortcircuit-analysis-interceptor");
            fail();
        } catch (IllegalArgumentException e) {
            // Nothing to do
        }
    }
}

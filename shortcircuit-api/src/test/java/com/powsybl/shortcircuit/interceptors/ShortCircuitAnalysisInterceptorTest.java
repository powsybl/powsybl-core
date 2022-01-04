/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.interceptors;

import org.junit.Test;

import static org.junit.Assert.fail;

public class ShortCircuitAnalysisInterceptorTest {
    @Test
    public void test() {

        try {
            ShortCircuitAnalysisInterceptor interceptor = ShortCircuitAnalysisInterceptors.createInterceptor(null);
            fail();
        } catch (NullPointerException e) {
            // Nothing to do
        }

        try {
            ShortCircuitAnalysisInterceptor interceptor = ShortCircuitAnalysisInterceptors.createInterceptor("unknown-shortcircuit-analysis-interceptor");
            fail();
        } catch (IllegalArgumentException e) {
            // Nothing to do
        }
    }
}

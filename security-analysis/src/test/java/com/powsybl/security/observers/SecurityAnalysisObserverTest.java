/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.observers;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class SecurityAnalysisObserverTest {

    @Test
    public void test() {
        assertEquals(Collections.singleton("SecurityAnalysisObserverMock"), SecurityAnalysisObservers.getExtensionNames());

        SecurityAnalysisObserver observer = SecurityAnalysisObservers.createObserver("SecurityAnalysisObserverMock");
        assertNotNull(observer);
        assertEquals(SecurityAnalysisObserverMock.class, observer.getClass());

        try {
            observer = SecurityAnalysisObservers.createObserver(null);
            fail();
        } catch (NullPointerException e) {
            // Nothing to do
        }

        observer = SecurityAnalysisObservers.createObserver("unknown-security-analysis-observer");
        assertNull(observer);
    }

}

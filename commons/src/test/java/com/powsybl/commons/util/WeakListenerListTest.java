/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WeakListenerListTest {

    interface TestListener {

        void onTest();
    }

    @Test
    public void concurrencyIssueTest() {
        // 2 adds using a different target object
        WeakListenerList<TestListener> listeners = new WeakListenerList<>();
        TestListener l = () -> {
            TestListener l2 = () -> {
            };
            listeners.add(l2);
        };
        listeners.add(l);

        // check there is no more java.util.ConcurrentModificationException coming from
        // the WeakHashMap
        listeners.notify(TestListener::onTest);

        assertTrue(listeners.remove(l));
        assertFalse(listeners.remove(l));
    }
}

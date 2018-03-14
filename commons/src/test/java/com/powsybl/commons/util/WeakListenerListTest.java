/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import org.junit.Test;

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
        Object target1 = new Object();
        Object target2 = new Object();
        WeakListenerList<TestListener> listeners = new WeakListenerList<>();
        listeners.add(target1, () -> listeners.add(target2, () -> {
        }));

        // check there is no more java.util.ConcurrentModificationException coming from
        // the WeakHashMap
        listeners.notify(TestListener::onTest);
        listeners.notify((target, testListener) -> testListener.onTest());
    }

    @Test
    public void concurrencyIssueTest2() {
        // 2 adds using same target object
        Object target1 = new Object();
        WeakListenerList<TestListener> listeners = new WeakListenerList<>();
        listeners.add(target1, () -> listeners.add(target1, () -> {
        }));

        // check there is no more java.util.ConcurrentModificationException coming from
        // the ArrayList
        listeners.notify(TestListener::onTest);
        listeners.notify((target, testListener) -> testListener.onTest());
    }
}

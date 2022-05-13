/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.monitor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class StateMonitorIndexTest {

    @Test
    public void test() {
        List<StateMonitor> monitors = new ArrayList<>();
        monitors.add(new StateMonitor(new FaultContext("f00"), false, false));
        monitors.add(new StateMonitor(new FaultContext("f01"), false, true));
        monitors.add(new StateMonitor(new FaultContext("f10"), true, false));
        monitors.add(new StateMonitor(new FaultContext("f11"), true, true));

        StateMonitorIndex monitorIndex = new StateMonitorIndex(monitors);

        assertEquals(new StateMonitor(new FaultContext("f00"), false, false), monitorIndex.getStateMonitors().get("f00"));
        assertEquals(new StateMonitor(new FaultContext("f01"), false, true), monitorIndex.getStateMonitors().get("f01"));
        assertEquals(new StateMonitor(new FaultContext("f10"), true, false), monitorIndex.getStateMonitors().get("f10"));
        assertEquals(new StateMonitor(new FaultContext("f11"), true, true), monitorIndex.getStateMonitors().get("f11"));
    }
}

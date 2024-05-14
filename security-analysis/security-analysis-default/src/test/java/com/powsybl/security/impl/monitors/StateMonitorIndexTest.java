/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.impl.monitors;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.monitor.StateMonitorIndex;
import org.junit.jupiter.api.Test;

import java.util.*;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class StateMonitorIndexTest {

    @Test
    void test() {
        List<StateMonitor> monitors = new ArrayList<>();
        monitors.add(new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC),
            Collections.singleton("Branch1"), Collections.singleton("Bus1"), Collections.singleton("ThreeWindingsTransformer1")));
        monitors.add(new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC),
            Collections.singleton("Branch6"), Collections.singleton("Bus6"), Collections.singleton("ThreeWindingsTransformer6")));
        monitors.add(new StateMonitor(new ContingencyContext(null, ContingencyContextType.NONE),
            Collections.singleton("Branch2"), Collections.singleton("Bus2"), Collections.singleton("ThreeWindingsTransformer2")));
        monitors.add(new StateMonitor(new ContingencyContext(null, ContingencyContextType.NONE),
            Collections.singleton("Branch3"), Collections.singleton("Bus3"), Collections.singleton("ThreeWindingsTransformer3")));
        monitors.add(new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL),
            Collections.singleton("Branch4"), Collections.singleton("Bus4"), Collections.singleton("ThreeWindingsTransformer4")));
        monitors.add(new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL),
            Collections.singleton("Branch5"), Collections.singleton("Bus5"), Collections.singleton("ThreeWindingsTransformer5")));
        StateMonitorIndex monitorIndex = new StateMonitorIndex(monitors);

        assertEquals(new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC), Set.of("Branch1", "Branch6"),
            Set.of("Bus1", "Bus6"), Set.of("ThreeWindingsTransformer1", "ThreeWindingsTransformer6")), monitorIndex.getSpecificStateMonitors().get("contingency1"));
        assertEquals(new StateMonitor(new ContingencyContext(null, ContingencyContextType.ALL), Set.of("Branch4", "Branch5"),
            Set.of("Bus4", "Bus5"), Set.of("ThreeWindingsTransformer4", "ThreeWindingsTransformer5")), monitorIndex.getAllStateMonitor());
        assertEquals(new StateMonitor(new ContingencyContext(null, ContingencyContextType.NONE), Set.of("Branch2", "Branch3"),
            Set.of("Bus2", "Bus3"), Set.of("ThreeWindingsTransformer2", "ThreeWindingsTransformer3")), monitorIndex.getNoneStateMonitor());

    }
}

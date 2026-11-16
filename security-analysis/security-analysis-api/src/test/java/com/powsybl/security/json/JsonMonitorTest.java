/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.contingency.ContingencyContextType;
import com.powsybl.security.monitor.StateMonitor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class JsonMonitorTest extends AbstractSerDeTest {
    @Test
    void roundTrip() throws IOException {
        List<StateMonitor> monitors = new ArrayList<>();
        monitors.add(new StateMonitor(new ContingencyContext("contingency1", ContingencyContextType.SPECIFIC),
            Collections.singleton("Branch1"), Collections.singleton("Bus1"), Collections.singleton("ThreeWindingsTransformer1")));
        roundTripTest(monitors, StateMonitor::write, StateMonitor::read, "/MonitoringFileTest.json");
    }
}

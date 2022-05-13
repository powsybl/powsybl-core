/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.shortcircuit.monitor.FaultContext;
import com.powsybl.shortcircuit.monitor.StateMonitor;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class JsonMonitorTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        List<StateMonitor> monitors = new ArrayList<>();
        monitors.add(new com.powsybl.shortcircuit.monitor.StateMonitor(new FaultContext("f00"), false, false));
        monitors.add(new com.powsybl.shortcircuit.monitor.StateMonitor(new FaultContext("f01"), false, true));
        monitors.add(new com.powsybl.shortcircuit.monitor.StateMonitor(new FaultContext("f10"), true, false));
        monitors.add(new com.powsybl.shortcircuit.monitor.StateMonitor(new FaultContext("f11"), true, true));
        roundTripTest(monitors, StateMonitor::write, StateMonitor::read, "/MonitoringFile.json");
    }

    @Test
    public void readError() throws IOException {
        Files.copy(getClass().getResourceAsStream("/MonitoringFileInvalid.json"), fileSystem.getPath("/MonitoringFileInvalid.json"));

        expected.expect(UncheckedIOException.class);
        expected.expectMessage("Unrecognized field \"unexpected\"");
        StateMonitor.read(fileSystem.getPath("/MonitoringFileInvalid.json"));
    }
}

/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.extensions.OperatingStatus;
import com.powsybl.iidm.network.impl.extensions.ActivePowerControlImpl;
import com.powsybl.iidm.network.impl.extensions.OperatingStatusImpl;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmVersion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class AlternativeExtensionXmlTest extends AbstractIidmSerDeTest {

    // This test is the same as OperatingStatusXmlTest::test, but the extension is exported/imported
    // using the defined alternative (which is not versioned).
    @Test
    void nonVersionedAlternativeTest() throws IOException {
        Network network = OperatingStatusXmlTest.createTestNetwork();

        // extend line
        Line line = network.getLine("L");
        assertNotNull(line);
        OperatingStatus<Line> lineOperatingStatus = new OperatingStatusImpl<>(line,
                OperatingStatus.Status.PLANNED_OUTAGE);
        line.addExtension(OperatingStatus.class, lineOperatingStatus);

        var exportOptions = new ExportOptions().addExtensionVersion(OperatingStatus.NAME, "alternative");
        Network network2 = allFormatsRoundTripTest(network, "/alternativeOperatingStatusRef.xml", exportOptions);

        Line line2 = network2.getLine("L");
        assertNotNull(line2);
        OperatingStatus<Line> lineOperatingStatus2 = line2.getExtension(OperatingStatus.class);
        assertNotNull(lineOperatingStatus2);
        assertEquals(lineOperatingStatus.getStatus(), lineOperatingStatus2.getStatus());

        lineOperatingStatus2.setStatus(OperatingStatus.Status.IN_OPERATION);
        assertEquals(OperatingStatus.Status.IN_OPERATION, lineOperatingStatus2.getStatus());
    }

    @Test
    void versionedAlternativeWithDefaultVersionTest() throws IOException {
        Network network = BatteryNetworkFactory.create();

        Battery bat = network.getBattery("BAT");
        bat.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(bat, true, 4.0, 1.2));

        Generator generator = network.getGenerator("GEN");
        generator.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(generator, false, 3.0, 1.0));

        // No version is specified for the alternative: it should use the default version (v1.1)
        var exportOptions = new ExportOptions().addExtensionVersion(ActivePowerControl.NAME, "alternative");
        Network network2 = allFormatsRoundTripTest(network, "/alternativeActivePowerControlV1_1.xml", IidmVersion.V_1_0, exportOptions);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);
        assertEquals(1.2, activePowerControl2.getParticipationFactor(), 0.001d); // default version supports participationFactor
    }

    @Test
    void versionedAlternativeWithSpecificVersionTest() throws IOException {
        Network network = BatteryNetworkFactory.create();

        Battery bat = network.getBattery("BAT");
        bat.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(bat, true, 4.0, 1.2));

        Generator generator = network.getGenerator("GEN");
        generator.addExtension(ActivePowerControl.class, new ActivePowerControlImpl<>(generator, false, 3.0, 1.0));

        // Explicitly ask for version 1.0 of the alternative
        var exportOptions = new ExportOptions().addExtensionVersion(ActivePowerControl.NAME, "alternative-1.0");
        Network network2 = allFormatsRoundTripTest(network, "/alternativeActivePowerControlV1_0.xml", IidmVersion.V_1_0, exportOptions);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        ActivePowerControl<Battery> activePowerControl2 = bat2.getExtension(ActivePowerControl.class);
        assertNotNull(activePowerControl2);
        assertTrue(Double.isNaN(activePowerControl2.getParticipationFactor())); // version 1.0 does NOT support participationFactor
    }
}

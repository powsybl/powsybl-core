/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.PilotPoint;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SecondaryVoltageControlXmlTest extends AbstractIidmSerDeTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.setCaseDate(ZonedDateTime.parse("2023-01-07T20:43:11.819+01:00"));

        network.newExtension(SecondaryVoltageControlAdder.class)
                .newControlZone()
                    .withName("z1")
                    .newPilotPoint()
                        .withBuses(List.of(new PilotPoint.BusRef("VLLOAD", "NLOAD")))
                        .withTargetV(15d)
                    .add()
                    .newControlUnit()
                        .withId("GEN")
                        .withParticipate(false)
                    .add()
                    .newControlUnit()
                        .withId("GEN2")
                    .add()
                .add()
            .add();
        return network;
    }

    private static void assertControlEquals(SecondaryVoltageControl control, SecondaryVoltageControl control2) {
        assertNotNull(control2);
        assertEquals(control.getControlZones().size(), control2.getControlZones().size());
        assertEquals(control.getControlZones().get(0).getPilotPoint().getBuses(),
                     control2.getControlZones().get(0).getPilotPoint().getBuses());
        assertEquals(control.getControlZones().get(0).getPilotPoint().getBusbarSectionIds(),
                     control2.getControlZones().get(0).getPilotPoint().getBusbarSectionIds());
        assertEquals(control.getControlZones().get(0).getPilotPoint().getTargetV(),
                     control2.getControlZones().get(0).getPilotPoint().getTargetV(), 0d);
        assertEquals(control.getControlZones().get(0).getControlUnits().size(),
                     control2.getControlZones().get(0).getControlUnits().size());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(0).getId(),
                     control2.getControlZones().get(0).getControlUnits().get(0).getId());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(0).isParticipate(),
                     control2.getControlZones().get(0).getControlUnits().get(0).isParticipate());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(1).getId(),
                     control2.getControlZones().get(0).getControlUnits().get(1).getId());
        assertEquals(control.getControlZones().get(0).getControlUnits().get(1).isParticipate(),
                     control2.getControlZones().get(0).getControlUnits().get(1).isParticipate());
    }

    @Test
    void test() throws IOException {
        Network network = createNetwork();
        SecondaryVoltageControl control = network.getExtension(SecondaryVoltageControl.class);

        Network network2 = allFormatsRoundTripTest(network, "/secondaryVoltageControlRoundTripRef.xml", CURRENT_IIDM_VERSION);

        assertControlEquals(control, network2.getExtension(SecondaryVoltageControl.class));
    }

    @Test
    void testNodeBreakerWithBusbarSections() throws IOException {
        Network network = NetworkTest1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2023-01-07T20:43:11.819+01:00"));

        SecondaryVoltageControl control = network.newExtension(SecondaryVoltageControlAdder.class)
                .newControlZone()
                    .withName("z1")
                    .newPilotPoint()
                        .withBusbarSectionIds(List.of("voltageLevel1BusbarSection1", "voltageLevel1BusbarSection2"))
                        .withTargetV(400d)
                    .add()
                    .newControlUnit()
                        .withId("generator1")
                        .withParticipate(false)
                    .add()
                .add()
            .add();

        Network network2 = allFormatsRoundTripTest(network, "/secondaryVoltageControlNodeBreakerRoundTripRef.xml", CURRENT_IIDM_VERSION);

        SecondaryVoltageControl control2 = network2.getExtension(SecondaryVoltageControl.class);
        assertNotNull(control2);
        PilotPoint pilotPoint = control.getControlZones().get(0).getPilotPoint();
        PilotPoint pilotPoint2 = control2.getControlZones().get(0).getPilotPoint();
        assertEquals(List.of(), pilotPoint2.getBuses());
        assertEquals(List.of("voltageLevel1BusbarSection1", "voltageLevel1BusbarSection2"), pilotPoint2.getBusbarSectionIds());
        assertEquals(pilotPoint.getBusbarSectionIds(), pilotPoint2.getBusbarSectionIds());
        assertEquals(pilotPoint.getTargetV(), pilotPoint2.getTargetV(), 0d);
    }

    @Test
    void testVersion10() throws IOException {
        Network network = createNetwork();
        SecondaryVoltageControl control = network.getExtension(SecondaryVoltageControl.class);

        // Version 1.0 does not distinguish buses from busbar sections and does not serialize the voltage level.
        // On reading back, the flat "NLOAD" reference is resolved against the network into the bus of VLLOAD.
        Network network2 = allFormatsRoundTripTest(network, "/secondaryVoltageControlRoundTripRef-1.0.xml", CURRENT_IIDM_VERSION,
                new ExportOptions().addExtensionVersion(SecondaryVoltageControl.NAME, "1.0"));

        assertControlEquals(control, network2.getExtension(SecondaryVoltageControl.class));
    }
}

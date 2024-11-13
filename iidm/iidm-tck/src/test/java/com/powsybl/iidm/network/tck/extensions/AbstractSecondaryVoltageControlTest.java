/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.events.ExtensionCreationNetworkEvent;
import com.powsybl.iidm.network.events.ExtensionRemovalNetworkEvent;
import com.powsybl.iidm.network.events.ExtensionUpdateNetworkEvent;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractSecondaryVoltageControlTest {

    private Network network;

    private SecondaryVoltageControl control;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        control = network.newExtension(SecondaryVoltageControlAdder.class)
                    .newControlZone()
                        .withName("z1")
                        .newPilotPoint()
                            .withBusbarSectionsOrBusesIds(List.of("NLOAD"))
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
    }

    @Test
    public void test() throws IOException {
        assertEquals(1, control.getControlZones().size());
        assertTrue(control.getControlZone("z1").isPresent());
        ControlZone z1 = control.getControlZones().get(0);
        assertEquals("z1", z1.getName());
        assertNotNull(z1.getPilotPoint());
        assertEquals(List.of("NLOAD"), z1.getPilotPoint().getBusbarSectionsOrBusesIds());
        assertEquals(15d, z1.getPilotPoint().getTargetV(), 0d);
        assertEquals(2, z1.getControlUnits().size());
        assertEquals("GEN", z1.getControlUnits().get(0).getId());
        assertFalse(z1.getControlUnits().get(0).isParticipate());
        assertEquals("GEN2", z1.getControlUnits().get(1).getId());
        assertTrue(z1.getControlUnits().get(1).isParticipate());
        z1.getPilotPoint().setTargetV(16);
        assertEquals(16d, z1.getPilotPoint().getTargetV(), 0d);
        assertTrue(z1.getControlUnit("GEN").isPresent());
    }

    @Test
    public void pilotPointTargetVoltageNotificationTest() {
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        ControlZone controlZone = control.getControlZones().get(0);
        controlZone.getPilotPoint().setTargetV(16);
        assertEquals(List.of(new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "pilotPointTargetV", null,
                new PilotPoint.TargetVoltageEvent("z1", 15d), new PilotPoint.TargetVoltageEvent("z1", 16d))),
                eventRecorder.getEvents());
    }

    @Test
    public void controlUnitParticipateNotificationTest() {
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        ControlZone controlZone = control.getControlZones().get(0);
        ControlUnit controlUnit = controlZone.getControlUnits().get(0);
        controlUnit.setParticipate(true);
        assertEquals(List.of(new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "controlUnitParticipate", null,
                        new ControlUnit.ParticipateEvent("z1", "GEN", false), new ControlUnit.ParticipateEvent("z1", "GEN", true))),
                eventRecorder.getEvents());
    }

    @Test
    public void extensionRemovalAndCreationNotificationTest() {
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);

        network.removeExtension(SecondaryVoltageControl.class);
        assertEquals(List.of(new ExtensionRemovalNetworkEvent("sim1", "secondaryVoltageControl", false),
                             new ExtensionRemovalNetworkEvent("sim1", "secondaryVoltageControl", true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        control = network.newExtension(SecondaryVoltageControlAdder.class)
                .newControlZone()
                    .withName("z2")
                    .newPilotPoint()
                        .withBusbarSectionsOrBusesIds(List.of("NGEN"))
                        .withTargetV(7d)
                    .add()
                    .newControlUnit()
                        .withId("GEN")
                    .add()
                .add()
            .add();
        assertEquals(List.of(new ExtensionCreationNetworkEvent("sim1", "secondaryVoltageControl")),
                eventRecorder.getEvents());
    }
}

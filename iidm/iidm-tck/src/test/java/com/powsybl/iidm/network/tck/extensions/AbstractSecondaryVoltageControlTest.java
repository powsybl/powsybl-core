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
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.events.ExtensionCreationNetworkEvent;
import com.powsybl.iidm.network.events.ExtensionRemovalNetworkEvent;
import com.powsybl.iidm.network.events.ExtensionUpdateNetworkEvent;
import com.powsybl.iidm.network.extensions.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public void test() {
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
        assertEquals(List.of(new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "pilotPointTargetV", "InitialState",
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
        assertEquals(List.of(new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "controlUnitParticipate", "InitialState",
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

    @Test
    public void variantTest() {
        network.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "v");
        ControlZone z1 = control.getControlZones().get(0);
        assertEquals(15d, z1.getPilotPoint().getTargetV(), 0d);
        ControlUnit cu1 = z1.getControlUnits().get(0);
        ControlUnit cu2 = z1.getControlUnits().get(1);
        assertFalse(cu1.isParticipate());
        assertTrue(cu2.isParticipate());

        // try to change value on new variant
        network.getVariantManager().setWorkingVariant("v");
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        z1.getPilotPoint().setTargetV(16d);
        assertEquals(16d, z1.getPilotPoint().getTargetV(), 0d);
        cu1.setParticipate(true);
        cu2.setParticipate(false);
        assertTrue(cu1.isParticipate());
        assertFalse(cu2.isParticipate());

        // check events are correctly tagged with variant id
        assertEquals(List.of(
                new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "pilotPointTargetV", "v",
                        new PilotPoint.TargetVoltageEvent("z1", 15d), new PilotPoint.TargetVoltageEvent("z1", 16d)),
                new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "controlUnitParticipate", "v",
                        new ControlUnit.ParticipateEvent("z1", "GEN", false), new ControlUnit.ParticipateEvent("z1", "GEN", true)),
                new ExtensionUpdateNetworkEvent("sim1", "secondaryVoltageControl", "controlUnitParticipate", "v",
                        new ControlUnit.ParticipateEvent("z1", "GEN2", true), new ControlUnit.ParticipateEvent("z1", "GEN2", false))),
                eventRecorder.getEvents());

        // check the initial variant is unchanged
        network.getVariantManager().setWorkingVariant(VariantManagerConstants.INITIAL_VARIANT_ID);
        assertEquals(15d, z1.getPilotPoint().getTargetV(), 0d);
        assertFalse(cu1.isParticipate());
        assertTrue(cu2.isParticipate());

        // check variant copy
        network.getVariantManager().cloneVariant("v", "v2");
        network.getVariantManager().setWorkingVariant("v2");
        assertEquals(16d, z1.getPilotPoint().getTargetV(), 0d);
        assertTrue(cu1.isParticipate());
        assertFalse(cu2.isParticipate());

        // remove variant 'v' and check 'v2' is unchanged
        network.getVariantManager().removeVariant("v");
        assertEquals(16d, z1.getPilotPoint().getTargetV(), 0d);
        assertTrue(cu1.isParticipate());
        assertFalse(cu2.isParticipate());

        // re-clone initial variant on 'v'
        network.getVariantManager().cloneVariant("v2", "v");
        network.getVariantManager().setWorkingVariant("v");
        assertEquals(16d, z1.getPilotPoint().getTargetV(), 0d);
        assertTrue(cu1.isParticipate());
        assertFalse(cu2.isParticipate());
    }
}

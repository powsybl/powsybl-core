/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ControlUnit;
import com.powsybl.iidm.network.extensions.ControlZone;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
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
    }

    @Test
    public void pilotPointTargetVoltageNotificationTest() {
        boolean[] notified = new boolean[1];
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void onExtensionUpdate(Extension<?> extendable, String attribute, Object oldValue, Object newValue) {
                assertInstanceOf(SecondaryVoltageControl.class, extendable);
                assertEquals("pilotPointTargetV", attribute);
                assertEquals(15d, (double) oldValue, 0d);
                assertEquals(16d, (double) newValue, 0d);
                notified[0] = true;
            }
        });
        ControlZone controlZone = control.getControlZones().get(0);
        controlZone.getPilotPoint().setTargetV(16);
        assertTrue(notified[0]);
    }

    @Test
    public void controlUnitParticipateNotificationTest() {
        boolean[] notified = new boolean[1];
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void onExtensionUpdate(Extension<?> extendable, String attribute, Object oldValue, Object newValue) {
                assertInstanceOf(SecondaryVoltageControl.class, extendable);
                assertEquals("controlUnitParticipate", attribute);
                assertFalse((boolean) oldValue);
                assertTrue((boolean) newValue);
                notified[0] = true;
            }
        });
        ControlZone controlZone = control.getControlZones().get(0);
        ControlUnit controlUnit = controlZone.getControlUnits().get(0);
        controlUnit.setParticipate(true);
        assertTrue(notified[0]);
    }
}

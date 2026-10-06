/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.BatteryAdder;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
@SuppressWarnings("removal") // Removal warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnBatteryTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    public void testBatteryFullDeprecatedMethod() {
        // GIVEN
        int targetQ = 10;

        BatteryAdder adder = voltageLevel.newBattery()
            .setId("battery_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(10)
            .setMaxP(20)
            .setTargetQ(targetQ); // deprecated method
        // WHEN
        Battery battery = adder.add();
        // THEN
        assertNotNull(battery);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.REACTIVE_POWER,
            false,
            null,
            false);
        checkVoltageRegulationAttributes(expectedAttributes, battery);

        assertEquals(battery.getTerminal(), battery.getRegulatingTerminal());
        assertNull(battery.getVoltageRegulation());

        assertTrue(Double.isNaN(battery.getLocalTargetV()));
        assertTrue(Double.isNaN(battery.getRegulatingTargetV()));

        assertEquals(targetQ, battery.getLocalTargetQ());
        assertEquals(targetQ, battery.getTargetQ());
        assertEquals(targetQ, battery.getRegulatingTargetQ());

        assertFalse(battery.isRegulating());
    }

    @Test
    public void testBatteryFullDeprecatedMethodAndNewMethods() {
        // GIVEN
        int oldTargetQ = 10;
        int newTargetQ = 20;

        BatteryAdder adder = voltageLevel.newBattery()
            .setId("battery_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(10)
            .setMaxP(20)
            .setTargetQ(oldTargetQ) // deprecated method
            .setLocalTargetQ(newTargetQ);
        // WHEN
        Battery battery = adder.add();
        // THEN
        assertNotNull(battery);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            newTargetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.REACTIVE_POWER,
            false,
            null,
            false);
        checkVoltageRegulationAttributes(expectedAttributes, battery);

        assertEquals(battery.getTerminal(), battery.getRegulatingTerminal());
        assertNull(battery.getVoltageRegulation());

        assertTrue(Double.isNaN(battery.getLocalTargetV()));
        assertTrue(Double.isNaN(battery.getRegulatingTargetV()));

        assertEquals(newTargetQ, battery.getLocalTargetQ());
        assertEquals(newTargetQ, battery.getTargetQ());
        assertEquals(newTargetQ, battery.getRegulatingTargetQ());

        assertFalse(battery.isRegulating());
    }

    @Test
    void testNotifyUpdateOnSetTargetQ() {
        // GIVEN
        Battery battery = network.getBatteryStream().toList().getFirst();
        String id = battery.getId();
        double newTargetQ = 123.0;
        double oldTargetQ = battery.getTargetQ();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        battery.setTargetQ(newTargetQ);
        // THEN
        assertEquals(2, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals("localTargetQ", ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(newTargetQ, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(oldTargetQ, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) firstEvent).id());
        NetworkEvent secondEvent = listener.getEvents().get(1);
        assertEquals(NetworkEvent.Type.UPDATE, secondEvent.getType());
        assertEquals("targetQ", ((UpdateNetworkEvent) secondEvent).attribute());
        assertEquals(newTargetQ, ((UpdateNetworkEvent) secondEvent).newValue());
        assertEquals(oldTargetQ, ((UpdateNetworkEvent) secondEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) secondEvent).id());
    }

}

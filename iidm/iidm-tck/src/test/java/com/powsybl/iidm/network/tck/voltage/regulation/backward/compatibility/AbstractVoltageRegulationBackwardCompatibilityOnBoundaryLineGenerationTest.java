/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.BoundaryLineAdder;
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
@SuppressWarnings("deprecation") // Deprecation warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnBoundaryLineGenerationTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    private BoundaryLineAdder boundaryLineAdder(String id) {
        return voltageLevel.newBoundaryLine()
            .setId(id)
            .setR(1.0)
            .setX(1.0)
            .setG(0.0)
            .setB(0.0)
            .setP0(0.0)
            .setQ0(0.0)
            .setPairingKey("code_" + id)
            .setBus(LOCAL_BUS)
            .setConnectableBus(LOCAL_BUS);
    }

    @Test
    void testBoundaryLineGenerationRegulationOff() {
        // GIVEN
        double targetV = 220.0;
        double targetQ = 10.0;
        BoundaryLineAdder adder = boundaryLineAdder("bl_backwardCompatibility_off")
            .newGeneration()
                .setTargetP(20)
                .setMinP(0)
                .setMaxP(100)
                .setTargetV(targetV) // deprecated method
                .setTargetQ(targetQ) // deprecated method
                .setVoltageRegulationOn(false) // deprecated method
            .add();
        // WHEN
        BoundaryLine boundaryLine = adder.add();
        // THEN
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        assertNotNull(generation);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            null,
            false,
            null,
            false);
        checkVoltageRegulationAttributes(expectedAttributes, generation);

        assertEquals(targetV, generation.getTargetV());
        assertEquals(targetQ, generation.getTargetQ());
        assertFalse(generation.isVoltageRegulationOn());
        assertFalse(generation.isRegulating());
    }

    @Test
    void testBoundaryLineGenerationRegulationOn() {
        // GIVEN
        double targetV = 220.0;
        double targetQ = 10.0;
        BoundaryLineAdder adder = boundaryLineAdder("bl_backwardCompatibility_on")
            .newGeneration()
                .setTargetP(20)
                .setMinP(0)
                .setMaxP(100)
                .setTargetV(targetV) // deprecated method
                .setTargetQ(targetQ) // deprecated method
                .setVoltageRegulationOn(true) // deprecated method
            .add();
        // WHEN
        BoundaryLine boundaryLine = adder.add();
        // THEN
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        assertNotNull(generation);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generation);

        assertNull(generation.getVoltageRegulation().getTerminal());
        assertTrue(Double.isNaN(generation.getVoltageRegulation().getTargetValue()));

        assertEquals(targetV, generation.getTargetV());
        assertEquals(targetQ, generation.getTargetQ());
        assertTrue(generation.isVoltageRegulationOn());
        assertTrue(generation.isRegulating());
    }

    @Test
    void testBoundaryLineGenerationRegulationOnWithPartialDeprecatedMethods() {
        // GIVEN
        double targetV = 220.0;
        double targetQ = 10.0;
        BoundaryLineAdder adder = boundaryLineAdder("bl_backwardCompatibility_partial")
            .newGeneration()
                .setTargetP(20)
                .setMinP(0)
                .setMaxP(100)
                .setTargetV(targetV) // deprecated method
                .setTargetQ(targetQ) // deprecated method
                .newVoltageRegulation()
                    .withMode(RegulationMode.VOLTAGE)
                    .add()
            .add();
        // WHEN
        BoundaryLine boundaryLine = adder.add();
        // THEN
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        assertNotNull(generation);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generation);

        assertEquals(targetV, generation.getTargetV());
        assertEquals(targetQ, generation.getTargetQ());
        assertTrue(generation.isRegulating());
    }

    @Test
    void testNotifyUpdateOnSetTargetQ() {
        // GIVEN
        BoundaryLine boundaryLine = boundaryLineAdder("bl_backwardCompatibility_notifyQ")
            .newGeneration()
                .setTargetP(20)
                .setMinP(0)
                .setMaxP(100)
                .setTargetV(220.0) // deprecated method
                .setTargetQ(10.0) // deprecated method
                .setVoltageRegulationOn(false) // deprecated method
            .add()
            .add();
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        double oldTargetQ = generation.getTargetQ();
        double newTargetQ = 123.0;
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generation.setTargetQ(newTargetQ); // deprecated method
        // THEN
        assertEquals(1, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals("localTargetQ", ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(newTargetQ, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(oldTargetQ, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(boundaryLine.getId(), ((UpdateNetworkEvent) firstEvent).id());
    }

    @Test
    void testNotifyUpdateOnSetTargetV() {
        // GIVEN
        BoundaryLine boundaryLine = boundaryLineAdder("bl_backwardCompatibility_notifyV")
            .newGeneration()
                .setTargetP(20)
                .setMinP(0)
                .setMaxP(100)
                .setTargetV(220.0) // deprecated method
                .setTargetQ(10.0) // deprecated method
                .setVoltageRegulationOn(false) // deprecated method
            .add()
            .add();
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        double oldTargetV = generation.getTargetV();
        double newTargetV = 123.0;
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generation.setTargetV(newTargetV); // deprecated method
        // THEN
        assertEquals(1, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals("localTargetV", ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(newTargetV, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(boundaryLine.getId(), ((UpdateNetworkEvent) firstEvent).id());
    }

    @Test
    void testNotifyUpdateOnSetVoltageRegulationOn() {
        // GIVEN
        BoundaryLine boundaryLine = boundaryLineAdder("bl_backwardCompatibility_notifyRegulationOn")
            .newGeneration()
                .setTargetP(20)
                .setMinP(0)
                .setMaxP(100)
                .setTargetV(220.0) // deprecated method
                .setTargetQ(10.0) // deprecated method
                .setVoltageRegulationOn(true) // deprecated method
            .add()
            .add();
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generation.setVoltageRegulationOn(false); // deprecated method
        // THEN
        assertEquals(1, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals("VoltageRegulation.isRegulating", ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(false, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(true, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(boundaryLine.getId(), ((UpdateNetworkEvent) firstEvent).id());
    }
}

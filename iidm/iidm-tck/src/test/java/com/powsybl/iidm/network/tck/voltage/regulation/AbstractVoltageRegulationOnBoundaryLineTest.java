/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Voltage regulation of a {@link BoundaryLine.Generation}. The regulation is local only: it cannot have a terminal,
 * a target value, or a remote regulation mode.
 *
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public abstract class AbstractVoltageRegulationOnBoundaryLineTest extends AbstractVoltageRegulationCommon<BoundaryLine.Generation> {

    @Override
    @BeforeEach
    void initNetwork() {
        super.initNetwork();
    }

    @Test
    void shouldCreateAndRemoveLocalVoltageRegulation() {
        // GIVEN
        BoundaryLine.Generation generation = createBoundaryLineGeneration(new DataVoltageRegulationHolderCreator("bl_create",
            null, false, Double.NaN, 400.0, 10.0, false));
        // WHEN
        generation.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .build();
        // THEN
        VoltageRegulation voltageRegulation = generation.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(voltageRegulation.isRegulating());
        assertTrue(generation.isRegulatingWithMode(RegulationMode.VOLTAGE));
        assertFalse(generation.hasRegulatingTerminal());
        // WHEN
        generation.removeVoltageRegulation();
        // THEN
        assertNull(generation.getVoltageRegulation());
        assertFalse(generation.isRegulating());
        new VoltageRegulation.VoltageRegulationAttributes(Double.NaN, Double.NaN, Double.NaN, RegulationMode.REACTIVE_POWER, false);
    }

    @Test
    void shouldRejectLocalReactivePowerVoltageRegulation() {
        // GIVEN
        BoundaryLine.Generation generation = createBoundaryLineGeneration(new DataVoltageRegulationHolderCreator("bl_reactive",
            null, false, Double.NaN, Double.NaN, 10.0, false));
        // WHEN / THEN
        assertThrows(ValidationException.class, () -> generation.newVoltageRegulation()
            .withMode(RegulationMode.REACTIVE_POWER)
            .withRegulating(true)
            .build());
        assertNull(generation.getVoltageRegulation());
    }

    @Test
    void shouldRejectTerminalOnRegulation() {
        // GIVEN
        BoundaryLine.Generation generation = createBoundaryLineGeneration(new DataVoltageRegulationHolderCreator("bl_terminal",
            null, false, Double.NaN, 400.0, 10.0, false));
        // WHEN / THEN
        assertThrows(ValidationException.class, () -> generation.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .withTerminal(remoteTerminal)
            .build());
        assertNull(generation.getVoltageRegulation());
    }

    @Test
    void shouldRejectRemoteOnlyRegulationMode() {
        // GIVEN
        BoundaryLine.Generation generation = createBoundaryLineGeneration(new DataVoltageRegulationHolderCreator("bl_mode",
            null, false, Double.NaN, 400.0, 10.0, false));
        // WHEN / THEN
        assertThrows(ValidationException.class, () -> generation.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)
            .withTargetValue(10.0)
            .withRegulating(true)
            .build());
        assertNull(generation.getVoltageRegulation());
    }

    @Test
    void shouldNotifyOnNewAndRemovedVoltageRegulation() {
        // GIVEN
        String id = "bl_notify";
        BoundaryLine.Generation generation = createBoundaryLineGeneration(new DataVoltageRegulationHolderCreator(id,
            null, false, Double.NaN, 400.0, 10.0, false));
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generation.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withRegulating(true)
            .build();
        // THEN
        assertEquals(1, listener.getEvents().size());
        assertNetworkEvent(listener.getEvents().getFirst(), VoltageRegulation.NotifyUpdateKey.NEW_REGULATION.getKey(),
            generation.getVoltageRegulation(), null, id);
        // WHEN
        VoltageRegulation oldVoltageRegulation = generation.getVoltageRegulation();
        generation.removeVoltageRegulation();
        // THEN
        assertEquals(2, listener.getEvents().size());
        NetworkEvent secondEvent = listener.getEvents().get(1);
        assertNetworkEvent(secondEvent, VoltageRegulation.NotifyUpdateKey.REMOVE_REGULATION.getKey(), null, oldVoltageRegulation, id);
    }

    @Test
    void testNotifyOnLocalTargetV() {
        String id = "bl_local_target_v";
        BoundaryLine.Generation generation = createBoundaryLineGeneration(new DataVoltageRegulationHolderCreator(id,
            null, false, Double.NaN, 400.0, 10.0, false));
        testNotifyOnLocalTargetV(generation, id);
    }

    private BoundaryLine.Generation createBoundaryLineGeneration(DataVoltageRegulationHolderCreator creator) {
        BoundaryLine boundaryLine = voltageLevel.newBoundaryLine()
            .setId(creator.id())
            .setR(1.0)
            .setX(1.0)
            .setG(0.0)
            .setB(0.0)
            .setP0(0.0)
            .setQ0(0.0)
            .setPairingKey("code_" + creator.id())
            .setBus("NGEN")
            .setConnectableBus("NGEN")
            .newGeneration()
                .setTargetP(0.0)
                .setLocalTargetQ(creator.localTargetQ())
                .setLocalTargetV(creator.localTargetV())
                .add()
            .add();
        return boundaryLine.getGeneration();
    }
}

/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.BeforeEach;

import static com.powsybl.iidm.network.regulation.VoltageRegulation.NotifyUpdateKey.REGULATING;
import static com.powsybl.iidm.network.regulation.VoltageRegulation.NotifyUpdateKey.REGULATION_MODE;
import static com.powsybl.iidm.network.regulation.VoltageRegulation.NotifyUpdateKey.TARGET_DEADBAND;
import static com.powsybl.iidm.network.regulation.VoltageRegulation.NotifyUpdateKey.TARGET_VALUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// TODO MSA Complete me with some extra tests like removeTerminal on multiVariants
/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
abstract class AbstractVoltageRegulationCommon<T extends VoltageRegulationHolder<T>> {

    VoltageLevel voltageLevel;
    Terminal remoteTerminal;
    Network network;

    @BeforeEach
    void initNetwork() {
        network = BatteryNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("VLGEN");
        remoteTerminal = network.getBattery("BAT").getTerminal();
    }

    public void changeTerminalTest(T holder) {
        var vlgen = network.getVoltageLevel("VLGEN");
        Bus ngen = vlgen.getBusBreakerView().getBus("NGEN");

        var battery2 = network.getBattery("BAT2");
        Terminal remoteTerminal2 = battery2.getTerminal();

        Battery battery3 = vlgen.newBattery()
            .setId("BAT3")
            .setBus(ngen.getId())
            .setConnectableBus(ngen.getId())
            .setTargetP(9999.99)
            .setLocalTargetQ(9999.99)
            .setMinP(-9999.99)
            .setMaxP(9999.99)
            .add();
        battery3.newMinMaxReactiveLimits()
            .setMinQ(-9999.99)
            .setMaxQ(9999.99)
            .add();
        battery3.getTerminal().setP(-605);
        battery3.getTerminal().setQ(-225);
        Terminal remoteTerminal3 = battery3.getTerminal();

        double localTargetV = 25.0;
        double targetValue = 50.0;
        double newTargetValue = 120.0;
        VoltageRegulation voltageRegulation = holder.newVoltageRegulation()
            .withTerminal(remoteTerminal2)
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(targetValue)
            .withTargetDeadband(15.0)
            .build();

        assertEquals(remoteTerminal2, voltageRegulation.getTerminal());
        assertEquals(targetValue, holder.getRegulatingTargetV());
        voltageRegulation.setTerminal(remoteTerminal3, newTargetValue);
        assertEquals(newTargetValue, holder.getRegulatingTargetV());
        assertEquals(remoteTerminal3, voltageRegulation.getTerminal());
        // Removing battery 2 should not change the regulating terminal
        battery2.remove();
        assertEquals(newTargetValue, holder.getRegulatingTargetV());
        assertEquals(remoteTerminal3, voltageRegulation.getTerminal());
        // Removing battery 3 should change the regulating terminal to the local one (fallback)
        battery3.remove();
        assertEquals(newTargetValue, holder.getRegulatingTargetV());
        assertEquals(holder.getTerminal(), holder.getRegulatingTerminal());
        // Switch to local regulation (this was already the case)
        holder.setLocalTargetV(localTargetV);
        voltageRegulation.setTerminal(null, Double.NaN);
        assertEquals(localTargetV, holder.getRegulatingTargetV());
        assertEquals(holder.getTerminal(), holder.getRegulatingTerminal());
    }

    public void testMergeWithTerminalInMultiVariant(VoltageRegulationHolder<T> holder, String equipmentName, String equipmentType) {
        this.testMergeWithTerminalInMultiVariant(holder, equipmentName, equipmentType, Double.NaN);
    }

    public void testMergeWithTerminalInMultiVariant(VoltageRegulationHolder<T> holder, String equipmentName, String equipmentType, double targetDeadband) {
        String initialVariantId = network.getVariantManager().getWorkingVariantId();
        String other = "Other";
        network.getVariantManager().cloneVariant(initialVariantId, other);
        network.getVariantManager().setWorkingVariant(other);

        // Creating a VoltageRegulation object with a terminal could be considered as changing the terminal.
        // This is not allowed in multi-variant mode.
        VoltageRegulationBuilder builder = holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetValue(120)
            .withTerminal(holder.getTerminal())
            .withTargetDeadband(targetDeadband)
            .withRegulating(true);
        PowsyblException powsyblException = assertThrows(PowsyblException.class, builder::build);
        String expectedMessage = String.format("%s '%s': Cannot set terminal when there are multiple variants", equipmentType, equipmentName);
        assertEquals(expectedMessage, powsyblException.getMessage());

        // But it must be possible to create a voltage regulation in multi-variant mode if the terminal is not changed.
        builder = holder.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .withTargetDeadband(targetDeadband)
            .withRegulating(true);
        assertDoesNotThrow(builder::build);
    }

    public void testNotifyOnLocalTargetV(VoltageRegulationHolder<T> holder, String id) {
        double newTargetV = 123.0;
        double oldTargetV = holder.getLocalTargetV();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);

        holder.setLocalTargetV(newTargetV);
        assertEquals(1, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertNetworkEvent(firstEvent, "localTargetV", newTargetV, oldTargetV, id);
    }

    public void testNotifyOnRemoveVoltageRegulation(VoltageRegulationHolder<T> holder, String id) {
        VoltageRegulation oldVoltageRegulation = holder.getVoltageRegulation();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        holder.removeVoltageRegulation();
        assertEquals(1, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertNetworkEvent(firstEvent, VoltageRegulation.NotifyUpdateKey.REMOVE_REGULATION.getKey(), null, oldVoltageRegulation, id);
    }

    public void testNotifyOnNewVoltageRegulationAfterRemoveVoltageRegulation(VoltageRegulationHolder<T> holder, String id) {
        this.testNotifyOnNewVoltageRegulationAfterRemoveVoltageRegulation(holder, id, Double.NaN);
    }

    public void testNotifyOnNewVoltageRegulationAfterRemoveVoltageRegulation(VoltageRegulationHolder<T> holder, String id, double targetDeadband) {
        holder.removeVoltageRegulation();
        double targetValue = 120.0;
        RegulationMode mode = RegulationMode.VOLTAGE;
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        holder.newVoltageRegulation()
            .withMode(mode)
            .withTargetValue(targetValue)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .withTargetDeadband(targetDeadband)
            .build();
        assertEquals(1, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertNetworkEvent(firstEvent, VoltageRegulation.NotifyUpdateKey.NEW_REGULATION.getKey(), holder.getVoltageRegulation(), null, id);
    }

    public void testNotifyOnNewVoltageRegulationWithPreviousVoltageRegulation(VoltageRegulationHolder<T> holder, String id, double newTargetDeadband) {
        double targetValue = 120.0;
        boolean updateTargetDeadband = !Double.isNaN(newTargetDeadband);
        double targetDeadband = updateTargetDeadband ? 15.0 : Double.NaN;
        double newTargetValue = 220.0;
        RegulationMode mode = RegulationMode.VOLTAGE;
        RegulationMode newMode = RegulationMode.REACTIVE_POWER;
        holder.removeVoltageRegulation();
        holder.newVoltageRegulation()
            .withMode(mode)
            .withTargetValue(targetValue)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .withTargetDeadband(targetDeadband)
            .build();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        holder.newVoltageRegulation()
            .withMode(newMode)
            .withTargetValue(newTargetValue)
            .withTerminal(remoteTerminal) // Same terminal -> no notification
            .withRegulating(false)
            .withTargetDeadband(newTargetDeadband)
            .build();
        int listenerSize = updateTargetDeadband ? 4 : 3;
        assertEquals(listenerSize, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertNetworkEvent(firstEvent, REGULATION_MODE.getKey(), newMode, mode, id);
        NetworkEvent secondEvent = listener.getEvents().get(1);
        assertNetworkEvent(secondEvent, TARGET_VALUE.getKey(), newTargetValue, targetValue, id);
        NetworkEvent thirdEvent = listener.getEvents().get(2);
        assertNetworkEvent(thirdEvent, REGULATING.getKey(), false, true, id);
        if (updateTargetDeadband) {
            NetworkEvent fourthEvent = listener.getEvents().get(3);
            assertNetworkEvent(fourthEvent, TARGET_DEADBAND.getKey(), newTargetDeadband, targetDeadband, id);
        }
    }

    public void testNotifyOnNewVoltageRegulationWithPreviousVoltageRegulationSameMode(VoltageRegulationHolder<T> holder, String id, double targetDeadband) {
        double targetValue = 120.0;
        double newTargetValue = 220.0;
        RegulationMode mode = RegulationMode.VOLTAGE;
        holder.removeVoltageRegulation();
        holder.newVoltageRegulation()
            .withMode(mode)
            .withTargetValue(targetValue)
            .withTerminal(remoteTerminal)
            .withRegulating(true)
            .withTargetDeadband(targetDeadband)
            .build();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        holder.newVoltageRegulation()
            .withMode(mode)
            .withTargetValue(newTargetValue)
            .withTerminal(remoteTerminal) // Same terminal -> no notification
            .withRegulating(false)
            .withTargetDeadband(targetDeadband) // Same targetDeadband -> no notification
            .build();
        assertEquals(2, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertNetworkEvent(firstEvent, TARGET_VALUE.getKey(), newTargetValue, targetValue, id);
        NetworkEvent secondEvent = listener.getEvents().get(1);
        assertNetworkEvent(secondEvent, REGULATING.getKey(), false, true, id);
    }

    public void testNotifyCommon(VoltageRegulationHolder<T> holder, String id) {
        this.testNotifyCommon(holder, id, Double.NaN);
    }

    public void testNotifyCommon(VoltageRegulationHolder<T> holder, String id, double targetDeadband) {
        testNotifyOnLocalTargetV(holder, id);
        testNotifyOnRemoveVoltageRegulation(holder, id);
        testNotifyOnNewVoltageRegulationAfterRemoveVoltageRegulation(holder, id, targetDeadband);
        testNotifyOnNewVoltageRegulationWithPreviousVoltageRegulation(holder, id, targetDeadband);
    }

    protected static void assertNetworkEvent(NetworkEvent firstEvent, String expectedAttribute, Object expectedNewValue, Object expectedOldValue, String expectedId) {
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals(expectedAttribute, ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(expectedNewValue, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(expectedOldValue, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(expectedId, ((UpdateNetworkEvent) firstEvent).id());
    }

}

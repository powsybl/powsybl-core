/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.GeneratorAdder;
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
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnGeneratorTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    void testGeneratorRemoteVoltageRegulationOff() {
        // GIVEN
        double targetV = 220.0;
        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setVoltageRegulatorOn(false) // deprecated method
            .setRegulatingTerminal(remoteTerminal) // deprecated method
            .setTargetV(targetV) // deprecated method
            .setTargetQ(0.0); // deprecated method
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator.getVoltageRegulation());
        assertFalse(generator.isRegulating());
        assertTrue(generator.isWithMode(RegulationMode.VOLTAGE));
        assertEquals(remoteTerminal, generator.getRegulatingTerminal());
        assertEquals(targetV, generator.getVoltageRegulation().getTargetValue());
    }

    @Test
    public void testGeneratorRemoteVoltageRegulation() {
        // GIVEN
        int remoteTargetV = 220;
        int localTargetV = 110;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setVoltageRegulatorOn(true) // deprecated method
            .setRegulatingTerminal(remoteTerminal) // deprecated method
            .setTargetV(remoteTargetV, localTargetV) // deprecated method
            .setTargetQ(targetQ); // deprecated method
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            localTargetV,
            targetQ,
            remoteTargetV,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(remoteTerminal, generator.getRegulatingTerminal());
        assertEquals(remoteTerminal, generator.getVoltageRegulation().getTerminal());

        assertEquals(localTargetV, generator.getLocalTargetV());
        assertEquals(localTargetV, generator.getEquivalentLocalTargetV());
        assertEquals(remoteTargetV, generator.getTargetV());
        assertEquals(remoteTargetV, generator.getRegulatingTargetV());
        assertEquals(remoteTargetV, generator.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertTrue(generator.isRegulating());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    public void testGeneratorLocalVoltageRegulation() {
        // GIVEN
        int targetV = 220;
        int equivalentLocalTargetV = 110;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setVoltageRegulatorOn(true) // deprecated method
            .setTargetV(targetV, equivalentLocalTargetV) // deprecated method
            .setTargetQ(targetQ); // deprecated method
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            equivalentLocalTargetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());
        assertNull(generator.getVoltageRegulation().getTerminal());

        assertEquals(equivalentLocalTargetV, generator.getLocalTargetV());
        assertEquals(equivalentLocalTargetV, generator.getEquivalentLocalTargetV());
        assertEquals(equivalentLocalTargetV, generator.getTargetV());
        assertEquals(equivalentLocalTargetV, generator.getRegulatingTargetV());
        assertTrue(Double.isNaN(generator.getVoltageRegulation().getTargetValue()));

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertTrue(generator.isRegulating());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    public void testGeneratorLocalVoltageRegulationOff() {
        // GIVEN
        int targetV = 220;
        int equivalentLocalTargetV = 110;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setVoltageRegulatorOn(false) // deprecated method
            .setTargetV(targetV, equivalentLocalTargetV) // deprecated method
            .setTargetQ(targetQ); // deprecated method
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            equivalentLocalTargetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            null,
            false,
            null,
            false);
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());

        assertEquals(equivalentLocalTargetV, generator.getLocalTargetV());
        assertEquals(equivalentLocalTargetV, generator.getLocalTargetV());
        assertEquals(equivalentLocalTargetV, generator.getTargetV());
        assertEquals(equivalentLocalTargetV, generator.getRegulatingTargetV());

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertFalse(generator.isRegulating());
        assertFalse(generator.isVoltageRegulatorOn());
    }

    @Test
    public void testGeneratorLocalVoltageRegulationOnWithPartialDeprecatedMethods() {
        // GIVEN
        int targetV = 220;
        int equivalentLocalTargetV = 110;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setTargetV(targetV, equivalentLocalTargetV) // deprecated method
            .setTargetQ(targetQ) // deprecated method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .add();
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            equivalentLocalTargetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());

        assertEquals(equivalentLocalTargetV, generator.getLocalTargetV());
        assertEquals(equivalentLocalTargetV, generator.getLocalTargetV());
        assertEquals(equivalentLocalTargetV, generator.getTargetV());
        assertEquals(equivalentLocalTargetV, generator.getRegulatingTargetV());

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertTrue(generator.isRegulating());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    public void testGeneratorLocalVoltageRegulationOnWithPartialDeprecatedMethodsWithoutEquivalentLocalTargetV() {
        // GIVEN
        int targetV = 220;
        int targetQ = 10;

        GeneratorAdder adder = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setTargetV(targetV) // deprecated method
            .setTargetQ(targetQ) // deprecated method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .add();
        // WHEN
        Generator generator = adder.add();
        // THEN
        assertNotNull(generator);
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
        checkVoltageRegulationAttributes(expectedAttributes, generator);

        assertEquals(generator.getTerminal(), generator.getRegulatingTerminal());

        assertEquals(targetV, generator.getLocalTargetV());
        assertEquals(targetV, generator.getEquivalentLocalTargetV());
        assertEquals(targetV, generator.getTargetV());
        assertEquals(targetV, generator.getRegulatingTargetV());

        assertEquals(targetQ, generator.getLocalTargetQ());
        assertEquals(targetQ, generator.getTargetQ());
        assertEquals(targetQ, generator.getRegulatingTargetQ());

        assertTrue(generator.isRegulating());
        assertTrue(generator.isVoltageRegulatorOn());
    }

    @Test
    void testNotifyUpdateOnSetTargetQ() {
        // GIVEN
        Generator generator = voltageLevel.getGeneratorStream().toList().getFirst();
        String id = generator.getId();
        double newTargetQ = 123.0;
        double oldTargetQ = generator.getTargetQ();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generator.setTargetQ(newTargetQ);
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

    @Test
    void testNotifyUpdateOnSetTargetV() {
        // GIVEN
        Generator generator = voltageLevel.getGeneratorStream().toList().getFirst();
        String id = generator.getId();
        double newTargetV = 123.0;
        double oldTargetV = generator.getTargetV();
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generator.setTargetV(newTargetV);
        // THEN
        assertEquals(3, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals("localTargetV", ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(newTargetV, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) firstEvent).id());
        NetworkEvent secondEvent = listener.getEvents().get(1);
        assertEquals(NetworkEvent.Type.UPDATE, secondEvent.getType());
        assertEquals("targetV", ((UpdateNetworkEvent) secondEvent).attribute());
        assertEquals(newTargetV, ((UpdateNetworkEvent) secondEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) secondEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) secondEvent).id());
        NetworkEvent thirdEvent = listener.getEvents().get(2);
        assertEquals(NetworkEvent.Type.UPDATE, thirdEvent.getType());
        assertEquals("equivalentLocalTargetV", ((UpdateNetworkEvent) thirdEvent).attribute());
        assertEquals(newTargetV, ((UpdateNetworkEvent) thirdEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) thirdEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) thirdEvent).id());
    }

    @Test
    void testNotifyUpdateOnSetTargetVAndEquivalentTargetV() {
        // GIVEN
        Generator generator = voltageLevel.getGeneratorStream().toList().getFirst();
        String id = generator.getId();
        double newTargetV = 123.0;
        double newEquivalentTargetV = 12.0;
        double oldTargetV = generator.getTargetV();
        generator.setRegulatingTerminal(remoteTerminal);
        NetworkEventRecorder listener = new NetworkEventRecorder();
        network.addListener(listener);
        // WHEN
        generator.setTargetV(newTargetV, newEquivalentTargetV);
        // THEN
        assertEquals(4, listener.getEvents().size());
        NetworkEvent firstEvent = listener.getEvents().getFirst();
        assertEquals(NetworkEvent.Type.UPDATE, firstEvent.getType());
        assertEquals("localTargetV", ((UpdateNetworkEvent) firstEvent).attribute());
        assertEquals(newEquivalentTargetV, ((UpdateNetworkEvent) firstEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) firstEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) firstEvent).id());
        NetworkEvent secondEvent = listener.getEvents().get(1);
        assertEquals(NetworkEvent.Type.UPDATE, secondEvent.getType());
        assertEquals("VoltageRegulation.TargetValue", ((UpdateNetworkEvent) secondEvent).attribute());
        assertEquals(newTargetV, ((UpdateNetworkEvent) secondEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) secondEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) secondEvent).id());
        NetworkEvent thirdEvent = listener.getEvents().get(2);
        assertEquals(NetworkEvent.Type.UPDATE, thirdEvent.getType());
        assertEquals("targetV", ((UpdateNetworkEvent) thirdEvent).attribute());
        assertEquals(newTargetV, ((UpdateNetworkEvent) thirdEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) thirdEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) thirdEvent).id());
        NetworkEvent fourthEvent = listener.getEvents().get(3);
        assertEquals(NetworkEvent.Type.UPDATE, fourthEvent.getType());
        assertEquals("equivalentLocalTargetV", ((UpdateNetworkEvent) fourthEvent).attribute());
        assertEquals(newEquivalentTargetV, ((UpdateNetworkEvent) fourthEvent).newValue());
        assertEquals(oldTargetV, ((UpdateNetworkEvent) fourthEvent).oldValue());
        assertEquals(id, ((UpdateNetworkEvent) fourthEvent).id());
    }

    @Test
    void testSetRegulatingTerminal() {
        // GIVEN
        int expectedTargetQ = 10;
        Generator generatorWithRemoteReactiveRegulationOff = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility_remoteReactiveRegulationOff")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .setLocalTargetQ(expectedTargetQ)
            .newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER)
                .withRegulating(false)
                .add()
            .add();
        Generator generatorWithRemoteReactiveRegulationOn = voltageLevel.newGenerator()
            .setId("generator_backwardCompatibility_remoteReactiveRegulationOn")
            .setConnectableBus(LOCAL_BUS)
            .setMinP(0)
            .setTargetP(20)
            .setMaxP(100)
            .newVoltageRegulation()
                .withMode(RegulationMode.REACTIVE_POWER)
                .withTerminal(remoteTerminal)
                .withRegulating(true)
                .withTargetValue(-expectedTargetQ)
                .add()
            .add();
        // WHEN
        generatorWithRemoteReactiveRegulationOff.setRegulatingTerminal(remoteTerminal);
        generatorWithRemoteReactiveRegulationOn.setRegulatingTerminal(remoteTerminal);
        // THEN
        assertEquals(-expectedTargetQ, generatorWithRemoteReactiveRegulationOff.getVoltageRegulation().getTargetValue());
        assertEquals(expectedTargetQ, generatorWithRemoteReactiveRegulationOff.getLocalTargetQ());
        assertEquals(expectedTargetQ, generatorWithRemoteReactiveRegulationOff.getRegulatingTargetQ());

        assertEquals(-expectedTargetQ, generatorWithRemoteReactiveRegulationOn.getVoltageRegulation().getTargetValue());
        assertTrue(Double.isNaN(generatorWithRemoteReactiveRegulationOn.getLocalTargetQ()));
        assertEquals(expectedTargetQ, generatorWithRemoteReactiveRegulationOn.getRegulatingTargetQ());
    }

}

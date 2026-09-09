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

}

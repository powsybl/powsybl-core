/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensatorAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
@SuppressWarnings("removal") // Removal warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnStaticVarCompensatorTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    public void testStaticVarCompensatorFullDeprecatedMethod() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
            .setId("staticVarCompensator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setBmin(0)
            .setBmax(1)
            .setRegulating(true) // deprecated method
            .setReactivePowerSetpoint(targetQ) // deprecated method
            .setRegulationMode(RegulationMode.VOLTAGE) // deprecated method
            .setVoltageSetpoint(targetV) // deprecated method
            .setRegulatingTerminal(remoteTerminal); // deprecated method
        // WHEN
        StaticVarCompensator staticVarCompensator = adder.add();
        // THEN
        assertNotNull(staticVarCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            Double.NaN,
            targetQ,
            targetV,
            Double.NaN,
            Double.NaN,
            RegulationMode.VOLTAGE,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, staticVarCompensator);

        assertEquals(remoteTerminal, staticVarCompensator.getRegulatingTerminal());
        assertEquals(remoteTerminal, staticVarCompensator.getVoltageRegulation().getTerminal());

        assertTrue(Double.isNaN(staticVarCompensator.getLocalTargetV()));
        assertEquals(targetV, staticVarCompensator.getVoltageSetpoint());
        assertEquals(targetV, staticVarCompensator.getRegulatingTargetV());
        assertEquals(targetV, staticVarCompensator.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, staticVarCompensator.getLocalTargetQ());
        assertEquals(targetQ, staticVarCompensator.getReactivePowerSetpoint());
        assertEquals(targetQ, staticVarCompensator.getRegulatingTargetQ());

        assertTrue(staticVarCompensator.isRegulating());
    }

    @Test
    public void testStaticVarCompensatorFullDeprecatedMethodAndNewMethods() {
        // GIVEN
        boolean oldRegulating = false;
        int oldTargetV = 120;
        int oldTargetQ = 10;
        boolean newRegulating = true;
        int targetV = 220;
        int targetQ = 20;
        int targetValue = 20;
        int slope = 2;

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
            .setId("staticVarCompensator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setBmin(0)
            .setBmax(1)
            .setRegulating(oldRegulating) // deprecated method
            .setReactivePowerSetpoint(oldTargetQ) // deprecated method
            .setRegulationMode(RegulationMode.VOLTAGE) // deprecated method
            .setVoltageSetpoint(oldTargetV) // deprecated method
            .setRegulatingTerminal(remoteTerminal) // deprecated method
            .setLocalTargetV(targetV) // new method
            .setLocalTargetQ(targetQ) // new method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)
                .withRegulating(newRegulating)
                .withTerminal(remoteTerminal)
                .withTargetValue(targetValue)
                .withSlope(slope)
                .add();
        // WHEN
        StaticVarCompensator staticVarCompensator = adder.add();
        // THEN
        assertNotNull(staticVarCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            targetQ,
            targetValue,
            Double.NaN,
            slope,
            RegulationMode.VOLTAGE_PER_REACTIVE_POWER,
            newRegulating,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, staticVarCompensator);

        assertEquals(remoteTerminal, staticVarCompensator.getRegulatingTerminal());
        assertEquals(remoteTerminal, staticVarCompensator.getVoltageRegulation().getTerminal());

        assertEquals(targetV, staticVarCompensator.getLocalTargetV());
        assertEquals(targetValue, staticVarCompensator.getVoltageSetpoint());
        assertEquals(targetValue, staticVarCompensator.getRegulatingTargetV());
        assertEquals(targetValue, staticVarCompensator.getVoltageRegulation().getTargetValue());

        assertEquals(targetQ, staticVarCompensator.getLocalTargetQ());
        assertEquals(targetQ, staticVarCompensator.getReactivePowerSetpoint());
        assertEquals(targetQ, staticVarCompensator.getRegulatingTargetQ());

        assertEquals(newRegulating, staticVarCompensator.isRegulating());
    }

    @Test
    public void testStaticVarCompensatorWithDeprecatedAndNewMethods() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;
        double slope = 12.0;

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
            .setId("staticVarCompensator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setBmin(0)
            .setBmax(1)
            .setReactivePowerSetpoint(targetQ) // deprecated method
            .setVoltageSetpoint(targetV) // deprecated method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)
                .withRegulating(true)
                .withSlope(slope)
                .add();
        // WHEN
        StaticVarCompensator staticVarCompensator = adder.add();
        // THEN
        assertNotNull(staticVarCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            targetQ,
            Double.NaN,
            Double.NaN,
            slope,
            RegulationMode.VOLTAGE_PER_REACTIVE_POWER,
            true,
            null,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, staticVarCompensator);

        assertEquals(staticVarCompensator.getTerminal(), staticVarCompensator.getRegulatingTerminal());
        assertNull(staticVarCompensator.getVoltageRegulation().getTerminal());

        assertEquals(targetV, staticVarCompensator.getLocalTargetV());
        assertEquals(targetV, staticVarCompensator.getVoltageSetpoint());
        assertEquals(targetV, staticVarCompensator.getRegulatingTargetV());
        assertTrue(Double.isNaN(staticVarCompensator.getVoltageRegulation().getTargetValue()));

        assertEquals(targetQ, staticVarCompensator.getLocalTargetQ());
        assertEquals(targetQ, staticVarCompensator.getReactivePowerSetpoint());
        assertEquals(targetQ, staticVarCompensator.getRegulatingTargetQ());

        assertTrue(staticVarCompensator.isRegulating());
    }

    @Test
    public void testStaticVarCompensatorWithDeprecatedAndNewMethodsAndRemote() {
        // GIVEN
        int targetV = 120;
        int targetValue = 220;
        int targetQ = 10;
        int localTargetQ = 20;
        double slope = 12.0;

        StaticVarCompensatorAdder adder = voltageLevel.newStaticVarCompensator()
            .setId("staticVarCompensator_backwardCompatibility")
            .setConnectableBus(LOCAL_BUS)
            .setBmin(0)
            .setBmax(1)
            .setRegulating(false) // deprecated method
            .setReactivePowerSetpoint(targetQ) // deprecated method
            .setVoltageSetpoint(targetV) // deprecated method
            .setLocalTargetQ(localTargetQ)
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)
                .withRegulating(true)
                .withSlope(slope)
                .withTerminal(remoteTerminal)
                .withTargetValue(targetValue)
                .add();
        // WHEN
        StaticVarCompensator staticVarCompensator = adder.add();
        // THEN
        assertNotNull(staticVarCompensator);
        VoltageRegulationAttributesToCheck expectedAttributes = new VoltageRegulationAttributesToCheck(
            targetV,
            localTargetQ,
            targetValue,
            Double.NaN,
            slope,
            RegulationMode.VOLTAGE_PER_REACTIVE_POWER,
            true,
            remoteTerminal,
            true);
        checkVoltageRegulationAttributes(expectedAttributes, staticVarCompensator);

        assertEquals(remoteTerminal, staticVarCompensator.getRegulatingTerminal());
        assertEquals(remoteTerminal, staticVarCompensator.getVoltageRegulation().getTerminal());

        assertEquals(targetV, staticVarCompensator.getLocalTargetV());

        assertEquals(targetValue, staticVarCompensator.getVoltageSetpoint());
        assertEquals(targetValue, staticVarCompensator.getRegulatingTargetV());
        assertEquals(targetValue, staticVarCompensator.getVoltageRegulation().getTargetValue());

        assertEquals(localTargetQ, staticVarCompensator.getLocalTargetQ());
        assertEquals(localTargetQ, staticVarCompensator.getReactivePowerSetpoint());
        assertEquals(localTargetQ, staticVarCompensator.getRegulatingTargetQ());

        assertTrue(staticVarCompensator.isRegulating());
    }

}

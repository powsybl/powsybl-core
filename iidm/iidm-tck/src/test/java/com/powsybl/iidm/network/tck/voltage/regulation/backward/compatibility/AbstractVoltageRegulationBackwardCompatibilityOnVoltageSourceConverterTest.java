/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.voltage.regulation.backward.compatibility;

import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
@SuppressWarnings("removal") // Removal warnings are ignored. In fact, these tests are here to verify backward compatibility
public abstract class AbstractVoltageRegulationBackwardCompatibilityOnVoltageSourceConverterTest extends AbstractVoltageRegulationBackwardCompatibilityCommon {

    @Test
    public void testVoltageSourceConverter() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        DcNode dcNode1 = network.newDcNode()
            .setId("dcNode1")
            .setNominalV(500.)
            .add();
        DcNode dcNode2 = network.newDcNode()
            .setId("dcNode2")
            .setNominalV(500.)
            .add();
        VoltageSourceConverterAdder adder = voltageLevel.newVoltageSourceConverter()
            .setId("vscConverterStation_backwardCompatibility")
            .setControlMode(AcDcConverter.ControlMode.V_DC)
            .setTargetVdc(110)
            .setConnectableBus1(LOCAL_BUS)
            .setConnectableBus2(LOCAL_BUS)
            .setDcNode1(dcNode1.getId())
            .setDcNode2(dcNode2.getId())
            .setReactivePowerSetpoint(targetQ)
            .setVoltageSetpoint(targetV)
            .setVoltageRegulatorOn(true);
        // WHEN
        VoltageSourceConverter voltageSourceConverter = adder.add();
        // THEN
        assertNotNull(voltageSourceConverter);
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
        checkVoltageRegulationAttributes(expectedAttributes, voltageSourceConverter);

        assertEquals(targetV, voltageSourceConverter.getLocalTargetV());
        assertEquals(targetV, voltageSourceConverter.getVoltageSetpoint());
        assertEquals(targetV, voltageSourceConverter.getRegulatingTargetV());
        assertTrue(Double.isNaN(voltageSourceConverter.getVoltageRegulation().getTargetValue()));

        assertEquals(targetQ, voltageSourceConverter.getLocalTargetQ());
        assertEquals(targetQ, voltageSourceConverter.getReactivePowerSetpoint());
        assertEquals(targetQ, voltageSourceConverter.getRegulatingTargetQ());

        assertTrue(voltageSourceConverter.isVoltageRegulatorOn());
        assertTrue(voltageSourceConverter.isRegulating());
    }

    @Test
    public void testVoltageSourceConverterWithNewAndDeprecatedMethods() {
        // GIVEN
        int targetV = 120;
        int targetQ = 10;

        DcNode dcNode1 = network.newDcNode()
            .setId("dcNode1")
            .setNominalV(500.)
            .add();
        DcNode dcNode2 = network.newDcNode()
            .setId("dcNode2")
            .setNominalV(500.)
            .add();
        VoltageSourceConverterAdder adder = voltageLevel.newVoltageSourceConverter()
            .setId("vscConverterStation_backwardCompatibility")
            .setControlMode(AcDcConverter.ControlMode.V_DC)
            .setTargetVdc(110)
            .setConnectableBus1(LOCAL_BUS)
            .setConnectableBus2(LOCAL_BUS)
            .setDcNode1(dcNode1.getId())
            .setDcNode2(dcNode2.getId())
            .setReactivePowerSetpoint(targetQ) // Deprecated method
            .setVoltageSetpoint(targetV) // Deprecated method
            .newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .add();
        // WHEN
        VoltageSourceConverter voltageSourceConverter = adder.add();
        // THEN
        assertNotNull(voltageSourceConverter);
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
        checkVoltageRegulationAttributes(expectedAttributes, voltageSourceConverter);

        assertEquals(targetV, voltageSourceConverter.getLocalTargetV());
        assertEquals(targetV, voltageSourceConverter.getVoltageSetpoint());
        assertEquals(targetV, voltageSourceConverter.getRegulatingTargetV());
        assertTrue(Double.isNaN(voltageSourceConverter.getVoltageRegulation().getTargetValue()));

        assertEquals(targetQ, voltageSourceConverter.getLocalTargetQ());
        assertEquals(targetQ, voltageSourceConverter.getReactivePowerSetpoint());
        assertEquals(targetQ, voltageSourceConverter.getRegulatingTargetQ());

        assertTrue(voltageSourceConverter.isVoltageRegulatorOn());
        assertTrue(voltageSourceConverter.isRegulating());
    }

}

/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class StaticVarCompensatorsValidationTest extends AbstractValidationTest {

    private double p = 0;
    private double q = 3.72344;
    private double v = 380;
    private double nominalV = 380;
    private double reactivePowerSetpoint = 3.72344;
    private final double voltageSetpoint = 380;
    private RegulationMode regulationMode = RegulationMode.REACTIVE_POWER;
    private boolean regulating = true;
    private final double bMin = -.01;
    private final double bMax = .1;
    private final boolean connected = true;
    private boolean mainComponent = true;

    private BusView svcBusView;
    private Terminal svcTerminal;
    private StaticVarCompensator svc;

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();

        Bus svcBus = mock(Bus.class);
        when(svcBus.getV()).thenReturn(v);
        when(svcBus.isInMainConnectedComponent()).thenReturn(mainComponent);

        svcBusView = mock(BusView.class);
        when(svcBusView.getBus()).thenReturn(svcBus);
        when(svcBusView.getConnectableBus()).thenReturn(svcBus);

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getNominalV()).thenReturn(nominalV);

        svcTerminal = mock(Terminal.class);
        when(svcTerminal.getP()).thenReturn(p);
        when(svcTerminal.getQ()).thenReturn(q);
        when(svcTerminal.getBusView()).thenReturn(svcBusView);
        when(svcTerminal.getVoltageLevel()).thenReturn(voltageLevel);

        svc = mock(StaticVarCompensator.class);
        when(svc.getId()).thenReturn("svc");
        when(svc.getTerminal()).thenReturn(svcTerminal);
        when(svc.getReactivePowerSetpoint()).thenReturn(reactivePowerSetpoint);
        when(svc.getVoltageSetpoint()).thenReturn(voltageSetpoint);
        when(svc.getRegulationMode()).thenReturn(regulationMode);
        when(svc.isRegulating()).thenReturn(regulating);
        when(svc.getBmin()).thenReturn(bMin);
        when(svc.getBmax()).thenReturn(bMax);
    }

    @Test
    void checkSvcsValues() {
        // active power should be equal to 0
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        p = -39.8;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        p = -0.01;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        // if regulating = false then reactive power should be equal to 0
        regulating = false;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        regulating = true;
        //  if regulationMode = REACTIVE_POWER if the setpoint is in [Qmin=bMin*V*V, Qmax=bMax*V*V] then then reactive power should be equal to setpoint
        regulationMode = RegulationMode.REACTIVE_POWER;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        q = 3.7;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.INSTANCE));

        // check with NaN values
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, Float.NaN, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, Float.NaN, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(false);

        // if regulationMode = VOLTAGE then either V at the connected bus is equal to voltageSetpoint and q is bounded within [-Qmax=bMax*V*V, -Qmin=bMin*V*V]
        regulating = true;
        regulationMode = RegulationMode.VOLTAGE;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 400;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 380;
        q = 1445;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        // check with NaN values
        q = 3.7;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, Float.NaN, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, Float.NaN, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, Float.NaN, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, Float.NaN, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(false);

        // if regulationMode = VOLTAGE then either q is equal to -bMin * V * V and V is lower than voltageSetpoint
        q = 1296;
        v = 360;
        nominalV = 360;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 340;
        nominalV = 340;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        // if regulationMode = VOLTAGE then either q is equal to -bMax * V * V and V is higher than voltageSetpoint
        q = -16000;
        v = 400;
        nominalV = 400;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 420;
        nominalV = 420;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        // check main component
        mainComponent = false;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        mainComponent = true;

        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380;
        nominalV = 380;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        reactivePowerSetpoint = 0;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkSvcs() {
        // active power should be equal to 0
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        when(svcTerminal.getP()).thenReturn(-39.8);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));

        // the unit is disconnected
        when(svcBusView.getBus()).thenReturn(null);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkNetworkSvcs() throws IOException {
        Network network = mock(Network.class);
        when(network.getId()).thenReturn("network");
        when(network.getStaticVarCompensatorStream()).thenAnswer(dummy -> Stream.of(svc));

        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(network, looseConfig, data));

        assertTrue(ValidationType.SVCS.check(network, looseConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.INSTANCE, ValidationType.SVCS);
        assertTrue(ValidationType.SVCS.check(network, looseConfig, validationWriter));
    }

    // Rule1: active power should be equal to 0
    @Test
    void checkSVCActivePowerShouldBeZeroWithinThreshold() {
        // Given (p = 0)
        when(svcTerminal.getP()).thenReturn(0.0);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // Given (p = 0.01 ~ threshold)
        when(svcTerminal.getP()).thenReturn(0.01);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        //Given (p > 0.01)
        when(svcTerminal.getP()).thenReturn(0.02);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule2: **reactivePowerSetpoint** must be 0 if p or q is missing (NaN)
    @Test
    void checkSVCReactivePowerSetpointWhenPOrQMissing() {
        // non-zero setpoint with missing p/q => KO
        when(svcTerminal.getP()).thenReturn(Double.NaN);
        when(svcTerminal.getQ()).thenReturn(Double.NaN);
        when(svc.getReactivePowerSetpoint()).thenReturn(5.0);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // zero setpoint with missing p/q => OK
        when(svc.getReactivePowerSetpoint()).thenReturn(0.0);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule 3: regulationMode = REACTIVE_POWER
    // Test condition: required inputs missing => OK only if okMissingValues=true
    @Test
    void checkSVCReactivePowerModeMissingInputs() {
        // Given regulation enabled, and regulationMode is REACTIVE_POWER
        when(svc.getRegulationMode()).thenReturn(RegulationMode.REACTIVE_POWER);
        when(svc.isRegulating()).thenReturn(true);
        // Given Rule1, Rule2 (OK)
        when(svcTerminal.getP()).thenReturn(0.0);
        when(svc.getReactivePowerSetpoint()).thenReturn(Double.NaN);
        // When
        strictConfig.setOkMissingValues(false);
        // Then
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // When
        strictConfig.setOkMissingValues(true);
        // Then
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule 3: regulationMode = REACTIVE_POWER
    // Test condition: Q must match reactivePowerSetpoint within threshold
    @Test
    void checkSVCReactivePowerModeQMustMatchSetpoint() {
        // Given regulation enabled, and regulationMode is REACTIVE_POWER
        when(svc.getRegulationMode()).thenReturn(RegulationMode.REACTIVE_POWER);
        when(svc.isRegulating()).thenReturn(true);

        when(svcTerminal.getP()).thenReturn(0.0);
        when(svc.getReactivePowerSetpoint()).thenReturn(3.0);
        // When
        when(svcTerminal.getQ()).thenReturn(3.01);
        // Then
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // When
        when(svcTerminal.getQ()).thenReturn(3.5); // diff > (threshold = 0.01)
        // Then
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule 4: regulationMode = VOLTAGE
    // Test condition: required inputs missing => OK only if okMissingValues=true
    @Test
    void checkSVCVoltageModeMissingInputs() {
        // Given regulation enabled, and regulationMode is VOLTAGE
        when(svc.getRegulationMode()).thenReturn(RegulationMode.VOLTAGE);
        when(svc.isRegulating()).thenReturn(true);
        // Given Rule1, Rule 2 (OK)
        when(svcTerminal.getQ()).thenReturn(0.0);
        when(svc.getVoltageSetpoint()).thenReturn(Double.NaN);
        // When
        strictConfig.setOkMissingValues(false);
        // Then
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // When
        strictConfig.setOkMissingValues(true);
        // Then
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule 4: regulationMode = VOLTAGE
    // Test condition: V controlled < V setPoint, then Q must match maxQ.
    @Test
    void checkSVCVoltageModeVLowerThanSetpoint() {
        // Given regulation enabled, and regulationMode is VOLTAGE
        when(svc.getRegulationMode()).thenReturn(RegulationMode.VOLTAGE);
        when(svc.isRegulating()).thenReturn(true);
        when(svcTerminal.getP()).thenReturn(0.0);
        // Given V controlled < V setpoint
        Terminal regulatingTerminal = regulatingTerminalWithVoltage(360.0); // V controlled = 360.0
        when(svc.getRegulatingTerminal()).thenReturn(regulatingTerminal);
        when(svc.getVoltageSetpoint()).thenReturn(380.0); // V setpoint 380.0
        double expectedQmax = -bMin * v * v;
        assertEquals(1444.0, expectedQmax);
        // Given q matching Qmax
        when(svcTerminal.getQ()).thenReturn(expectedQmax);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // Given q not matching Qmax
        when(svcTerminal.getQ()).thenReturn(1300.0);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }


    // Rule 4: regulationMode = VOLTAGE
    // Test condition: V regulation > V setPoint, then Q must match minQ.
    @Test
    void checkSVCVoltageModeVHigherThanSetpoint() {
        // Given regulation enabled, and regulationMode is VOLTAGE
        when(svc.getRegulationMode()).thenReturn(RegulationMode.VOLTAGE);
        when(svc.isRegulating()).thenReturn(true);
        when(svcTerminal.getP()).thenReturn(0.0);
        // Given V controlled > V setpoint
        Terminal regulatingTerminal = regulatingTerminalWithVoltage(400.0); // V controlled = 400.0
        when(svc.getRegulatingTerminal()).thenReturn(regulatingTerminal);
        when(svc.getVoltageSetpoint()).thenReturn(380.0); // V setpoint 380.0
        double expectedQmin = -bMax * v * v;
        assertEquals(-14440.0, expectedQmin);
        // Given q matching Qmin
        when(svcTerminal.getQ()).thenReturn(expectedQmin);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
         // Given q not matching Qmin
        when(svcTerminal.getQ()).thenReturn(-13000.0);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule 4: regulationMode = VOLTAGE
    // Test condition: V regulation ~ V setPoint, then Q must be within [minQ, maxQ].
    @Test
    void checkSVCVoltageModeVAtSetpoint() {
        // Given regulation enabled, and regulationMode is VOLTAGE
        when(svc.getRegulationMode()).thenReturn(RegulationMode.VOLTAGE);
        when(svc.isRegulating()).thenReturn(true);
        when(svcTerminal.getP()).thenReturn(0.0);
        // Given V controlled ~ V setpoint
        Terminal regulatingTerminal = regulatingTerminalWithVoltage(380.0); // V controlled = 380.0.0
        when(svc.getRegulatingTerminal()).thenReturn(regulatingTerminal);
        when(svc.getVoltageSetpoint()).thenReturn(380.0); // V setpoint 380.0
        double expectedQmax = -bMin * v * v;
        assertEquals(1444.0, expectedQmax);
        double expectedQmin = -bMax * v * v;
        assertEquals(-14440.0, expectedQmin);
        // Given q inside bounds [-14440.0, 1444.0]
        when(svcTerminal.getQ()).thenReturn(0.0); // inside bounds [-14440.0, 1444.0]
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        // Given q outside bounds [-14440.0, 1444.0]
        when(svcTerminal.getQ()).thenReturn(2000.0);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    // Rule 5: if regulating is false then reactive power (Q) should be equal to 0 (within threshold)
    @Test
    void checkSVCWhenNoRegulatingQShouldBeZero() {
        when(svc.getRegulationMode()).thenReturn(RegulationMode.VOLTAGE);
        when(svc.isRegulating()).thenReturn(false);
        when(svcTerminal.getP()).thenReturn(0.0);
        when(svcTerminal.getQ()).thenReturn(0.009); // ~ threshold => invalid result
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
        when(svcTerminal.getQ()).thenReturn(0.02); // > threshold => invalid result
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.INSTANCE));
    }

    private Terminal regulatingTerminalWithVoltage(double vControlled) {
        Bus controlledBus = mock(Bus.class);
        when(controlledBus.getV()).thenReturn(vControlled);

        BusView controlledBusView = mock(BusView.class);
        when(controlledBusView.getBus()).thenReturn(controlledBus);

        Terminal regulatingTerminal = mock(Terminal.class);
        when(regulatingTerminal.getBusView()).thenReturn(controlledBusView);
        return regulatingTerminal;
    }
}

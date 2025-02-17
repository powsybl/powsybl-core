/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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

        Bus svcBus = Mockito.mock(Bus.class);
        Mockito.when(svcBus.getV()).thenReturn(v);
        Mockito.when(svcBus.isInMainConnectedComponent()).thenReturn(mainComponent);

        svcBusView = Mockito.mock(BusView.class);
        Mockito.when(svcBusView.getBus()).thenReturn(svcBus);
        Mockito.when(svcBusView.getConnectableBus()).thenReturn(svcBus);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(nominalV);

        svcTerminal = Mockito.mock(Terminal.class);
        Mockito.when(svcTerminal.getP()).thenReturn(p);
        Mockito.when(svcTerminal.getQ()).thenReturn(q);
        Mockito.when(svcTerminal.getBusView()).thenReturn(svcBusView);
        Mockito.when(svcTerminal.getVoltageLevel()).thenReturn(voltageLevel);

        svc = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svc.getId()).thenReturn("svc");
        Mockito.when(svc.getTerminal()).thenReturn(svcTerminal);
        Mockito.when(svc.getReactivePowerSetpoint()).thenReturn(reactivePowerSetpoint);
        Mockito.when(svc.getVoltageSetpoint()).thenReturn(voltageSetpoint);
        Mockito.when(svc.getRegulationMode()).thenReturn(regulationMode);
        Mockito.when(svc.isRegulating()).thenReturn(regulating);
        Mockito.when(svc.getBmin()).thenReturn(bMin);
        Mockito.when(svc.getBmax()).thenReturn(bMax);
    }

    @Test
    void checkSvcsValues() {
        // active power should be equal to 0
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = -39.8;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = -0.01;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        // if regulating = false then reactive power should be equal to 0
        regulating = false;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        regulating = true;
        //  if regulationMode = REACTIVE_POWER if the setpoint is in [Qmin=bMin*V*V, Qmax=bMax*V*V] then then reactive power should be equal to setpoint
        regulationMode = RegulationMode.REACTIVE_POWER;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        q = 3.7;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, Float.NaN, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, Float.NaN, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);

        // if regulationMode = VOLTAGE then either V at the connected bus is equal to voltageSetpoint and q is bounded within [-Qmax=bMax*V*V, -Qmin=bMin*V*V]
        regulating = true;
        regulationMode = RegulationMode.VOLTAGE;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 400;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 380;
        q = 1445;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        q = 3.7;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, Float.NaN, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, Float.NaN, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, Float.NaN, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, Float.NaN, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, Float.NaN, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(false);

        // if regulationMode = VOLTAGE then either q is equal to -bMin * V * V and V is lower than voltageSetpoint
        q = 1296;
        v = 360;
        nominalV = 360;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 340;
        nominalV = 340;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        // if regulationMode = VOLTAGE then either q is equal to -bMax * V * V and V is higher than voltageSetpoint
        q = -16000;
        v = 400;
        nominalV = 400;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 420;
        nominalV = 420;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        // check main component
        mainComponent = false;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        mainComponent = true;

        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380;
        nominalV = 380;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        reactivePowerSetpoint = 0;
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs("test", p, q, v, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, regulating, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    void checkSvcs() {
        // active power should be equal to 0
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(svcTerminal.getP()).thenReturn(-39.8);
        assertFalse(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.NULL_WRITER));

        // the unit is disconnected
        Mockito.when(svcBusView.getBus()).thenReturn(null);
        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(svc, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    void checkNetworkSvcs() throws IOException {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getStaticVarCompensatorStream()).thenAnswer(dummy -> Stream.of(svc));

        assertTrue(StaticVarCompensatorsValidation.INSTANCE.checkSVCs(network, looseConfig, data));

        assertTrue(ValidationType.SVCS.check(network, looseConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.NULL_WRITER, ValidationType.SVCS);
        assertTrue(ValidationType.SVCS.check(network, looseConfig, validationWriter));
    }
}

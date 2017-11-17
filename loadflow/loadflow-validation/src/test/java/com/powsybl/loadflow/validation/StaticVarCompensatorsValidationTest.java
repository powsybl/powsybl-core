/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class StaticVarCompensatorsValidationTest extends AbstractValidationTest {

    private float p = 0;
    private float q = 3.72344f;
    private float v = 380f;
    private float reactivePowerSetpoint = -3.72344f;
    private final float voltageSetpoint = 380f;
    private RegulationMode regulationMode = RegulationMode.REACTIVE_POWER;
    private final float bMin = -.01f;
    private final float bMax = .1f;

    private BusView svcBusView;
    private Terminal svcTerminal;
    private StaticVarCompensator svc;

    @Before
    public void setUp() {
        Bus svcBus = Mockito.mock(Bus.class);
        Mockito.when(svcBus.getV()).thenReturn(v);

        svcBusView = Mockito.mock(BusView.class);
        Mockito.when(svcBusView.getBus()).thenReturn(svcBus);

        svcTerminal = Mockito.mock(Terminal.class);
        Mockito.when(svcTerminal.getP()).thenReturn(p);
        Mockito.when(svcTerminal.getQ()).thenReturn(q);
        Mockito.when(svcTerminal.getBusView()).thenReturn(svcBusView);

        svc =  Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svc.getId()).thenReturn("svc");
        Mockito.when(svc.getTerminal()).thenReturn(svcTerminal);
        Mockito.when(svc.getReactivePowerSetPoint()).thenReturn(reactivePowerSetpoint);
        Mockito.when(svc.getVoltageSetPoint()).thenReturn(voltageSetpoint);
        Mockito.when(svc.getRegulationMode()).thenReturn(regulationMode);
        Mockito.when(svc.getBmin()).thenReturn(bMin);
        Mockito.when(svc.getBmax()).thenReturn(bMax);
    }

    @Test
    public void checkSvcsValues() {
        // active power should be equal to 0
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        p = -39.8f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        p = -0.01f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));

        //  if regulationMode=VOLTAGE then reactive power should be equal to setpoint
        regulationMode = RegulationMode.VOLTAGE;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        q = 3.7f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, looseConfig, NullWriter.NULL_WRITER));
        // check with NaN values
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, Float.NaN, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, Float.NaN, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(false);

        // if regulationMode<>VOLTAGE then either V at the connected bus is equal to voltageSetpoint
        regulationMode = RegulationMode.REACTIVE_POWER;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        v = 400f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        v = 380f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, Float.NaN, bMax, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, Float.NaN, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, Float.NaN, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, Float.NaN, bMax, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, Float.NaN, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, Float.NaN, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(false);

        // if regulationMode<>VOLTAGE then either q is equal to bMin * V and V is lower than voltageSetpoint
        q = 3.6f;
        v = 360f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        v = 340f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));

        // if regulationMode<>VOLTAGE then either q is equal to bMax * V and v is higher than voltageSetpoint
        q = -40f;
        v = 400f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        v = 420f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));

        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380f;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
        reactivePowerSetpoint = 0f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkSvcs() {
        // active power should be equal to 0
        assertTrue(StaticVarCompensatorsValidation.checkSVCs(svc, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(svcTerminal.getP()).thenReturn(-39.8f);
        assertFalse(StaticVarCompensatorsValidation.checkSVCs(svc, strictConfig, NullWriter.NULL_WRITER));

        // the unit is disconnected
        Mockito.when(svcBusView.getBus()).thenReturn(null);
        assertTrue(StaticVarCompensatorsValidation.checkSVCs(svc, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkSvcs() {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getStaticVarCompensatorStream()).thenAnswer(dummy -> Stream.of(svc));

        assertTrue(StaticVarCompensatorsValidation.checkSVCs(network, looseConfig, NullWriter.NULL_WRITER));
    }
}

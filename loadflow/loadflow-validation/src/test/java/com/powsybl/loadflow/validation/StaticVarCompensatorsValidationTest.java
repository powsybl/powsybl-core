/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
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
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class StaticVarCompensatorsValidationTest extends AbstractValidationTest {

    private float p = 0;
    private float q = 3.72344f;
    private float v = 380f;
    private float nominalV = 380f;
    private float reactivePowerSetpoint = -3.72344f;
    private final float voltageSetpoint = 380f;
    private RegulationMode regulationMode = RegulationMode.REACTIVE_POWER;
    private final float bMin = -.01f;
    private final float bMax = .1f;
    private final boolean connected = true;
    private boolean mainComponent = true;

    private BusView svcBusView;
    private Terminal svcTerminal;
    private StaticVarCompensator svc;

    @Before
    public void setUp() {
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
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = -39.8f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        p = -0.01f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        // if regulationMode = OFF then reactive power should be equal to 0
        regulationMode = RegulationMode.OFF;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        //  if regulationMode = REACTIVE_POWER if the setpoint is in [Qmin=bMin*V*V, Qmax=bMax*V*V] then then reactive power should be equal to setpoint
        regulationMode = RegulationMode.REACTIVE_POWER;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        q = 3.7f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, Float.NaN, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, Float.NaN, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, Float.NaN, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, Float.NaN, bMax, connected, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);

        // if regulationMode = REACTIVE_POWER if the setpoint is outside [Qmin=bMin*V*V, Qmax=bMax*V*V] then the reactive power is equal to the nearest bound
        reactivePowerSetpoint = -1500f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        q = 1444f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        reactivePowerSetpoint = -3.72344f;
        q = 3.7f;

        // if regulationMode = VOLTAGE then either V at the connected bus is equal to voltageSetpoint and q is bounded within [-Qmax=bMax*V*V, -Qmin=bMin*V*V]
        regulationMode = RegulationMode.VOLTAGE;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 400f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 380f;
        q = 1445f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        q = 3.7f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, Float.NaN, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, Float.NaN, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, Float.NaN, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, Float.NaN, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, Float.NaN, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, Float.NaN, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(false);

        // if regulationMode = VOLTAGE then either q is equal to bMax * V * V and V is lower than voltageSetpoint
        q = -12960f;
        v = 360f;
        nominalV = 360f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 340f;
        nominalV = 340f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));

        // if regulationMode = VOLTAGE then either q is equal to bMin * V * V and V is higher than voltageSetpoint
        q = 1600f;
        v = 400f;
        nominalV = 400f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        v = 420f;
        nominalV = 420f;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        // check main component
        mainComponent = false;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        mainComponent = true;

        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380f;
        nominalV = 380f;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        reactivePowerSetpoint = 0f;
        assertTrue(StaticVarCompensatorsValidation.checkSVCs("test", p, q, v, nominalV, reactivePowerSetpoint, voltageSetpoint, regulationMode, bMin, bMax, connected, mainComponent, strictConfig, NullWriter.NULL_WRITER));
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

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.NULL_WRITER, ValidationType.SVCS);
        assertTrue(ValidationType.SVCS.check(network, looseConfig, validationWriter));
    }
}

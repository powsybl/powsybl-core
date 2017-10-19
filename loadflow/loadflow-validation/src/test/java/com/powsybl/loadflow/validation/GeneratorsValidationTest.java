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
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class GeneratorsValidationTest extends AbstractValidationTest {

    private float p = -39.5056f;
    private float q = 3.72344f;
    private float v = 380f;
    private float targetP = 39.5056f;
    private float targetQ = -3.72344f;
    private final float targetV = 380f;
    private boolean voltageRegulatorOn = true;
    private final float minQ = -10f;
    private final float maxQ = 0f;

    private BusView genBusView;
    private Terminal genTerminal;
    private Generator generator;

    @Before
    public void setUp() {
        Bus genBus = Mockito.mock(Bus.class);
        Mockito.when(genBus.getV()).thenReturn(v);

        genBusView = Mockito.mock(BusView.class);
        Mockito.when(genBusView.getBus()).thenReturn(genBus);

        genTerminal = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal.getP()).thenReturn(p);
        Mockito.when(genTerminal.getQ()).thenReturn(q);
        Mockito.when(genTerminal.getBusView()).thenReturn(genBusView);

        ReactiveLimits genReactiveLimits = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        generator =  Mockito.mock(Generator.class);
        Mockito.when(generator.getId()).thenReturn("gen");
        Mockito.when(generator.getTerminal()).thenReturn(genTerminal);
        Mockito.when(generator.getTargetP()).thenReturn(targetP);
        Mockito.when(generator.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator.getTargetV()).thenReturn(targetV);
        Mockito.when(generator.getReactiveLimits()).thenReturn(genReactiveLimits);
    }

    @Test
    public void checkGeneratorsValues() {
        // active power should be equal to set point
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        p = -39.8f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        p = -39.5056f;
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        // check with NaN values
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, Float.NaN, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, Float.NaN, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(false);

        //  if voltageRegulatorOn="false" then reactive power should be equal to set point
        voltageRegulatorOn = false;
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        q = 3.7f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, looseConfig, NullWriter.NULL_WRITER));
        // check with NaN values
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, Float.NaN, targetV, voltageRegulatorOn, minQ, maxQ, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, Float.NaN, targetV, voltageRegulatorOn, minQ, maxQ, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);

        // if voltageRegulatorOn="true" then either V at the connected bus is equal to g.getTargetV()
        voltageRegulatorOn = true;
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        v = 400f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));

        // check with NaN values
        v = 380f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, Float.NaN, maxQ, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, Float.NaN, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, Float.NaN, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, Float.NaN, maxQ, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, Float.NaN, strictConfig, NullWriter.NULL_WRITER));
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, Float.NaN, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(false);

        // if voltageRegulatorOn="true" then either q is equal to g.getReactiveLimits().getMinQ(p) and v is lower than targetV
        q = 10f;
        v = 360f;
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        v = 400f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        q = 5f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));

        // if voltageRegulatorOn="true" then either q is equal to g.getReactiveLimits().getMaxQ(p) and v is higher than targetV
        q = 0f;
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        v = 360f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        q = 5f;
        v = 400f;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380f;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
        targetP = 0f;
        targetQ = 0f;
        assertTrue(GeneratorsValidation.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minQ, maxQ, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkGenerators() {
        // active power should be equal to set point
        assertTrue(GeneratorsValidation.checkGenerators(generator, strictConfig, NullWriter.NULL_WRITER));
        Mockito.when(genTerminal.getP()).thenReturn(-39.8f);
        assertFalse(GeneratorsValidation.checkGenerators(generator, strictConfig, NullWriter.NULL_WRITER));

        // the unit is disconnected
        Mockito.when(genBusView.getBus()).thenReturn(null);
        assertTrue(GeneratorsValidation.checkGenerators(generator, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkGenerators() {
        Bus genBus1 = Mockito.mock(Bus.class);
        Mockito.when(genBus1.getV()).thenReturn(v);

        BusView genBusView1 = Mockito.mock(BusView.class);
        Mockito.when(genBusView1.getBus()).thenReturn(genBus1);

        Terminal genTerminal1 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal1.getP()).thenReturn(p);
        Mockito.when(genTerminal1.getQ()).thenReturn(q);
        Mockito.when(genTerminal1.getBusView()).thenReturn(genBusView1);

        ReactiveLimits genReactiveLimits1 = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits1.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits1.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        Generator generator1 =  Mockito.mock(Generator.class);
        Mockito.when(generator1.getId()).thenReturn("gen");
        Mockito.when(generator1.getTerminal()).thenReturn(genTerminal1);
        Mockito.when(generator1.getTargetP()).thenReturn(targetP);
        Mockito.when(generator1.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator1.getTargetV()).thenReturn(targetV);
        Mockito.when(generator1.getReactiveLimits()).thenReturn(genReactiveLimits1);

        assertTrue(GeneratorsValidation.checkGenerators(generator1, strictConfig, NullWriter.NULL_WRITER));

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getGeneratorStream()).thenAnswer(dummy -> Stream.of(generator, generator1));

        assertTrue(GeneratorsValidation.checkGenerators(network, looseConfig, NullWriter.NULL_WRITER));
    }
}

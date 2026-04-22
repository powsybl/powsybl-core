/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class GeneratorsValidationTest extends AbstractValidationTest {

    private double p = -39.5056;
    private double q = 3.72344;
    private double v = 380.0;
    private double targetP = 39.5056;
    private double targetQ = -3.72344;
    private final double targetV = 380.0;
    private boolean voltageRegulatorOn = true;
    private final double minP = 25.0;
    private final double maxP = 45.0;
    private final double minQ = -10.0;
    private final double maxQ = 0.0;
    private final boolean connected = true;
    private boolean mainComponent = true;

    private BusView genBusView;
    private Terminal genTerminal;
    private Generator generator;

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();

        Bus genBus = Mockito.mock(Bus.class);
        Mockito.when(genBus.getV()).thenReturn(v);
        Mockito.when(genBus.isInMainConnectedComponent()).thenReturn(mainComponent);

        genBusView = Mockito.mock(BusView.class);
        Mockito.when(genBusView.getBus()).thenReturn(genBus);
        Mockito.when(genBusView.getConnectableBus()).thenReturn(genBus);

        genTerminal = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal.getP()).thenReturn(p);
        Mockito.when(genTerminal.getQ()).thenReturn(q);
        Mockito.when(genTerminal.getBusView()).thenReturn(genBusView);

        ReactiveLimits genReactiveLimits = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getId()).thenReturn("gen");
        Mockito.when(generator.getTerminal()).thenReturn(genTerminal);
        Mockito.when(generator.isVoltageRegulatorOn()).thenReturn(false);
        Mockito.when(generator.getTargetP()).thenReturn(targetP);
        Mockito.when(generator.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator.getTargetV()).thenReturn(targetV);
        Mockito.when(generator.getMaxP()).thenReturn(maxP);
        Mockito.when(generator.getMinP()).thenReturn(minP);
        Mockito.when(generator.getReactiveLimits()).thenReturn(genReactiveLimits);
    }

    @Test
    void checkGeneratorsValues() {
        // active power should be equal to setpoint
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        p = -39.8;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        p = -39.5056;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        // check with NaN values
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, Float.NaN, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(true);
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, Float.NaN, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(false);

        //  if voltageRegulatorOn="false" then reactive power should be equal to setpoint
        voltageRegulatorOn = false;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        q = 3.7;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        // check with NaN values
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, Float.NaN, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(true);
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, Float.NaN, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(false);

        // if voltageRegulatorOn="true" then either V at the connected bus is equal to g.getTargetV() and the reactive bounds are satisfied
        voltageRegulatorOn = true;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 400;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 380;
        q = 11;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        // check main component
        mainComponent = false;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        mainComponent = true;
        q = 3.7;

        // check with NaN values
        v = 380;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, Float.NaN, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, Float.NaN, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, Float.NaN, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(true);
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, Float.NaN, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, Float.NaN, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, Float.NaN, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setOkMissingValues(false);

        // if voltageRegulatorOn="true" then either q is equal to g.getReactiveLimits().getMinQ(p) and v is higher than targetV
        q = 10;
        v = 360;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 400;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        q = 5;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        // when maxQ < minQ
        strictConfig.setNoRequirementIfReactiveBoundInversion(true);
        // if noRequirementIfReactiveBoundInversion return true
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, maxQ, minQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        strictConfig.setNoRequirementIfReactiveBoundInversion(false);
        // the code switches the 2 values to go back to a situation where minQ < maxQ and the normal tests are done
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, maxQ, minQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        // if voltageRegulatorOn="true" then either q is equal to g.getReactiveLimits().getMaxQ(p) and v is lower than targetV
        q = 0;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        v = 360;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        q = 5;
        v = 400;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));

        // a validation error should be detected if there is both a voltage and a target but no p or q
        v = 380;
        p = Float.NaN;
        q = Float.NaN;
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
        targetP = 0;
        targetQ = 0;
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators("test", p, q, v, targetP, targetQ, targetV, voltageRegulatorOn, minP, maxP, minQ, maxQ, connected, mainComponent, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkGenerators() {
        // active power should be equal to setpoint
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE));
        Mockito.when(genTerminal.getP()).thenReturn(-39.8);
        assertFalse(GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE));

        // the unit is disconnected
        Mockito.when(genBusView.getBus()).thenReturn(null);
        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkNetworkGenerators() throws IOException {
        Bus genBus1 = Mockito.mock(Bus.class);
        Mockito.when(genBus1.getV()).thenReturn(v);
        Mockito.when(genBus1.isInMainConnectedComponent()).thenReturn(mainComponent);

        BusView genBusView1 = Mockito.mock(BusView.class);
        Mockito.when(genBusView1.getBus()).thenReturn(genBus1);
        Mockito.when(genBusView1.getConnectableBus()).thenReturn(genBus1);

        Terminal genTerminal1 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal1.getP()).thenReturn(p);
        Mockito.when(genTerminal1.getQ()).thenReturn(q);
        Mockito.when(genTerminal1.getBusView()).thenReturn(genBusView1);

        ReactiveLimits genReactiveLimits1 = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits1.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits1.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        Generator generator1 = Mockito.mock(Generator.class);
        Mockito.when(generator1.getId()).thenReturn("gen1");
        Mockito.when(generator1.getTerminal()).thenReturn(genTerminal1);
        Mockito.when(generator1.isVoltageRegulatorOn()).thenReturn(false);
        Mockito.when(generator1.getTargetP()).thenReturn(targetP);
        Mockito.when(generator1.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator1.getTargetV()).thenReturn(targetV);
        Mockito.when(generator1.getMaxP()).thenReturn(maxP);
        Mockito.when(generator1.getMinP()).thenReturn(minP);
        Mockito.when(generator1.getReactiveLimits()).thenReturn(genReactiveLimits1);

        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators(generator1, strictConfig, NullWriter.INSTANCE));

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getGeneratorStream()).thenAnswer(dummy -> Stream.of(generator, generator1));

        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators(network, looseConfig, data));

        assertTrue(ValidationType.GENERATORS.check(network, looseConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.INSTANCE, ValidationType.GENERATORS);
        assertTrue(ValidationType.GENERATORS.check(network, looseConfig, validationWriter));

        // test generation adjustment
        Bus genBus2 = Mockito.mock(Bus.class);
        Mockito.when(genBus2.getV()).thenReturn(v);
        Mockito.when(genBus2.isInMainConnectedComponent()).thenReturn(mainComponent);

        BusView genBusView2 = Mockito.mock(BusView.class);
        Mockito.when(genBusView2.getBus()).thenReturn(genBus2);
        Mockito.when(genBusView2.getConnectableBus()).thenReturn(genBus2);

        Terminal genTerminal2 = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal2.getP()).thenReturn(-155.236);
        Mockito.when(genTerminal2.getQ()).thenReturn(q);
        Mockito.when(genTerminal2.getBusView()).thenReturn(genBusView1);

        ReactiveLimits genReactiveLimits2 = Mockito.mock(ReactiveLimits.class);
        Mockito.when(genReactiveLimits2.getMinQ(Mockito.anyFloat())).thenReturn(minQ);
        Mockito.when(genReactiveLimits2.getMaxQ(Mockito.anyFloat())).thenReturn(maxQ);

        Generator generator2 = Mockito.mock(Generator.class);
        Mockito.when(generator2.getId()).thenReturn("gen2");
        Mockito.when(generator2.getTerminal()).thenReturn(genTerminal2);
        Mockito.when(generator2.isVoltageRegulatorOn()).thenReturn(false);
        Mockito.when(generator2.getTargetP()).thenReturn(155.107);
        Mockito.when(generator2.getTargetQ()).thenReturn(targetQ);
        Mockito.when(generator2.getTargetV()).thenReturn(targetV);
        Mockito.when(generator2.getMaxP()).thenReturn(227.5);
        Mockito.when(generator2.getMinP()).thenReturn(-227.5);
        Mockito.when(generator2.getReactiveLimits()).thenReturn(genReactiveLimits1);

        Mockito.when(network.getGeneratorStream()).thenAnswer(dummy -> Stream.of(generator, generator1, generator2));

        assertTrue(GeneratorsValidation.INSTANCE.checkGenerators(network, looseConfig, NullWriter.INSTANCE));
    }

    @DisplayName("Rule 1: A validation error should be detected if there is both a voltage and a target but no p or q")
    @Test
    void checkGeneratorsShouldSucceedRuleWhenPAndQMissingButTargetsExist() {
        // Given
        when(genTerminal.getP()).thenReturn(Double.NaN);
        when(genTerminal.getQ()).thenReturn(Double.NaN);
        when(generator.getTargetP()).thenReturn(10.0);
        when(generator.getTargetQ()).thenReturn(10.0);
        // When
        boolean result = GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE);
        // Then
        assertFalse(result);
    }

    @DisplayName("Rule 2: If reactive limits are inverted (`maxQ < minQ`) and noRequirementIfReactiveBoundInversion = true, generator validation pass")
    @ParameterizedTest(name = "noRequirementIfReactiveBoundInversion flag={0} => valid={1}")
    @CsvSource({"true, true", "false, false"})
    void checkGeneratorsShouldSucceedRuleWhenReactiveBoundsInvertedAndFlagEnabled(boolean noRequirementIfReactiveBoundInversion, boolean expectedValid) {
        // Given
        strictConfig.setNoRequirementIfReactiveBoundInversion(noRequirementIfReactiveBoundInversion);
        // maxQ < minQ
        ReactiveLimits invertedLimits = mock(ReactiveLimits.class);
        when(invertedLimits.getMinQ(anyDouble())).thenReturn(0.0);
        when(invertedLimits.getMaxQ(anyDouble())).thenReturn(-10.0);
        when(generator.getReactiveLimits()).thenReturn(invertedLimits);
        when(genTerminal.getQ()).thenReturn(5.0); // bypassed by rule 1
        // When
        boolean result = GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE);
        // Then
        assertEquals(expectedValid, result);
    }

    @DisplayName("Rule 3: Active setpoint outside bounds, if `targetP` is outside `[minP, maxP]` and noRequirementIfSetpointOutsidePowerBounds = true, generator validation pass")
    @ParameterizedTest(name = "noRequirementIfReactiveBoundInversion flag={0} => valid={1}")
    @CsvSource({"true, true", "false, false"})
    void checkGeneratorsShouldSucceedRuleWhenTargetPOutsideBoundsAndFlagEnabled(boolean noRequirementIfReactiveBoundInversion, boolean expectedValid) {
        // Given
        strictConfig.setNoRequirementIfSetpointOutsidePowerBounds(noRequirementIfReactiveBoundInversion);
        when(generator.getMinP()).thenReturn(20.0);
        when(generator.getMaxP()).thenReturn(30.0);
        when(generator.getTargetP()).thenReturn(40.0); // outside [minP=20.0, maxP=30.0]
        // When
        boolean result = GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE);
        // Then
        assertEquals(expectedValid, result);
    }

    @DisplayName("Rule 4: Active power p matches expected setpoint = TargetP")
    @Test
    void checkGeneratorsShouldSucceedRuleWhenActivePowerNotMatchExpectedP() {
        // Given
        when(generator.getTargetP()).thenReturn(20.0);
        when(genTerminal.getP()).thenReturn(-22.0);
        // When
        boolean result = GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE);
        // Then
        assertFalse(result);
    }

    @DisplayName("Rule 5: If voltage regulator is disabled, reactive power Q matches targetQ")
    @Test
    void checkGeneratorsShouldSucceedRuleWhenVoltageRegulatorDisabledAndQNotMatchTargetQ() {
        // Given
        when(generator.isVoltageRegulatorOn()).thenReturn(false);
        // keep p consistent
        when(generator.getTargetP()).thenReturn(20.0);
        when(genTerminal.getP()).thenReturn(-20.0);

        when(generator.getTargetQ()).thenReturn(10.0);
        when(genTerminal.getQ()).thenReturn(-12.0); // expected q = -targetQ = -10
        // When
        boolean result = GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE);
        // Then
        assertFalse(result);
    }

    @DisplayName("Rule 6: If voltage regulator is enabled, reactive power q follow V/targetV logic")
    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    void checkGeneratorsShouldSucceedRuleWhenVoltageRegulationEnabled(String caseName, double v, double q, boolean expectedValid) {
        // Given
        strictConfig.setNoRequirementIfReactiveBoundInversion(false);
        strictConfig.setNoRequirementIfSetpointOutsidePowerBounds(false);
        Bus genBus = generator.getTerminal().getBusView().getBus();
        when(genBus.getV()).thenReturn(v);
        when(generator.isVoltageRegulatorOn()).thenReturn(true);
        // keep p consistent
        when(generator.getTargetP()).thenReturn(20.0);
        when(genTerminal.getP()).thenReturn(-20.0);

        // minQ and maxQ [-10, 0]
        ReactiveLimits reactiveLimits = mock(ReactiveLimits.class);
        when(reactiveLimits.getMinQ(anyDouble())).thenReturn(-10.0);
        when(reactiveLimits.getMaxQ(anyDouble())).thenReturn(0.0);
        when(generator.getReactiveLimits()).thenReturn(reactiveLimits);
        // q
        when(genTerminal.getQ()).thenReturn(q);
        // When
        boolean result = GeneratorsValidation.INSTANCE.checkGenerators(generator, strictConfig, NullWriter.INSTANCE);
        // Then
        assertEquals(expectedValid, result, caseName);
    }

    private static Stream<Arguments> cases() {
        return Stream.of(
                // TargetV 380
                // V > targetV + threshold -> qGen ~= minQ (-10) -> q ~= +10
                Arguments.of("V > TargetV -> qGen ~ minQ -> valid", 400.0, 10.0, true),
                Arguments.of("V > TargetV -> qGen not ~ minQ -> invalid", 400.0, 5.0, false),

                // TargetV 380
                // V < targetV - threshold -> qGen ~= maxQ (0) -> q ~= 0
                Arguments.of("V < TargetV -> qGen ~ maxQ -> valid", 360.0, 0.0, true),
                Arguments.of("V < TargetV -> qGen not ~ maxQ -> invalid", 360.0, 5.0, false),

                // TargetV 380
                // |V-targetV| <= threshold -> qGen in [minQ, maxQ] = [-10, 0]
                Arguments.of("V ~ TargetV -> qGen within bounds -> valid", 380.0, 5.0, true),
                Arguments.of("V ~ TargetV -> qGen out of bounds -> invalid", 380.0, 11.0, false)
        );
    }

}

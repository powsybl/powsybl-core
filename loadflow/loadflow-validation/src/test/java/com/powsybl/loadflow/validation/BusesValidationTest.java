/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 * @author Samir Romdhani {@literal <samir.romdhani at rte-france.com>}
 */
class BusesValidationTest extends AbstractValidationTest {

    private final double loadP = 47.2786;
    private final double loadQ = 163.38244;
    private final double genP = -2020;
    private final double genQ = 91.54;
    private final double batP = -10;
    private final double batQ = 11;
    private final double shuntP = 0.0;
    private final double shuntQ = 175.8437;
    private final double svcP = 0.0;
    private final double svcQ = 0.0;
    private final double vscCSP = 0.0;
    private final double vscCSQ = 0.0;
    private final double lineP = 1982.7713;
    private final double lineQ = -441.7662;
    private final double boundaryLineP = -15.0;
    private final double boundaryLineQ = -10.0;
    private final double t2wtP = 5.25;
    private final double t2wtQ = 4.75;
    private final double t3wtP = 9.75;
    private final double t3wtQ = 5.25;
    private boolean mainComponent = true;

    private Bus bus;
    private BoundaryLine boundaryLine;

    @BeforeEach
    @Override
    void setUp() throws IOException {
        super.setUp();

        Terminal loadTerminal = mock(Terminal.class);
        when(loadTerminal.getP()).thenReturn(loadP);
        when(loadTerminal.getQ()).thenReturn(loadQ);
        Load load = mock(Load.class);
        when(load.getTerminal()).thenReturn(loadTerminal);

        Terminal genTerminal = mock(Terminal.class);
        when(genTerminal.getP()).thenReturn(genP);
        when(genTerminal.getQ()).thenReturn(genQ);
        Generator gen = mock(Generator.class);
        when(gen.getTerminal()).thenReturn(genTerminal);

        Terminal batTerminal = mock(Terminal.class);
        when(batTerminal.getP()).thenReturn(batP);
        when(batTerminal.getQ()).thenReturn(batQ);
        Battery bat = mock(Battery.class);
        when(bat.getTerminal()).thenReturn(batTerminal);

        Terminal shuntTerminal = mock(Terminal.class);
        when(shuntTerminal.getP()).thenReturn(shuntP);
        when(shuntTerminal.getQ()).thenReturn(shuntQ);
        ShuntCompensator shunt = mock(ShuntCompensator.class);
        when(shunt.getTerminal()).thenReturn(shuntTerminal);

        Bus lineBus = mock(Bus.class);
        when(lineBus.getId()).thenReturn("bus");
        BusView lineBusView = mock(BusView.class);
        when(lineBusView.getBus()).thenReturn(lineBus);
        Terminal lineTerminal = mock(Terminal.class);
        when(lineTerminal.getP()).thenReturn(lineP);
        when(lineTerminal.getQ()).thenReturn(lineQ);
        when(lineTerminal.isConnected()).thenReturn(true);
        when(lineTerminal.getBusView()).thenReturn(lineBusView);
        Line line = mock(Line.class);
        when(line.getTerminal1()).thenReturn(lineTerminal);

        Bus boundaryLineBus = mock(Bus.class);
        when(boundaryLineBus.getId()).thenReturn("bus");
        BusView boundaryLineBusView = mock(BusView.class);
        when(boundaryLineBusView.getBus()).thenReturn(boundaryLineBus);
        Terminal boundaryLineTerminal = mock(Terminal.class);
        when(boundaryLineTerminal.getP()).thenReturn(boundaryLineP);
        when(boundaryLineTerminal.getQ()).thenReturn(boundaryLineQ);
        when(boundaryLineTerminal.isConnected()).thenReturn(true);
        when(boundaryLineTerminal.getBusView()).thenReturn(boundaryLineBusView);
        boundaryLine = mock(BoundaryLine.class);
        when(boundaryLine.getTerminal()).thenReturn(boundaryLineTerminal);
        when(boundaryLine.isPaired()).thenReturn(false);

        Bus t2wBus = mock(Bus.class);
        when(t2wBus.getId()).thenReturn("bus");
        BusView t2wBusView = mock(BusView.class);
        when(t2wBusView.getBus()).thenReturn(t2wBus);
        Terminal t2wTerminal = mock(Terminal.class);
        when(t2wTerminal.getP()).thenReturn(t2wtP);
        when(t2wTerminal.getQ()).thenReturn(t2wtQ);
        when(t2wTerminal.isConnected()).thenReturn(true);
        when(t2wTerminal.getBusView()).thenReturn(t2wBusView);
        TwoWindingsTransformer t2w = mock(TwoWindingsTransformer.class);
        when(t2w.getTerminal1()).thenReturn(t2wTerminal);

        Bus t3wBus = mock(Bus.class);
        when(t3wBus.getId()).thenReturn("bus");
        BusView t3wBusView = mock(BusView.class);
        when(t3wBusView.getBus()).thenReturn(t3wBus);
        Terminal t3wTerminal = mock(Terminal.class);
        when(t3wTerminal.getP()).thenReturn(t3wtP);
        when(t3wTerminal.getQ()).thenReturn(t3wtQ);
        when(t3wTerminal.isConnected()).thenReturn(true);
        when(t3wTerminal.getBusView()).thenReturn(t3wBusView);
        ThreeWindingsTransformer t3w = mock(ThreeWindingsTransformer.class);
        Leg t3wLeg = mock(Leg.class);
        when(t3w.getLeg1()).thenReturn(t3wLeg);
        when(t3wLeg.getTerminal()).thenReturn(t3wTerminal);

        bus = mock(Bus.class);
        when(bus.getId()).thenReturn("bus");
        when(bus.getLoadStream()).thenAnswer(dummyLoads -> Stream.of(load));
        when(bus.getGeneratorStream()).thenAnswer(dummyGens -> Stream.of(gen));
        when(bus.getBatteryStream()).thenAnswer(dummyBats -> Stream.of(bat));
        when(bus.getShuntCompensatorStream()).thenAnswer(dummyShunts -> Stream.of(shunt));
        when(bus.getStaticVarCompensatorStream()).thenAnswer(dummyShunts -> Stream.empty());
        when(bus.getVscConverterStationStream()).thenAnswer(dummyShunts -> Stream.empty());
        when(bus.getLineStream()).thenAnswer(dummyLines -> Stream.of(line));
        when(bus.getBoundaryLineStream(BoundaryLineFilter.ALL)).thenAnswer(dummyBoundaryLines -> Stream.of(boundaryLine));
        when(bus.getTwoWindingsTransformerStream()).thenAnswer(dummyTwoWindingsTransformers -> Stream.of(t2w));
        when(bus.getThreeWindingsTransformerStream()).thenAnswer(dummyThreeWindingsTransformers -> Stream.of(t3w));
        when(bus.isInMainConnectedComponent()).thenReturn(mainComponent);
    }

    @Test
    void checkBusesValues() {
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.INSTANCE));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, strictConfig, NullWriter.INSTANCE));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, 174.4932, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.INSTANCE));
        // check NaN values
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", Double.NaN, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.INSTANCE));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, Double.NaN, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(true);
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", Double.NaN, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.INSTANCE));
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, boundaryLineP, boundaryLineQ, t2wtP, Double.NaN, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.INSTANCE));
        looseConfig.setOkMissingValues(false);
        // check main component
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, boundaryLineP,
                                               boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, strictConfig, NullWriter.INSTANCE));
        mainComponent = false;
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, boundaryLineP,
                                              boundaryLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkBuses() {
        assertTrue(BusesValidation.INSTANCE.checkBuses(bus, looseConfig, NullWriter.INSTANCE));
        assertFalse(BusesValidation.INSTANCE.checkBuses(bus, strictConfig, NullWriter.INSTANCE));
    }

    @Test
    void checkNetworkBuses() throws IOException {
        Network.BusView networkBusView = mock(Network.BusView.class);
        when(networkBusView.getBusStream()).thenAnswer(dummy -> Stream.of(bus));
        Network network = mock(Network.class);
        when(network.getId()).thenReturn("network");
        when(network.getBusView()).thenReturn(networkBusView);

        assertTrue(BusesValidation.INSTANCE.checkBuses(network, looseConfig, data));
        assertFalse(BusesValidation.INSTANCE.checkBuses(network, strictConfig, data));

        assertTrue(ValidationType.BUSES.check(network, looseConfig, tmpDir));
        assertFalse(ValidationType.BUSES.check(network, strictConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.INSTANCE, ValidationType.BUSES);
        assertTrue(ValidationType.BUSES.check(network, looseConfig, validationWriter));

        // Consider paired boundaryLines
        when(boundaryLine.isPaired()).thenReturn(true);

        assertTrue(BusesValidation.INSTANCE.checkBuses(network, looseConfig, data));
        assertFalse(BusesValidation.INSTANCE.checkBuses(network, strictConfig, data));

        assertTrue(ValidationType.BUSES.check(network, looseConfig, tmpDir));
        assertFalse(ValidationType.BUSES.check(network, strictConfig, tmpDir));

        validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.INSTANCE, ValidationType.BUSES);
        assertTrue(ValidationType.BUSES.check(network, looseConfig, validationWriter));
    }

    // Rule: |incomingP + loadP| <= threshold and |incomingQ + loadQ| <= threshold"
    @DisplayName("P and Q balanced")
    @Test
    void checkBusesShouldSucceedWhenPAndQBalanced() {
        // Given threshold (0.01)
        Bus busForBalance = mockBusForBalance(100.0, 50.0, -100.0, -50.0);
        // When
        boolean result = BusesValidation.INSTANCE.checkBuses(busForBalance, strictConfig, NullWriter.INSTANCE);
        // Then
        assertTrue(result);
    }

    @DisplayName("P and Q unbalanced")
    @Test
    void checkBusesShouldSucceedWhenPAndQUnbalanced() {
        // Given threshold (0.01)
        Bus busForBalance = mockBusForBalance(100.0, 50.0, -100.0, -49.8);
        // When
        boolean result = BusesValidation.INSTANCE.checkBuses(busForBalance, strictConfig, NullWriter.INSTANCE);
        // Then
        assertFalse(result);
    }

    private Bus mockBusForBalance(double loadP, double loadQ, double genP, double genQ) {
        Terminal loadTerminal = mock(Terminal.class);
        when(loadTerminal.getP()).thenReturn(loadP);
        when(loadTerminal.getQ()).thenReturn(loadQ);
        Load load = mock(Load.class);
        when(load.getTerminal()).thenReturn(loadTerminal);
        Terminal genTerminal = mock(Terminal.class);
        when(genTerminal.getP()).thenReturn(genP);
        when(genTerminal.getQ()).thenReturn(genQ);
        Generator generator = mock(Generator.class);
        when(generator.getTerminal()).thenReturn(genTerminal);

        Bus busForBalance = mock(Bus.class);
        when(busForBalance.getId()).thenReturn("bus-test");
        when(busForBalance.isInMainConnectedComponent()).thenReturn(true);
        when(busForBalance.getLoadStream()).thenAnswer(i -> Stream.of(load));
        when(busForBalance.getGeneratorStream()).thenAnswer(i -> Stream.of(generator));
        // other contributors = 0
        when(busForBalance.getBatteryStream()).thenAnswer(i -> Stream.empty());
        when(busForBalance.getShuntCompensatorStream()).thenAnswer(i -> Stream.empty());
        when(busForBalance.getStaticVarCompensatorStream()).thenAnswer(i -> Stream.empty());
        when(busForBalance.getVscConverterStationStream()).thenAnswer(i -> Stream.empty());
        when(busForBalance.getLineStream()).thenAnswer(i -> Stream.empty());
        when(busForBalance.getBoundaryLineStream(any())).thenAnswer(i -> Stream.empty());
        when(busForBalance.getTwoWindingsTransformerStream()).thenAnswer(i -> Stream.empty());
        when(busForBalance.getThreeWindingsTransformerStream()).thenAnswer(i -> Stream.empty());
        return busForBalance;
    }

}

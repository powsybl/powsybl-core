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

import com.powsybl.iidm.network.*;
import org.apache.commons.io.output.NullWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
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
    private final double danglingLineP = -15.0;
    private final double danglingLineQ = -10.0;
    private final double t2wtP = 5.25;
    private final double t2wtQ = 4.75;
    private final double t3wtP = 9.75;
    private final double t3wtQ = 5.25;
    private boolean mainComponent = true;

    private Bus bus;
    private DanglingLine danglingLine;

    @BeforeEach
    void setUp() throws IOException {
        super.setUp();

        Terminal loadTerminal = Mockito.mock(Terminal.class);
        Mockito.when(loadTerminal.getP()).thenReturn(loadP);
        Mockito.when(loadTerminal.getQ()).thenReturn(loadQ);
        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getTerminal()).thenReturn(loadTerminal);

        Terminal genTerminal = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal.getP()).thenReturn(genP);
        Mockito.when(genTerminal.getQ()).thenReturn(genQ);
        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getTerminal()).thenReturn(genTerminal);

        Terminal batTerminal = Mockito.mock(Terminal.class);
        Mockito.when(batTerminal.getP()).thenReturn(batP);
        Mockito.when(batTerminal.getQ()).thenReturn(batQ);
        Battery bat = Mockito.mock(Battery.class);
        Mockito.when(bat.getTerminal()).thenReturn(batTerminal);

        Terminal shuntTerminal = Mockito.mock(Terminal.class);
        Mockito.when(shuntTerminal.getP()).thenReturn(shuntP);
        Mockito.when(shuntTerminal.getQ()).thenReturn(shuntQ);
        ShuntCompensator shunt = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shunt.getTerminal()).thenReturn(shuntTerminal);

        Bus lineBus = Mockito.mock(Bus.class);
        Mockito.when(lineBus.getId()).thenReturn("bus");
        BusView lineBusView = Mockito.mock(BusView.class);
        Mockito.when(lineBusView.getBus()).thenReturn(lineBus);
        Terminal lineTerminal = Mockito.mock(Terminal.class);
        Mockito.when(lineTerminal.getP()).thenReturn(lineP);
        Mockito.when(lineTerminal.getQ()).thenReturn(lineQ);
        Mockito.when(lineTerminal.isConnected()).thenReturn(true);
        Mockito.when(lineTerminal.getBusView()).thenReturn(lineBusView);
        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getTerminal1()).thenReturn(lineTerminal);

        Bus danglingLineBus = Mockito.mock(Bus.class);
        Mockito.when(danglingLineBus.getId()).thenReturn("bus");
        BusView danglingLineBusView = Mockito.mock(BusView.class);
        Mockito.when(danglingLineBusView.getBus()).thenReturn(danglingLineBus);
        Terminal danglingLineTerminal = Mockito.mock(Terminal.class);
        Mockito.when(danglingLineTerminal.getP()).thenReturn(danglingLineP);
        Mockito.when(danglingLineTerminal.getQ()).thenReturn(danglingLineQ);
        Mockito.when(danglingLineTerminal.isConnected()).thenReturn(true);
        Mockito.when(danglingLineTerminal.getBusView()).thenReturn(danglingLineBusView);
        danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getTerminal()).thenReturn(danglingLineTerminal);
        Mockito.when(danglingLine.isPaired()).thenReturn(false);

        Bus t2wBus = Mockito.mock(Bus.class);
        Mockito.when(t2wBus.getId()).thenReturn("bus");
        BusView t2wBusView = Mockito.mock(BusView.class);
        Mockito.when(t2wBusView.getBus()).thenReturn(t2wBus);
        Terminal t2wTerminal = Mockito.mock(Terminal.class);
        Mockito.when(t2wTerminal.getP()).thenReturn(t2wtP);
        Mockito.when(t2wTerminal.getQ()).thenReturn(t2wtQ);
        Mockito.when(t2wTerminal.isConnected()).thenReturn(true);
        Mockito.when(t2wTerminal.getBusView()).thenReturn(t2wBusView);
        TwoWindingsTransformer t2w = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(t2w.getTerminal1()).thenReturn(t2wTerminal);

        Bus t3wBus = Mockito.mock(Bus.class);
        Mockito.when(t3wBus.getId()).thenReturn("bus");
        BusView t3wBusView = Mockito.mock(BusView.class);
        Mockito.when(t3wBusView.getBus()).thenReturn(t3wBus);
        Terminal t3wTerminal = Mockito.mock(Terminal.class);
        Mockito.when(t3wTerminal.getP()).thenReturn(t3wtP);
        Mockito.when(t3wTerminal.getQ()).thenReturn(t3wtQ);
        Mockito.when(t3wTerminal.isConnected()).thenReturn(true);
        Mockito.when(t3wTerminal.getBusView()).thenReturn(t3wBusView);
        ThreeWindingsTransformer t3w = Mockito.mock(ThreeWindingsTransformer.class);
        Leg t3wLeg = Mockito.mock(Leg.class);
        Mockito.when(t3w.getLeg1()).thenReturn(t3wLeg);
        Mockito.when(t3wLeg.getTerminal()).thenReturn(t3wTerminal);

        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getId()).thenReturn("bus");
        Mockito.when(bus.getLoadStream()).thenAnswer(dummyLoads -> Stream.of(load));
        Mockito.when(bus.getGeneratorStream()).thenAnswer(dummyGens -> Stream.of(gen));
        Mockito.when(bus.getBatteryStream()).thenAnswer(dummyBats -> Stream.of(bat));
        Mockito.when(bus.getShuntCompensatorStream()).thenAnswer(dummyShunts -> Stream.of(shunt));
        Mockito.when(bus.getStaticVarCompensatorStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getVscConverterStationStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getLineStream()).thenAnswer(dummyLines -> Stream.of(line));
        Mockito.when(bus.getDanglingLineStream(DanglingLineFilter.ALL)).thenAnswer(dummyDanglingLines -> Stream.of(danglingLine));
        Mockito.when(bus.getTwoWindingsTransformerStream()).thenAnswer(dummyTwoWindingsTransformers -> Stream.of(t2w));
        Mockito.when(bus.getThreeWindingsTransformerStream()).thenAnswer(dummyThreeWindingsTransformers -> Stream.of(t3w));
        Mockito.when(bus.isInMainConnectedComponent()).thenReturn(mainComponent);
    }

    @Test
    void checkBusesValues() {
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, 174.4932, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        // check NaN values
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", Double.NaN, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, Double.NaN, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", Double.NaN, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, danglingLineP, danglingLineQ, t2wtP, Double.NaN, t3wtP, t3wtQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);
        // check main component
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, danglingLineP,
                                               danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        mainComponent = false;
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, danglingLineP,
                                              danglingLineQ, t2wtP, t2wtQ, t3wtP, t3wtQ, mainComponent, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    void checkBuses() {
        assertTrue(BusesValidation.INSTANCE.checkBuses(bus, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses(bus, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    void checkNetworkBuses() throws IOException {
        Network.BusView networkBusView = Mockito.mock(Network.BusView.class);
        Mockito.when(networkBusView.getBusStream()).thenAnswer(dummy -> Stream.of(bus));
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getBusView()).thenReturn(networkBusView);

        assertTrue(BusesValidation.INSTANCE.checkBuses(network, looseConfig, data));
        assertFalse(BusesValidation.INSTANCE.checkBuses(network, strictConfig, data));

        assertTrue(ValidationType.BUSES.check(network, looseConfig, tmpDir));
        assertFalse(ValidationType.BUSES.check(network, strictConfig, tmpDir));

        ValidationWriter validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.NULL_WRITER, ValidationType.BUSES);
        assertTrue(ValidationType.BUSES.check(network, looseConfig, validationWriter));

        // Consider paired danglingLines
        Mockito.when(danglingLine.isPaired()).thenReturn(true);

        assertTrue(BusesValidation.INSTANCE.checkBuses(network, looseConfig, data));
        assertFalse(BusesValidation.INSTANCE.checkBuses(network, strictConfig, data));

        assertTrue(ValidationType.BUSES.check(network, looseConfig, tmpDir));
        assertFalse(ValidationType.BUSES.check(network, strictConfig, tmpDir));

        validationWriter = ValidationUtils.createValidationWriter(network.getId(), looseConfig, NullWriter.NULL_WRITER, ValidationType.BUSES);
        assertTrue(ValidationType.BUSES.check(network, looseConfig, validationWriter));
    }
}

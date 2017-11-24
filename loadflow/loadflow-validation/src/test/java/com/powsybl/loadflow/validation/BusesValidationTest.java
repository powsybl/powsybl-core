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
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Terminal.BusView;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusesValidationTest extends AbstractValidationTest {

    private final double loadP = 37.2786f;
    private final double loadQ = 174.38244f;
    private final double genP = -2020f;
    private final double genQ = 91.54;
    private final double shuntP = 0f;
    private final double shuntQ = 175.8437f;
    private final double svcP = 0f;
    private final double svcQ = 0f;
    private final double vscCSP = 0f;
    private final double vscCSQ = 0f;
    private final double lineP = 1982.7713f;
    private final double lineQ = -441.7662f;
    private final double twtP = 0f;
    private final double twtQ = 0f;
    private final double tltP = 0f;
    private final double tltQ = 0f;

    private Bus bus;

    @Before
    public void setUp() {
        Terminal loadTerminal = Mockito.mock(Terminal.class);
        Mockito.when(loadTerminal.getP()).thenReturn((float) loadP);
        Mockito.when(loadTerminal.getQ()).thenReturn((float) loadQ);
        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getTerminal()).thenReturn(loadTerminal);

        Terminal genTerminal = Mockito.mock(Terminal.class);
        Mockito.when(genTerminal.getP()).thenReturn((float) genP);
        Mockito.when(genTerminal.getQ()).thenReturn((float) genQ);
        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getTerminal()).thenReturn(genTerminal);

        Terminal shauntTerminal = Mockito.mock(Terminal.class);
        Mockito.when(shauntTerminal.getP()).thenReturn((float) shuntP);
        Mockito.when(shauntTerminal.getQ()).thenReturn((float) shuntQ);
        ShuntCompensator shunt = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shunt.getTerminal()).thenReturn(shauntTerminal);

        Bus lineBus = Mockito.mock(Bus.class);
        Mockito.when(lineBus.getId()).thenReturn("bus");
        BusView lineBusView = Mockito.mock(BusView.class);
        Mockito.when(lineBusView.getBus()).thenReturn(lineBus);
        Terminal lineTerminal = Mockito.mock(Terminal.class);
        Mockito.when(lineTerminal.getP()).thenReturn((float) lineP);
        Mockito.when(lineTerminal.getQ()).thenReturn((float) lineQ);
        Mockito.when(lineTerminal.isConnected()).thenReturn(true);
        Mockito.when(lineTerminal.getBusView()).thenReturn(lineBusView);
        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getTerminal1()).thenReturn(lineTerminal);

        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getId()).thenReturn("bus");
        Mockito.when(bus.getLoadStream()).thenAnswer(dummyLoads -> Stream.of(load));
        Mockito.when(bus.getGeneratorStream()).thenAnswer(dummyGens -> Stream.of(gen));
        Mockito.when(bus.getShuntStream()).thenAnswer(dummyShunts -> Stream.of(shunt));
        Mockito.when(bus.getStaticVarCompensatorStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getVscConverterStationStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getLineStream()).thenAnswer(dummyLines -> Stream.of(line));
        Mockito.when(bus.getTwoWindingTransformerStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getThreeWindingTransformerStream()).thenAnswer(dummyShunts -> Stream.empty());
    }

    @Test
    public void checkBusesValues() {
        assertTrue(BusesValidation.checkBuses("test", loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, twtP, twtQ, tltP, tltQ, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.checkBuses("test", loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, twtP, twtQ, tltP, tltQ, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.checkBuses("test", loadP, 174.4932f, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, twtP, twtQ, tltP, tltQ, looseConfig, NullWriter.NULL_WRITER));
        // check NaN values
        assertFalse(BusesValidation.checkBuses("test", Double.NaN, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, twtP, twtQ, tltP, tltQ, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.checkBuses("test", loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, twtP, Double.NaN, tltP, tltQ, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(BusesValidation.checkBuses("test", Double.NaN, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, twtP, twtQ, tltP, tltQ, looseConfig, NullWriter.NULL_WRITER));
        assertTrue(BusesValidation.checkBuses("test", loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, twtP, Double.NaN, tltP, tltQ, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);
    }

    @Test
    public void checkBuses() {
        assertTrue(BusesValidation.checkBuses(bus, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.checkBuses(bus, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkGenerators() {
        Network.BusView networkBusView = Mockito.mock(Network.BusView.class);
        Mockito.when(networkBusView.getBusStream()).thenAnswer(dummy -> Stream.of(bus));
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getBusView()).thenReturn(networkBusView);

        assertTrue(BusesValidation.checkBuses(network, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.checkBuses(network, strictConfig, NullWriter.NULL_WRITER));
    }

}

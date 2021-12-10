/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.Terminal.BusView;
import com.powsybl.loadflow.validation.io.ValidationWriter;
import org.apache.commons.io.output.NullWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BusesValidationTest extends AbstractValidationTest {

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
    private final double danglingLineP = 0.0;
    private final double danglingLineQ = 0.0;
    private final double twtP = 0.0;
    private final double twtQ = 0.0;
    private final double tltP = 0.0;
    private final double tltQ = 0.0;
    private boolean mainComponent = true;

    private Bus bus;

    @Before
    public void setUp() throws IOException {
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

        Terminal shauntTerminal = Mockito.mock(Terminal.class);
        Mockito.when(shauntTerminal.getP()).thenReturn(shuntP);
        Mockito.when(shauntTerminal.getQ()).thenReturn(shuntQ);
        ShuntCompensator shunt = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shunt.getTerminal()).thenReturn(shauntTerminal);

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
        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getTerminal()).thenReturn(danglingLineTerminal);

        bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getId()).thenReturn("bus");
        Mockito.when(bus.getLoadStream()).thenAnswer(dummyLoads -> Stream.of(load));
        Mockito.when(bus.getGeneratorStream()).thenAnswer(dummyGens -> Stream.of(gen));
        Mockito.when(bus.getBatteryStream()).thenAnswer(dummyBats -> Stream.of(bat));
        Mockito.when(bus.getShuntCompensatorStream()).thenAnswer(dummyShunts -> Stream.of(shunt));
        Mockito.when(bus.getStaticVarCompensatorStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getVscConverterStationStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getLineStream()).thenAnswer(dummyLines -> Stream.of(line));
        Mockito.when(bus.getDanglingLineStream()).thenAnswer(dummyDanglingLines -> Stream.of(danglingLine));
        Mockito.when(bus.getTwoWindingsTransformerStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.getThreeWindingsTransformerStream()).thenAnswer(dummyShunts -> Stream.empty());
        Mockito.when(bus.isInMainConnectedComponent()).thenReturn(mainComponent);
    }

    @Test
    public void checkBusesValues() {
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, 174.4932, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        // check NaN values
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", Double.NaN, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                               lineP, lineQ, danglingLineP, danglingLineQ, twtP, Double.NaN, tltP, tltQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(true);
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", Double.NaN, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                                              lineP, lineQ, danglingLineP, danglingLineQ, twtP, Double.NaN, tltP, tltQ, mainComponent, looseConfig, NullWriter.NULL_WRITER));
        looseConfig.setOkMissingValues(false);
        // check main component
        assertFalse(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, danglingLineP,
                                               danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, strictConfig, NullWriter.NULL_WRITER));
        mainComponent = false;
        assertTrue(BusesValidation.INSTANCE.checkBuses("test", loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, danglingLineP,
                                              danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkBuses() {
        assertTrue(BusesValidation.INSTANCE.checkBuses(bus, looseConfig, NullWriter.NULL_WRITER));
        assertFalse(BusesValidation.INSTANCE.checkBuses(bus, strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkBuses() throws IOException {
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
    }

}

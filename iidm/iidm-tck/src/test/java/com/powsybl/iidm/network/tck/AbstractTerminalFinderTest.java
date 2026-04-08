/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.TerminalFinder;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractTerminalFinderTest {

    @Test
    public void testBbsTerminal() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlN = network.getVoltageLevel("N");
        Optional<Terminal> optTerminalN0 = vlN.getNodeBreakerView().getOptionalTerminal(0);
        assertTrue(optTerminalN0.isPresent());

        Stream<? extends Terminal> terminalStream = optTerminalN0.get().getBusBreakerView().getBus().getConnectedTerminalStream();
        Optional<? extends Terminal> bestOptTerminal = TerminalFinder.getDefault().find(terminalStream);
        assertTrue(bestOptTerminal.isPresent());

        Terminal bestTerminal = bestOptTerminal.get();
        assertEquals(IdentifiableType.BUSBAR_SECTION, bestTerminal.getConnectable().getType());
        assertEquals("N_0", bestTerminal.getBusBreakerView().getBus().getId());
        assertEquals("N_0", bestTerminal.getBusView().getBus().getId());
    }

    @Test
    public void testNoTerminal() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlN = network.getVoltageLevel("N");

        TerminalFinder slackTerminalFinder = TerminalFinder.getDefault();

        Bus bus = vlN.getBusBreakerView().getBus("N_13");
        assertNotNull(bus);

        Iterable<? extends Terminal> terminalIter = bus.getConnectedTerminals();
        assertFalse(slackTerminalFinder.find(terminalIter).isPresent());
    }

    @Test
    public void testLineTerminal1() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV1");

        Bus bus = vlhv2.getBusBreakerView().getBus("NHV1");
        assertNotNull(bus);

        Stream<? extends Terminal> terminalStream = bus.getConnectedTerminalStream();
        Optional<? extends Terminal> bestOptTerminal = TerminalFinder.getDefault().find(terminalStream);
        assertTrue(bestOptTerminal.isPresent());

        Terminal bestTerminal = bestOptTerminal.get();
        assertEquals(IdentifiableType.LINE, bestTerminal.getConnectable().getType());
        assertEquals("NHV1", bestTerminal.getBusBreakerView().getBus().getId());
        assertEquals("VLHV1_0", bestTerminal.getBusView().getBus().getId());
    }

    @Test
    public void testLineTerminal2() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");

        Bus bus = vlhv2.getBusBreakerView().getBus("NHV2");
        assertNotNull(bus);

        Stream<? extends Terminal> terminalStream = bus.getConnectedTerminalStream();
        Optional<? extends Terminal> bestOptTerminal = TerminalFinder.getDefault().find(terminalStream);
        assertTrue(bestOptTerminal.isPresent());

        Terminal bestTerminal = bestOptTerminal.get();
        assertEquals(IdentifiableType.LINE, bestTerminal.getConnectable().getType());
        assertEquals("NHV2", bestTerminal.getBusBreakerView().getBus().getId());
        assertEquals("VLHV2_0", bestTerminal.getBusView().getBus().getId());
    }

    @Test
    public void testGeneratorTerminal() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLGEN");

        Bus bus = vlhv2.getBusBreakerView().getBus("NGEN");
        assertNotNull(bus);

        Stream<? extends Terminal> terminalStream = bus.getConnectedTerminalStream();
        Optional<? extends Terminal> bestOptTerminal = TerminalFinder.getDefault().find(terminalStream);
        assertTrue(bestOptTerminal.isPresent());

        Terminal bestTerminal = bestOptTerminal.get();
        assertEquals(IdentifiableType.GENERATOR, bestTerminal.getConnectable().getType());
        assertEquals("NGEN", bestTerminal.getBusBreakerView().getBus().getId());
        assertEquals("VLGEN_0", bestTerminal.getBusView().getBus().getId());
    }

}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.TerminalFinder;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractTerminalFinderTest {

    @Test
    public void testBbsTerminal() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlN = network.getVoltageLevel("N");
        Optional<Terminal> optTerminalN0 = vlN.getNodeBreakerView().getOptionalTerminal(0);
        assertTrue(optTerminalN0.isPresent());

        Stream<? extends Terminal> terminalStream = optTerminalN0.get().getBusBreakerView().getBus().getConnectedTerminalStream();

        TerminalFinder slackTerminalFinder = TerminalFinder.getDefault();
        vlN.newExtension(SlackTerminalAdder.class)
            .withTerminal(slackTerminalFinder.find(terminalStream))
            .add();

        SlackTerminal slackTerminalN = vlN.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminalN);

        assertEquals(ConnectableType.BUSBAR_SECTION, slackTerminalN.getTerminal().getConnectable().getType());
        assertEquals("N_0", slackTerminalN.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals("N_0", slackTerminalN.getTerminal().getBusView().getBus().getId());
    }

    @Test
    public void testNoTerminal() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlN = network.getVoltageLevel("N");

        TerminalFinder slackTerminalFinder = TerminalFinder.getDefault();

        Bus bus = vlN.getBusBreakerView().getBus("N_13");
        assertNotNull(bus);

        Iterable<? extends Terminal> terminalIter = bus.getConnectedTerminals();
        assertNull(slackTerminalFinder.find(terminalIter));
    }

    @Test
    public void testLineTerminal1() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV1");

        Bus bus = vlhv2.getBusBreakerView().getBus("NHV1");
        assertNotNull(bus);

        TerminalFinder slackTerminalFinder = TerminalFinder.getDefault();
        vlhv2.newExtension(SlackTerminalAdder.class)
            .withTerminal(slackTerminalFinder.find(bus.getConnectedTerminals()))
            .add();

        SlackTerminal slackTerminalN = vlhv2.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminalN);

        assertEquals(ConnectableType.LINE, slackTerminalN.getTerminal().getConnectable().getType());
        assertEquals("NHV1", slackTerminalN.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals("VLHV1_0", slackTerminalN.getTerminal().getBusView().getBus().getId());
    }

    @Test
    public void testLineTerminal2() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");

        Bus bus = vlhv2.getBusBreakerView().getBus("NHV2");
        assertNotNull(bus);

        TerminalFinder slackTerminalFinder = TerminalFinder.getDefault();
        vlhv2.newExtension(SlackTerminalAdder.class)
            .withTerminal(slackTerminalFinder.find(bus.getConnectedTerminals()))
            .add();

        SlackTerminal slackTerminalN = vlhv2.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminalN);

        assertEquals(ConnectableType.LINE, slackTerminalN.getTerminal().getConnectable().getType());
        assertEquals("NHV2", slackTerminalN.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals("VLHV2_0", slackTerminalN.getTerminal().getBusView().getBus().getId());
    }

    @Test
    public void testGeneratorTerminal() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2 = network.getVoltageLevel("VLGEN");

        Bus bus = vlhv2.getBusBreakerView().getBus("NGEN");
        assertNotNull(bus);

        TerminalFinder slackTerminalFinder = TerminalFinder.getDefault();
        vlhv2.newExtension(SlackTerminalAdder.class)
            .withTerminal(slackTerminalFinder.find(bus.getConnectedTerminals()))
            .add();

        SlackTerminal slackTerminalN = vlhv2.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminalN);

        assertEquals(ConnectableType.GENERATOR, slackTerminalN.getTerminal().getConnectable().getType());
        assertEquals("NGEN", slackTerminalN.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals("VLGEN_0", slackTerminalN.getTerminal().getBusView().getBus().getId());
    }

}

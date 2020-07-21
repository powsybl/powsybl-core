/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TerminalChooserTest {

    @Test
    public void test() {
        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vlN = network.getVoltageLevel("N");
        Optional<Terminal> optTerminal = vlN.getNodeBreakerView().getOptionalTerminal(0);
        assertTrue(optTerminal.isPresent());

        Stream<? extends Terminal> terminalStream = optTerminal.get().getBusBreakerView().getBus().getConnectedTerminalStream();

        TerminalChooser slackTerminalChooser = TerminalChooser.getDefaultSlackTerminalChooser();
        vlN.newExtension(SlackTerminalAdder.class)
            .setTerminal(slackTerminalChooser.choose(terminalStream))
            .add();

        SlackTerminal slackTerminal = vlN.getExtension(SlackTerminal.class);
        assertNotNull(slackTerminal);

        assertEquals(ConnectableType.BUSBAR_SECTION, slackTerminal.getTerminal().getConnectable().getType());
        assertEquals("N_0", slackTerminal.getTerminal().getBusBreakerView().getBus().getId());
        assertEquals("N_0", slackTerminal.getTerminal().getBusView().getBus().getId());
    }

}

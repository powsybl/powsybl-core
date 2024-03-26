/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class GeneratorTrippingTest extends AbstractTrippingTest {

    @Test
    void generatorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        GeneratorTripping tripping = new GeneratorTripping("GEN");
        tripping.apply(network);

        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }

    @Test
    void unknownGeneratorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        GeneratorTripping tripping = new GeneratorTripping("generator");
        assertThrows(PowsyblException.class, () -> tripping.apply(network));
    }

    @Test
    void fictitiousSwitchTest() {
        Set<String> switchIds = Collections.singleton("BJ");

        Network network = FictitiousSwitchFactory.create();
        network.getSwitch("BT").setFictitious(true);
        List<Boolean> expectedSwitchStates = getSwitchStates(network, switchIds);

        GeneratorTripping tripping = new GeneratorTripping("CD");

        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();
        tripping.traverse(network, switchesToOpen, terminalsToDisconnect);
        assertEquals(switchIds, switchesToOpen.stream().map(Switch::getId).collect(Collectors.toSet()));
        assertEquals(Collections.emptySet(), terminalsToDisconnect);

        tripping.apply(network);
        assertTrue(network.getSwitch("BJ").isOpen());

        List<Boolean> switchStates = getSwitchStates(network, switchIds);
        assertEquals(expectedSwitchStates, switchStates);
    }
}

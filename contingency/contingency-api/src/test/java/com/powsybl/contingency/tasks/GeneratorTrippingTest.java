/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.GeneratorContingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class GeneratorTrippingTest extends AbstractTrippingTest {

    @Test
    public void generatorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        GeneratorContingency tripping = new GeneratorContingency("GEN");
        Contingency contingency = new Contingency("contingency", tripping);

        ModificationTask task = contingency.toTask();
        task.modify(network, null);

        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }

    @Test(expected = PowsyblException.class)
    public void unknownGeneratorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        GeneratorTripping tripping = new GeneratorTripping("generator");
        tripping.modify(network, null);
    }

    @Test
    public void fictitiousSwitchTest() {
        Set<String> switchIds = Collections.singleton("BJ");

        Network network = FictitiousSwitchFactory.create();
        network.getSwitch("BT").setFictitious(true);
        List<Boolean> expectedSwitchStates = getSwitchStates(network, switchIds);

        GeneratorTripping tripping = new GeneratorTripping("CD");

        Set<Switch> switchesToOpen = new HashSet<>();
        Set<Terminal> terminalsToDisconnect = new HashSet<>();
        tripping.traverse(network, null, switchesToOpen, terminalsToDisconnect);
        assertEquals(switchIds, switchesToOpen.stream().map(Switch::getId).collect(Collectors.toSet()));
        assertEquals(Collections.emptySet(), terminalsToDisconnect);

        tripping.modify(network, null);
        assertTrue(network.getSwitch("BJ").isOpen());

        List<Boolean> switchStates = getSwitchStates(network, switchIds);
        assertEquals(expectedSwitchStates, switchStates);
    }
}

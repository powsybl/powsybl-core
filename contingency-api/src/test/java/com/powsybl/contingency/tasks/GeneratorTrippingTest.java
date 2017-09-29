/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.contingency.ContingencyImpl;
import eu.itesla_project.contingency.GeneratorContingency;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.Switch;
import eu.itesla_project.iidm.network.Terminal;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
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
public class GeneratorTrippingTest extends TrippingTest {

    @Test
    public void generatorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        GeneratorContingency tripping = new GeneratorContingency("GEN");
        ContingencyImpl contingency = new ContingencyImpl("contingency", tripping);

        ModificationTask task = contingency.toTask();
        task.modify(network, null);

        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }

    @Test(expected = ITeslaException.class)
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

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
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class GeneratorTrippingTest {

    @Test
    public void generatorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        GeneratorContingency tripping = new GeneratorContingency("GEN");
        ContingencyImpl contingency = new ContingencyImpl("contingency", tripping);

        ModificationTask task = contingency.toTask();
        task.modify(network);

        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }

    @Test(expected = ITeslaException.class)
    public void unknownGeneratorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        GeneratorTripping tripping = new GeneratorTripping("generator");
        tripping.modify(network);
    }
}

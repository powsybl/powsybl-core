/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.tasks;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.contingency.ContingencyImpl;
import eu.itesla_project.contingency.LineContingency;
import eu.itesla_project.iidm.network.Line;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchTrippingTest {

    @Test
    public void lineTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_1");

        assertTrue(line.getTerminal1().isConnected());
        assertTrue(line.getTerminal2().isConnected());

        LineContingency tripping = new LineContingency("NHV1_NHV2_1");
        ContingencyImpl contingency = new ContingencyImpl("contingency", tripping);

        ModificationTask task = contingency.toTask();
        task.modify(network);

        assertFalse(line.getTerminal1().isConnected());
        assertFalse(line.getTerminal2().isConnected());

        line.getTerminal1().connect();
        line.getTerminal2().connect();
        assertTrue(line.getTerminal1().isConnected());
        assertTrue(line.getTerminal2().isConnected());

        tripping = new LineContingency("NHV1_NHV2_1", "P2");
        contingency = new ContingencyImpl("contingency", tripping);
        contingency.toTask().modify(network);

        assertTrue(line.getTerminal1().isConnected());
        assertFalse(line.getTerminal2().isConnected());
    }

    @Test
    public void transformerTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        TwoWindingsTransformer transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");

        assertTrue(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());

        LineContingency tripping = new LineContingency("NHV2_NLOAD", "P2");
        ContingencyImpl contingency = new ContingencyImpl("contingency", tripping);
        contingency.toTask().modify(network);

        assertFalse(transformer.getTerminal1().isConnected());
        assertTrue(transformer.getTerminal2().isConnected());
    }

    @Test(expected = ITeslaException.class)
    public void unknownBranchTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        BranchTripping tripping = new BranchTripping("transformer");
        tripping.modify(network);
    }

    @Test(expected = ITeslaException.class)
    public void unknownSubstationTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();

        BranchTripping tripping = new BranchTripping("NHV2_NLOAD", "UNKNOWN");
        tripping.modify(network);
    }
}

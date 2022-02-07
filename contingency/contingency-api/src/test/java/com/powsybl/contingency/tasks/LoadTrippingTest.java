/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.network.modification.NetworkModification;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class LoadTrippingTest {

    @Test
    public void loadTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getLoad("LOAD").getTerminal().isConnected());

        Contingency contingency = Contingency.load("LOAD");

        NetworkModification task = contingency.toModification();
        task.apply(network);

        assertFalse(network.getLoad("LOAD").getTerminal().isConnected());
    }
}

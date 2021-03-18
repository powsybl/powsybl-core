/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DanglingLineTrippingTest extends AbstractTrippingTest {

    private Network network;

    @Before
    public void setUp() {
        network = DanglingLineNetworkFactory.create();
    }

    @Test
    public void generatorTrippingTest() {
        assertTrue(network.getDanglingLine("DL").getTerminal().isConnected());

        Contingency contingency = Contingency.danglingLine("DL");

        ModificationTask task = contingency.toTask();
        task.modify(network, null);

        assertFalse(network.getDanglingLine("DL").getTerminal().isConnected());
    }

    @Test(expected = PowsyblException.class)
    public void unknownShuntCompensatorTest() {
        DanglingLineTripping tripping = new DanglingLineTripping("DL_THAT_DO_NOT_EXIST");
        tripping.modify(network, null);
    }
}

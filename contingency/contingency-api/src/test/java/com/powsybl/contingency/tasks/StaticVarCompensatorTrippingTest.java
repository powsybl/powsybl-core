/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class StaticVarCompensatorTrippingTest extends AbstractTrippingTest {

    private Network network;

    @Before
    public void setUp() {
        network = SvcTestCaseFactory.create();
    }

    @Test
    public void generatorTrippingTest() {
        assertTrue(network.getStaticVarCompensator("SVC2").getTerminal().isConnected());

        Contingency contingency = Contingency.staticVarCompensator("SVC2");

        ModificationTask task = contingency.toTask();
        task.modify(network, null);

        assertFalse(network.getStaticVarCompensator("SVC2").getTerminal().isConnected());
    }

    @Test(expected = PowsyblException.class)
    public void unknownShuntCompensatorTest() {
        StaticVarCompensatorTripping tripping = new StaticVarCompensatorTripping("SVC");
        tripping.modify(network, null);
    }

}

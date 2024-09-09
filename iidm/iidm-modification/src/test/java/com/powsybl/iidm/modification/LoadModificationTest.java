/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
class LoadModificationTest {

    private Network network;
    private Load load;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        load = network.getLoad("LOAD");
    }

    @Test
    void modifyQ0() {
        assertEquals(200.0, load.getQ0());
        LoadModification modification = new LoadModification("LOAD", false, null, 100.0);
        modification.apply(network);
        assertEquals(100.0, load.getQ0());
    }

    @Test
    void modifyP0Relatively() {
        assertEquals(600.0, load.getP0());
        LoadModification modification = new LoadModification("LOAD", true, -20.0, null);
        modification.apply(network);
        assertEquals(580.0, load.getP0());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new LoadModification("ID", 10., 10.);
        assertEquals("LoadModification", networkModification.getName());
    }
}

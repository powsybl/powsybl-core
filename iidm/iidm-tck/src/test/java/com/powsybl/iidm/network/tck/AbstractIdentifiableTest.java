/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractIdentifiableTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        // Test updating id
        Load load = network.getLoad("LOAD");
        assertEquals("LOAD", load.getId());
        load.setId("NEW_LOAD_ID");
        assertEquals(load.getId(), network.getIdentifiable("NEW_LOAD_ID").getId());
        assertEquals("NEW_LOAD_ID", load.getId());

        // Test setting an already used id
        Generator gen = network.getGenerator("GEN");
        assertThrows(PowsyblException.class, () -> gen.setId("NEW_LOAD_ID"), "Object with id (NEW_LOAD_ID) already exists");

        // Test setting same ID
        load.setId("NEW_LOAD_ID");
        assertEquals("NEW_LOAD_ID", load.getId());

        // Test nothing is returned when using old id
        assertNull(network.getIdentifiable("LOAD"));
    }
}

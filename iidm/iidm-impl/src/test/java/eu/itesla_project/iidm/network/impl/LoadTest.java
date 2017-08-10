/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class LoadTest {

    @Test
    public void testSetterGetter() {
        Network network = FictitiousSwitchFactory.create();
        Load load = network.getLoad("CE");
        load.setP0(-1.0f);
        assertEquals(-1.0f, load.getP0(), 0.0f);
        load.setQ0(-2.0f);
        assertEquals(-2.0f, load.getQ0(), 0.0f);
        load.setP0(1.0f);
        assertEquals(1.0f, load.getP0(), 0.0f);
        load.setQ0(0.0f);
        assertEquals(0.0f, load.getQ0(), 0.0f);
        load.setLoadType(LoadType.AUXILIARY);
        assertEquals(LoadType.AUXILIARY, load.getLoadType());
    }
}
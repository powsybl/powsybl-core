/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.MinMaxReactiveLimits;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.ReactiveLimitsKind;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class MinMaxReactiveLimitsTest {

    @Test
    public void testSetterGetter() {
        Network network = FictitiousSwitchFactory.create();
        Generator generator = network.getGenerator("CB");
        MinMaxReactiveLimits minMaxReactiveLimits = generator
                                                        .newMinMaxReactiveLimits()
                                                        .setMaxQ(100.0f)
                                                        .setMinQ(10.0f)
                                                    .add();
        assertEquals(100.0f, minMaxReactiveLimits.getMaxQ(), 0.0f);
        assertEquals(100.0f, minMaxReactiveLimits.getMaxQ(1.0f), 0.0f);
        assertEquals(10.0f, minMaxReactiveLimits.getMinQ(), 0.0f);
        assertEquals(10.0f, minMaxReactiveLimits.getMinQ(1.0f), 0.0f);
        assertEquals(ReactiveLimitsKind.MIN_MAX, minMaxReactiveLimits.getKind());
    }
}

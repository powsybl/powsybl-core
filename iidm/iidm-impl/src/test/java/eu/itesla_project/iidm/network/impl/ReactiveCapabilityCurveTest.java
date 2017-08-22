/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.ReactiveCapabilityCurve;
import eu.itesla_project.iidm.network.ReactiveLimitsKind;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ReactiveCapabilityCurveTest {
    @Test
    public void testSetterGetter() {
        Network network = FictitiousSwitchFactory.create();
        Generator generator = network.getGenerator("CB");
        ReactiveCapabilityCurve reactiveCapabilityCurve = generator.newReactiveCapabilityCurve()
                .beginPoint().setP(1.0f).setMaxQ(5.0f).setMinQ(1.0f).endPoint()
                .beginPoint().setP(2.0f).setMaxQ(10.0f).setMinQ(2.0f).endPoint()
                .beginPoint().setP(100.0f).setMaxQ(10.0f).setMinQ(2.0f).endPoint()
                .add();
        assertEquals(ReactiveLimitsKind.CURVE, reactiveCapabilityCurve.getKind());
        assertEquals(100.0f, reactiveCapabilityCurve.getMaxP(), 0.0f);
        assertEquals(1.0f, reactiveCapabilityCurve.getMinP(), 0.0f);
        assertEquals(3, reactiveCapabilityCurve.getPoints().size());
    }
}

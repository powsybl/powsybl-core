/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.EnergySource;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratorTest {
    @Test
    public void testSetterGetter() {
        Network network = FictitiousSwitchFactory.create();
        Generator generator = network.getGenerator("CB");
        assertNotNull(generator);
        assertEquals(EnergySource.HYDRO, generator.getEnergySource());
        generator.setEnergySource(EnergySource.NUCLEAR);
        assertEquals(EnergySource.NUCLEAR, generator.getEnergySource());
        Float minP = 10.0f;
        generator.setMinP(minP);
        assertEquals(minP, generator.getMinP(), 0.0f);
        Float maxP = 20.0f;
        generator.setMaxP(maxP);
        assertEquals(maxP, generator.getMaxP(), 0.0f);

        Float targetP = 11.0f;
        Float targetQ = 21.0f;
        Float targetV = 31.0f;
        Float ratedS = 41.0f;
        generator.setTargetP(targetP);
        generator.setTargetQ(targetQ);
        generator.setTargetV(targetV);
        generator.setRatedS(ratedS);
        assertEquals(targetP, generator.getTargetP(), 0.0f);
        assertEquals(targetQ, generator.getTargetQ(), 0.0f);
        assertEquals(targetV, generator.getTargetV(), 0.0f);
        assertEquals(ratedS, generator.getRatedS(), 0.0f);
    }
}

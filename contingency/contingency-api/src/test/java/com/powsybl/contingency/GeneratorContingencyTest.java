/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.tasks.GeneratorTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class GeneratorContingencyTest {

    @Test
    public void test() {
        Contingency contingency = Contingency.generator("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.GENERATOR, contingency.getElements().get(0).getType());

        GeneratorContingency genContingency = new GeneratorContingency("id");
        assertEquals("id", genContingency.getId());
        assertEquals(ContingencyElementType.GENERATOR, genContingency.getType());

        assertNotNull(genContingency.toTask());
        assertTrue(genContingency.toTask() instanceof GeneratorTripping);

        new EqualsTester()
                .addEqualityGroup(new GeneratorContingency("g1"), new GeneratorContingency("g1"))
                .addEqualityGroup(new GeneratorContingency("g2"), new GeneratorContingency("g2"))
                .testEquals();
    }

    @Test
    public void test2() {
        Network network = EurostagTutorialExample1Factory.create();
        ContingencyList contingencyList = ContingencyList.of(Contingency.generator("GEN"), Contingency.generator("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        GeneratorContingency genCtg = (GeneratorContingency) contingencies.get(0).getElements().get(0);
        assertEquals("GEN", genCtg.getId());
    }
}

/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.CompoundModificationTask;
import com.powsybl.contingency.tasks.ModificationTask;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class ContingencyTest {

    @Test
    public void test() {
        ContingencyElement element1 = new BranchContingency("line");
        ContingencyElement element2 = new GeneratorContingency("generator");
        Contingency contingency = new Contingency("contingency", element1, element2);

        assertEquals("contingency", contingency.getId());
        assertEquals(2, contingency.getElements().size());

        Iterator<ContingencyElement> iterator = contingency.getElements().iterator();
        assertEquals(element1, iterator.next());
        assertEquals(element2, iterator.next());

        ModificationTask task = contingency.toTask();
        assertTrue(task instanceof CompoundModificationTask);
    }

    @Test
    public void validationTest() {
        Network network = EurostagTutorialExample1Factory.create();
        Contingency generatorContingency = new Contingency("GEN contingency", new GeneratorContingency("GEN"));
        Contingency generatorInvalidContingency = new Contingency("GEN invalid contingency", new GeneratorContingency("GE"));
        Contingency lineContingency = new Contingency("NHV1_NHV2_1 contingency", new BranchContingency("NHV1_NHV2_1", "VLHV1"));
        Contingency lineInvalidContingency = new Contingency("NHV1_NHV2_1 invalid contingency", new BranchContingency("NHV1_NHV2_1", "VLHV"));
        List<Contingency> validContingencies = Contingency.checkValidity(Arrays.asList(generatorContingency, generatorInvalidContingency,
                lineContingency, lineInvalidContingency), network);
        assertEquals(Arrays.asList("GEN contingency", "NHV1_NHV2_1 contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }

    @Test
    public void validationTestForShunt() {
        Network network = HvdcTestNetwork.createLcc();
        Contingency shuntCompensatorContingency = new Contingency("Shunt contingency", new ShuntCompensatorContingency("C1_Filter1"));
        Contingency shuntCompensatorInvalidContingency = new Contingency("Shunt invalid contingency", new ShuntCompensatorContingency("C_Filter"));
        List<Contingency> validContingencies = Contingency.checkValidity(Arrays.asList(shuntCompensatorContingency, shuntCompensatorInvalidContingency), network);
        assertEquals(Arrays.asList("Shunt contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }

    @Test
    public void validationTestForSVC() {
        Network network = SvcTestCaseFactory.create();
        Contingency staticVarCompensatorContingency = new Contingency("SVC contingency", new StaticVarCompensatorContingency("SVC2"));
        Contingency staticVarCompensatorInvalidContingency = new Contingency("SVC invalid contingency", new StaticVarCompensatorContingency("SVC"));
        List<Contingency> validContingencies = Contingency.checkValidity(Arrays.asList(staticVarCompensatorContingency, staticVarCompensatorInvalidContingency), network);
        assertEquals(Arrays.asList("SVC contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }
}

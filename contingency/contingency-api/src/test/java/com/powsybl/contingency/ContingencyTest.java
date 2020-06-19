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
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import org.junit.Test;

import java.util.Arrays;
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
        Contingency contingency = Contingency.builder("contingency")
                .branch("line")
                .generator("generator")
                .build();

        assertEquals("contingency", contingency.getId());
        assertEquals(2, contingency.getElements().size());

        List<ContingencyElement> elements = contingency.getElements();
        assertEquals("line", elements.get(0).getId());
        assertEquals(ContingencyElementType.BRANCH, elements.get(0).getType());
        assertEquals("generator", elements.get(1).getId());
        assertEquals(ContingencyElementType.GENERATOR, elements.get(1).getType());

        ModificationTask task = contingency.toTask();
        assertTrue(task instanceof CompoundModificationTask);
    }

    @Test
    public void validationTest() {
        Network network = EurostagTutorialExample1Factory.create();
        Contingency generatorContingency = Contingency.builder("GEN contingency").generator("GEN").build();
        Contingency generatorInvalidContingency = Contingency.builder("GEN invalid contingency").generator("GE").build();
        Contingency lineContingency = Contingency.builder("NHV1_NHV2_1 contingency").line("NHV1_NHV2_1", "VLHV1").build();
        Contingency lineInvalidContingency = Contingency.builder("NHV1_NHV2_1 invalid contingency").line("NHV1_NHV2_1", "VLHV").build();

        List<Contingency> validContingencies = ContingencyList.of(generatorContingency, generatorInvalidContingency, lineContingency, lineInvalidContingency)
                .getContingencies(network);

        assertEquals(Arrays.asList("GEN contingency", "NHV1_NHV2_1 contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }

    @Test
    public void validationTestForShunt() {
        Network network = HvdcTestNetwork.createLcc();
        Contingency shuntCompensatorContingency = Contingency.builder("Shunt contingency").shuntCompensator("C1_Filter1").build();
        Contingency shuntCompensatorInvalidContingency = Contingency.builder("Shunt invalid contingency").shuntCompensator("C_Filter").build();

        List<Contingency> validContingencies = ContingencyList.of(shuntCompensatorContingency, shuntCompensatorInvalidContingency)
                .getContingencies(network);

        assertEquals(Arrays.asList("Shunt contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }

    @Test
    public void validationTestForSVC() {
        Network network = SvcTestCaseFactory.create();
        Contingency staticVarCompensatorContingency = Contingency.builder("SVC contingency").staticVarCompensator("SVC2").build();
        Contingency staticVarCompensatorInvalidContingency = Contingency.builder("SVC invalid contingency").staticVarCompensator("SVC").build();
        List<Contingency> validContingencies = ContingencyList.of(staticVarCompensatorContingency, staticVarCompensatorInvalidContingency)
                .getContingencies(network);

        assertEquals(Arrays.asList("SVC contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }

    @Test
    public void validationTestForDL() {
        Network network = DanglingLineNetworkFactory.create();
        Contingency danglingLineContingency = new Contingency("DL contingency", new DanglingLineContingency("DL"));
        Contingency danglingLineInvalidContingency = new Contingency("DL invalid contingency", new DanglingLineContingency("DL_THAT_DO_NOT_EXIST"));
        List<Contingency> validContingencies = ContingencyList.of(danglingLineContingency, danglingLineInvalidContingency)
                .getContingencies(network);

        assertEquals(Arrays.asList("DL contingency"),
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
    }
}

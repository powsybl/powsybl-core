/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.NetworkModification;
import com.powsybl.iidm.modification.NetworkModificationList;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class ContingencyTest {

    @Test
    void test() {
        Contingency contingency = Contingency.builder("contingency")
                .addBranch("line")
                .addGenerator("generator")
                .build();

        assertEquals("contingency", contingency.getId());
        assertEquals(2, contingency.getElements().size());

        List<ContingencyElement> elements = contingency.getElements();
        assertEquals("line", elements.get(0).getId());
        assertEquals(ContingencyElementType.BRANCH, elements.get(0).getType());
        assertEquals("generator", elements.get(1).getId());
        assertEquals(ContingencyElementType.GENERATOR, elements.get(1).getType());

        NetworkModification modification = contingency.toModification();
        assertTrue(modification instanceof NetworkModificationList);

        ContingencyElement bbsElement = new BusbarSectionContingency("bbs");
        contingency.addElement(bbsElement);
        assertEquals(3, contingency.getElements().size());
        contingency.removeElement(bbsElement);
        assertEquals(2, contingency.getElements().size());

        contingency = new Contingency("test", bbsElement);
        contingency.addElement(new LineContingency("UNKNOWN"));
        assertEquals(2, contingency.getElements().size());
    }

    @Test
    void validationTest() {
        Network network = EurostagTutorialExample1Factory.create();
        Contingency generatorContingency = Contingency.builder("GEN contingency").addGenerator("GEN").build();
        Contingency generatorInvalidContingency = Contingency.builder("GEN invalid contingency").addGenerator("GE").build();
        Contingency lineContingency = Contingency.builder("NHV1_NHV2_1 contingency").addLine("NHV1_NHV2_1", "VLHV1").build();
        Contingency lineInvalidContingency = Contingency.builder("NHV1_NHV2_1 invalid contingency").addLine("NHV1_NHV2_1", "VLHV").build();

        List<Contingency> validContingencies = ContingencyList.of(generatorContingency, generatorInvalidContingency, lineContingency, lineInvalidContingency)
                .getContingencies(network);
        List<String> expectedValidIds = Arrays.asList("GEN contingency", "NHV1_NHV2_1 contingency");

        assertEquals(expectedValidIds,
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

        assertEquals(expectedValidIds,
                ContingencyList.getValidContingencies(Arrays.asList(generatorContingency, generatorInvalidContingency, lineContingency, lineInvalidContingency), network)
                        .stream()
                        .map(Contingency::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void validationTestForShunt() {
        Network network = HvdcTestNetwork.createLcc();
        Contingency shuntCompensatorContingency = Contingency.builder("Shunt contingency").addShuntCompensator("C1_Filter1").build();
        Contingency shuntCompensatorInvalidContingency = Contingency.builder("Shunt invalid contingency").addShuntCompensator("C_Filter").build();

        List<Contingency> validContingencies = ContingencyList.of(shuntCompensatorContingency, shuntCompensatorInvalidContingency)
                .getContingencies(network);

        List<String> expectedValidIds = Collections.singletonList("Shunt contingency");

        assertEquals(expectedValidIds,
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

        assertEquals(expectedValidIds,
                ContingencyList.getValidContingencies(Arrays.asList(shuntCompensatorContingency, shuntCompensatorInvalidContingency), network)
                        .stream()
                        .map(Contingency::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void validationTestForSVC() {
        Network network = SvcTestCaseFactory.create();
        Contingency staticVarCompensatorContingency = Contingency.builder("SVC contingency").addStaticVarCompensator("SVC2").build();
        Contingency staticVarCompensatorInvalidContingency = Contingency.builder("SVC invalid contingency").addStaticVarCompensator("SVC").build();
        List<Contingency> validContingencies = ContingencyList.of(staticVarCompensatorContingency, staticVarCompensatorInvalidContingency)
                .getContingencies(network);

        List<String> expectedValidIds = Collections.singletonList("SVC contingency");

        assertEquals(expectedValidIds,
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

        assertEquals(expectedValidIds,
                ContingencyList.getValidContingencies(Arrays.asList(staticVarCompensatorContingency, staticVarCompensatorInvalidContingency), network)
                        .stream()
                        .map(Contingency::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void validationTestForDL() {
        Network network = BoundaryLineNetworkFactory.create();
        Contingency boundaryLineContingency = Contingency.builder("DL contingency").addBoundaryLine("DL").build();
        Contingency boundaryLineInvalidContingency = Contingency.builder("DL invalid contingency").addBoundaryLine("DL_THAT_DO_NOT_EXIST").build();
        List<Contingency> validContingencies = ContingencyList.of(boundaryLineContingency, boundaryLineInvalidContingency)
                .getContingencies(network);

        List<String> expectedValidIds = Collections.singletonList("DL contingency");

        assertEquals(expectedValidIds,
                validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

        assertEquals(expectedValidIds,
                ContingencyList.getValidContingencies(Arrays.asList(boundaryLineContingency, boundaryLineInvalidContingency), network)
                        .stream()
                        .map(Contingency::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void validationTestForTwt3() {
        var network = ThreeWindingsTransformerNetworkFactory.create();
        var twt3Contingency = Contingency.builder("Twt3 contingency").addThreeWindingsTransformer("3WT").build();
        var invalidContingency = Contingency.builder("Twt3 invalid contingency").addThreeWindingsTransformer("3WT_THAT_DO_NOT_EXIST").build();
        var validContingencies = ContingencyList.of(twt3Contingency, invalidContingency).getContingencies(network);
        var expectedIds = Collections.singletonList("Twt3 contingency");

        assertEquals(expectedIds, validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));
        assertEquals(expectedIds,
                ContingencyList.getValidContingencies(Arrays.asList(twt3Contingency, invalidContingency), network)
                        .stream()
                        .map(Contingency::getId)
                        .collect(Collectors.toList()));
    }

    @Test
    void testIdentifiableBuilder() {
        Network network = EurostagTutorialExample1Factory.create();
        Contingency generatorContingency = Contingency.builder("GEN contingency").addIdentifiable("GEN", network).build();
        Contingency loadContingency = Contingency.builder("LOAD contingency").addIdentifiable("LOAD", network).build();
        Contingency lineContingency = Contingency.builder("NHV1_NHV2_1 contingency").addIdentifiable("NHV1_NHV2_1", network).build();
        Contingency twtContingency = Contingency.builder("NGEN_NHV1 contingency").addIdentifiable("NGEN_NHV1", network).build();
        Contingency multipleContingency = Contingency.builder("N-2 contingency").addIdentifiable("NHV1_NHV2_2", network).addIdentifiable("NHV2_NLOAD", network).build();

        List<Contingency> validContingencies = ContingencyList.of(generatorContingency, loadContingency, lineContingency, twtContingency, multipleContingency)
            .getContingencies(network);
        List<String> expectedValidIds = Arrays.asList("GEN contingency", "LOAD contingency", "NHV1_NHV2_1 contingency", "NGEN_NHV1 contingency", "N-2 contingency");

        assertEquals(expectedValidIds,
            validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

        assertEquals(expectedValidIds,
            ContingencyList.getValidContingencies(Arrays.asList(generatorContingency, loadContingency, lineContingency, twtContingency, multipleContingency), network)
                .stream()
                .map(Contingency::getId)
                .collect(Collectors.toList()));
    }

    @Test
    void testIdentifiableBuilderOtherProposition() {
        Network network = EurostagTutorialExample1Factory.create();
        Identifiable<?> generator = network.getIdentifiable("GEN");
        Contingency generatorContingency = Contingency.builder("GEN contingency").addIdentifiable(generator).build();
        Identifiable<?> load = network.getIdentifiable("LOAD");
        Contingency loadContingency = Contingency.builder("LOAD contingency").addIdentifiable(load).build();
        Identifiable<?> line = network.getLine("NHV1_NHV2_1");
        Contingency lineContingency = Contingency.builder("NHV1_NHV2_1 contingency").addIdentifiable(line).build();
        Identifiable<?> twt = network.getIdentifiable("NGEN_NHV1");
        Contingency twtContingency = Contingency.builder("NGEN_NHV1 contingency").addIdentifiable(twt).build();
        Identifiable<?> line2 = network.getIdentifiable("NHV1_NHV2_2");
        Identifiable<?> twt2 = network.getIdentifiable("NHV2_NLOAD");
        Contingency multipleContingency = Contingency.builder("N-2 contingency").addIdentifiable(line2).addIdentifiable(twt2).build();

        List<Contingency> validContingencies = ContingencyList.of(generatorContingency, loadContingency, lineContingency, twtContingency, multipleContingency)
            .getContingencies(network);
        List<String> expectedValidIds = Arrays.asList("GEN contingency", "LOAD contingency", "NHV1_NHV2_1 contingency", "NGEN_NHV1 contingency", "N-2 contingency");

        assertEquals(expectedValidIds,
            validContingencies.stream().map(Contingency::getId).collect(Collectors.toList()));

        assertEquals(expectedValidIds,
            ContingencyList.getValidContingencies(Arrays.asList(generatorContingency, loadContingency, lineContingency, twtContingency, multipleContingency), network)
                .stream()
                .map(Contingency::getId)
                .collect(Collectors.toList()));
    }

    @Test
    void testNotValidIdentifiableBuilder() {
        Network network = EurostagTutorialExample1Factory.create();
        ContingencyBuilder contingencyBuilder = Contingency.builder("Invalid contingency");
        PowsyblException exception = assertThrows(PowsyblException.class, () -> contingencyBuilder.addIdentifiable("Unknown", network));
        assertEquals("Element Unknown has not been found in the network", exception.getMessage());
    }
}

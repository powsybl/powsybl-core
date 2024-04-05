/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.contingency.list.IdentifierContingencyList;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.identifiers.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
class NetworkElementIdentifierContingencyListTest {

    @Test
    void testSimpleIdentifier() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("LINE_S2S3", "LINE_S2S3"));
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("LINE_S3S4", "LINE_S3S4"));
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("LINE_S4S1", "LINE_S4S1"));
        networkElementIdentifierList.add(new IdBasedNetworkElementIdentifier("test", "test"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("LINE_S2S3", new LineContingency("LINE_S2S3")), contingencies.get(0));
        assertEquals(new Contingency("LINE_S3S4", new LineContingency("LINE_S3S4")), contingencies.get(1));

        // test not found elements in network
        Map<String, Set<String>> notFoundElements = contingencyList.getNotFoundElements(network);
        assertEquals(2, notFoundElements.size());
        assertEquals(Set.of("test"), notFoundElements.get("test"));
        assertEquals(Set.of("LINE_S4S1"), notFoundElements.get("LINE_S4S1"));
    }

    @Test
    void testSimpleIdentifierWithSeveralElements() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        List<NetworkElementIdentifier> subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("LINE_S2S3"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("LINE_S3S4"));
        networkElementIdentifierList.add(new NetworkElementIdentifierContingencyList(subNetworkElementIdentifiers, "2-same-elements-contingency"));
        subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("LINE_S2S3"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("GH1"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("HVDC1"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("LD4"));
        networkElementIdentifierList.add(new NetworkElementIdentifierContingencyList(subNetworkElementIdentifiers, "4-different-elements-contingency"));
        subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("LINE_S3S4"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("Unknown"));
        networkElementIdentifierList.add(new NetworkElementIdentifierContingencyList(subNetworkElementIdentifiers, "test-one-unexpected-element"));
        subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("test"));
        networkElementIdentifierList.add(new NetworkElementIdentifierContingencyList(subNetworkElementIdentifiers, "test"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(3, contingencies.size());
        assertEquals(new Contingency("2-same-elements-contingency", new LineContingency("LINE_S2S3"),
                new LineContingency("LINE_S3S4")), contingencies.get(0));
        assertEquals(new Contingency("4-different-elements-contingency",
                new LineContingency("LINE_S2S3"),
                new GeneratorContingency("GH1"),
                new HvdcLineContingency("HVDC1"),
                new LoadContingency("LD4")), contingencies.get(1));
        assertEquals(new Contingency("test-one-unexpected-element", new LineContingency("LINE_S3S4")), contingencies.get(2));

        // test not found elements in network
        Map<String, Set<String>> notFoundElements = contingencyList.getNotFoundElements(network);
        assertEquals(2, notFoundElements.size());
        assertEquals(Set.of("test"), notFoundElements.get("test"));
        assertEquals(Set.of("Unknown"), notFoundElements.get("test-one-unexpected-element"));
    }

    @Test
    void testUcteIdentifier() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        networkElementIdentifierList.add(new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1', "contingencyId"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("contingencyId", new LineContingency("NHV1_NHV2_1")), contingencies.get(0));

        // test not found elements in network
        Map<String, Set<String>> notFoundElements = contingencyList.getNotFoundElements(network);
        assertEquals(0, notFoundElements.size());
    }

    @Test
    void testIdentifierList() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierList = new ArrayList<>();
        List<NetworkElementIdentifier> networkElementIdentifierListElements = new ArrayList<>();
        networkElementIdentifierListElements.add(new IdBasedNetworkElementIdentifier("test", "test"));
        networkElementIdentifierListElements.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2", "NHV1_NHV2"));
        networkElementIdentifierListElements.add(new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '2', "contingencyId"));
        networkElementIdentifierListElements.add(new VoltageLevelAndOrderNetworkElementIdentifier("VLHV1", "VLHV2", '1', "contingencyId"));
        networkElementIdentifierList.add(new NetworkElementIdentifierContingencyList(networkElementIdentifierListElements, "contingency"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierList);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        assertEquals(new Contingency("contingency", Arrays.asList(new LineContingency("NHV1_NHV2_2"), new LineContingency("NHV1_NHV2_1"))), contingencies.get(0));

        // test not found elements in network
        Map<String, Set<String>> notFoundElements = contingencyList.getNotFoundElements(network);
        assertEquals(1, notFoundElements.size());
        assertEquals(Set.of("test", "NHV1_NHV2"), notFoundElements.get("contingency"));
    }

    @Test
    void testIdentifierListOfIdBasedWithSeveralIds() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierListElements = new ArrayList<>();
        List<NetworkElementIdentifier> subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_1"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_2"));
        List<NetworkElementIdentifier> subNetworkElementIdentifiersBis = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_1"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("GEN"));
        networkElementIdentifierListElements.add(
                new NetworkElementIdentifierContingencyList(List.of(
                        new NetworkElementIdentifierContingencyList(
                                subNetworkElementIdentifiers
                        ),
                        new NetworkElementIdentifierContingencyList(
                                subNetworkElementIdentifiersBis
                        )
                ), "contingencyId1")
        );
        subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_2"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("GEN"));
        networkElementIdentifierListElements.add(
                new NetworkElementIdentifierContingencyList(List.of(
                        new NetworkElementIdentifierContingencyList(List.of(
                                new NetworkElementIdentifierContingencyList(
                                        subNetworkElementIdentifiers
                                )))
                ), "contingencyId2")
        );
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierListElements);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals(new Contingency("contingencyId1", List.of(new LineContingency("NHV1_NHV2_1"),
                new LineContingency("NHV1_NHV2_2"), new GeneratorContingency("GEN"))), contingencies.get(0));
        assertEquals(new Contingency("contingencyId2", List.of(new LineContingency("NHV1_NHV2_2"),
                new GeneratorContingency("GEN"))), contingencies.get(1));

        // test not found elements in network
        Map<String, Set<String>> notFoundElements = contingencyList.getNotFoundElements(network);
        assertEquals(0, notFoundElements.size());
    }

    @Test
    void testIdentifierListOfDefaultIds() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierListElements = new ArrayList<>();
        List<NetworkElementIdentifier> subNetworkElementIdentifiers = new ArrayList<>();
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_2"));
        subNetworkElementIdentifiers.add(new IdBasedNetworkElementIdentifier("GEN"));
        networkElementIdentifierListElements.add(new NetworkElementIdentifierContingencyList(subNetworkElementIdentifiers));
        networkElementIdentifierListElements.add(new IdBasedNetworkElementIdentifier("NHV1_NHV2_1"));
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierListElements);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(2, contingencies.size());
        assertEquals("Contingency : NHV1_NHV2_2 + GEN", contingencies.get(0).getId());
        assertEquals("Contingency : NHV1_NHV2_1", contingencies.get(1).getId());

        // test not found elements in network
        Map<String, Set<String>> notFoundElements = contingencyList.getNotFoundElements(network);
        assertEquals(0, notFoundElements.size());
    }

    @Test
    void testUnknownCharacterIdentifierWithList() {
        Network network = EurostagTutorialExample1Factory.create();
        List<NetworkElementIdentifier> networkElementIdentifierListElements = new ArrayList<>();
        NetworkElementIdentifier elementIdentifier = new ElementWithUnknownCharacterIdentifier("NHV1_NHV2_?");
        networkElementIdentifierListElements.add(elementIdentifier);
        IdentifierContingencyList contingencyList = new IdentifierContingencyList("list", networkElementIdentifierListElements);
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());
        List<String> elementIds = contingencies.get(0).getElements().stream().map(ContingencyElement::getId).toList();
        assertTrue(elementIds.containsAll(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2")));
    }

    @Test
    void testUnknownCharacterIdentifier() {
        String message = Assertions.assertThrows(PowsyblException.class, () -> new ElementWithUnknownCharacterIdentifier("NHV1_NHV2_?_?_?_?_?_?_?")).getMessage();
        Assertions.assertEquals("there can be maximum 5 \'?\'", message);
        String message2 = Assertions.assertThrows(PowsyblException.class, () -> new ElementWithUnknownCharacterIdentifier("NHV1_NHV2_ç")).getMessage();
        Assertions.assertEquals("Only characters allowed for this identifier are letters, numbers, \'_\', \'?\', \'.\' and \'-\'", message2);
        NetworkElementIdentifier elementIdentifier = new ElementWithUnknownCharacterIdentifier("NHV1_NHV?_?");
        Network network = EurostagTutorialExample1Factory.create();
        List<String> identifiables = elementIdentifier.filterIdentifiable(network).stream().map(Identifiable::getId).toList();
        Assertions.assertEquals(2, identifiables.size());
        assertTrue(identifiables.containsAll(Arrays.asList("NHV1_NHV2_1", "NHV1_NHV2_2")));
    }
}

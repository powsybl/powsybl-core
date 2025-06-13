/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.modification.NetworkModificationImpact;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.BusbarSectionPositionAdder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Franck Lecuyer {@literal <franck.lecuyer_externe at rte-france.com>}
 */
class CreateVoltageLevelSectionsTest extends AbstractModificationTest {

    private void createBusbarSection(VoltageLevel vl, String id, int node, int busbarIndex, int sectionIndex) {
        BusbarSection bbs = vl.getNodeBreakerView()
            .newBusbarSection()
            .setId(id)
            .setNode(node)
            .add();
        if (busbarIndex != -1 && sectionIndex != -1) {
            bbs.newExtension(BusbarSectionPositionAdder.class)
                .withBusbarIndex(busbarIndex)
                .withSectionIndex(sectionIndex)
                .add();
        }
    }

    private void createDisconnector(VoltageLevel vl, String id, int node1, int node2) {
        vl.getNodeBreakerView().newDisconnector()
            .setId(id)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(false)
            .add();
    }

    private void createBreaker(VoltageLevel vl, String id, int node1, int node2) {
        vl.getNodeBreakerView().newBreaker()
            .setId(id)
            .setNode1(node1)
            .setNode2(node2)
            .setOpen(false)
            .add();
    }

    private void createGenerator(VoltageLevel vl, String id, int node) {
        vl.newGenerator()
            .setId(id)
            .setNode(node)
            .setMaxP(100)
            .setMinP(0)
            .setTargetP(100)
            .setTargetV(400)
            .setVoltageRegulatorOn(true)
            .add();
    }

    private Network createNetwork() {
        // Initialisation
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2023-12-13T10:05:55.570Z"));

        // Substations
        Substation s1 = network.newSubstation()
            .setId("S1")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
            .setId("VL1")
            .setNominalV(1.0)
            .setTopologyKind(TopologyKind.NODE_BREAKER)
            .add();

        // Busbar sections
        createBusbarSection(vl1, "BBS11", 0, 1, 1);
        createBusbarSection(vl1, "BBS21", 1, 2, 1);
        createBusbarSection(vl1, "BBS31", 2, 3, 1);
        createBusbarSection(vl1, "BBS12", 3, 1, 2);
        createBusbarSection(vl1, "BBS22", 4, 2, 2);
        createBusbarSection(vl1, "BBS32", 5, 3, 2);
        createBusbarSection(vl1, "BBS13", 6, 1, 3);
        createBusbarSection(vl1, "BBS23", 7, 2, 3);
        createBusbarSection(vl1, "BBS33", 8, 3, 3);

        // Disconnectors and/or disconnectors/breakers/disconnectors for coupling between busbar sections
        createDisconnector(vl1, "D_BBS11_BBS12", 0, 3);
        createDisconnector(vl1, "D_BBS12_BBS13_1", 3, 20);
        createBreaker(vl1, "B_BBS12_BBS13", 20, 21);
        createDisconnector(vl1, "D_BBS12_BBS13_2", 21, 6);

        createDisconnector(vl1, "D_BBS21_BBS22", 1, 4);
        createDisconnector(vl1, "D_BBS22_BBS23_1", 4, 22);
        createBreaker(vl1, "B_BBS22_BBS23", 22, 23);
        createDisconnector(vl1, "D_BBS22_BBS23_2", 23, 7);

        createDisconnector(vl1, "D_BBS31_BBS32", 2, 5);
        createDisconnector(vl1, "D_BBS32_BBS33_1", 5, 24);
        createBreaker(vl1, "B_BBS32_BBS33", 24, 25);
        createDisconnector(vl1, "D_BBS32_BBS33_2", 25, 8);

        // Add some equipments connected to the busbar sections
        createDisconnector(vl1, "D_11_GEN_1", 0, 30);
        createDisconnector(vl1, "D_21_GEN_1", 1, 30);
        createDisconnector(vl1, "D_31_GEN_1", 2, 30);
        createBreaker(vl1, "B_GEN_1", 30, 31);
        createGenerator(vl1, "GEN_1", 31);

        createDisconnector(vl1, "D_12_GEN_2", 3, 40);
        createDisconnector(vl1, "D_22_GEN_2", 4, 40);
        createDisconnector(vl1, "D_32_GEN_2", 5, 40);
        createBreaker(vl1, "B_GEN_2", 40, 41);
        createGenerator(vl1, "GEN_2", 41);

        createDisconnector(vl1, "D_13_GEN_3", 6, 50);
        createDisconnector(vl1, "D_23_GEN_3", 7, 50);
        createDisconnector(vl1, "D_33_GEN_3", 8, 50);
        createBreaker(vl1, "B_GEN_3", 50, 51);
        createGenerator(vl1, "GEN_3", 51);

        return network;
    }

    @ParameterizedTest
    @MethodSource("parametersOK")
    void testInsertBetweenTwoSectionsWithDisconnectors(String referenceBusbarSectionId, boolean createTheBusbarSectionsAfterTheReferenceBusbarSection, boolean allBusbars,
                                                       SwitchKind leftSwitchKind, boolean leftSwitchFictitious,
                                                       SwitchKind rightSwitchKind, boolean rightSwitchFictitious,
                                                       String resourceFile) throws IOException {
        // Network creation
        Network network = createNetwork();

        // Network modification
        CreateVoltageLevelSections modification = new CreateVoltageLevelSectionsBuilder()
            .withReferenceBusbarSectionId(referenceBusbarSectionId)
            .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(createTheBusbarSectionsAfterTheReferenceBusbarSection)
            .withAllBusbars(allBusbars)
            .withLeftSwitchKind(leftSwitchKind)
            .withLeftSwitchFictitious(leftSwitchFictitious)
            .withRightSwitchKind(rightSwitchKind)
            .withRightSwitchFictitious(rightSwitchFictitious)
            .build();
        modification.apply(network);
        writeXmlTest(network, resourceFile);
    }

    private static Stream<Arguments> parametersOK() {
        return Stream.of(
            Arguments.of("BBS12", true, true, SwitchKind.DISCONNECTOR, false, SwitchKind.DISCONNECTOR, false, "/create-vl-sections-insert-between-2-sections-with-disconnectors.xiidm"),
            Arguments.of("BBS11", true, true, SwitchKind.BREAKER, false, SwitchKind.BREAKER, false, "/create-vl-sections-insert-between-2-sections-with-breakers.xiidm"),
            Arguments.of("BBS11", true, true, SwitchKind.DISCONNECTOR, false, SwitchKind.BREAKER, false, "/create-vl-sections-insert-between-2-sections-with-disconnectors-on-left-side-and-breakers-on-right-side.xiidm"),
            Arguments.of("BBS12", true, false, SwitchKind.DISCONNECTOR, false, SwitchKind.DISCONNECTOR, false, "/create-vl-sections-insert-between-2-sections-on-only-one-busbar-with-disconnectors.xiidm"),
            Arguments.of("BBS21", false, true, SwitchKind.BREAKER, false, SwitchKind.BREAKER, false, "/create-vl-sections-insert-before-first-section-with-breakers.xiidm"),
            Arguments.of("BBS13", true, true, SwitchKind.DISCONNECTOR, false, SwitchKind.DISCONNECTOR, false, "/create-vl-sections-insert-after-last-section-with-disconnectors.xiidm")
        );
    }

    @ParameterizedTest
    @MethodSource("parametersNOK")
    void testWithException(String referenceBusbarSectionId, boolean createTheBusbarSectionsAfterTheReferenceBusbarSection, boolean allBusbars,
                           SwitchKind leftSwitchKind, boolean leftSwitchFictitious,
                           SwitchKind rightSwitchKind, boolean rightSwitchFictitious,
                           boolean withMissingBbsPosition, String exceptionMessage) {
        // Network creation
        Network network = createNetwork();
        if (withMissingBbsPosition) {
            VoltageLevel vl1 = network.getVoltageLevel("VL1");
            createBusbarSection(vl1, "BBS41", 60, -1, -1);
        }

        // Network modification
        CreateVoltageLevelSections modification = new CreateVoltageLevelSectionsBuilder()
            .withReferenceBusbarSectionId(referenceBusbarSectionId)
            .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(createTheBusbarSectionsAfterTheReferenceBusbarSection)
            .withAllBusbars(allBusbars)
            .withLeftSwitchKind(leftSwitchKind)
            .withLeftSwitchFictitious(leftSwitchFictitious)
            .withRightSwitchKind(rightSwitchKind)
            .withRightSwitchFictitious(rightSwitchFictitious)
            .build();
        PowsyblException exception = assertThrows(PowsyblException.class, () -> modification.apply(network, true, ReportNode.NO_OP));
        assertEquals(exceptionMessage, exception.getMessage());
    }

    private static Stream<Arguments> parametersNOK() {
        return Stream.of(
            Arguments.of("unknown_bbs", true, true, SwitchKind.DISCONNECTOR, false, SwitchKind.DISCONNECTOR, false, false, "Busbar section unknown_bbs not found"),
            Arguments.of("GEN_1", true, true, SwitchKind.DISCONNECTOR, false, SwitchKind.DISCONNECTOR, false, false, "Busbar section GEN_1 not found"),
            Arguments.of("BBS12", true, true, SwitchKind.DISCONNECTOR, false, SwitchKind.DISCONNECTOR, false, true, "Some busbar sections have no position in voltage level (VL1)")
        );
    }

    @Test
    void testImpactOnNetwork() {
        // Network creation
        Network network = createNetwork();

        // Network modification
        CreateVoltageLevelSections modification1 = new CreateVoltageLevelSectionsBuilder()
            .withReferenceBusbarSectionId("BBS11")
            .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(true)
            .withAllBusbars(true)
            .withLeftSwitchKind(SwitchKind.DISCONNECTOR)
            .withLeftSwitchFictitious(false)
            .withRightSwitchKind(SwitchKind.DISCONNECTOR)
            .withRightSwitchFictitious(false)
            .build();
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification1.hasImpactOnNetwork(network));

        CreateVoltageLevelSections modification2 = new CreateVoltageLevelSectionsBuilder()
            .withReferenceBusbarSectionId("BBS1")
            .withCreateTheBusbarSectionsAfterTheReferenceBusbarSection(true)
            .withAllBusbars(true)
            .withLeftSwitchKind(SwitchKind.DISCONNECTOR)
            .withLeftSwitchFictitious(false)
            .withRightSwitchKind(SwitchKind.DISCONNECTOR)
            .withRightSwitchFictitious(false)
            .build();
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification2.hasImpactOnNetwork(network));
    }
}

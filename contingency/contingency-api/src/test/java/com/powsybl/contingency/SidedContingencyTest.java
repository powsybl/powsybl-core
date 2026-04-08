/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.powsybl.iidm.network.test.EurostagTutorialExample1Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SidedContingencyTest {

    private static Network eurostagNetwork;

    @BeforeAll
    public static void setup() {
        eurostagNetwork = EurostagTutorialExample1Factory.create();
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("sidedElementProvider")
    void testSideGetter(Network network, SidedContingencyElement element, TwoSides expectedSide) {
        assertEquals(expectedSide, SidedContingencyElement.getContingencySide(network, element));
    }

    @Test
    void testIdNotFound() {
        SidedContingencyElement element = new BranchContingency("WRONG_ID", VLHV2);
        assertNull(SidedContingencyElement.getContingencySide(eurostagNetwork, element));
    }

    @Test
    void testVoltageIdNotFound() {
        SidedContingencyElement element = new BranchContingency(NHV1_NHV2_2, "WRONG_ID");
        assertNull(SidedContingencyElement.getContingencySide(eurostagNetwork, element));
    }

    @Test
    void testNullVoltageId() {
        SidedContingencyElement element = new BranchContingency(NHV1_NHV2_2);
        assertNull(SidedContingencyElement.getContingencySide(eurostagNetwork, element));
    }

    private static Stream<Arguments> sidedElementProvider() {
        return Stream.of(
                Arguments.of(eurostagNetwork,
                        new TwoWindingsTransformerContingency(NGEN_NHV1, VLGEN),
                        TwoSides.ONE),
                Arguments.of(eurostagNetwork,
                        new LineContingency(NHV1_NHV2_1, VLHV1),
                        TwoSides.ONE),
                Arguments.of(eurostagNetwork,
                        new BranchContingency(NHV1_NHV2_2, VLHV2),
                        TwoSides.TWO),
                Arguments.of(EurostagTutorialExample1Factory.createWithTieLine(),
                        new TieLineContingency(NHV1_NHV2_1, VLHV2),
                        TwoSides.TWO),
                Arguments.of(HvdcTestNetwork.createVsc(),
                        new HvdcLineContingency("L", "VL2"),
                        TwoSides.TWO)
        );
    }
}

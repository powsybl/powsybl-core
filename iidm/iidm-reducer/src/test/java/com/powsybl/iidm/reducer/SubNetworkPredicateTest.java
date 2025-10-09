/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubNetworkPredicateTest {

    @Test
    void testEsgTuto() {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkPredicate predicate = new SubNetworkPredicate(network.getVoltageLevel("VLHV1"), 1);
        assertEquals(List.of("P1", "P2"), network.getSubstationStream().filter(predicate::test).map(Identifiable::getId).toList());
        assertEquals(List.of("VLGEN", "VLHV1", "VLHV2"), network.getVoltageLevelStream().filter(predicate::test).map(Identifiable::getId).toList());
    }

    @Test
    void test3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        NetworkPredicate predicate = new SubNetworkPredicate(network.getVoltageLevel("VL_132"), 1);
        assertEquals(List.of("SUBSTATION"), network.getSubstationStream().filter(predicate::test).map(Identifiable::getId).toList());
        assertEquals(List.of("VL_132", "VL_33", "VL_11"), network.getVoltageLevelStream().filter(predicate::test).map(Identifiable::getId).toList());
    }

    @Test
    void testFourSubstations() {
        Network network = FourSubstationsNodeBreakerFactory.create();
        NetworkPredicate predicate0 = new SubNetworkPredicate(network.getVoltageLevel("S1VL1"), 0);
        assertEquals(List.of("S1"), network.getSubstationStream().filter(predicate0::test).map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL1"), network.getVoltageLevelStream().filter(predicate0::test).map(Identifiable::getId).toList());

        NetworkPredicate predicate1 = new SubNetworkPredicate(network.getVoltageLevel("S1VL1"), 1);
        assertEquals(List.of("S1"), network.getSubstationStream().filter(predicate1::test).map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL1", "S1VL2"), network.getVoltageLevelStream().filter(predicate1::test).map(Identifiable::getId).toList());

        NetworkPredicate predicate2 = new SubNetworkPredicate(network.getVoltageLevel("S1VL1"), 2);
        assertEquals(List.of("S1", "S2", "S3"), network.getSubstationStream().filter(predicate2::test).map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL1", "S1VL2", "S2VL1", "S3VL1"), network.getVoltageLevelStream().filter(predicate2::test).map(Identifiable::getId).toList());

        NetworkPredicate predicate3 = new SubNetworkPredicate(network.getVoltageLevel("S1VL1"), 3);
        assertEquals(List.of("S1", "S2", "S3", "S4"), network.getSubstationStream().filter(predicate3::test).map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL1", "S1VL2", "S2VL1", "S3VL1", "S4VL1"), network.getVoltageLevelStream().filter(predicate3::test).map(Identifiable::getId).toList());

        NetworkPredicate predicate150 = new SubNetworkPredicate(network.getVoltageLevel("S1VL1"), 150);
        assertEquals(List.of("S1", "S2", "S3", "S4"), network.getSubstationStream().filter(predicate150::test).map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL1", "S1VL2", "S2VL1", "S3VL1", "S4VL1"), network.getVoltageLevelStream().filter(predicate150::test).map(Identifiable::getId).toList());
    }

    @Test
    void shouldThrowInvalidMaxDepth() {
        Network network = EurostagTutorialExample1Factory.create();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new SubNetworkPredicate(network.getVoltageLevel("VLHV1"), -2));
        assertTrue(e.getMessage().contains("Invalid max depth value: -2"));
    }
}

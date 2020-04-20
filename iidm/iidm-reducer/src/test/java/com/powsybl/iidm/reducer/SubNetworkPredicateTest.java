/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubNetworkPredicateTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testEsgTuto() {
        Network network = EurostagTutorialExample1Factory.create();
        NetworkPredicate predicate = new SubNetworkPredicate(network.getVoltageLevel("VLHV1"), 1);
        assertEquals(Arrays.asList("P1", "P2"), network.getSubstationStream().filter(predicate::test).map(Identifiable::getId).collect(Collectors.toList()));
        assertEquals(Arrays.asList("VLGEN", "VLHV1", "VLHV2"), network.getVoltageLevelStream().filter(predicate::test).map(Identifiable::getId).collect(Collectors.toList()));
    }

    @Test
    public void test3wt() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        NetworkPredicate predicate = new SubNetworkPredicate(network.getVoltageLevel("VL_132"), 1);
        assertEquals(Collections.singletonList("SUBSTATION"), network.getSubstationStream().filter(predicate::test).map(Identifiable::getId).collect(Collectors.toList()));
        assertEquals(Arrays.asList("VL_132", "VL_33", "VL_11"), network.getVoltageLevelStream().filter(predicate::test).map(Identifiable::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldThrowInvalidMaxDepth() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid max depth value: -2");
        Network network = EurostagTutorialExample1Factory.create();
        new SubNetworkPredicate(network.getVoltageLevel("VLHV1"), -2);
    }
}

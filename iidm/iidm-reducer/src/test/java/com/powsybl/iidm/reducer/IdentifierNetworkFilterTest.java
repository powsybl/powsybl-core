/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class IdentifierNetworkFilterTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();

        NetworkFilter filter = new IdentifierNetworkFilter(Collections.singleton("P2"));
        assertFalse(filter.accept(network.getSubstation("P1")));
        assertFalse(filter.accept(network.getVoltageLevel("VLGEN")));
        assertFalse(filter.accept(network.getVoltageLevel("VLHV1")));
        assertFalse(filter.accept(network.getTwoWindingsTransformer("NGEN_NHV1")));

        assertTrue(filter.accept(network.getSubstation("P2")));
        assertTrue(filter.accept(network.getVoltageLevel("VLLOAD")));
        assertTrue(filter.accept(network.getVoltageLevel("VLHV2")));
        assertTrue(filter.accept(network.getTwoWindingsTransformer("NHV2_NLOAD")));

        assertFalse(filter.accept(network.getLine("NHV1_NHV2_1")));
        assertFalse(filter.accept(network.getLine("NHV1_NHV2_2")));
    }

    @Test
    public void testHvdc() {
        Network network = HvdcTestNetwork.createLcc();

        NetworkFilter filter1 = new IdentifierNetworkFilter(Arrays.asList("VL1", "VL2"));
        NetworkFilter filter2 = new IdentifierNetworkFilter(Collections.singleton("VL1"));

        assertTrue(filter1.accept(network.getHvdcLine("L")));
        assertFalse(filter2.accept(network.getHvdcLine("L")));
    }

    @Test
    public void test3WT() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();

        NetworkFilter filter1 = new IdentifierNetworkFilter(Arrays.asList("VL_132", "VL_33", "VL_11"));
        NetworkFilter filter2 = new IdentifierNetworkFilter(Arrays.asList("VL_132", "VL_33"));

        assertTrue(filter1.accept(network.getThreeWindingsTransformer("3WT")));
        assertFalse(filter2.accept(network.getThreeWindingsTransformer("3WT")));
    }
}

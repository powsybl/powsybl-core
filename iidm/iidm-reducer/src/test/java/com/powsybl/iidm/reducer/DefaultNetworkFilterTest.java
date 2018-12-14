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

import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultNetworkFilterTest {

    private final NetworkFilter filter = new DefaultNetworkFilter();

    @Test
    public void testDefault() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getSubstationStream().allMatch(filter::accept));
        assertTrue(network.getVoltageLevelStream().allMatch(filter::accept));
        assertTrue(network.getLineStream().allMatch(filter::accept));
        assertTrue(network.getTwoWindingsTransformerStream().allMatch(filter::accept));

    }

    @Test
    public void testHvdc() {
        Network network = HvdcTestNetwork.createVsc();
        assertTrue(network.getHvdcLineStream().allMatch(filter::accept));
    }

    @Test
    public void test3WT() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertTrue(network.getThreeWindingsTransformerStream().allMatch(filter::accept));
    }
}

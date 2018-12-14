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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class NominalVoltageNetworkFilterTest {

    @Test
    public void testVHV() {
        NetworkFilter filter = new NominalVoltageNetworkFilter(200.0, 400.0);

        Network network = EurostagTutorialExample1Factory.create();
        assertEquals(2, network.getSubstationStream().filter(filter::accept).count());
        assertEquals(2, network.getVoltageLevelStream().filter(filter::accept).count());
        assertEquals(2, network.getLineStream().filter(filter::accept).count());
        assertEquals(0, network.getTwoWindingsTransformerStream().filter(filter::accept).count());
    }

    @Test
    public void testHvdc() {
        NetworkFilter filter1 = new NominalVoltageNetworkFilter(380.0, 420.0);
        NetworkFilter filter2 = new NominalVoltageNetworkFilter(0.0, 100.0);

        Network network = HvdcTestNetwork.createLcc();
        assertTrue(filter1.accept(network.getHvdcLine("L")));
        assertFalse(filter2.accept(network.getHvdcLine("L")));

        network = HvdcTestNetwork.createVsc();
        assertTrue(filter1.accept(network.getHvdcLine("L")));
        assertFalse(filter2.accept(network.getHvdcLine("L")));
    }

    @Test
    public void test3WT() {
        NetworkFilter filter1 = new NominalVoltageNetworkFilter(0.0, 400.0);
        NetworkFilter filter2 = new NominalVoltageNetworkFilter(120.0, 400.0);

        Network network = ThreeWindingsTransformerNetworkFactory.create();
        assertTrue(filter1.accept(network.getThreeWindingsTransformer("3WT")));
        assertFalse(filter2.accept(network.getThreeWindingsTransformer("3WT")));
    }
}

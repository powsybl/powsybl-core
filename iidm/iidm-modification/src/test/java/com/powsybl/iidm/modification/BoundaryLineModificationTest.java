/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
class BoundaryLineModificationTest {

    private Network network;
    private BoundaryLine boundaryLine;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithTieLine();
        boundaryLine = network.getDanglingLine("NHV1_XNODE1");
        boundaryLine.setP0(10.0);
        boundaryLine.setQ0(4.0);
    }

    @Test
    void modifyP0() {
        assertEquals(10.0, boundaryLine.getP0());
        BoundaryLineModification modification = new BoundaryLineModification("NHV1_XNODE1", false, 5.0, null);
        modification.apply(network);
        assertEquals(5.0, boundaryLine.getP0());
    }

    @Test
    void modifyQ0Relatively() {
        assertEquals(4.0, boundaryLine.getQ0());
        BoundaryLineModification modification = new BoundaryLineModification("NHV1_XNODE1", true, null, 2.0);
        modification.apply(network);
        assertEquals(6.0, boundaryLine.getQ0());
    }

    @Test
    void testGetName() {
        AbstractNetworkModification networkModification = new BoundaryLineModification("ID", 10., 10.);
        assertEquals("DanglingLineModification", networkModification.getName());
    }

    @Test
    void testHasImpact() {
        BoundaryLineModification modification1 = new BoundaryLineModification("DL_NOT_EXISTING", true, null, 2.0);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        BoundaryLineModification modification2 = new BoundaryLineModification("NHV1_XNODE1", true, null, 2.0);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        BoundaryLineModification modification3 = new BoundaryLineModification("NHV1_XNODE1", true, 5.0, null);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification3.hasImpactOnNetwork(network));

        BoundaryLineModification modification4 = new BoundaryLineModification("NHV1_XNODE1", true, null, null);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        BoundaryLineModification modification5 = new BoundaryLineModification("NHV1_XNODE1", false, 10.0, 4.0);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));

        BoundaryLineModification modification6 = new BoundaryLineModification("NHV1_XNODE1", true, 10.0, 4.0);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification6.hasImpactOnNetwork(network));
    }
}

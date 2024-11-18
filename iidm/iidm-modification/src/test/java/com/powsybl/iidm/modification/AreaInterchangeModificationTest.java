/*
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
class AreaInterchangeModificationTest {

    private Network network;
    private Area area;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.createWithTieLinesAndAreas();
        assertTrue(network.getAreaCount() > 0);
        area = network.getArea("ControlArea_A");
    }

    @Test
    void testModification() {
        AreaInterchangeTargetModification modification = new AreaInterchangeTargetModification(area.getId(), -750);
        assertTrue(area.getInterchangeTarget().isPresent());
        assertEquals(-602.6, area.getInterchangeTarget().getAsDouble());
        modification.apply(network);
        assertEquals(-750, area.getInterchangeTarget().getAsDouble());

        AreaInterchangeTargetModification modification2 = new AreaInterchangeTargetModification(area.getId(), Double.NaN);
        modification2.apply(network);
        assertTrue(area.getInterchangeTarget().isEmpty());

        AreaInterchangeTargetModification modification3 = new AreaInterchangeTargetModification("AREA_UNKNOWN", 2.0);
        PowsyblException exception = assertThrows(PowsyblException.class, () -> modification3.apply(network, true, ReportNode.NO_OP));
        assertEquals("Area 'AREA_UNKNOWN' not found", exception.getMessage());
    }

    @Test
    void testModificationGetters() {
        AreaInterchangeTargetModification modification = new AreaInterchangeTargetModification(area.getId(), 1.0);
        assertEquals(area.getId(), modification.getAreaId());
        assertEquals(1.0, modification.getInterchangeTarget());
    }

    @Test
    void testHasImpact() {
        AreaInterchangeTargetModification modification1 = new AreaInterchangeTargetModification("AREA_NOT_EXISTING", 2.0);
        assertEquals(NetworkModificationImpact.CANNOT_BE_APPLIED, modification1.hasImpactOnNetwork(network));

        AreaInterchangeTargetModification modification2 = new AreaInterchangeTargetModification(area.getId(), 1.0);
        assertEquals(NetworkModificationImpact.HAS_IMPACT_ON_NETWORK, modification2.hasImpactOnNetwork(network));

        AreaInterchangeTargetModification modification4 = new AreaInterchangeTargetModification(area.getId(), -602.6);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification4.hasImpactOnNetwork(network));

        area.setInterchangeTarget(Double.NaN);
        AreaInterchangeTargetModification modification5 = new AreaInterchangeTargetModification(area.getId(), Double.NaN);
        assertEquals(NetworkModificationImpact.NO_IMPACT_ON_NETWORK, modification5.hasImpactOnNetwork(network));
    }
}

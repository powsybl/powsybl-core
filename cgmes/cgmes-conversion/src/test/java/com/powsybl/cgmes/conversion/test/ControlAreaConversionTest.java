/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Clement Philipot {@literal <clement.philipot at rte-france.com>}
 */

class ControlAreaConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/control-areas/";

    @Test
    void controlAreaWithTieFlows() {
        Network network = readCgmesResources(DIR, "control_area_with_tie_flows_EQ.xml", "control_area_with_tie_flows_SSH.xml");

        assertEquals(1, network.getAreaCount());
        Area area = network.getArea("CONTROL_AREA_BE");
        assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area.getAreaType());
        assertEquals("Belgium", area.getNameOrId());
        assertEquals("10BE------1", area.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).orElseThrow());
        assertEquals(-205.90011555672567, area.getInterchangeTarget().getAsDouble(), 0.0);
        assertEquals(5, area.getAreaBoundaryStream().count());
    }

    @Test
    void tieFlowWithoutControlArea() {
        Network network = readCgmesResources(DIR, "tie_flow_without_control_area_EQ.xml");
        assertEquals(0, network.getAreaCount());
    }
}

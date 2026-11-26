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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Clement Philipot {@literal <clement.philipot at rte-france.com>}
 */

class ControlAreaConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/control-areas/";

    @Test
    void validControlAreaTest() {
        // CGMES network:
        //   A ControlArea of type Interchange with 2 TieFlows
        // IIDM network:
        //   All EQ and SSH fields have been correctly read.
        Network network = readCgmesResources(DIR, "valid_control_area_with_tie_flows_EQ.xml", "valid_control_area_with_tie_flows_SSH.xml");

        // Check the ControlArea EQ data is correctly read
        assertEquals(1, network.getAreaCount());
        Area area = network.getArea("CONTROL_AREA_BE");
        assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area.getAreaType());
        assertEquals("Belgium", area.getNameOrId());
        assertEquals("10BE------1", area.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).orElseThrow());
        assertEquals(2, area.getAreaBoundaryStream().count());

        // Check the ControlArea SSH data is correctly read
        assertTrue(area.getInterchangeTarget().isPresent());
        assertEquals(-205.0, area.getInterchangeTarget().getAsDouble(), 0.0);
    }

    @Test
    void tieFlowWithoutControlArea() {
        Network network = readCgmesResources(DIR, "tie_flow_without_control_area_EQ.xml");
        assertEquals(0, network.getAreaCount());
    }
}

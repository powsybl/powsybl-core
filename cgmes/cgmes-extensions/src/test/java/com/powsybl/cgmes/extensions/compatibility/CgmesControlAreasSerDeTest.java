/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions.compatibility;

import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaBoundary;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class CgmesControlAreasSerDeTest {

    private static final double EPSILON = 0.0001;

    @Test
    void readingCompatibilityTest() {
        Network network = Network.read("eurostag_cgmes_control_area.xml",
                getClass().getResourceAsStream("/eurostag_cgmes_control_area.xml"));
        assertEquals(3, network.getAreaCount());
        Area area1 = network.getArea("cgmesControlAreaId");
        assertNotNull(area1);
        assertAll(
                () -> assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area1.getAreaType()),
                () -> assertEquals("cgmesControlAreaName", area1.getNameOrId()),
                () -> assertEquals(100.0, area1.getInterchangeTarget().orElse(Double.NaN), EPSILON),
                () -> assertNull(area1.getProperty(CgmesNames.P_TOLERANCE)),
                () -> assertEquals("energyIdentCodeEic", area1.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).orElse("")),
                () -> assertEquals(2, area1.getAreaBoundaryStream().count())
        );
        Iterator<AreaBoundary> boundaryIterator = area1.getAreaBoundaries().iterator();
        AreaBoundary b1 = boundaryIterator.next();
        AreaBoundary b2 = boundaryIterator.next();
        AreaBoundary boundaryOnTerminal;
        AreaBoundary boundaryOnBoundary;
        if (b1.getTerminal().isPresent()) {
            boundaryOnTerminal = b1;
            boundaryOnBoundary = b2;
        } else {
            boundaryOnBoundary = b1;
            boundaryOnTerminal = b2;
        }
        assertAll(
                () -> assertTrue(boundaryOnTerminal.isAc()),
                () -> assertEquals("NHV1_NHV2_1", boundaryOnTerminal.getTerminal().orElseThrow().getConnectable().getId()),
                () -> assertEquals(ThreeSides.ONE, boundaryOnTerminal.getTerminal().orElseThrow().getSide()),
                () -> assertFalse(boundaryOnTerminal.getBoundary().isPresent()),
                () -> assertTrue(boundaryOnBoundary.isAc()),
                () -> assertFalse(boundaryOnBoundary.getTerminal().isPresent()),
                () -> assertEquals("DL", boundaryOnBoundary.getBoundary().orElseThrow().getDanglingLine().getId())
        );

        Area area2 = network.getArea("areaId-2");
        assertNotNull(area2);
        assertAll(
                () -> assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area2.getAreaType()),
                () -> assertEquals("area-2", area2.getNameOrId()),
                () -> assertEquals(90.0, area2.getInterchangeTarget().orElse(Double.NaN), EPSILON),
                () -> assertEquals(0.024, Double.parseDouble(area2.getProperty(CgmesNames.P_TOLERANCE, "NaN")), EPSILON),
                () -> assertFalse(area2.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).isPresent()),
                () -> assertEquals(1, area2.getAreaBoundaryStream().count())
        );
        AreaBoundary b = area2.getAreaBoundaries().iterator().next();
        assertAll(
                () -> assertTrue(b.isAc()),
                () -> assertEquals("NHV1_NHV2_2", b.getTerminal().orElseThrow().getConnectable().getId()),
                () -> assertEquals(ThreeSides.TWO, b.getTerminal().orElseThrow().getSide()),
                () -> assertFalse(b.getBoundary().isPresent())
        );

        // When an Area with the same ID already exists, the CgmesControlArea is not imported (and the Area is not updated)
        Area area0 = network.getArea("alreadyExistingArea");
        assertNotNull(area2);
        assertAll(
                () -> assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area0.getAreaType()),
                () -> assertEquals("area", area0.getNameOrId()),
                () -> assertEquals(Double.NaN, area0.getInterchangeTarget().orElse(Double.NaN)),
                () -> assertEquals(Double.NaN, Double.parseDouble(area0.getProperty(CgmesNames.P_TOLERANCE, "NaN"))),
                () -> assertFalse(area0.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).isPresent()),
                () -> assertEquals(1, area0.getAreaBoundaryStream().count())
        );
        AreaBoundary b0 = area0.getAreaBoundaries().iterator().next();
        assertAll(
                () -> assertTrue(b0.isAc()),
                () -> assertEquals("DL0", b0.getBoundary().orElseThrow().getDanglingLine().getId()),
                () -> assertFalse(b0.getTerminal().isPresent())
        );
    }
}

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

        assertEquals(4, network.getAreaCount());

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
        AreaBoundary b11 = boundaryIterator.next();
        AreaBoundary b12 = boundaryIterator.next();
        AreaBoundary boundaryOnTerminal;
        AreaBoundary boundaryOnBoundary;
        if (b11.getTerminal().isPresent()) {
            boundaryOnTerminal = b11;
            boundaryOnBoundary = b12;
        } else {
            boundaryOnBoundary = b11;
            boundaryOnTerminal = b12;
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
        AreaBoundary b2 = area2.getAreaBoundaries().iterator().next();
        assertAll(
            () -> assertTrue(b2.isAc()),
            () -> assertEquals("NHV1_NHV2_2", b2.getTerminal().orElseThrow().getConnectable().getId()),
            () -> assertEquals(ThreeSides.TWO, b2.getTerminal().orElseThrow().getSide()),
            () -> assertFalse(b2.getBoundary().isPresent())
        );

        Area area3 = network.getArea("areaId-3");
        assertNotNull(area3);
        assertAll(
            () -> assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area3.getAreaType()),
            () -> assertEquals("area-3", area3.getNameOrId())
        );
        AreaBoundary b3 = area3.getAreaBoundaries().iterator().next();
        assertAll(
            () -> assertTrue(b3.isAc()),
            () -> assertFalse(b3.getTerminal().isPresent()),
            () -> assertTrue(b3.getBoundary().isPresent()),
            () -> assertEquals("DL8", b3.getBoundary().orElseThrow().getDanglingLine().getId())
        );

        // When an Area with the same ID already exists, the CgmesControlArea is not imported (and the Area is not updated)
        Area area4 = network.getArea("alreadyExistingArea");
        assertNotNull(area4);
        assertAll(
            () -> assertEquals(CgmesNames.CONTROL_AREA_TYPE_KIND_INTERCHANGE, area4.getAreaType()),
            () -> assertEquals("area", area4.getNameOrId()),
            () -> assertEquals(Double.NaN, area4.getInterchangeTarget().orElse(Double.NaN)),
            () -> assertEquals(Double.NaN, Double.parseDouble(area4.getProperty(CgmesNames.P_TOLERANCE, "NaN"))),
            () -> assertFalse(area4.getAliasFromType(CgmesNames.ENERGY_IDENT_CODE_EIC).isPresent()),
            () -> assertEquals(1, area4.getAreaBoundaryStream().count())
        );
        AreaBoundary b4 = area4.getAreaBoundaries().iterator().next();
        assertAll(
            () -> assertTrue(b4.isAc()),
            () -> assertEquals("DL0", b4.getBoundary().orElseThrow().getDanglingLine().getId()),
            () -> assertFalse(b4.getTerminal().isPresent())
        );
    }
}

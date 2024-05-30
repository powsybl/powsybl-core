/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public abstract class AbstractAreaTest {

    final Network network = EurostagTutorialExample1Factory.createWithAreas();

    final String biddingZone = "biddingZone";
    final String region = "region";
    final String aic = "aic";
    final Area biddingZoneA = network.getArea("bza");
    final Area biddingZoneB = network.getArea("bzb");
    final Area regionA = network.getArea("rga");
    final Area aicA = network.getArea("aic_a");

    @Test
    void areaAttributesTest() {
        assertEquals(IdentifiableType.AREA, biddingZoneA.getType());

        assertEquals("bza", biddingZoneA.getId());
        assertEquals("BZ_A", biddingZoneA.getNameOrId());
        assertEquals(biddingZone, biddingZoneA.getAreaType());
        assertEquals(Optional.empty(), biddingZoneA.getAcNetInterchangeTarget());
        assertEquals(Optional.empty(), biddingZoneA.getAcNetInterchangeTolerance());

        assertEquals("aic_a", aicA.getId());
        assertEquals("Aic_A", aicA.getNameOrId());
        assertEquals(aic, aicA.getAreaType());
        assertEquals(Optional.of(10.0), aicA.getAcNetInterchangeTarget());
        assertEquals(Optional.of(0.1), aicA.getAcNetInterchangeTolerance());

        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        assertEquals(Set.of(vlhv2), aicA.getVoltageLevels());
        assertEquals(List.of(vlhv2), aicA.getVoltageLevelStream().toList());

        final Terminal boundary1 = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1();
        final Terminal boundary2 = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2).getTerminal2();
        final Terminal boundary3 = network.getGenerator("GEN").getTerminal();
        final List<Area.BoundaryTerminal> boundaries = List.of(new Area.BoundaryTerminal(boundary1, true),
                new Area.BoundaryTerminal(boundary2, false), new Area.BoundaryTerminal(boundary3, true));
        assertEquals(boundaries, aicA.getBoundaryTerminals());
        assertEquals(boundaries, aicA.getBoundaryTerminalStream().toList());
    }

    @Test
    void areaIterableAndStreamGetterCheck() {
        List<Area> areas = List.of(biddingZoneA, biddingZoneB, regionA, aicA);
        List<String> areaTypes = List.of(biddingZone, region, aic);

        assertEquals(areas, network.getAreaStream().toList());
        assertEquals(areaTypes, network.getAreaTypeStream().toList());

        assertEquals(4, Iterables.size(network.getAreas()));
        areas.forEach(area -> assertTrue(Iterables.contains(network.getAreas(), area)));
        assertEquals(3, Iterables.size(network.getAreaTypes()));
        areaTypes.forEach(areaType -> assertTrue(Iterables.contains(network.getAreaTypes(), areaType)));
    }

    @Test
    void checkSubnetworkGetAreas() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");

        VoltageLevel sn1VL1 = subnetwork.newVoltageLevel()
                .setId("sub1_vl1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        assertEquals(List.of(), subnetwork.getAreaTypeStream().toList());
        assertEquals(List.of(), subnetwork.getAreaStream().toList());

        biddingZoneA.addVoltageLevel(sn1VL1);

        assertEquals(List.of(biddingZone), subnetwork.getAreaTypeStream().toList());
        assertEquals(List.of(biddingZoneA), subnetwork.getAreaStream().toList());

        assertEquals(1, Iterables.size(subnetwork.getAreas()));
        assertTrue(Iterables.contains(subnetwork.getAreas(), biddingZoneA));
        assertEquals(1, Iterables.size(subnetwork.getAreaTypes()));
        assertTrue(Iterables.contains(subnetwork.getAreaTypes(), biddingZone));
    }

    @Test
    void checkSubnetworkNewAreas() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");
        Area area = subnetwork.newArea()
                .setId("area")
                .setName("Area")
                .setAreaType("controlArea")
                .add();

        assertTrue(Iterables.contains(network.getAreaTypes(), "controlArea"));
        assertTrue(Iterables.contains(network.getAreas(), area));
    }

    @Test
    void addVoltageLevelsToAreaTest() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");

        biddingZoneA.addVoltageLevel(vlhv1);
        vlhv2.addArea(biddingZoneA);

        assertEquals(Set.of(biddingZoneA), vlhv1.getAreas());
        assertEquals(Set.of(biddingZoneA, aicA), vlhv2.getAreas());
        assertEquals(Set.of(vlhv1, vlhv2), biddingZoneA.getVoltageLevels());
    }

    @Test
    void addSameVoltageLevelToAreaTest() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        biddingZoneA.addVoltageLevel(vlhv1);
        vlhv1.addArea(biddingZoneA);

        assertEquals(Set.of(biddingZoneA), vlhv1.getAreas());
        assertEquals(Set.of(vlhv1), biddingZoneA.getVoltageLevels());
    }

    @Test
    void addAreasToVoltageLevelTest() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        vlhv1.addArea(biddingZoneA);
        regionA.addVoltageLevel(vlhv1);

        assertEquals(Set.of(biddingZoneA, regionA), vlhv1.getAreas());
        assertEquals(Set.of(vlhv1), biddingZoneA.getVoltageLevels());
        assertEquals(Set.of(vlhv1), regionA.getVoltageLevels());
    }

    @Test
    void throwAddNewAreaSameTypeTest() {
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        biddingZoneA.addVoltageLevel(vlhv2);
        var e1 = assertThrows(PowsyblException.class, () -> biddingZoneB.addVoltageLevel(vlhv2));
        var e2 = assertThrows(PowsyblException.class, () -> vlhv2.addArea(biddingZoneB));

        String expectedMessage = "VoltageLevel VLHV2 is already in Area of the same type=biddingZone with id=bza";
        assertEquals(expectedMessage, e1.getMessage());
        assertEquals(expectedMessage, e2.getMessage());
    }

    @Test
    void throwRemovedVoltageLevel() {
        VoltageLevel dummy = network.newVoltageLevel()
                .setId("dummy")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .setLowVoltageLimit(0.0)
                .setHighVoltageLimit(0.0)
                .add();

        dummy.addArea(biddingZoneB);
        assertEquals(Set.of(dummy), biddingZoneB.getVoltageLevels());
        dummy.remove();
        assertEquals(Set.of(), biddingZoneB.getVoltageLevels());

        Throwable e1 = assertThrows(PowsyblException.class, () -> dummy.getArea(biddingZone));
        Throwable e2 = assertThrows(PowsyblException.class, dummy::getAreas);
        Throwable e3 = assertThrows(PowsyblException.class, dummy::getAreasStream);
        Throwable e4 = assertThrows(PowsyblException.class, () -> dummy.addArea(biddingZoneA));

        String expectedMessage = "Cannot access areas of removed voltage level dummy";
        assertEquals(expectedMessage, e1.getMessage());
        assertEquals(expectedMessage, e2.getMessage());
        assertEquals(expectedMessage, e3.getMessage());
        assertEquals("Cannot add areas to removed voltage level dummy", e4.getMessage());
    }

}

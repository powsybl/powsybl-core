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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public abstract class AbstractAreaTest {

    final String biddingZone = "biddingZone";
    final String region = "region";
    final String aic = "aic";
    Network network;
    Area biddingZoneA;
    Area biddingZoneB;
    Area regionA;
    Area aicA;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithAreas();
        biddingZoneA = network.getArea("bza");
        biddingZoneB = network.getArea("bzb");
        regionA = network.getArea("rga");
        aicA = network.getArea("aic_a");
    }

    @Test
    void areaAttributesTest() {
        assertEquals(IdentifiableType.AREA, biddingZoneA.getType());

        assertEquals("bza", biddingZoneA.getId());
        assertEquals("BZ_A", biddingZoneA.getNameOrId());
        assertEquals(biddingZone, biddingZoneA.getAreaType());
        assertEquals(Optional.empty(), biddingZoneA.getAcNetInterchangeTarget());

        assertEquals("aic_a", aicA.getId());
        assertEquals("Aic_A", aicA.getNameOrId());
        assertEquals(aic, aicA.getAreaType());
        assertEquals(Optional.of(10.0), aicA.getAcNetInterchangeTarget());

        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        assertEquals(Set.of(vlhv2), aicA.getVoltageLevels());
        assertEquals(List.of(vlhv2), aicA.getVoltageLevelStream().toList());

        final Terminal boundary1 = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1();
        final Terminal boundary2 = network.getGenerator("GEN").getTerminal();
        final Boundary boundary3 = network.getDanglingLine("danglingLine1").getBoundary();
        final Map<Object, Boolean> expectedBoundaries = Map.of(boundary1, true, boundary2, true, boundary3, false);
        assertBoundaries(expectedBoundaries, aicA);
        AreaBoundary dcBoundary = aicA.getAreaBoundaryStream().filter(boundary -> !boundary.isAc()).findFirst().orElse(null);
        assertEquals(-5., dcBoundary.getP());
        assertEquals(-3., dcBoundary.getQ());
    }

    @Test
    void areaNetPositionComputation() {
        // Check current Net Interchanges (NaN P values are ignored)
        assertEquals(-0., aicA.getAcNetInterchange());
        assertEquals(5., aicA.getDcNetInterchange());
        assertEquals(5., aicA.getTotalNetInterchange());

        // Update the AreaBoundary active power and check that the net positions values are recomputed accordingly
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1().setP(10);
        network.getGenerator("GEN").getTerminal().setP(10);
        network.getDanglingLine("danglingLine1").setP0(-30);
        assertEquals(-20., aicA.getAcNetInterchange());
        assertEquals(-30., aicA.getDcNetInterchange());
        assertEquals(-50., aicA.getTotalNetInterchange());
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

    @Test
    void throwAddVoltageLevelOtherNetwork() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");
        VoltageLevel sn1VL1 = subnetwork.newVoltageLevel()
                .setId("sub1_vl1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        Throwable e = assertThrows(PowsyblException.class, () -> biddingZoneA.addVoltageLevel(sn1VL1));
        assertEquals("VoltageLevel sub1_vl1 cannot be added to Area bza. They do not belong to the same network or subnetwork", e.getMessage());
    }

    @Test
    void addAreaBoundaryTerminal() {
        Terminal terminal = network.getLoad("LOAD").getTerminal();
        biddingZoneA.newAreaBoundary()
                .setTerminal(terminal)
                .setAc(true)
                .add();
        Map<Object, Boolean> expectedBoundaries = Map.of(terminal, true);
        assertBoundaries(expectedBoundaries, biddingZoneA);
    }

    @Test
    void addAreaBoundaryDanglingLine() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        DanglingLine danglingLine = vlhv1.newDanglingLine()
                .setId("danglingLine")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(0.0)
                .setX(0.0)
                .setBus("NHV1")
                .add();
        biddingZoneA.newAreaBoundary()
                .setBoundary(danglingLine.getBoundary())
                .setAc(true)
                .add();
        Map<Object, Boolean> expectedBoundaries = Map.of(danglingLine.getBoundary(), true);
        assertBoundaries(expectedBoundaries, biddingZoneA);
    }

    @Test
    void removeAreaBoundaries() {
        Terminal boundary1 = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1();
        Terminal boundary2 = network.getGenerator("GEN").getTerminal();
        Boundary boundary3 = network.getDanglingLine("danglingLine1").getBoundary();

        aicA.removeAreaBoundary(boundary1);
        Map<Object, Boolean> expectedBoundaries = Map.of(boundary2, true, boundary3, false);
        assertBoundaries(expectedBoundaries, aicA);

        aicA.removeAreaBoundary(boundary3);
        expectedBoundaries = Map.of(boundary2, true);
        assertBoundaries(expectedBoundaries, aicA);
    }

    @Test
    void removeEquipmentRemovesAreaBoundary() {
        assertEquals(3, aicA.getAreaBoundaryStream().count());

        // Deleting equipment from the network should automatically delete their AreaBoundary-s
        network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).remove();
        network.getGenerator("GEN").remove();
        network.getDanglingLine("danglingLine1").remove();

        assertEquals(0, aicA.getAreaBoundaryStream().count());
        assertBoundaries(Map.of(), aicA);
    }

    void assertBoundaries(Map<Object, Boolean> expectedBoundaries, Area area) {
        final Function<AreaBoundary, ?> getBoundaryOrTerminalFunction = areaBoundary -> areaBoundary.getTerminal().isPresent() ? areaBoundary.getTerminal().get() : areaBoundary.getBoundary().get();
        Map<Object, Boolean> actualBoundariesFromIterable = StreamSupport.stream(area.getAreaBoundaries().spliterator(), false)
                                                                    .collect(Collectors.toMap(getBoundaryOrTerminalFunction, AreaBoundary::isAc));
        Map<Object, Boolean> actualBoundariesFromStream = area.getAreaBoundaryStream()
                                                         .collect(Collectors.toMap(getBoundaryOrTerminalFunction, AreaBoundary::isAc));
        assertEquals(expectedBoundaries, actualBoundariesFromIterable);
        assertEquals(expectedBoundaries, actualBoundariesFromStream);
    }

    @Test
    void throwBoundaryOtherNetwork() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");
        VoltageLevel sn1VL1 = subnetwork.newVoltageLevel()
                .setId("sub1_vl1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bus = sn1VL1.getBusBreakerView().newBus()
                .setId("sub1_bus")
                .add();
        Load load = sn1VL1.newLoad()
                .setId("sub1_load")
                .setP0(0.0)
                .setQ0(0.0)
                .setBus(bus.getId())
                .add();
        Terminal terminal = load.getTerminal();
        AreaBoundaryAdder areaBoundaryAdder = biddingZoneA.newAreaBoundary().setTerminal(terminal).setAc(true);
        Throwable e = assertThrows(PowsyblException.class, areaBoundaryAdder::add);
        assertEquals("Terminal of connectable sub1_load cannot be added to Area bza boundaries. They do not belong to the same network or subnetwork", e.getMessage());
    }

    @Test
    void throwBoundaryAttributeNotSet() {
        final Terminal boundary1 = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1).getTerminal1();
        AreaBoundaryAdder areaBoundaryAdder1 = biddingZoneA.newAreaBoundary().setAc(true);
        Throwable e1 = assertThrows(PowsyblException.class, areaBoundaryAdder1::add);
        assertEquals("AreaBoundary must have a 'terminal' or a 'boundary' attribute set to be added", e1.getMessage());
        AreaBoundaryAdder areaBoundaryAdder2 = biddingZoneA.newAreaBoundary().setTerminal(boundary1);
        Throwable e2 = assertThrows(PowsyblException.class, areaBoundaryAdder2::add);
        assertEquals("AreaBoundary must have an attribute 'ac' set to be added", e2.getMessage());
    }

}

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public abstract class AbstractAreaTest {

    private static final String CONTROL_AREA_TYPE = "ControlArea";
    private static final String REGION_AREA_TYPE = "Region";
    public static final double DELTA = 1e-3;
    Network network;
    Area controlAreaA;
    Area controlAreaB;
    Area regionAB;
    VoltageLevel vlgen;
    VoltageLevel vlhv1;
    VoltageLevel vlhv2;
    VoltageLevel vlload;
    DanglingLine dlXnode1A;
    DanglingLine dlXnode1B;
    DanglingLine dlXnode2A;
    DanglingLine dlXnode2B;
    TieLine tieLine1;
    TieLine tieLine2;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithTieLinesAndAreas();
        controlAreaA = network.getArea("ControlArea_A");
        controlAreaB = network.getArea("ControlArea_B");
        regionAB = network.getArea("Region_AB");
        vlgen = network.getVoltageLevel("VLGEN");
        vlhv1 = network.getVoltageLevel("VLHV1");
        vlhv2 = network.getVoltageLevel("VLHV2");
        vlload = network.getVoltageLevel("VLLOAD");
        dlXnode1A = network.getDanglingLine("NHV1_XNODE1");
        dlXnode1B = network.getDanglingLine("XNODE1_NHV2");
        dlXnode2A = network.getDanglingLine("NVH1_XNODE2");
        dlXnode2B = network.getDanglingLine("XNODE2_NHV2");
        tieLine1 = network.getTieLine("NHV1_NHV2_1");
        tieLine2 = network.getTieLine("NHV1_NHV2_2");
    }

    @Test
    public void areaAttributes() {
        assertEquals(IdentifiableType.AREA, controlAreaA.getType());

        assertEquals("ControlArea_A", controlAreaA.getId());
        assertEquals("Control Area A", controlAreaA.getOptionalName().orElseThrow());
        assertEquals(CONTROL_AREA_TYPE, controlAreaA.getAreaType());
        assertEquals(-602.6, controlAreaA.getAcInterchangeTarget().orElseThrow());
        assertEquals(Set.of(vlgen, vlhv1), controlAreaA.getVoltageLevels());
        assertEquals(2, controlAreaA.getAreaBoundaryStream().count());
        var boundary1aFound = controlAreaA.getAreaBoundaryStream().filter(b -> b.getBoundary().isPresent() && b.isAc() && b.getBoundary().get().getDanglingLine() == dlXnode1A).toList();
        assertEquals(1, boundary1aFound.size());
        var boundary1a = boundary1aFound.get(0);
        assertEquals(-301.315, boundary1a.getP(), DELTA);
        assertEquals(-116.524, boundary1a.getQ(), DELTA);
        var boundary2aFound = controlAreaA.getAreaBoundaryStream().filter(b -> b.getBoundary().isPresent() && b.isAc() && b.getBoundary().get().getDanglingLine() == dlXnode2A).toList();
        assertEquals(1, boundary2aFound.size());
        var boundary2a = boundary2aFound.get(0);
        assertEquals(-301.315, boundary2a.getP(), DELTA);
        assertEquals(-116.524, boundary2a.getQ(), DELTA);

        assertEquals("ControlArea_B", controlAreaB.getId());
        assertEquals("Control Area B", controlAreaB.getOptionalName().orElseThrow());
        assertEquals(CONTROL_AREA_TYPE, controlAreaB.getAreaType());
        assertEquals(+602.6, controlAreaB.getAcInterchangeTarget().orElseThrow());
        assertEquals(Set.of(vlhv2, vlload), controlAreaB.getVoltageLevels());
        assertEquals(2, controlAreaB.getAreaBoundaryStream().count());

        assertEquals("Region_AB", regionAB.getId());
        assertEquals("Region AB", regionAB.getOptionalName().orElseThrow());
        assertEquals(REGION_AREA_TYPE, regionAB.getAreaType());
        assertFalse(regionAB.getAcInterchangeTarget().isPresent());
        assertEquals(Set.of(vlgen, vlhv1, vlhv2, vlload), regionAB.getVoltageLevels());
        assertEquals(0, regionAB.getAreaBoundaryStream().count());
    }

    @Test
    public void areaInterchangeComputation() {
        assertEquals(-602.631, controlAreaA.getAcInterchange(), DELTA);
        assertEquals(0.0, controlAreaA.getDcInterchange());
        assertEquals(-602.631, controlAreaA.getTotalInterchange(), DELTA);

        assertEquals(+603.563, controlAreaB.getAcInterchange(), DELTA);
        assertEquals(0.0, controlAreaB.getDcInterchange());
        assertEquals(+603.563, controlAreaB.getTotalInterchange(), DELTA);

        // no boundaries defined
        assertEquals(0.0, regionAB.getAcInterchange());
        assertEquals(0.0, regionAB.getDcInterchange());
        assertEquals(0.0, regionAB.getTotalInterchange());

        // verify NaN do not mess up the calculation
        dlXnode1A.getTerminal().setP(Double.NaN);
        assertEquals(-301.315, controlAreaA.getAcInterchange(), DELTA);
        assertEquals(0.0, controlAreaA.getDcInterchange());
        assertEquals(-301.315, controlAreaA.getTotalInterchange(), DELTA);
    }

    @Test
    public void areaIterableAndStreamGetterCheck() {
        List<Area> areas = List.of(controlAreaA, controlAreaB, regionAB);
        List<String> areaTypes = List.of(CONTROL_AREA_TYPE, REGION_AREA_TYPE);

        assertEquals(areas, network.getAreaStream().toList());
        assertEquals(areaTypes, network.getAreaTypeStream().toList());

        assertEquals(3, Iterables.size(network.getAreas()));
        areas.forEach(area -> assertTrue(Iterables.contains(network.getAreas(), area)));
        assertEquals(2, Iterables.size(network.getAreaTypes()));
        areaTypes.forEach(areaType -> assertTrue(Iterables.contains(network.getAreaTypes(), areaType)));
    }

    @Test
    public void addVoltageLevelsToArea() {
        var newVl = vlhv1.getNullableSubstation().newVoltageLevel()
                .setId("NewVl")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        // we can add new VL from the area
        controlAreaA.addVoltageLevel(newVl);
        // or from the new VL
        newVl.addArea(regionAB);

        assertEquals(Set.of(controlAreaA, regionAB), newVl.getAreas());
        assertEquals(Set.of(vlgen, vlhv1, newVl), controlAreaA.getVoltageLevels());
        assertEquals(Set.of(vlhv2, vlload), controlAreaB.getVoltageLevels());
        assertEquals(Set.of(vlgen, vlhv1, newVl, vlhv2, vlload), regionAB.getVoltageLevels());
    }

    @Test
    public void addSameVoltageLevelToArea() {
        // vlhv1 already in controlAreaA, no-op
        controlAreaA.addVoltageLevel(vlhv1);
        vlhv1.addArea(controlAreaA);

        assertEquals(Set.of(controlAreaA, regionAB), vlhv1.getAreas());
        assertEquals(Set.of(vlgen, vlhv1), controlAreaA.getVoltageLevels());
    }

    @Test
    void testWithTerminals() {
        // change boundary to be at transformer NGEN_NHV1 side 2
        controlAreaA.removeAreaBoundary(dlXnode1A.getBoundary());
        controlAreaA.removeAreaBoundary(dlXnode2A.getBoundary());
        controlAreaB.removeAreaBoundary(dlXnode1B.getBoundary());
        controlAreaB.removeAreaBoundary(dlXnode2B.getBoundary());

        TwoWindingsTransformer ngenNhv1 = network.getTwoWindingsTransformer("NGEN_NHV1");
        controlAreaA.newAreaBoundary().setTerminal(ngenNhv1.getTerminal2()).setAc(true).add();
        controlAreaB.newAreaBoundary().setTerminal(dlXnode1A.getTerminal()).setAc(true).add();
        controlAreaB.newAreaBoundary().setTerminal(dlXnode2A.getTerminal()).setAc(true).add();

        assertEquals(-604.891, controlAreaA.getAcInterchange(), DELTA);
        assertEquals(0.0, controlAreaA.getDcInterchange());
        assertEquals(-604.891, controlAreaA.getTotalInterchange(), DELTA);

        assertEquals(+604.888, controlAreaB.getAcInterchange(), DELTA);
        assertEquals(0.0, controlAreaB.getDcInterchange());
        assertEquals(+604.888, controlAreaB.getTotalInterchange(), DELTA);

        // verify NaN do not mess up the calculation
        ngenNhv1.getTerminal2().setP(Double.NaN);
        assertEquals(0.0, controlAreaA.getAcInterchange());
        assertEquals(0.0, controlAreaA.getDcInterchange());
        assertEquals(0.0, controlAreaA.getTotalInterchange());
    }

    @Test
    void testWithDc() {
        // remove entirely control area B, set one dangling line AC, the other one DC
        controlAreaB.remove();
        regionAB.remove();
        tieLine1.remove();
        tieLine2.remove();
        network.getSubstation("P2").remove();
        controlAreaA.removeAreaBoundary(dlXnode2A.getBoundary());
        controlAreaA.newAreaBoundary().setBoundary(dlXnode2A.getBoundary()).setAc(false).add();
        dlXnode1A.setP0(290.0);
        dlXnode2A.setP0(310.0);
        assertEquals(-290.0, controlAreaA.getAcInterchange(), DELTA);
        assertEquals(-310.0, controlAreaA.getDcInterchange());
        assertEquals(-600.0, controlAreaA.getTotalInterchange(), DELTA);
    }

    @Test
    public void throwAddNewAreaSameType() {
        var e1 = assertThrows(PowsyblException.class, () -> controlAreaA.addVoltageLevel(vlhv2));
        assertEquals("VoltageLevel VLHV2 is already in Area of the same type=ControlArea with id=ControlArea_B", e1.getMessage());
        var e2 = assertThrows(PowsyblException.class, () -> vlhv1.addArea(controlAreaB));
        assertEquals("VoltageLevel VLHV1 is already in Area of the same type=ControlArea with id=ControlArea_A", e2.getMessage());
    }

    @Test
    public void throwRemovedVoltageLevel() {
        VoltageLevel newVoltageLevel = network.newVoltageLevel()
                .setId("newVoltageLevel")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        newVoltageLevel.addArea(controlAreaB);
        assertEquals(Set.of(vlhv2, vlload, newVoltageLevel), controlAreaB.getVoltageLevels());
        newVoltageLevel.remove();
        assertEquals(Set.of(vlhv2, vlload), controlAreaB.getVoltageLevels());

        Throwable e1 = assertThrows(PowsyblException.class, () -> newVoltageLevel.getArea(CONTROL_AREA_TYPE));
        Throwable e2 = assertThrows(PowsyblException.class, newVoltageLevel::getAreas);
        Throwable e3 = assertThrows(PowsyblException.class, newVoltageLevel::getAreasStream);
        Throwable e4 = assertThrows(PowsyblException.class, () -> newVoltageLevel.addArea(controlAreaA));

        String expectedMessage = "Cannot access areas of removed voltage level newVoltageLevel";
        assertEquals(expectedMessage, e1.getMessage());
        assertEquals(expectedMessage, e2.getMessage());
        assertEquals(expectedMessage, e3.getMessage());
        assertEquals("Cannot add areas to removed voltage level newVoltageLevel", e4.getMessage());
    }

    @Test
    public void throwAddVoltageLevelOtherNetwork() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "code");
        VoltageLevel newVoltageLevel = subnetwork.newVoltageLevel()
                .setId("newVoltageLevel")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();

        Throwable e = assertThrows(PowsyblException.class, () -> controlAreaA.addVoltageLevel(newVoltageLevel));
        assertEquals("VoltageLevel newVoltageLevel cannot be added to Area ControlArea_A. It does not belong to the same network or subnetwork.", e.getMessage());
    }

    @Test
    public void removeAreaBoundaries() {
        assertEquals(2, controlAreaA.getAreaBoundaryStream().count());

        controlAreaA.removeAreaBoundary(dlXnode1A.getBoundary());

        assertEquals(1, controlAreaA.getAreaBoundaryStream().count());
    }

    @Test
    public void removeEquipmentRemovesAreaBoundary() {
        assertEquals(2, controlAreaA.getAreaBoundaryStream().count());
        assertEquals(2, controlAreaB.getAreaBoundaryStream().count());

        // Deleting equipment from the network should automatically delete their AreaBoundary-s
        // here we remove only one side of the tie-line, on control area A side
        tieLine1.remove();
        dlXnode1A.remove();

        assertEquals(1, controlAreaA.getAreaBoundaryStream().count());
        assertEquals(2, controlAreaB.getAreaBoundaryStream().count());
    }

    @Test
    public void throwBoundaryOtherNetwork() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");
        VoltageLevel sn1VL1 = subnetwork.newVoltageLevel()
                .setId("sub1_vl1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus bus = sn1VL1.getBusBreakerView().newBus()
                .setId("sub1_bus")
                .add();
        DanglingLine danglingLine = sn1VL1.newDanglingLine()
                .setId("sub1_dl")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setBus(bus.getId())
                .setPairingKey("XNODE")
                .add();
        AreaBoundaryAdder areaBoundaryAdder = controlAreaA.newAreaBoundary().setBoundary(danglingLine.getBoundary()).setAc(true);
        Throwable e = assertThrows(PowsyblException.class, areaBoundaryAdder::add);
        assertEquals("Boundary of DanglingLinesub1_dl cannot be added to Area ControlArea_A boundaries. It does not belong to the same network or subnetwork.", e.getMessage());
    }

    @Test
    public void throwBoundaryAttributeNotSet() {
        AreaBoundaryAdder areaBoundaryAdder1 = controlAreaA.newAreaBoundary().setAc(true);
        Throwable e1 = assertThrows(PowsyblException.class, areaBoundaryAdder1::add);
        assertEquals("No AreaBoundary element (terminal or boundary) is set.", e1.getMessage());
        AreaBoundaryAdder areaBoundaryAdder2 = controlAreaA.newAreaBoundary().setBoundary(dlXnode1A.getBoundary());
        Throwable e2 = assertThrows(PowsyblException.class, areaBoundaryAdder2::add);
        assertEquals("AreaBoundary AC flag is not set.", e2.getMessage());
    }

    @Test
    public void removeArea() {
        controlAreaA.remove();
        assertFalse(Iterables.contains(network.getAreas(), controlAreaA));
        assertFalse(network.getAreaStream().toList().contains(controlAreaA));

        Throwable e1 = assertThrows(PowsyblException.class, controlAreaA::getAreaType);
        assertEquals("Cannot access area type of removed area ControlArea_A", e1.getMessage());
        Throwable e2 = assertThrows(PowsyblException.class, controlAreaA::getAcInterchangeTarget);
        assertEquals("Cannot access AC interchange target of removed area ControlArea_A", e2.getMessage());
        Throwable e3 = assertThrows(PowsyblException.class, controlAreaA::getAcInterchange);
        assertEquals("Cannot access AC interchange of removed area ControlArea_A", e3.getMessage());
        Throwable e4 = assertThrows(PowsyblException.class, controlAreaA::getDcInterchange);
        assertEquals("Cannot access DC interchange of removed area ControlArea_A", e4.getMessage());
        Throwable e5 = assertThrows(PowsyblException.class, controlAreaA::getTotalInterchange);
        assertEquals("Cannot access total interchange of removed area ControlArea_A", e5.getMessage());
        Throwable e6 = assertThrows(PowsyblException.class, controlAreaA::getVoltageLevels);
        assertEquals("Cannot access voltage levels of removed area ControlArea_A", e6.getMessage());
        Throwable e7 = assertThrows(PowsyblException.class, controlAreaA::getVoltageLevelStream);
        assertEquals("Cannot access voltage levels of removed area ControlArea_A", e7.getMessage());
        Throwable e8 = assertThrows(PowsyblException.class, controlAreaA::getAreaBoundaries);
        assertEquals("Cannot access area boundaries of removed area ControlArea_A", e8.getMessage());
        Throwable e9 = assertThrows(PowsyblException.class, controlAreaA::getAreaBoundaryStream);
        assertEquals("Cannot access area boundaries of removed area ControlArea_A", e9.getMessage());
    }
}

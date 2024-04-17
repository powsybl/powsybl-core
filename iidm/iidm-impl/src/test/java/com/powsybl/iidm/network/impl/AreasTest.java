package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AreasTest {

    final Network network = createEurostagExampleWithAreas();

    final AreaType biddingZone = network.getAreaType("bz");
    final AreaType region = network.getAreaType("rg");
    final Area biddingZoneA = network.getArea("bza");
    final Area biddingZoneB = network.getArea("bzb");
    final Area regionA = network.getArea("rga");
    final Area regionB = network.getArea("rgb");

    @Test
    void checkAreasCreated() {
        assertEquals("bz", biddingZone.getId());
        assertEquals("Bidding_Zone", biddingZone.getNameOrId());
        assertEquals(network, biddingZone.getNetwork());

        assertEquals("rg", region.getId());
        assertEquals("Region", region.getNameOrId());
        assertEquals(network, region.getNetwork());

        assertEquals("bza", biddingZoneA.getId());
        assertEquals("BZ_A", biddingZoneA.getNameOrId());
        assertEquals(biddingZone, biddingZoneA.getAreaType());

        assertEquals("bzb", biddingZoneB.getId());
        assertEquals("BZ_B", biddingZoneB.getNameOrId());
        assertEquals(biddingZone, biddingZoneB.getAreaType());

        assertEquals("rga", regionA.getId());
        assertEquals("Region_A", regionA.getNameOrId());
        assertEquals(region, regionA.getAreaType());
        assertEquals("rgb", regionB.getId());
        assertEquals("Region_B", regionB.getNameOrId());
        assertEquals(region, regionB.getAreaType());

        assertEquals(List.of(biddingZoneA, biddingZoneB, regionA, regionB), network.getAreaStream().toList());
        assertEquals(List.of(biddingZone, region), network.getAreaTypeStream().toList());
    }

    @Test
    void addVoltageLevelsTest() {
        // add voltageLevel to Area
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        VoltageLevel vlload = network.getVoltageLevel("VLLOAD");
        VoltageLevel vlgen = network.getVoltageLevel("VLGEN");

        biddingZoneA.addVoltageLevel(vlhv2);
        vlload.addToArea(biddingZoneA);
        biddingZoneB.addVoltageLevel(vlhv1);
        vlgen.addToArea(biddingZoneB);

        regionA.addVoltageLevel(vlload);
        vlhv1.addToArea(regionA);
        regionB.addVoltageLevel(vlgen);
        vlhv2.addToArea(regionB);

        assertEquals(List.of(biddingZoneA, regionA), vlload.getAreasStream().toList());
        assertEquals(List.of(biddingZoneB, regionB), vlgen.getAreasStream().toList());
        assertEquals(List.of(biddingZoneA, regionB), vlhv2.getAreasStream().toList());
        assertEquals(List.of(biddingZoneB, regionA), vlhv1.getAreasStream().toList());

        assertEquals(List.of(vlhv2, vlload), biddingZoneA.getVoltageLevelsStream().toList());
        assertEquals(List.of(vlhv1, vlgen), biddingZoneB.getVoltageLevelsStream().toList());
        assertEquals(List.of(vlload, vlhv1), regionA.getVoltageLevelsStream().toList());
        assertEquals(List.of(vlgen, vlhv2), regionB.getVoltageLevelsStream().toList());

        var e1 = assertThrows(PowsyblException.class, () -> biddingZoneA.addVoltageLevel(vlgen));
        assertEquals("VoltageLevel VLGEN is already in Area bza", e1.getMessage());
        var e2 = assertThrows(PowsyblException.class, () -> biddingZoneB.addVoltageLevel(vlhv2));
        assertEquals("VoltageLevel VLHV2 is already in Area bzb", e2.getMessage());
        var e3 = assertThrows(PowsyblException.class, () -> regionA.addVoltageLevel(vlgen));
        assertEquals("VoltageLevel VLGEN is already in Area rga", e3.getMessage());
        var e4 = assertThrows(PowsyblException.class, () -> regionB.addVoltageLevel(vlhv1));
        assertEquals("VoltageLevel VLHV1 is already in Area rgb", e4.getMessage());
    }

    static Network createEurostagExampleWithAreas() {
        Network network = EurostagTutorialExample1Factory.create();
        final AreaType biddingZone = network.newAreaType()
                .setId("bz")
                .setName("Bidding_Zone")
                .add();
        final AreaType region = network.newAreaType()
                .setId("rg")
                .setName("Region")
                .add();

        network.newArea()
                .setId("bza")
                .setName("BZ_A")
                .setAreaType(biddingZone)
                .add();
        network.newArea()
                .setId("bzb")
                .setName("BZ_B")
                .setAreaType(biddingZone)
                .add();

        network.newArea()
                .setId("rga")
                .setName("Region_A")
                .setAreaType(region)
                .add();
        network.newArea()
                .setId("rgb")
                .setName("Region_B")
                .setAreaType(region)
                .add();
        return network;
    }
}

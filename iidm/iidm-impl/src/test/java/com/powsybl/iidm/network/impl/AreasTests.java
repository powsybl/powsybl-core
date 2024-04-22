package com.powsybl.iidm.network.impl;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AreasTests {

    final Network network = createEurostagExampleWithAreas();

    final AreaType biddingZone = network.getAreaType("bz");
    final AreaType region = network.getAreaType("rg");

    final AreaType aic = network.getAreaType("aic");
    final Area biddingZoneA = network.getArea("bza");
    final Area biddingZoneB = network.getArea("bzb");
    final Area regionA = network.getArea("rga");

    final AicArea aicA = (AicArea) network.getArea("aic_a");

    @Test
    void areaAttributesTest() {
        assertEquals(IdentifiableType.AREA, biddingZoneA.getType());
        assertEquals(IdentifiableType.AREA_TYPE, biddingZone.getType());

        assertEquals("rg", region.getId());
        assertEquals("Region", region.getNameOrId());
        assertEquals(network, region.getNetwork());

        assertEquals("bza", biddingZoneA.getId());
        assertEquals("BZ_A", biddingZoneA.getNameOrId());
        assertEquals(biddingZone, biddingZoneA.getAreaType());

        assertEquals("aic_a", aicA.getId());
        assertEquals("Aic_A", aicA.getNameOrId());
        assertEquals(aic, aicA.getAreaType());
        assertEquals(10.0, aicA.getAcNetInterchangeTarget());
        assertEquals(0.1, aicA.getAcNetInterchangeTolerance());

    }

    @Test
    void areaIterableAndStreamGetterCheck() {
        assertEquals(4, Iterables.size(network.getAreas()));
        assertEquals(1, Iterables.size(network.getAicAreas()));
        assertEquals(3, Iterables.size(network.getAreaTypes()));

        assertEquals(List.of(biddingZoneA, biddingZoneB, regionA, aicA), network.getAreaStream().toList());
        assertEquals(List.of(aicA), network.getAicAreaStream().toList());
        assertEquals(List.of(biddingZone, region, aic), network.getAreaTypeStream().toList());
    }

    @Test
    void checkSubnetworkGetAreas() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");
        assertEquals(biddingZone, subnetwork.getAreaType("bz"));
        assertEquals(biddingZoneA, subnetwork.getArea("bza"));
        assertEquals(aicA, subnetwork.getArea("aic_a"));

        assertEquals(4, Iterables.size(subnetwork.getAreas()));
        assertEquals(1, Iterables.size(subnetwork.getAicAreas()));
        assertEquals(3, Iterables.size(subnetwork.getAreaTypes()));

        assertEquals(List.of(biddingZoneA, biddingZoneB, regionA, aicA), subnetwork.getAreaStream().toList());
        assertEquals(List.of(aicA), subnetwork.getAicAreaStream().toList());
        assertEquals(List.of(biddingZone, region, aic), subnetwork.getAreaTypeStream().toList());
    }

    @Test
    void checkSubnetworkNewAreas() {
        Network subnetwork = network.createSubnetwork("subnetwork_id", "Subnetwork", "json");
        AreaType areaType = subnetwork.newAreaType()
                .setId("areatype")
                .setName("AreaType")
                .add();
        Area area = subnetwork.newArea()
                .setId("area")
                .setName("Area")
                .setAreaType(areaType)
                .add();

        AicArea aicArea = subnetwork.newAicArea()
                .setAcNetInterchangeTolerance(0.1)
                .setAcNetInterchangeTarget(10.0)
                .setId("aicarea")
                .setName("AicArea")
                .setAreaType(areaType)
                .add();

        assertTrue(Iterables.contains(network.getAreaTypes(), areaType));
        assertTrue(Iterables.contains(network.getAreas(), area));
        assertTrue(Iterables.contains(network.getAicAreas(), aicArea));

    }

    @Test
    void addVoltageLevelsToAreaTest() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        biddingZoneA.addVoltageLevel(vlhv1);
        vlhv2.addArea(biddingZoneA);

        assertEquals(List.of(biddingZoneA), vlhv1.getAreasStream().toList());
        assertEquals(List.of(biddingZoneA), vlhv2.getAreasStream().toList());
        assertEquals(List.of(vlhv1, vlhv2), biddingZoneA.getVoltageLevelStream().toList());
        assertEquals(2, Iterables.size(biddingZoneA.getVoltageLevels()));
    }

    @Test
    void addSameVoltageLevelToAreaTest() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        biddingZoneA.addVoltageLevel(vlhv1);
        vlhv1.addArea(biddingZoneA);

        assertEquals(List.of(biddingZoneA), vlhv1.getAreasStream().toList());
        assertEquals(List.of(vlhv1), biddingZoneA.getVoltageLevelStream().toList());
    }

    @Test
    void addAreasToVoltageLevelTest() {
        VoltageLevel vlhv1 = network.getVoltageLevel("VLHV1");
        vlhv1.addArea(biddingZoneA);
        regionA.addVoltageLevel(vlhv1);

        assertEquals(List.of(biddingZoneA, regionA), vlhv1.getAreasStream().toList());
        assertEquals(List.of(vlhv1), biddingZoneA.getVoltageLevelStream().toList());
        assertEquals(List.of(vlhv1), regionA.getVoltageLevelStream().toList());
    }

    @Test
    void throwAddNewAreaSameTypeTest() {
        VoltageLevel vlhv2 = network.getVoltageLevel("VLHV2");
        biddingZoneA.addVoltageLevel(vlhv2);
        var e1 = assertThrows(PowsyblException.class, () -> biddingZoneB.addVoltageLevel(vlhv2));
        var e2 = assertThrows(PowsyblException.class, () -> vlhv2.addArea(biddingZoneB));

        String expectedMessage = "VoltageLevel VLHV2 is already in Area of the same type=Bidding_Zone with id=bza";
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
        dummy.remove();

        var e1 = assertThrows(PowsyblException.class, () -> dummy.getArea(biddingZone));
        var e2 = assertThrows(PowsyblException.class, dummy::getAreasStream);

        String expectedMessage = "Cannot access area of removed voltage level dummy";
        assertEquals(expectedMessage, e1.getMessage());
        assertEquals(expectedMessage, e2.getMessage());
    }

    @Test
    void throwDifferntNetworksTest() {
        Network network2 = EurostagTutorialExample1Factory.create();
        VoltageLevel vlhv2Bis = network2.getVoltageLevel("VLHV2");

        var e1 = assertThrows(PowsyblException.class, () -> biddingZoneB.addVoltageLevel(vlhv2Bis));
        var e2 = assertThrows(PowsyblException.class, () -> vlhv2Bis.addArea(biddingZoneB));

        String expectedMessage = "Identifiable VLHV2 must belong to the same network as the Area to which it is added";
        assertEquals(expectedMessage, e1.getMessage());
        assertEquals(expectedMessage, e2.getMessage());
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
        final AreaType aic = network.newAreaType()
                .setId("aic")
                .setName("AIC")
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
        network.newAicArea()
                .setAcNetInterchangeTarget(10.0)
                .setAcNetInterchangeTolerance(0.1)
                .setId("aic_a")
                .setName("Aic_A")
                .setAreaType(aic)
                .add();
        return network;
    }
}

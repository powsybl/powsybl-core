/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NetworkTest1Factory;
import com.powsybl.iidm.network.util.TieLineUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.powsybl.iidm.network.test.NetworkTest1Factory.id;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractSubnetworksExplorationTest {

    public static final String ID_1 = "1";
    public static final String ID_2 = "2";
    public static final String SHARED_TIE_LINE = TieLineUtil.buildMergedId(id("danglingLine3", ID_1), id("danglingLine3", ID_2));
    private static Network merged;
    private static Network subnetwork1;
    private static Network subnetwork2;
    private static Collection<String> n1Identifiables;
    private static Collection<String> n2Identifiables;

    @BeforeAll
    static void setUpClass() {
        Network n1 = createNetwork(ID_1, Country.ES);
        Network n2 = createNetwork(ID_2, Country.BE);

        n1Identifiables = getIdentifiables(n1);
        n2Identifiables = getIdentifiables(n2);

        merged = Network.merge("merged", n1, n2);
        subnetwork1 = merged.getSubnetwork(id("network", ID_1));
        subnetwork2 = merged.getSubnetwork(id("network", ID_2));
    }

    private static Network createNetwork(String networkId, Country otherSubstationCountry) {
        Network n = NetworkTest1Factory.create(networkId);
        Area area1 = n.newArea()
                .setId(id("area1", networkId))
                .setName("AREA")
                .setAreaType(id("areaType1", networkId))
                .add();
        VoltageLevel voltageLevel1 = n.getVoltageLevel(id("voltageLevel1", networkId));
        voltageLevel1.addArea(area1);
        voltageLevel1.newBattery()
                .setId(id("battery1", networkId))
                .setMaxP(20.0)
                .setMinP(10.0)
                .setTargetP(15.0)
                .setTargetQ(10.0)
                .setNode(4)
                .add();
        voltageLevel1.newShuntCompensator()
                .setId(id("shuntCompensator1", networkId))
                .setNode(7)
                .setSectionCount(0)
                .newLinearModel()
                    .setBPerSection(1e-5)
                    .setMaximumSectionCount(1)
                    .add()
                .add();
        voltageLevel1.newStaticVarCompensator()
                .setId(id("svc1", networkId))
                .setNode(12)
                .setBmin(-5e-2)
                .setBmax(5e-2)
                .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                .setVoltageSetpoint(400)
                .add();
        voltageLevel1.newLccConverterStation()
                .setId(id("lcc1", networkId))
                .setNode(8)
                .setPowerFactor(0.95f)
                .setLossFactor(0.99f)
                .add();
        voltageLevel1.newVscConverterStation()
                .setId(id("vsc1", networkId))
                .setNode(10)
                .setLossFactor(1.1f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();

        Substation substation2 = n.newSubstation()
                .setId(id("substation2", networkId))
                .setCountry(otherSubstationCountry)
                .setTso(id("TSO2", networkId))
                .setGeographicalTags(id("region2", networkId))
                .add();
        VoltageLevel voltageLevel2 = substation2.newVoltageLevel()
                .setId(id("voltageLevel2", networkId))
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        voltageLevel2.newLccConverterStation()
                .setId(id("lcc2", networkId))
                .setNode(9)
                .setPowerFactor(0.95f)
                .setLossFactor(0.99f)
                .add();
        voltageLevel2.newVscConverterStation()
                .setId(id("vsc2", networkId))
                .setNode(11)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();

        n.newHvdcLine()
                .setId(id("hvdcLine1", networkId))
                .setR(1)
                .setNominalV(400)
                .setConverterStationId1(id("lcc1", networkId))
                .setConverterStationId2(id("lcc2", networkId))
                .setMaxP(2000)
                .setActivePowerSetpoint(50)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                .add();
        n.newHvdcLine()
                .setId(id("hvdcLine2", networkId))
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(50.0)
                .setActivePowerSetpoint(20.0)
                .setConverterStationId1(id("vsc1", networkId))
                .setConverterStationId2(id("vsc2", networkId))
                .add();

        Substation substation3 = n.newSubstation()
                .setId(id("substation3", networkId))
                .setCountry(Country.DE)
                .setTso(id("TSO3", networkId))
                .add();
        VoltageLevel voltageLevel3 = substation3.newVoltageLevel()
                .setId(id("voltageLevel3", networkId))
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        substation3.newVoltageLevel()
                .setId(id("voltageLevel4", networkId))
                .setNominalV(225)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        substation3.newVoltageLevel()
                .setId(id("voltageLevel5", networkId))
                .setNominalV(90)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        ThreeWindingsTransformerAdder threeWindingsTransformerAdder1 = substation3.newThreeWindingsTransformer()
                .setId(id("threeWindingsTransformer1", networkId))
                .setRatedU0(400);
        threeWindingsTransformerAdder1.newLeg1()
                .setNode(1)
                .setR(0.001).setX(0.000001).setB(0).setG(0)
                .setRatedU(400)
                .setVoltageLevel(id("voltageLevel3", networkId))
                .add();
        threeWindingsTransformerAdder1.newLeg2()
                .setNode(1)
                .setR(0.1).setX(0.00001).setB(0).setG(0)
                .setRatedU(225)
                .setVoltageLevel(id("voltageLevel4", networkId))
                .add();
        threeWindingsTransformerAdder1.newLeg3()
                .setNode(1)
                .setR(0.01).setX(0.0001).setB(0).setG(0)
                .setRatedU(90)
                .setVoltageLevel(id("voltageLevel5", networkId))
                .add();
        threeWindingsTransformerAdder1.add();
        substation3.newTwoWindingsTransformer()
                .setId(id("twoWindingsTransformer1", networkId))
                .setVoltageLevel1(id("voltageLevel3", networkId))
                .setNode1(2)
                .setRatedU1(400)
                .setVoltageLevel2(id("voltageLevel4", networkId))
                .setNode2(2)
                .setRatedU2(225)
                .setR(0.24 / 1300 * (38 * 38)).setX(Math.sqrt(10 * 10 - 0.24 * 0.24) / 1300 * (38 * 38))
                .setG(0.0).setB(0.0)
                .add();
        String lineId1 = id("line1", networkId);
        n.newLine()
                .setId(lineId1)
                .setVoltageLevel1(id("voltageLevel1", networkId))
                .setNode1(13)
                .setVoltageLevel2(id("voltageLevel2", networkId))
                .setNode2(14)
                .setR(1).setX(1).setG1(0).setG2(0).setB1(0).setB2(0)
                .add();
        voltageLevel1.newDanglingLine()
                .setId(id("danglingLine1", networkId))
                .setNode(15)
                .setR(1.0).setX(0.1).setG(0.0).setB(0.001).setP0(10).setQ0(1)
                .add();
        voltageLevel2.newDanglingLine()
                .setId(id("danglingLine2", networkId))
                .setNode(16)
                .setR(1.0).setX(0.1).setG(0.0).setB(0.001).setP0(10).setQ0(1)
                .add();
        n.newTieLine()
                .setId(id("tieLine1", networkId))
                .setDanglingLine1(id("danglingLine1", networkId))
                .setDanglingLine2(id("danglingLine2", networkId))
                .add();
        voltageLevel3.newDanglingLine()
                .setId(id("danglingLine3", networkId))
                .setNode(17)
                .setR(1.0).setX(0.1).setG(0.0).setB(0.001).setP0(10).setQ0(1)
                .setPairingKey("mergingKey") // when merging both networks, this key will be used to create a tie line
                .add();
        substation3.newOverloadManagementSystem()
                .setId(id("overloadManagementSystem", networkId))
                .setEnabled(true)
                .setMonitoredElementId(lineId1)
                .setMonitoredElementSide(ThreeSides.ONE)
                .newBranchTripping()
                    .setKey("branchTripping")
                    .setCurrentLimit(80.)
                    .setOpenAction(true)
                    .setBranchToOperateId(lineId1)
                    .setSideToOperate(TwoSides.ONE)
                    .add()
                .add();
        return n;
    }

    private static Set<String> getIdentifiables(Network n1) {
        return n1.getIdentifiables().stream().map(Identifiable::getId).collect(Collectors.toSet());
    }

    @Test
    public void testExploreSubnetworks() {
        assertEquals(2, merged.getSubnetworks().size());
        assertEquals(0, subnetwork1.getSubnetworks().size());
        assertEquals(0, subnetwork2.getSubnetworks().size());
        assertNull(subnetwork1.getSubnetwork("merged"));
        assertNull(subnetwork1.getSubnetwork(id("network", ID_1)));
        assertNull(subnetwork1.getSubnetwork(id("network", ID_2)));
        assertNull(subnetwork2.getSubnetwork("merged"));
        assertNull(subnetwork2.getSubnetwork(id("network", ID_1)));
        assertNull(subnetwork2.getSubnetwork(id("network", ID_2)));
    }

    @Test
    public void testExploreNetwork() {
        assertEquals(merged, merged.getNetwork());
        assertEquals(merged, subnetwork1.getNetwork());
        assertEquals(merged, subnetwork2.getNetwork());
    }

    @Test
    public void testExploreVariantManager() {
        assertEquals(merged.getVariantManager(), subnetwork1.getVariantManager());
        assertEquals(merged.getVariantManager(), subnetwork2.getVariantManager());
    }

    @Test
    public void testExploreCountries() {
        assertEquals(4, merged.getCountryCount());
        assertEquals(3, subnetwork1.getCountryCount());
        assertEquals(3, subnetwork2.getCountryCount());
        assertCollection(List.of(Country.FR, Country.ES, Country.BE, Country.DE), merged.getCountries());
        assertCollection(List.of(Country.FR, Country.ES, Country.DE), subnetwork1.getCountries());
        assertCollection(List.of(Country.FR, Country.BE, Country.DE), subnetwork2.getCountries());
    }

    @Test
    public void testExploreAreaTypes() {
        assertEquals(2, merged.getAreaTypeCount());
        assertEquals(1, subnetwork1.getAreaTypeCount());
        assertEquals(1, subnetwork2.getAreaTypeCount());
        String areaTypeId1 = id("areaType1", ID_1);
        String areaTypeId2 = id("areaType1", ID_2);
        assertCollection(List.of(areaTypeId1, areaTypeId2), merged.getAreaTypeStream().toList());
        assertCollection(List.of(areaTypeId1), subnetwork1.getAreaTypeStream().toList());
        assertCollection(List.of(areaTypeId2), subnetwork2.getAreaTypeStream().toList());
    }

    @Test
    public void testExploreAreas() {
        var expectedIdsForSubnetwork1 = List.of(id("area1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("area1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getAreas,
                Network::getAreaStream,
                Network::getAreaCount,
                Network::getArea);
    }

    @Test
    public void testExploreSubstations() {
        String n1Substation1 = id("substation1", ID_1);
        String n1Substation2 = id("substation2", ID_1);
        String n2Substation1 = id("substation1", ID_2);
        String n2Substation2 = id("substation2", ID_2);
        var expectedIdsForSubnetwork1 = List.of(n1Substation1, n1Substation2, id("substation3", ID_1));
        var expectedIdsForSubnetwork2 = List.of(n2Substation1, n2Substation2, id("substation3", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getSubstations,
                Network::getSubstationStream,
                Network::getSubstationCount,
                Network::getSubstation);

        assertIds(List.of(n1Substation1, n2Substation1), merged.getSubstations(Country.FR, null));
        assertIds(List.of(n1Substation1), subnetwork1.getSubstations(Country.FR, null));
        assertIds(List.of(n2Substation1), subnetwork2.getSubstations(Country.FR, null));
        var countryName = Country.ES.getName();
        assertIds(List.of(n1Substation2), merged.getSubstations(countryName, null));
        assertIds(List.of(n1Substation2), subnetwork1.getSubstations(countryName, null));
        assertFalse(subnetwork2.getSubstations(countryName, null).iterator().hasNext());
        countryName = Country.BE.getName();
        assertIds(List.of(n2Substation2), merged.getSubstations(countryName, null));
        assertFalse(subnetwork1.getSubstations(countryName, null).iterator().hasNext());
        assertIds(List.of(n2Substation2), subnetwork2.getSubstations(countryName, null));
    }

    @Test
    public void testExploreVoltageLevels() {
        var expectedIdsForSubnetwork1 = List.of(id("voltageLevel1", ID_1),
                id("voltageLevel2", ID_1), id("voltageLevel3", ID_1),
                id("voltageLevel4", ID_1), id("voltageLevel5", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("voltageLevel1", ID_2),
                id("voltageLevel2", ID_2), id("voltageLevel3", ID_2),
                id("voltageLevel4", ID_2), id("voltageLevel5", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getVoltageLevels,
                Network::getVoltageLevelStream,
                Network::getVoltageLevelCount,
                Network::getVoltageLevel);
    }

    @Test
    public void testExploreGenerators() {
        var expectedIdsForSubnetwork1 = List.of(id("generator1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("generator1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getGenerators,
                Network::getGeneratorStream,
                Network::getGeneratorCount,
                Network::getGenerator);
    }

    @Test
    public void testExploreLoads() {
        var expectedIdsForSubnetwork1 = List.of(id("load1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("load1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getLoads,
                Network::getLoadStream,
                Network::getLoadCount,
                Network::getLoad);
    }

    @Test
    public void testExploreBatteries() {
        var expectedIdsForSubnetwork1 = List.of(id("battery1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("battery1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getBatteries,
                Network::getBatteryStream,
                Network::getBatteryCount,
                Network::getBattery);
    }

    @Test
    public void testExploreShuntCompensators() {
        var expectedIdsForSubnetwork1 = List.of(id("shuntCompensator1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("shuntCompensator1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getShuntCompensators,
                Network::getShuntCompensatorStream,
                Network::getShuntCompensatorCount,
                Network::getShuntCompensator);
    }

    @Test
    public void testExploreStaticVarCompensators() {
        var expectedIdsForSubnetwork1 = List.of(id("svc1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("svc1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getStaticVarCompensators,
                Network::getStaticVarCompensatorStream,
                Network::getStaticVarCompensatorCount,
                Network::getStaticVarCompensator);
    }

    @Test
    public void testExploreBusbarSections() {
        var expectedIdsForSubnetwork1 = List.of(id("voltageLevel1BusbarSection1", ID_1),
                id("voltageLevel1BusbarSection2", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("voltageLevel1BusbarSection1", ID_2),
                id("voltageLevel1BusbarSection2", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getBusbarSections,
                Network::getBusbarSectionStream,
                Network::getBusbarSectionCount,
                Network::getBusbarSection);
    }

    @Test
    public void testExploreSwitches() {
        var expectedIdsForSubnetwork1 = List.of(id("voltageLevel1Breaker1", ID_1),
                id("load1Disconnector1", ID_1),
                id("load1Breaker1", ID_1),
                id("generator1Disconnector1", ID_1),
                id("generator1Breaker1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("voltageLevel1Breaker1", ID_2),
                id("load1Disconnector1", ID_2),
                id("load1Breaker1", ID_2),
                id("generator1Disconnector1", ID_2),
                id("generator1Breaker1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getSwitches,
                Network::getSwitchStream,
                Network::getSwitchCount,
                Network::getSwitch);
    }

    @Test
    public void testExploreHvdcConverterStations() {
        var lccIdsForSubnetwork1 = List.of(id("lcc1", ID_1),
                id("lcc2", ID_1));
        var lccIdsForSubnetwork2 = List.of(id("lcc1", ID_2),
                id("lcc2", ID_2));
        testExploreElements(lccIdsForSubnetwork1, lccIdsForSubnetwork2,
                Network::getLccConverterStations,
                Network::getLccConverterStationStream,
                Network::getLccConverterStationCount,
                Network::getLccConverterStation);

        var vscIdsForSubnetwork1 = List.of(id("vsc1", ID_1),
                id("vsc2", ID_1));
        var vscIdsForSubnetwork2 = List.of(id("vsc1", ID_2),
                id("vsc2", ID_2));
        testExploreElements(vscIdsForSubnetwork1, vscIdsForSubnetwork2,
                Network::getVscConverterStations,
                Network::getVscConverterStationStream,
                Network::getVscConverterStationCount,
                Network::getVscConverterStation);

        var hvdcConvertersForSubnetwork1 = concat(lccIdsForSubnetwork1, vscIdsForSubnetwork1);
        var hvdcConvertersForSubnetwork2 = concat(lccIdsForSubnetwork2, vscIdsForSubnetwork2);
        testExploreElements(hvdcConvertersForSubnetwork1, hvdcConvertersForSubnetwork2,
                Network::getHvdcConverterStations,
                Network::getHvdcConverterStationStream,
                Network::getHvdcConverterStationCount,
                Network::getHvdcConverterStation);
    }

    @Test
    public void testExploreHvdcLines() {
        String n1HvdcLine1 = id("hvdcLine1", ID_1);
        String n1HvdcLine2 = id("hvdcLine2", ID_1);
        String n2HvdcLine1 = id("hvdcLine1", ID_2);
        String n2HvdcLine2 = id("hvdcLine2", ID_2);
        var expectedIdsForSubnetwork1 = List.of(n1HvdcLine1, n1HvdcLine2);
        var expectedIdsForSubnetwork2 = List.of(n2HvdcLine1, n2HvdcLine2);
        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getHvdcLines,
                Network::getHvdcLineStream,
                Network::getHvdcLineCount,
                Network::getHvdcLine);

        HvdcConverterStation<?> converter = merged.getLccConverterStation(id("lcc1", ID_1));
        assertEquals(n1HvdcLine1, merged.getHvdcLine(converter).getId());
        assertEquals(n1HvdcLine1, subnetwork1.getHvdcLine(converter).getId());
        assertNull(subnetwork2.getHvdcLine(converter));

        converter = merged.getVscConverterStation(id("vsc2", ID_1));
        assertEquals(n1HvdcLine2, merged.getHvdcLine(converter).getId());
        assertEquals(n1HvdcLine2, subnetwork1.getHvdcLine(converter).getId());
        assertNull(subnetwork2.getHvdcLine(converter));

        converter = merged.getVscConverterStation(id("vsc1", ID_2));
        assertEquals(n2HvdcLine2, merged.getHvdcLine(converter).getId());
        assertNull(subnetwork1.getHvdcLine(converter));
        assertEquals(n2HvdcLine2, subnetwork2.getHvdcLine(converter).getId());

        converter = merged.getLccConverterStation(id("lcc2", ID_2));
        assertEquals(n2HvdcLine1, merged.getHvdcLine(converter).getId());
        assertNull(subnetwork1.getHvdcLine(converter));
        assertEquals(n2HvdcLine1, subnetwork2.getHvdcLine(converter).getId());
    }

    @Test
    public void testExploreThreeWindingsTransformers() {
        var expectedIdsForSubnetwork1 = List.of(id("threeWindingsTransformer1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("threeWindingsTransformer1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getThreeWindingsTransformers,
                Network::getThreeWindingsTransformerStream,
                Network::getThreeWindingsTransformerCount,
                Network::getThreeWindingsTransformer);
    }

    @Test
    public void testExploreTwoWindingsTransformers() {
        var expectedIdsForSubnetwork1 = List.of(id("twoWindingsTransformer1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("twoWindingsTransformer1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getTwoWindingsTransformers,
                Network::getTwoWindingsTransformerStream,
                Network::getTwoWindingsTransformerCount,
                Network::getTwoWindingsTransformer);
    }

    @Test
    public void testExploreLines() {
        var expectedIdsForSubnetwork1 = List.of(id("line1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("line1", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getLines,
                Network::getLineStream,
                Network::getLineCount,
                Network::getLine);
    }

    @Test
    public void testExploreDanglingLines() {
        var expectedIdsForSubnetwork1 = List.of(id("danglingLine1", ID_1),
                id("danglingLine2", ID_1),
                id("danglingLine3", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("danglingLine1", ID_2),
                id("danglingLine2", ID_2),
                id("danglingLine3", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getDanglingLines,
                Network::getDanglingLineStream,
                Network::getDanglingLineCount,
                Network::getDanglingLine);
    }

    @Test
    public void testExploreTieLines() {
        var expectedIdsOnlyForMerged = List.of(SHARED_TIE_LINE);
        var expectedIdsForSubnetwork1 = List.of(id("tieLine1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("tieLine1", ID_2));

        testExploreElements(expectedIdsOnlyForMerged, expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getTieLines,
                Network::getTieLineStream,
                Network::getTieLineCount,
                Network::getTieLine);
    }

    @Test
    public void testExploreBranches() {
        var expectedIdsOnlyForMerged = List.of(SHARED_TIE_LINE);
        var expectedIdsForSubnetwork1 = List.of(id("line1", ID_1),
                id("twoWindingsTransformer1", ID_1),
                id("tieLine1", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("line1", ID_2),
                id("twoWindingsTransformer1", ID_2),
                id("tieLine1", ID_2));

        testExploreElements(expectedIdsOnlyForMerged, expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getBranches,
                Network::getBranchStream,
                Network::getBranchCount,
                Network::getBranch);
    }

    @Test
    public void testExploreOverloadManagementSystems() {
        var expectedIdsForSubnetwork1 = List.of(id("overloadManagementSystem", ID_1));
        var expectedIdsForSubnetwork2 = List.of(id("overloadManagementSystem", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getOverloadManagementSystems,
                Network::getOverloadManagementSystemStream,
                Network::getOverloadManagementSystemCount,
                Network::getOverloadManagementSystem);
    }

    @Test
    public void testExploreConnectables() {
        var expectedIdsForSubnetwork1 = List.of(id("battery1", ID_1),
                id("voltageLevel1BusbarSection1", ID_1), id("voltageLevel1BusbarSection2", ID_1),
                id("generator1", ID_1),
                id("lcc1", ID_1), id("lcc2", ID_1),
                id("line1", ID_1),
                id("load1", ID_1),
                id("shuntCompensator1", ID_1),
                id("svc1", ID_1),
                id("vsc1", ID_1), id("vsc2", ID_1),
                id("threeWindingsTransformer1", ID_1),
                id("twoWindingsTransformer1", ID_1),
                id("danglingLine1", ID_1), id("danglingLine2", ID_1), id("danglingLine3", ID_1));

        var expectedIdsForSubnetwork2 = List.of(id("battery1", ID_2),
                id("voltageLevel1BusbarSection1", ID_2), id("voltageLevel1BusbarSection2", ID_2),
                id("generator1", ID_2),
                id("lcc1", ID_2), id("lcc2", ID_2),
                id("line1", ID_2),
                id("load1", ID_2),
                id("shuntCompensator1", ID_2),
                id("svc1", ID_2),
                id("vsc1", ID_2), id("vsc2", ID_2),
                id("threeWindingsTransformer1", ID_2),
                id("twoWindingsTransformer1", ID_2),
                id("danglingLine1", ID_2), id("danglingLine2", ID_2), id("danglingLine3", ID_2));

        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getConnectables,
                Network::getConnectableStream,
                Network::getConnectableCount,
                Network::getConnectable);

        expectedIdsForSubnetwork1 = List.of(id("battery1", ID_1));
        expectedIdsForSubnetwork2 = List.of(id("battery1", ID_2));
        testExploreElements(expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                n -> n.getConnectables(Battery.class),
                n -> n.getConnectableStream(Battery.class),
                n -> n.getConnectableCount(Battery.class),
                null);
    }

    @Test
    public void testExploreIdentifiables() {
        var expectedIdsOnlyForMerged = List.of(SHARED_TIE_LINE, "merged", "n1_network", "n2_network");
        Collection<String> expectedIdsForSubnetwork1 = new ArrayList<>(n1Identifiables);
        Collection<String> expectedIdsForSubnetwork2 = new ArrayList<>(n2Identifiables);

        testExploreElements(expectedIdsOnlyForMerged, expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                Network::getIdentifiables,
                null,
                null,
                Network::getIdentifiable);

        expectedIdsForSubnetwork1 = List.of(id("generator1", ID_1));
        expectedIdsForSubnetwork2 = List.of(id("generator1", ID_2));
        assertIds(expectedIdsForSubnetwork1, subnetwork1.getIdentifiableStream(IdentifiableType.GENERATOR));
        assertIds(expectedIdsForSubnetwork2, subnetwork2.getIdentifiableStream(IdentifiableType.GENERATOR));
    }

    private <T extends Identifiable<?>> void testExploreElements(Collection<String> expectedIdsForSubnetwork1,
                                                                 Collection<String> expectedIdsForSubnetwork2,
                                                                 Function<Network, Iterable<T>> getIterableFunction,
                                                                 Function<Network, Stream<T>> getStreamFunction,
                                                                 Function<Network, Integer> getCountFunction,
                                                                 BiFunction<Network, String, T> getElementByIdFunction) {
        testExploreElements(Collections.emptyList(),
                expectedIdsForSubnetwork1, expectedIdsForSubnetwork2,
                getIterableFunction, getStreamFunction, getCountFunction, getElementByIdFunction);
    }

    private <T extends Identifiable<?>> void testExploreElements(Collection<String> expectedIdsOnlyForMerged,
                                                                 Collection<String> expectedIdsForSubnetwork1,
                                                                 Collection<String> expectedIdsForSubnetwork2,
                                                                 Function<Network, Iterable<T>> getIterableFunction,
                                                                 Function<Network, Stream<T>> getStreamFunction,
                                                                 Function<Network, Integer> getCountFunction,
                                                                 BiFunction<Network, String, T> getElementByIdFunction) {

        var expectedIdsForMerged = concat(expectedIdsOnlyForMerged, expectedIdsForSubnetwork1, expectedIdsForSubnetwork2);

        // Test the function returning an Iterable of <T> elements
        assertIds(expectedIdsForMerged, getIterableFunction.apply(merged));
        assertIds(expectedIdsForSubnetwork1, getIterableFunction.apply(subnetwork1));
        assertIds(expectedIdsForSubnetwork2, getIterableFunction.apply(subnetwork2));

        // Test the function returning a Stream of <T> elements
        if (getStreamFunction != null) {
            assertIds(expectedIdsForMerged, getStreamFunction.apply(merged));
            assertIds(expectedIdsForSubnetwork1, getStreamFunction.apply(subnetwork1));
            assertIds(expectedIdsForSubnetwork2, getStreamFunction.apply(subnetwork2));
        }

        // Test the function returning the <T> elements count
        if (getCountFunction != null) {
            assertEquals(expectedIdsForMerged.size(), getCountFunction.apply(merged));
            assertEquals(expectedIdsForSubnetwork1.size(), getCountFunction.apply(subnetwork1));
            assertEquals(expectedIdsForSubnetwork2.size(), getCountFunction.apply(subnetwork2));
        }

        // Test the function returning the <T> element from its id
        if (getElementByIdFunction != null) {
            String idInSubnetwork1 = findOneElement(expectedIdsForSubnetwork1);
            String idInSubnetwork2 = findOneElement(expectedIdsForSubnetwork2);
            assertNotNull(getElementByIdFunction.apply(merged, idInSubnetwork1));
            assertNotNull(getElementByIdFunction.apply(merged, idInSubnetwork2));
            assertNotNull(getElementByIdFunction.apply(subnetwork1, idInSubnetwork1));
            assertNull(getElementByIdFunction.apply(subnetwork1, idInSubnetwork2));
            assertNull(getElementByIdFunction.apply(subnetwork2, idInSubnetwork1));
            assertNotNull(getElementByIdFunction.apply(subnetwork2, idInSubnetwork2));
            if (!expectedIdsOnlyForMerged.isEmpty()) {
                String idOnlyInMerged = findOneElement(expectedIdsOnlyForMerged);
                assertNotNull(getElementByIdFunction.apply(merged, idOnlyInMerged));
                assertNull(getElementByIdFunction.apply(subnetwork1, idOnlyInMerged));
                assertNull(getElementByIdFunction.apply(subnetwork2, idOnlyInMerged));
            }
        }
    }

    private static String findOneElement(Collection<String> expectedIdsForSubnetwork1) {
        // sorted, for reproducibility
        return expectedIdsForSubnetwork1.stream().sorted().findFirst().orElseThrow();
    }

    @SafeVarargs
    private static Collection<String> concat(Collection<String>... lists) {
        Collection<String> res = new HashSet<>();
        for (var l : lists) {
            res.addAll(l);
        }
        return res;
    }

    private <T> void assertCollection(Collection<T> expected, Collection<T> actual) {
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }

    private void assertIds(Collection<String> expectedIds, Iterable<? extends Identifiable<?>> iterable) {
        assertIds(expectedIds, StreamSupport.stream(iterable.spliterator(), false));
    }

    private void assertIds(Collection<String> expectedIds, Stream<? extends Identifiable<?>> stream) {
        Set<String> ids = stream.map(Identifiable::getId).collect(Collectors.toSet());
        assertCollection(expectedIds, ids);
    }
}

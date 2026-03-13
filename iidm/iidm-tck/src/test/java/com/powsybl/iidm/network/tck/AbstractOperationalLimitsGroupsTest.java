/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import com.powsybl.iidm.network.util.LoadingLimitsUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
public abstract class AbstractOperationalLimitsGroupsTest {

    private static Network createNetworkWithLines() {
        Network network = Network.create("test", "test");
        Substation s1 = network.newSubstation().setId("S1").setCountry(Country.FR).add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus().setId("B1").add();
        Substation s2 = network.newSubstation().setId("S2").setCountry(Country.FR).add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus().setId("B2").add();
        network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .add();

        network.newLine()
                .setId("L2")
                .setVoltageLevel1("VL1")
                .setConnectableBus1("B1")
                .setBus1("B1")
                .setVoltageLevel2("VL2")
                .setConnectableBus2("B2")
                .setBus2("B2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .add();
        return network;
    }

    private static Network createNetworkWithOperationalLimitsGroupsOnLine() {
        Network network = createNetworkWithLines();
        Line l = network.getLine("L");
        l.newOperationalLimitsGroup1("1")
                .newCurrentLimits()
                .setPermanentLimit(900.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1100.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1300.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500.0)
                .endTemporaryLimit()
                .add();
        l.newOperationalLimitsGroup1("2")
                .newActivePowerLimits()
                .setPermanentLimit(750.0)
                .beginTemporaryLimit()
                .setName("25'")
                .setAcceptableDuration(25 * 60)
                .setValue(1250.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("7'")
                .setAcceptableDuration(7 * 60)
                .setValue(1350.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("2'")
                .setAcceptableDuration(2 * 60)
                .setValue(1550.0)
                .endTemporaryLimit()
                .add();
        l.getOperationalLimitsGroup1("2")
                .ifPresent(olg -> olg.newCurrentLimits()
                        .setPermanentLimit(1000.0)
                        .beginTemporaryLimit()
                        .setName("20'")
                        .setAcceptableDuration(20 * 60)
                        .setValue(1200.0)
                        .endTemporaryLimit()
                        .beginTemporaryLimit()
                        .setName("5'")
                        .setAcceptableDuration(5 * 60)
                        .setValue(1400.0)
                        .endTemporaryLimit()
                        .beginTemporaryLimit()
                        .setName("1'")
                        .setAcceptableDuration(60)
                        .setValue(1600.0)
                        .endTemporaryLimit()
                        .add());
        l.getOperationalLimitsGroup1("2")
                .ifPresent(olg -> olg.newApparentPowerLimits()
                        .setPermanentLimit(890.0)
                        .add());
        l.newOperationalLimitsGroup1("3").newApparentPowerLimits().setPermanentLimit(850.0).add();
        l.getOperationalLimitsGroup1("3")
                .ifPresent(olg -> olg.newCurrentLimits()
                        .setPermanentLimit(1000.0)
                        .add());
        l.newOperationalLimitsGroup2("1").newCurrentLimits().setPermanentLimit(850.0).add();
        l.getOperationalLimitsGroup2("1")
                .flatMap(OperationalLimitsGroup::getCurrentLimits)
                .ifPresent(cl -> cl.setPermanentLimit(800.0));
        l.setSelectedOperationalLimitsGroup2("1");
        l.getOperationalLimitsGroup2("1")
                .ifPresent(olg -> olg.newApparentPowerLimits()
                        .setPermanentLimit(1100.0)
                        .add());
        l.getOperationalLimitsGroup2("1")
                .flatMap(OperationalLimitsGroup::getApparentPowerLimits)
                .ifPresent(apl -> apl.setPermanentLimit(900.0));
        l.getOperationalLimitsGroup1("3").orElseThrow().setProperty("propName1", "propValue1");
        l.getOperationalLimitsGroup2("1").orElseThrow().setProperty("propName2", "propValue2");
        return network;
    }

    @Test
    public void testForOperationalLimitsGroupsOnLine() {
        Network network = createNetworkWithOperationalLimitsGroupsOnLine();
        Line l = network.getLine("L");

        // Set some values for limits on side 1
        l.getTerminal1().getBusBreakerView().getBus().setV(390.0);
        l.getTerminal1().setP(800.0).setQ(400.0); // i = 1324.0969
        assertFalse(Double.isNaN(l.getTerminal1().getI()));

        checkBehaveLikeNoLimitsWhenNoDefaultOneOnLine(l);

        checkDefaultLimitsOnLine(l);

        checkChangeOfDefaultLimitsOnLine(l);

        checkRemoveNonDefaultLimitsHasNoImpactOnLine(l);

        checkRemoveOnDefaultLimitsHasAnImpactOnLine(l);

        checkRemoveDefaultLimitsOnLine(l);

        // Set some values for limits on side 2
        l.getTerminal2().getBusBreakerView().getBus().setV(390.0);
        l.getTerminal2().setP(900.0).setQ(500.0); // i = 1524.1499
        assertFalse(Double.isNaN(l.getTerminal2().getI()));

        checkLimitsOnSecondHolder(l);

        checkBehaveLikeNoLimitsWhenCancelDefaultLimits(l);
    }

    private static void checkBehaveLikeNoLimitsWhenCancelDefaultLimits(Line l) {
        // cancel default limits, no group is the default limits, impact expected, no more limits
        l.cancelSelectedOperationalLimitsGroup2();
        assertFalse(l.checkPermanentLimit2(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit2(LimitType.ACTIVE_POWER));
        assertFalse(l.checkPermanentLimit2(LimitType.APPARENT_POWER));
    }

    private static void checkLimitsOnSecondHolder(Line l) {
        // A group by default is defined and fill, but no active power defined
        assertTrue(l.checkPermanentLimit2(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit2(LimitType.ACTIVE_POWER));
        assertTrue(l.checkPermanentLimit2(LimitType.APPARENT_POWER));
    }

    private static void checkRemoveDefaultLimitsOnLine(Line l) {
        // Remove default limits group, impact expected, no more limits
        l.removeOperationalLimitsGroup1("3");
        assertFalse(l.isOverloaded());
        assertEquals(2147483647L, l.getOverloadDuration());
        assertFalse(l.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit1(LimitType.ACTIVE_POWER));
        assertFalse(l.checkPermanentLimit1(LimitType.APPARENT_POWER));
    }

    private static void checkRemoveOnDefaultLimitsHasAnImpactOnLine(Line l) {
        // Remove limits inside default limits group, impact expected, no more current limits
        l.getOperationalLimitsGroup1("3").ifPresent(OperationalLimitsGroup::removeCurrentLimits);
        assertFalse(l.isOverloaded());
        assertEquals(2147483647L, l.getOverloadDuration());
        assertFalse(l.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit1(LimitType.ACTIVE_POWER));
        assertTrue(l.checkPermanentLimit1(LimitType.APPARENT_POWER));
        assertFalse(l.getOperationalLimitsGroup1("3").map(OperationalLimitsGroup::isEmpty).orElse(true));
    }

    private static void checkRemoveNonDefaultLimitsHasNoImpactOnLine(Line l) {
        // Remove limits inside non default limits group, so no impact expected
        l.getOperationalLimitsGroup1("2").ifPresent(OperationalLimitsGroup::removeCurrentLimits);
        assertTrue(l.isOverloaded());
        assertEquals(2147483647L, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit1(LimitType.ACTIVE_POWER));
        assertTrue(l.checkPermanentLimit1(LimitType.APPARENT_POWER));
    }

    private static void checkChangeOfDefaultLimitsOnLine(Line l) {
        // Change default limits group, so active limits is changed, this one have no temporary limits
        l.setSelectedOperationalLimitsGroup1("3");
        assertTrue(l.isOverloaded());
        assertEquals(2147483647L, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit1(LimitType.ACTIVE_POWER));
        assertNull(l.checkTemporaryLimits1(LimitType.ACTIVE_POWER));
        assertTrue(l.checkPermanentLimit1(LimitType.APPARENT_POWER));
        assertNull(l.checkTemporaryLimits1(LimitType.APPARENT_POWER));
    }

    private static void checkDefaultLimitsOnLine(Line l) {
        // Set a default limits group, so we have an active limits
        l.setSelectedOperationalLimitsGroup1("2");
        assertTrue(l.isOverloaded());
        assertEquals(5 * 60L, l.getOverloadDuration());
        assertTrue(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNotNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertEquals(5 * 60L, l.checkTemporaryLimits1(LimitType.CURRENT).getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, l.checkTemporaryLimits1(LimitType.CURRENT).getPreviousLimit(), 0.0);
        assertTrue(l.checkPermanentLimit1(LimitType.ACTIVE_POWER));
        assertNotNull(l.checkTemporaryLimits1(LimitType.ACTIVE_POWER));
        assertEquals(25 * 60L, l.checkTemporaryLimits1(LimitType.ACTIVE_POWER)
                .getTemporaryLimit()
                .getAcceptableDuration());
        assertEquals(750.0, l.checkTemporaryLimits1(LimitType.ACTIVE_POWER).getPreviousLimit(), 0.0);
        assertTrue(l.checkPermanentLimit1(LimitType.APPARENT_POWER));
        assertNull(l.checkTemporaryLimits1(LimitType.APPARENT_POWER));
    }

    private static void checkBehaveLikeNoLimitsWhenNoDefaultOneOnLine(Line l) {
        // No limits group by default, so no limits, so everything is false or not-equals
        assertFalse(l.isOverloaded());
        assertNotEquals(5 * 60L, l.getOverloadDuration());
        assertFalse(l.checkPermanentLimit1(LimitType.CURRENT));
        assertNull(l.checkTemporaryLimits1(LimitType.CURRENT));
        assertFalse(l.checkPermanentLimit1(LimitType.ACTIVE_POWER));
        assertNull(l.checkTemporaryLimits1(LimitType.ACTIVE_POWER));
        assertFalse(l.checkPermanentLimit1(LimitType.APPARENT_POWER));
        assertNull(l.checkTemporaryLimits1(LimitType.APPARENT_POWER));
    }

    private static Network createNetworkWithTWT() {
        Network network = Network.create("test_3wt", "test");
        Substation s1 = network.newSubstation()
                .setId("S1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1")
                .add();
        VoltageLevel vl2 = s1.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VL3")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl3.getBusBreakerView().newBus()
                .setId("B3")
                .add();
        s1.newThreeWindingsTransformer()
                .setId("3WT")
                .setRatedU0(132.0)
                .newLeg1()
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(132.0)
                .setVoltageLevel("VL1")
                .setBus("B1")
                .add()
                .newLeg2() // not used for the test
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(132.0)
                .setVoltageLevel("VL2")
                .setBus("B2")
                .add()
                .newLeg3() // not used for the test
                .setR(1.0)
                .setX(1.0)
                .setG(0.0)
                .setB(0.0)
                .setRatedU(132.0)
                .setVoltageLevel("VL3")
                .setBus("B3")
                .add()
                .add();
        return network;
    }

    private static Network createNetworkWithOperationalLimitsGroupsOnTWTLeg3() {
        Network network = createNetworkWithTWT();
        ThreeWindingsTransformer.Leg l = network.getThreeWindingsTransformer("3WT").getLeg3();
        l.newOperationalLimitsGroup("1")
                .newCurrentLimits()
                .setPermanentLimit(900.0)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1100.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1300.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("1'")
                .setAcceptableDuration(60)
                .setValue(1500.0)
                .endTemporaryLimit()
                .add();
        l.newOperationalLimitsGroup("2")
                .newActivePowerLimits()
                .setPermanentLimit(750.0)
                .beginTemporaryLimit()
                .setName("25'")
                .setAcceptableDuration(25 * 60)
                .setValue(1250.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("7'")
                .setAcceptableDuration(7 * 60)
                .setValue(1350.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("2'")
                .setAcceptableDuration(2 * 60)
                .setValue(1550.0)
                .endTemporaryLimit()
                .add();
        l.getOperationalLimitsGroup("2")
                .ifPresent(olg -> olg.newCurrentLimits()
                        .setPermanentLimit(1000.0)
                        .beginTemporaryLimit()
                        .setName("20'")
                        .setAcceptableDuration(20 * 60)
                        .setValue(1200.0)
                        .endTemporaryLimit()
                        .beginTemporaryLimit()
                        .setName("5'")
                        .setAcceptableDuration(5 * 60)
                        .setValue(1400.0)
                        .endTemporaryLimit()
                        .beginTemporaryLimit()
                        .setName("1'")
                        .setAcceptableDuration(60)
                        .setValue(1600.0)
                        .endTemporaryLimit()
                        .add());
        l.getOperationalLimitsGroup("2")
                .ifPresent(olg -> olg.newApparentPowerLimits()
                        .setPermanentLimit(890.0)
                        .add());
        l.newOperationalLimitsGroup("3").newApparentPowerLimits().setPermanentLimit(850.0).add();
        l.getOperationalLimitsGroup("3")
                .ifPresent(olg -> olg.newCurrentLimits()
                        .setPermanentLimit(1000.0)
                        .add());
        return network;
    }

    @Test
    public void testForOperationalLimitsGroupsOn3WTLeg3() {
        Network network = createNetworkWithOperationalLimitsGroupsOnTWTLeg3();
        ThreeWindingsTransformer t = network.getThreeWindingsTransformer("3WT");
        ThreeWindingsTransformer.Leg l = t.getLeg3();

        // Set some values for limits on side 1
        l.getTerminal().getBusBreakerView().getBus().setV(390.0);
        l.getTerminal().setP(800.0).setQ(400.0); // i = 1324.0969
        assertFalse(Double.isNaN(l.getTerminal().getI()));

        checkBehaveLikeNoLimitsWhenNoDefaultOneOnTWT(t);

        checkDefaultLimitsOnTWT(l, t);

        checkChangeOfDefaultLimitsOnTWT(l, t);

        checkRemoveNonDefaultLimitsHasNoImpactOnTWT(l, t);

        checkRemoveOnDefaultLimitsHasAnImpactOnTWT(l, t);

        checkRemoveDefaultLimitsOnTWT(l, t);
    }

    private static void checkRemoveDefaultLimitsOnTWT(ThreeWindingsTransformer.Leg l, ThreeWindingsTransformer t) {
        // Remove default limits group, impact expected, no more limits
        l.removeOperationalLimitsGroup("3");
        assertFalse(t.isOverloaded());
        assertEquals(2147483647L, t.getOverloadDuration());
        assertFalse(t.checkPermanentLimit3(LimitType.CURRENT));
        assertFalse(t.checkPermanentLimit3(LimitType.ACTIVE_POWER));
        assertFalse(t.checkPermanentLimit3(LimitType.APPARENT_POWER));
    }

    private static void checkRemoveOnDefaultLimitsHasAnImpactOnTWT(ThreeWindingsTransformer.Leg l, ThreeWindingsTransformer t) {
        // Remove limits inside default limits group, impact expected, no more current limits
        l.getOperationalLimitsGroup("3").ifPresent(OperationalLimitsGroup::removeCurrentLimits);
        assertFalse(t.isOverloaded());
        assertEquals(2147483647L, t.getOverloadDuration());
        assertFalse(t.checkPermanentLimit3(LimitType.CURRENT));
        assertFalse(t.checkPermanentLimit3(LimitType.ACTIVE_POWER));
        assertTrue(t.checkPermanentLimit3(LimitType.APPARENT_POWER));
        assertFalse(l.getOperationalLimitsGroup("3").map(OperationalLimitsGroup::isEmpty).orElse(true));
    }

    private static void checkRemoveNonDefaultLimitsHasNoImpactOnTWT(ThreeWindingsTransformer.Leg l, ThreeWindingsTransformer t) {
        // Remove limits inside non default limits group, so no impact expected
        l.getOperationalLimitsGroup("2").ifPresent(OperationalLimitsGroup::removeCurrentLimits);
        assertTrue(t.isOverloaded());
        assertEquals(2147483647L, t.getOverloadDuration());
        assertTrue(t.checkPermanentLimit3(LimitType.CURRENT));
        assertFalse(t.checkPermanentLimit3(LimitType.ACTIVE_POWER));
        assertTrue(t.checkPermanentLimit3(LimitType.APPARENT_POWER));
    }

    private static void checkChangeOfDefaultLimitsOnTWT(ThreeWindingsTransformer.Leg l, ThreeWindingsTransformer t) {
        // Change default limits group, so active limits is changed, this one have no temporary limits
        l.setSelectedOperationalLimitsGroup("3");
        assertTrue(t.isOverloaded());
        assertEquals(2147483647L, t.getOverloadDuration());
        assertTrue(t.checkPermanentLimit3(LimitType.CURRENT));
        assertNull(t.checkTemporaryLimits1(LimitType.CURRENT));
        assertFalse(t.checkPermanentLimit3(LimitType.ACTIVE_POWER));
        assertNull(t.checkTemporaryLimits1(LimitType.ACTIVE_POWER));
        assertTrue(t.checkPermanentLimit3(LimitType.APPARENT_POWER));
        assertNull(t.checkTemporaryLimits1(LimitType.APPARENT_POWER));
    }

    private static void checkDefaultLimitsOnTWT(ThreeWindingsTransformer.Leg l, ThreeWindingsTransformer t) {
        // Set a default limits group, so we have an active limits
        l.setSelectedOperationalLimitsGroup("2");
        assertTrue(t.isOverloaded());
        assertEquals(5 * 60L, t.getOverloadDuration());
        assertTrue(t.checkPermanentLimit3(LimitType.CURRENT));
        assertNotNull(t.checkTemporaryLimits3(LimitType.CURRENT));
        assertEquals(5 * 60L, t.checkTemporaryLimits3(LimitType.CURRENT).getTemporaryLimit().getAcceptableDuration());
        assertEquals(1200.0, t.checkTemporaryLimits3(LimitType.CURRENT).getPreviousLimit(), 0.0);
        assertTrue(t.checkPermanentLimit3(LimitType.ACTIVE_POWER));
        assertNotNull(t.checkTemporaryLimits3(LimitType.ACTIVE_POWER));
        assertEquals(25 * 60L, t.checkTemporaryLimits3(LimitType.ACTIVE_POWER)
                .getTemporaryLimit()
                .getAcceptableDuration());
        assertEquals(750.0, t.checkTemporaryLimits3(LimitType.ACTIVE_POWER).getPreviousLimit(), 0.0);
        assertTrue(t.checkPermanentLimit3(LimitType.APPARENT_POWER));
        assertNull(t.checkTemporaryLimits3(LimitType.APPARENT_POWER));
    }

    private static void checkBehaveLikeNoLimitsWhenNoDefaultOneOnTWT(ThreeWindingsTransformer t) {
        // No limits group by default, so no limits, so everything is false or not-equals
        assertFalse(t.isOverloaded());
        assertNotEquals(5 * 60L, t.getOverloadDuration());
        assertFalse(t.checkPermanentLimit3(LimitType.CURRENT));
        assertNull(t.checkTemporaryLimits3(LimitType.CURRENT));
        assertFalse(t.checkPermanentLimit3(LimitType.ACTIVE_POWER));
        assertNull(t.checkTemporaryLimits3(LimitType.ACTIVE_POWER));
        assertFalse(t.checkPermanentLimit3(LimitType.APPARENT_POWER));
        assertNull(t.checkTemporaryLimits3(LimitType.APPARENT_POWER));
    }

    @Test
    public void propertiesTests() {
        Network network = createNetworkWithOperationalLimitsGroupsOnLine();
        Line l = network.getLine("L");
        OperationalLimitsGroup group = l.getOperationalLimitsGroup1("1").orElseThrow();
        assertFalse(group.hasProperty());
        String property1 = "property_1";
        String property2 = "property_2";

        group.setProperty(property1, "A");
        assertAll(
                () -> assertTrue(group.hasProperty()),
                () -> assertTrue(group.hasProperty(property1)),
                () -> assertFalse(group.hasProperty(property2)),
                () -> assertEquals(Set.of(property1), group.getPropertyNames()),
                () -> assertEquals("A", group.getProperty(property1)),
                () -> assertNull(group.getProperty(property2)),
                () -> assertEquals("DEFAULT", group.getProperty(property2, "DEFAULT")));

        assertDoesNotThrow(() -> group.removeProperty(property2));
        assertEquals(Set.of(property1), group.getPropertyNames());

        group.removeProperty(property1);
        assertAll(
                () -> assertFalse(group.hasProperty()),
                () -> assertFalse(group.hasProperty(property1)),
                () -> assertEquals(Set.of(), group.getPropertyNames()),
                () -> assertNull(group.getProperty(property1)));
    }

    @Test
    void operationalLimitsGroupOrdering() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        Line line = n.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        line.cancelSelectedOperationalLimitsGroup1();
        line.addSelectedOperationalLimitsGroups(TwoSides.ONE,
            EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
            EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
            EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
            EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
            EurostagTutorialExample1Factory.NOT_ACTIVATED,
            "DEFAULT"
        );
        line.deselectOperationalLimitsGroups(TwoSides.ONE, EurostagTutorialExample1Factory.NOT_ACTIVATED);
        List<String> orderedGroupIds = line.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.ONE);
        List<String> expectedOrderedIds = List.of(
            EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
            EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
            "DEFAULT"
        );
        assertEquals(expectedOrderedIds.size(), orderedGroupIds.size());
    }

    private <L extends LoadingLimits> void assertLimits(Collection<L> actualLimits, Tuple... expectedLimitValues) {
        Assertions.assertThat(actualLimits)
            .hasSize(expectedLimitValues.length)
            .extracting(
                LoadingLimits::getPermanentLimit,
                this::extractTemporaryLimitsSet
            ).containsExactlyInAnyOrder(expectedLimitValues);
    }

    @Test
    void getAllSelectedCurrentLimits() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        Line line = n.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2);
        Collection<CurrentLimits> currentLimits = line.getAllSelectedCurrentLimits(TwoSides.TWO);
        assertLimits(currentLimits,
            new Tuple(500.0, Set.of()),
            new Tuple(200.0, Set.of(600.0, Double.MAX_VALUE)),
            new Tuple(300.0, Set.of(Double.MAX_VALUE))
        );

        line.deselectOperationalLimitsGroups(TwoSides.TWO, EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE);

        currentLimits = (Collection<CurrentLimits>) line.getAllSelectedLimits(LimitType.CURRENT, TwoSides.TWO);

        assertLimits(currentLimits,
            new Tuple(500.0, Set.of()),
            new Tuple(300.0, Set.of(Double.MAX_VALUE))
        );
    }

    @Test
    void getAllSelectedActivePowerLimits() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedActivePowerLimits();
        ThreeWindingsTransformer.Leg legThree = n.getThreeWindingsTransformer(EurostagTutorialExample1Factory.NGEN_V2_NHV1).getLeg(ThreeSides.THREE);
        Collection<ActivePowerLimits> activePowerLimits = legThree.getAllSelectedActivePowerLimits();
        assertLimits(activePowerLimits,
            new Tuple(250.0, Set.of()),
            new Tuple(350.0, Set.of(400.0))
        );

        //check that deselecting a non-selected group does nothing
        legThree.newOperationalLimitsGroup(EurostagTutorialExample1Factory.NOT_ACTIVATED);
        legThree.deselectOperationalLimitsGroups(EurostagTutorialExample1Factory.NOT_ACTIVATED);

        activePowerLimits = (Collection<ActivePowerLimits>) legThree.getAllSelectedLimits(LimitType.ACTIVE_POWER);
        assertEquals(2, activePowerLimits.size());

        legThree.deselectOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE, "DEFAULT");
        activePowerLimits = (Collection<ActivePowerLimits>) legThree.getAllSelectedLimits(LimitType.ACTIVE_POWER);
        assertTrue(activePowerLimits.isEmpty());
    }

    @Test
    void getAllSelectedApparentPowerLimits() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedApparentPowerLimits();
        TwoWindingsTransformer twoWindingsTransformer = n.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NGEN_NHV1);
        Collection<ApparentPowerLimits> apparentPowerLimits = twoWindingsTransformer.getAllSelectedApparentPowerLimits(TwoSides.TWO);
        Assertions.assertThat(apparentPowerLimits)
            .hasSize(2)
            .extracting(
                    LoadingLimits::getPermanentLimit,
                    this::extractTemporaryLimitsSet
            ).containsExactlyInAnyOrder(
                    new Tuple(230.0, Set.of(240.0)),
                    new Tuple(240.0, Set.of(250.0))
            );

        // Shouldn't affect the group 2
        twoWindingsTransformer.cancelSelectedOperationalLimitsGroup1();
        apparentPowerLimits = (Collection<ApparentPowerLimits>) twoWindingsTransformer.getAllSelectedLimits(LimitType.APPARENT_POWER, TwoSides.TWO);
        assertEquals(2, apparentPowerLimits.size());

        twoWindingsTransformer.cancelSelectedOperationalLimitsGroup2();
        apparentPowerLimits = (Collection<ApparentPowerLimits>) twoWindingsTransformer.getAllSelectedLimits(LimitType.APPARENT_POWER, TwoSides.TWO);
        assertEquals(0, apparentPowerLimits.size());
    }

    private Set<Double> extractTemporaryLimitsSet(LoadingLimits limits) {
        return limits.getTemporaryLimits()
            .stream()
            .map(LoadingLimits.TemporaryLimit::getValue)
            .collect(Collectors.toSet());
    }

    @ParameterizedTest
    @MethodSource("provideOverloadDurationWithMultipleSelectedOperationalLimitsGroupArguments")
    void overloadDurationWithMultipleSelectedOperationalLimitsGroup(Network n, String lineId, double p, double q, int expectedOverloadDuration) {
        Line line = n.getLine(lineId);
        line.getTerminal1().setP(p).setQ(q);
        line.getTerminal2().setP(-p).setQ(-q);
        n.getBusBreakerView().getBus(EurostagTutorialExample1Factory.NHV1).setV(400);
        n.getBusBreakerView().getBus(EurostagTutorialExample1Factory.NHV2).setV(400);
        assertEquals(expectedOverloadDuration, line.getOverloadDuration());
    }

    private static Stream<Arguments> provideOverloadDurationWithMultipleSelectedOperationalLimitsGroupArguments() {
        String line1 = EurostagTutorialExample1Factory.NHV1_NHV2_1;
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        return Stream.of(
            Arguments.of(n, line1, 200, 10, Integer.MAX_VALUE), // current = 289 A
            Arguments.of(n, line1, 200, 60, 40 * 60), // 301 A
            Arguments.of(n, line1, 350, 60, 40 * 60), // 512 A
            Arguments.of(n, line1, 450, 60, 10 * 60), // 655 A
            Arguments.of(n, line1, 550, 100, 30), // 806 A
            Arguments.of(n, line1, 700, 100, 0), // 1020 A
            Arguments.of(n, line1, 800, 200, 0), // 1190 A
            Arguments.of(n, line1, 800, 250, 0), // 1209 A
            Arguments.of(n, line1, 1000, 300, 0), // 1506 A
            Arguments.of(n, line1, 1100, 300, 0) // 1645 A
        );
    }

    @Test
    void checkAddWithPredicate() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        Line line = n.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        line.cancelSelectedOperationalLimitsGroup1();
        Assertions.assertThat(line.getOperationalLimitsGroups1().stream().map(OperationalLimitsGroup::getId).toList())
            .containsExactlyInAnyOrder(
                "DEFAULT",
                EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                EurostagTutorialExample1Factory.NOT_ACTIVATED
            );
        assertTrue(line.getAllSelectedOperationalLimitsGroups(TwoSides.ONE).isEmpty());

        // should have activated_1_1 and activated_1_2
        line.addSelectedOperationalLimitsGroupByPredicate(TwoSides.ONE, s -> s.contains("activated_1"));
        Assertions.assertThat(line.getAllSelectedOperationalLimitsGroupIds(TwoSides.ONE))
            .containsExactlyInAnyOrder(
                EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO
            );

        // should also have not_activated now
        line.addSelectedOperationalLimitsGroupByPredicate(TwoSides.ONE, s -> s.contains("activated"));
        Assertions.assertThat(line.getAllSelectedOperationalLimitsGroupIds(TwoSides.ONE))
            .containsExactlyInAnyOrder(
                EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                EurostagTutorialExample1Factory.NOT_ACTIVATED
            );

        // adding with a predicate should not remove activated groups that do not match the predicate
        line.cancelSelectedOperationalLimitsGroup1();
        line.addSelectedOperationalLimitsGroups(TwoSides.ONE, EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO);
        line.addSelectedOperationalLimitsGroupByPredicate(TwoSides.ONE, "DEFAULT"::equals);
        Assertions.assertThat(line.getAllSelectedOperationalLimitsGroupIds(TwoSides.ONE))
            .containsExactlyInAnyOrder(
                EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                "DEFAULT"
            );

        // check that all groups can be activated
        line.addSelectedOperationalLimitsGroupByPredicate(TwoSides.ONE, s -> true);
        Assertions.assertThat(line.getAllSelectedOperationalLimitsGroupIds(TwoSides.ONE))
            .containsExactlyInAnyOrder(
                "DEFAULT",
                EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                EurostagTutorialExample1Factory.NOT_ACTIVATED
            );
    }

    @Test
    void doNotSelectGroupsIfAnyIsNullOrDoesNotExist() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedActivePowerLimits();
        ThreeWindingsTransformer.Leg leg = n.getThreeWindingsTransformer(EurostagTutorialExample1Factory.NGEN_V2_NHV1).getLeg(ThreeSides.THREE);
        leg.deselectOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE);
        assertEquals(List.of("DEFAULT"), leg.getAllSelectedOperationalLimitsGroupIdsOrdered());

        //check no group is selected if any is null
        assertThrows(NullPointerException.class, () -> leg.addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE, null, EurostagTutorialExample1Factory.NOT_ACTIVATED));
        assertEquals(List.of("DEFAULT"), leg.getAllSelectedOperationalLimitsGroupIdsOrdered());

        //check no group selected if any does not exist
        String errorMessage = assertThrows(PowsyblException.class, () -> leg.addSelectedOperationalLimitsGroups(
            EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE,
            "not a group",
            "also wrong",
            EurostagTutorialExample1Factory.NOT_ACTIVATED)
        ).getMessage();
        assertTrue(errorMessage.contains("not a group"));
        assertTrue(errorMessage.contains("also wrong"));
        assertEquals(List.of("DEFAULT"), leg.getAllSelectedOperationalLimitsGroupIdsOrdered());

        leg.addSelectedOperationalLimitsGroups(EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE);
        assertEquals(List.of("DEFAULT", EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE), leg.getAllSelectedOperationalLimitsGroupIdsOrdered());
    }

    @Test
    void checkGetOrCreateSetsSelected() {
        Network n = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedApparentPowerLimits();
        TwoWindingsTransformer transformer = n.getTwoWindingsTransformer(EurostagTutorialExample1Factory.NGEN_NHV1);
        assertFalse(transformer.getAllSelectedOperationalLimitsGroupIds(TwoSides.TWO).contains("DEFAULT"));
        transformer.getOrCreateSelectedOperationalLimitsGroup2();
        assertEquals(List.of("DEFAULT"), transformer.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.TWO));
    }

    @ParameterizedTest
    @MethodSource("provideUtilCheckTemporaryLimitsArguments")
    void violationUtilCheckTemporaryLimits(Identifiable<?> identifiable, ThreeSides side, Collection<Double> limitReductions, LimitType type, double value, Collection<ExpectedOverload> expected) {
        for (double limitReduction : limitReductions) {
            Collection<Overload> overloads = switch (identifiable) {
                case Branch<?> b -> {
                    if (limitReduction == 1) {
                        yield LimitViolationUtils.checkAllTemporaryLimits(b, side.toTwoSides(), new LimitsComputer.NoModificationsImpl(), value, type);
                    } else {
                        //multiply by limitReduction because all the limits will be reduced, so we also want to reduce the actual value to match that.
                        //the arguments to this method test both with and without limitReduction. All the cases with a limitReduction are the exact same case as without, but with a coefficient
                        //To get the same situation, we need to reduce both the limits and the value. The limits are supposed to be reduced by checkAllTemporaryLimits, and we reduce the value in the test
                        yield LimitViolationUtils.checkAllTemporaryLimits(b, side.toTwoSides(), limitReduction, value * limitReduction, type);
                    }
                }
                case ThreeWindingsTransformer t -> {
                    if (limitReduction == 1) {
                        yield LimitViolationUtils.checkAllTemporaryLimits(t, side, new LimitsComputer.NoModificationsImpl(), value, type);
                    } else {
                        //multiply by limitReduction because all the limits will be reduced, so we also want to reduce the actual value to match that.
                        //the arguments to this method test both with and without limitReduction. All the cases with a limitReduction are the exact same case as without, but with a coefficient
                        //To get the same situation, we need to reduce both the limits and the value. The limits are supposed to be reduced by checkAllTemporaryLimits, and we reduce the value in the test
                        yield LimitViolationUtils.checkAllTemporaryLimits(t, side, limitReduction, value * limitReduction, type);
                    }
                }
                default -> throw new UnsupportedOperationException(String.format("The class %s cannot be used to check temporary limits", identifiable.getClass()));
            };

            Assertions.assertThat(overloads)
                .extracting(
                    Overload::getPreviousLimitName,
                    Overload::getOperationalLimitsGroupId,
                    Overload::getPreviousLimit,
                    o -> o.getTemporaryLimit().getAcceptableDuration()
                )
                .containsExactlyInAnyOrderElementsOf(expected.stream()
                    .map(r -> tuple(r.previousLimitName, r.operationalLimitsGroupId, r.limit, r.acceptableDuration))
                    .toList());
        }
    }

    private record ExpectedOverload(String previousLimitName, String operationalLimitsGroupId, double limit, int acceptableDuration) { }

    private static Stream<Arguments> provideUtilCheckTemporaryLimitsArguments() {
        Network networkLine = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        Line l = networkLine.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);

        Network network3wt = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedActivePowerLimits();
        ThreeWindingsTransformer transformer = network3wt.getThreeWindingsTransformer(EurostagTutorialExample1Factory.NGEN_V2_NHV1);

        return Stream.of(
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.95), LimitType.CURRENT, 299, List.of()), // below any permanent limit
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.8), LimitType.CURRENT, 310, List.of(
                new ExpectedOverload(
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    300,
                    60 * 40
                )
            )), // above permanent of activated_1_2
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.82), LimitType.CURRENT, 510, List.of(
                new ExpectedOverload(
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    300,
                    60 * 40
                )
            )), // above permanent of Default, but no temporary above. `checkAllTemporaryLimits` doesn't detect anything in this case. It is the responsibility of `checkPermanentLimit`
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.7), LimitType.CURRENT, 701, List.of(
                new ExpectedOverload(
                    "40'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    700,
                    30
                )
            )), // above first temporary of 1_2
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.92), LimitType.CURRENT, 1122, List.of(
                new ExpectedOverload(
                    "40'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    700,
                    30
                ),
                new ExpectedOverload(
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                    1100,
                    60 * 10
                )
            )), // above permanent of 1_1
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.87), LimitType.CURRENT, 1450, List.of(
                new ExpectedOverload(
                    "40'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    700,
                    30
                ),
                new ExpectedOverload(
                    "10'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                    1200,
                    60
                )
            )), // above first temporary of 1_1
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.3), LimitType.CURRENT, 1500, List.of(
                new ExpectedOverload(
                    "40'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    700,
                    30
                ),
                new ExpectedOverload(
                    "1'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                    1500,
                    0
                )
            )), // above last temporary of 1_1
            Arguments.of(l, ThreeSides.ONE, List.of(1., 0.84), LimitType.CURRENT, 1601, List.of(
                new ExpectedOverload(
                    "0.5'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO,
                    1600,
                    0
                ),
                new ExpectedOverload(
                    "1'",
                    EurostagTutorialExample1Factory.ACTIVATED_ONE_ONE,
                    1500,
                    0
                )
            )), // above last temporary of 1_2
            Arguments.of(transformer, ThreeSides.THREE, List.of(1., 0.99), LimitType.ACTIVE_POWER, 200, List.of()), //under all limits
            Arguments.of(transformer, ThreeSides.THREE, List.of(1., 0.96), LimitType.ACTIVE_POWER, 275, List.of()), // above permanent of Default, but no temporary above. `checkAllTemporaryLimits` doesn't detect anything in this case. It is the responsibility of `checkPermanentLimit`
            Arguments.of(transformer, ThreeSides.THREE, List.of(1., 0.88), LimitType.ACTIVE_POWER, 375, List.of(
                new ExpectedOverload(
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE,
                    350,
                    45 * 60
                )
            )), // above permanent of activated_3_1
            Arguments.of(transformer, ThreeSides.THREE, List.of(1., 0.77), LimitType.ACTIVE_POWER, 405, List.of(
                new ExpectedOverload(
                    "45'",
                    EurostagTutorialExample1Factory.ACTIVATED_THREE_ONE,
                    400,
                    0
                )
            )) // above last temporary of activated_3_1
        );
    }

    @Test
    public void testCopy() {
        String name1 = "propName1";
        String value1 = "propValue1";
        String name2 = "propName2";
        String value2 = "propValue2";
        String name3 = "propName3";
        String value3 = "propValue3";

        Network network = EurostagTutorialExample1Factory.createWithMultipleSelectedFixedCurrentLimits();
        Line line = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_1);
        Line line2 = network.getLine(EurostagTutorialExample1Factory.NHV1_NHV2_2);
        line.getOperationalLimitsGroup1("DEFAULT").orElseThrow().setProperty(name1, value1);
        line.getOperationalLimitsGroup2(EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE).orElseThrow().setProperty(name2, value2);
        line.getOperationalLimitsGroup1(EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO).orElseThrow().setProperty(name3, value3);
        LoadingLimitsUtil.copyOperationalLimits(line, line2);

        //check properties are copied
        assertEquals(value1, line2.getOperationalLimitsGroup1("DEFAULT").orElseThrow().getProperty(name1));
        assertEquals(value2, line2.getOperationalLimitsGroup2(EurostagTutorialExample1Factory.ACTIVATED_TWO_ONE).orElseThrow().getProperty(name2));
        assertEquals(value3, line2.getOperationalLimitsGroup1(EurostagTutorialExample1Factory.ACTIVATED_ONE_TWO).orElseThrow().getProperty(name3));

        //check all selected are properly set
        Collection<String> selectedIds1 = line2.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.ONE);
        assertEquals(3, selectedIds1.size());
        assertEquals(line.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.ONE), selectedIds1);

        Collection<String> selectedIds2 = line2.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.TWO);
        assertEquals(2, selectedIds2.size());
        assertEquals(line.getAllSelectedOperationalLimitsGroupIdsOrdered(TwoSides.TWO), selectedIds2);

        //check all groups are copied
        assertEquals(4, line2.getOperationalLimitsGroups1().size());
        assertEquals(4, line2.getOperationalLimitsGroups2().size());
    }
}

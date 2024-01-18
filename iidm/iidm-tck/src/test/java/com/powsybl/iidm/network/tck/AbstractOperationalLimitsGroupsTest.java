/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
public abstract class AbstractOperationalLimitsGroupsTest {

    private static Network createNetworkWithLine() {
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
        return network;
    }

    private static Network createNetworkWithOperationalLimitsGroupsOnLine() {
        Network network = createNetworkWithLine();
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
}

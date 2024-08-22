/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public abstract class AbstractApparentPowerLimitsTest {

    private static Network createNetwork() {
        Network network = EurostagTutorialExample1Factory.create();
        Line line = network.getLine("NHV1_NHV2_2");
        line.newApparentPowerLimits1()
                .setPermanentLimit(300)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(320)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(350)
                .endTemporaryLimit()
                .add();
        line.newApparentPowerLimits2()
                .setPermanentLimit(310)
                .add();
        return network;
    }

    private static void testLimits1(ApparentPowerLimits apparentPowerLimits1) {
        assertNotNull(apparentPowerLimits1);
        assertEquals(LimitType.APPARENT_POWER, apparentPowerLimits1.getLimitType());
        assertEquals(300, apparentPowerLimits1.getPermanentLimit(), 0.0);
        assertEquals(2, apparentPowerLimits1.getTemporaryLimits().size());
        assertEquals("20'", apparentPowerLimits1.getTemporaryLimit(20 * 60).getName());
        assertEquals(320.0, apparentPowerLimits1.getTemporaryLimit(20 * 60).getValue(), 0.0);
        assertEquals("5'", apparentPowerLimits1.getTemporaryLimit(5 * 60).getName());
        assertEquals(350, apparentPowerLimits1.getTemporaryLimit(5 * 60).getValue(), 0.0);
    }

    private static void testLimits2(ApparentPowerLimits apparentPowerLimits2) {
        assertNotNull(apparentPowerLimits2);
        assertEquals(LimitType.APPARENT_POWER, apparentPowerLimits2.getLimitType());
        assertEquals(310, apparentPowerLimits2.getPermanentLimit(), 0.0);
        assertTrue(apparentPowerLimits2.getTemporaryLimits().isEmpty());
    }

    @Test
    public void test() {
        Network network = createNetwork();
        Line l = network.getLine("NHV1_NHV2_2");

        // limits1
        testLimits1(l.getApparentPowerLimits1().orElse(null));
        testLimits1((ApparentPowerLimits) l.getLimits(LimitType.APPARENT_POWER, TwoSides.ONE).orElse(null));

        // limits2
        ApparentPowerLimits apparentPowerLimits2 = l.getApparentPowerLimits2().orElseThrow(IllegalStateException::new);
        testLimits2(apparentPowerLimits2);
        testLimits2((ApparentPowerLimits) l.getLimits(LimitType.APPARENT_POWER, TwoSides.TWO).orElse(null));

        apparentPowerLimits2.remove();
        assertTrue(l.getActivePowerLimits2().isEmpty());
    }

    @Test
    public void testAdderByCopy() {
        // First limit
        Network network = createNetwork();
        Line line = network.getLine("NHV1_NHV2_2");

        ApparentPowerLimitsAdder adder = line.newApparentPowerLimits1()
                .setPermanentLimit(1000.)
                .beginTemporaryLimit()
                .setName("TL1")
                .setAcceptableDuration(20 * 60)
                .setValue(1200.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL2")
                .setAcceptableDuration(10 * 60)
                .setValue(1400.0)
                .endTemporaryLimit()
                .beginTemporaryLimit()
                .setName("TL3")
                .setAcceptableDuration(5 * 60)
                .setValue(1600.0)
                .endTemporaryLimit();
        adder.add();
        ApparentPowerLimits limits1 = line.getApparentPowerLimits1().get();

        // Second limit
        ApparentPowerLimitsAdder adder2 = line.newApparentPowerLimits2(limits1);

        adder2.add();
        ApparentPowerLimits limits2 = line.getApparentPowerLimits2().get();

        // Check
        boolean areIdentical = limits1.getPermanentLimit() == limits2.getPermanentLimit();

        List<LoadingLimits.TemporaryLimit> tempLimits1 = limits1.getTemporaryLimits().stream().toList();
        List<LoadingLimits.TemporaryLimit> tempLimits2 = limits2.getTemporaryLimits().stream().toList();

        if (areIdentical && tempLimits1.size() == tempLimits2.size()) {
            for (int i = 0; i < tempLimits1.size(); i++) {
                LoadingLimits.TemporaryLimit limit1 = tempLimits1.get(i);
                LoadingLimits.TemporaryLimit limit2 = tempLimits2.get(i);

                if (!limit1.getName().equals(limit2.getName()) ||
                        limit1.getAcceptableDuration() != limit2.getAcceptableDuration() ||
                        limit1.getValue() != limit2.getValue()) {
                    areIdentical = false;
                    break;
                }
            }
        } else {
            areIdentical = false;
        }

        // Tests
        assertTrue(areIdentical);
        assertNotNull(limits2);
    }
}

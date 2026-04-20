/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineCouplingsAdder;
import com.powsybl.iidm.network.extensions.LineCouplings;
import com.powsybl.iidm.network.extensions.MutualCoupling;
import com.powsybl.iidm.network.extensions.MutualCouplingAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public abstract class AbstractLineCouplingsTest {

    @Test
    public void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(ZonedDateTime.parse("2016-12-07T11:18:52.881+01:00"));
        network.newExtension(LineCouplingsAdder.class).add();
        network.getExtension(LineCouplings.class)
                .newMutualCoupling()
                    .withLine1(network.getLine("NHV1_NHV2_1"))
                    .withLine2(network.getLine("NHV1_NHV2_2"))
                    .withR(0.5)
                    .withX(1.0)
                .add();

        LineCouplings mutualCouplings = network.getExtension(LineCouplings.class);
        assertEquals(1, mutualCouplings.getMutualCouplings().size());
        MutualCoupling mutualCoupling = mutualCouplings.getMutualCouplings().getFirst();
        assertEquals("NHV1_NHV2_1", mutualCoupling.getLine1().getId());
        assertEquals("NHV1_NHV2_2", mutualCoupling.getLine2().getId());
        assertEquals(0.5, mutualCoupling.getR());
        assertEquals(1.0, mutualCoupling.getX());
        assertEquals(0, mutualCoupling.getLine1Start());
        assertEquals(0, mutualCoupling.getLine2Start());
        assertEquals(1, mutualCoupling.getLine1End());
        assertEquals(1, mutualCoupling.getLine2End());
    }

    @Test
    void testSameLine() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        Line line = network.getLine("NHV1_NHV2_1");
        MutualCouplingAdder adder = network.getExtension(LineCouplings.class)
            .newMutualCoupling()
            .withLine1(line)
            .withLine2(line)
            .withR(0.1)
            .withX(0.2);

        PowsyblException exception = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Lines must be different.", exception.getMessage());
    }

    @Test
    void testNullLine() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        Line line = network.getLine("NHV1_NHV2_1");
        MutualCouplingAdder adder = network.getExtension(LineCouplings.class)
            .newMutualCoupling()
            .withLine1(line)
            .withLine2(null)
            .withR(0.1)
            .withX(0.2);

        PowsyblException exception = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Lines cannot be null.", exception.getMessage());
    }

    @Test
    void testInvalidPositions() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        MutualCouplingAdder adder = network.getExtension(LineCouplings.class)
            .newMutualCoupling()
            .withLine1(network.getLine("NHV1_NHV2_1"))
            .withLine2(network.getLine("NHV1_NHV2_2"))
            .withR(0.1)
            .withX(0.2)
            .withLine1Start(0.8)
            .withLine1End(0.2);

        PowsyblException exception = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Invalid line1 positions: start=0.8, end=0.2", exception.getMessage());
    }

    @Test
    void testPositionsOutOfBounds() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        MutualCouplingAdder adder = network.getExtension(LineCouplings.class)
            .newMutualCoupling()
            .withLine1(network.getLine("NHV1_NHV2_1"))
            .withLine2(network.getLine("NHV1_NHV2_2"))
            .withR(0.1)
            .withX(0.2)
            .withLine1Start(-0.5)
            .withLine1End(2.0);

        PowsyblException exception = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Invalid line1 positions: start=-0.5, end=2.0", exception.getMessage());
    }

    @Test
    void testDuplicateCoupling() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        LineCouplings lc = network.getExtension(LineCouplings.class);

        lc.newMutualCoupling()
            .withLine1(network.getLine("NHV1_NHV2_1"))
            .withLine2(network.getLine("NHV1_NHV2_2"))
            .withR(0.1)
            .withX(0.2)
            .add();

        MutualCouplingAdder adder = lc.newMutualCoupling()
            .withLine1(network.getLine("NHV1_NHV2_2")) // reversed order
            .withLine2(network.getLine("NHV1_NHV2_1"))
            .withR(0.3)
            .withX(0.4);

        PowsyblException exception = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Mutual coupling already exists between lines NHV1_NHV2_2 and NHV1_NHV2_1", exception.getMessage());
    }

    @Test
    void testFindSymmetric() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        LineCouplings lc = network.getExtension(LineCouplings.class);

        Line l1 = network.getLine("NHV1_NHV2_1");
        Line l2 = network.getLine("NHV1_NHV2_2");

        lc.newMutualCoupling()
            .withLine1(l1)
            .withLine2(l2)
            .withR(0.1)
            .withX(0.2)
            .add();

        assertTrue(lc.findMutualCoupling(l1, l2).isPresent());
        assertTrue(lc.findMutualCoupling(l2, l1).isPresent());
    }

    @Test
    void testRemoveByLines() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        LineCouplings lc = network.getExtension(LineCouplings.class);

        Line l1 = network.getLine("NHV1_NHV2_1");
        Line l2 = network.getLine("NHV1_NHV2_2");

        lc.newMutualCoupling()
            .withLine1(l1)
            .withLine2(l2)
            .withR(0.1)
            .withX(0.2)
            .add();

        assertTrue(lc.removeMutualCoupling(l2, l1)); // reversed
        assertEquals(0, lc.getMutualCouplings().size());
    }

    @Test
    void testSetters() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        Line l1 = network.getLine("NHV1_NHV2_1");
        Line l2 = network.getLine("NHV1_NHV2_2");

        MutualCoupling mc = network.getExtension(LineCouplings.class)
            .newMutualCoupling()
            .withLine1(l1)
            .withLine2(l2)
            .withR(0.1)
            .withX(0.2)
            .add();

        assertEquals(0.1, mc.getR());
        assertEquals(0.2, mc.getX());
        assertEquals(0, mc.getLine1Start());
        assertEquals(1, mc.getLine1End());
        assertEquals(0, mc.getLine2Start());
        assertEquals(1, mc.getLine2End());

        mc.setR(0.5);
        mc.setX(1.2);
        mc.setLine1Position(0.2, 0.8);
        mc.setLine2Position(0.1, 0.9);

        assertEquals(0.5, mc.getR());
        assertEquals(1.2, mc.getX());
        assertEquals(0.2, mc.getLine1Start());
        assertEquals(0.8, mc.getLine1End());
        assertEquals(0.1, mc.getLine2Start());
        assertEquals(0.9, mc.getLine2End());
    }

    @Test
    void testSetInvalidLinePosition() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(LineCouplingsAdder.class).add();

        MutualCoupling mc = network.getExtension(LineCouplings.class)
            .newMutualCoupling()
            .withLine1(network.getLine("NHV1_NHV2_1"))
            .withLine2(network.getLine("NHV1_NHV2_2"))
            .withR(0.1)
            .withX(0.2)
            .withLine1Start(0.5)
            .withLine1End(0.8)
            .add();

        PowsyblException exception = assertThrows(PowsyblException.class, () -> mc.setLine1Position(0.6, 0.4));
        assertEquals("Invalid line1 positions: start=0.6, end=0.4", exception.getMessage());

        PowsyblException exception2 = assertThrows(PowsyblException.class, () -> mc.setLine2Position(-0.5, 1.1));
        assertEquals("Invalid line2 positions: start=-0.5, end=1.1", exception2.getMessage());
    }
}

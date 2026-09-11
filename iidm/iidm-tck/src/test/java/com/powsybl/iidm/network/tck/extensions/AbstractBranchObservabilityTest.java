/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
public abstract class AbstractBranchObservabilityTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        assertNotNull(line);
        line.newExtension(BranchObservabilityAdder.class)
                .withObservable(true)
                .withStandardDeviationP1(0.02d)
                .withStandardDeviationP2(0.04d)
                .withRedundantP1(true)
                .withRedundantP2(false)
                .withStandardDeviationQ1(0.5d)
                .withStandardDeviationQ2(1.0d)
                .withRedundantQ1(true)
                .withRedundantQ2(false)
                .add();
        BranchObservability<Line> branchObservability = line.getExtension(BranchObservability.class);
        assertEquals("branchObservability", branchObservability.getName());
        assertEquals("NHV1_NHV2_1", branchObservability.getExtendable().getId());

        assertTrue(branchObservability.isObservable());
        branchObservability.setObservable(false);
        assertFalse(branchObservability.isObservable());

        // P
        assertTrue(branchObservability.getQualityP1().isPresent());
        assertEquals(branchObservability.getNullableQualityP1(), branchObservability.getQualityP1().orElse(null));
        assertTrue(branchObservability.getQualityP2().isPresent());
        assertEquals(branchObservability.getNullableQualityP2(), branchObservability.getQualityP2().orElse(null));
        assertEquals(0.02d, branchObservability.getNullableQualityP1().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityP1().setStandardDeviation(0.03d);
        assertEquals(0.03d, branchObservability.getNullableQualityP1().getStandardDeviation(), 0d);
        assertEquals(0.04d, branchObservability.getNullableQualityP2().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityP2().setStandardDeviation(0.08d);
        assertEquals(0.08d, branchObservability.getNullableQualityP2().getStandardDeviation(), 0d);

        assertTrue(branchObservability.getNullableQualityP1().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityP1().isRedundant().get());
        branchObservability.getNullableQualityP1().setRedundant(false);
        assertTrue(branchObservability.getNullableQualityP1().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityP1().isRedundant().get());
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityP2().isRedundant().get());
        branchObservability.getNullableQualityP2().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().get());

        // Q
        assertTrue(branchObservability.getQualityQ1().isPresent());
        assertTrue(branchObservability.getQualityQ2().isPresent());
        assertEquals(branchObservability.getNullableQualityQ1(), branchObservability.getQualityQ1().orElse(null));
        assertEquals(branchObservability.getNullableQualityQ2(), branchObservability.getQualityQ2().orElse(null));
        assertEquals(0.5d, branchObservability.getNullableQualityQ1().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityQ1().setStandardDeviation(0.6d);
        assertEquals(0.6d, branchObservability.getNullableQualityQ1().getStandardDeviation(), 0d);
        assertEquals(1.0d, branchObservability.getNullableQualityQ2().getStandardDeviation(), 0d);
        branchObservability.getNullableQualityQ2().setStandardDeviation(1.01d);
        assertEquals(1.01d, branchObservability.getNullableQualityQ2().getStandardDeviation(), 0d);

        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().get());
        branchObservability.getNullableQualityQ1().setRedundant(false);
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityQ1().isRedundant().get());
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityQ1().isRedundant().get());
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().isPresent());
        assertFalse(branchObservability.getNullableQualityQ2().isRedundant().get());
        branchObservability.getNullableQualityQ2().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().get());
    }

    @Test
    public void testMissingQuality() {
        Network network = BatteryNetworkFactory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        assertNotNull(line);
        line.newExtension(BranchObservabilityAdder.class)
                .add();
        BranchObservability<Line> branchObservability = line.getExtension(BranchObservability.class);
        assertEquals("branchObservability", branchObservability.getName());
        assertEquals("NHV1_NHV2_1", branchObservability.getExtendable().getId());

        assertFalse(branchObservability.getQualityP1().isPresent());
        assertFalse(branchObservability.getQualityP2().isPresent());
        assertFalse(branchObservability.getQualityQ1().isPresent());
        assertFalse(branchObservability.getQualityQ2().isPresent());

        assertNull(branchObservability.getNullableQualityP1());
        assertNull(branchObservability.getNullableQualityP2());
        assertNull(branchObservability.getNullableQualityQ1());
        assertNull(branchObservability.getNullableQualityQ2());

        // P1
        assertSame(branchObservability, branchObservability.setQualityP1(0.03d));
        assertEquals(0.03d, branchObservability.getNullableQualityP1().getStandardDeviation(), 0d);
        assertSame(branchObservability, branchObservability.setQualityP1(0.04d));
        assertEquals(0.04d, branchObservability.getNullableQualityP1().getStandardDeviation(), 0d);
        assertFalse(branchObservability.getNullableQualityP1().isRedundant().isPresent());
        branchObservability.getNullableQualityP1().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityP1().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityP1().isRedundant().get());

        // P2
        assertSame(branchObservability, branchObservability.setQualityP2(0.031d));
        assertEquals(0.031d, branchObservability.getNullableQualityP2().getStandardDeviation(), 0d);
        assertSame(branchObservability, branchObservability.setQualityP2(0.041d));
        assertEquals(0.041d, branchObservability.getNullableQualityP2().getStandardDeviation(), 0d);

        assertFalse(branchObservability.getNullableQualityP2().isRedundant().isPresent());
        branchObservability.getNullableQualityP2().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityP2().isRedundant().get());

        // Q1
        assertSame(branchObservability, branchObservability.setQualityQ1(0.6d));
        assertEquals(0.6d, branchObservability.getNullableQualityQ1().getStandardDeviation(), 0d);
        assertSame(branchObservability, branchObservability.setQualityQ1(0.61d));
        assertEquals(0.61d, branchObservability.getNullableQualityQ1().getStandardDeviation(), 0d);

        assertFalse(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        branchObservability.getNullableQualityQ1().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityQ1().isRedundant().get());

        // Q2
        assertSame(branchObservability, branchObservability.setQualityQ2(0.6d));
        assertEquals(0.6d, branchObservability.getNullableQualityQ2().getStandardDeviation(), 0d);
        assertSame(branchObservability, branchObservability.setQualityQ2(0.61d));
        assertEquals(0.61d, branchObservability.getNullableQualityQ2().getStandardDeviation(), 0d);

        assertFalse(branchObservability.getNullableQualityQ2().isRedundant().isPresent());
        branchObservability.getNullableQualityQ2().setRedundant(true);
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().isPresent());
        assertTrue(branchObservability.getNullableQualityQ2().isRedundant().get());
    }

    @Test
    public void testRedundancy() {
        Network network = EurostagTutorialExample1Factory.create();
        BranchObservabilityAdder adder = network.getLine("NHV1_NHV2_1").newExtension(BranchObservabilityAdder.class);
        adder.withStandardDeviationP1(0.5)
                .withRedundantP1(false)
                .withStandardDeviationP2(0.2)
                .withRedundantQ1(true)
                .add();
        BranchObservability injectionObservability = network.getLine("NHV1_NHV2_1").getExtension(BranchObservability.class);
        assertNull(injectionObservability.getNullableQualityQ2());
        assertNull(injectionObservability.getNullableQualityQ1());
        assertFalse((Boolean) injectionObservability.getNullableQualityP1().isRedundant().get());
        assertEquals(0.5, injectionObservability.getNullableQualityP1().getStandardDeviation(), 0.01);
        assertFalse(injectionObservability.getNullableQualityP2().isRedundant().isPresent());
        assertEquals(0.2, injectionObservability.getNullableQualityP2().getStandardDeviation(), 0.01);
    }
}

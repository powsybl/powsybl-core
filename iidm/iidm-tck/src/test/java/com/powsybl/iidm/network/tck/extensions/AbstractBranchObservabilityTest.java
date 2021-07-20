/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public abstract class AbstractBranchObservabilityTest {

    @Test
    public void test() {
        Network network = BatteryNetworkFactory.create();
        Line line = network.getLine("NHV1_NHV2_1");
        assertNotNull(line);
        line.newExtension(BranchObservabilityAdder.class)
                .withObservable(true)
                .withStandardDeviationP(0.02d, Branch.Side.ONE)
                .withStandardDeviationP(0.04d, Branch.Side.TWO)
                .withRedundantP(true, Branch.Side.ONE)
                .withRedundantP(false, Branch.Side.TWO)
                .withStandardDeviationQ(0.5d, Branch.Side.ONE)
                .withStandardDeviationQ(1.0d, Branch.Side.TWO)
                .withRedundantQ(true, Branch.Side.ONE)
                .withRedundantQ(false, Branch.Side.TWO)
                .withStandardDeviationV(0.0d, Branch.Side.ONE)
                .withStandardDeviationV(0.2d, Branch.Side.TWO)
                .withRedundantV(true, Branch.Side.ONE)
                .withRedundantV(false, Branch.Side.TWO)
                .add();
        BranchObservability<Line> branchObservability = line.getExtension(BranchObservability.class);
        assertEquals("branchObservability", branchObservability.getName());
        assertEquals("NHV1_NHV2_1", branchObservability.getExtendable().getId());

        assertTrue(branchObservability.isObservable());
        branchObservability.setObservable(false);
        assertFalse(branchObservability.isObservable());

        // P
        assertEquals(0.02d, branchObservability.getStandardDeviationP(Branch.Side.ONE), 0d);
        branchObservability.setStandardDeviationP(0.03d, Branch.Side.ONE);
        assertEquals(0.03d, branchObservability.getStandardDeviationP(Branch.Side.ONE), 0d);
        assertEquals(0.04d, branchObservability.getStandardDeviationP(Branch.Side.TWO), 0d);
        branchObservability.setStandardDeviationP(0.08d, Branch.Side.TWO);
        assertEquals(0.08d, branchObservability.getStandardDeviationP(Branch.Side.TWO), 0d);

        assertTrue(branchObservability.isRedundantP(Branch.Side.ONE));
        branchObservability.setRedundantP(false, Branch.Side.ONE);
        assertFalse(branchObservability.isRedundantP(Branch.Side.ONE));
        assertFalse(branchObservability.isRedundantP(Branch.Side.TWO));
        branchObservability.setRedundantP(true, Branch.Side.TWO);
        assertTrue(branchObservability.isRedundantP(Branch.Side.TWO));

        // Q
        assertEquals(0.5d, branchObservability.getStandardDeviationQ(Branch.Side.ONE), 0d);
        branchObservability.setStandardDeviationQ(0.6d, Branch.Side.ONE);
        assertEquals(0.6d, branchObservability.getStandardDeviationQ(Branch.Side.ONE), 0d);
        assertEquals(1.0d, branchObservability.getStandardDeviationQ(Branch.Side.TWO), 0d);
        branchObservability.setStandardDeviationQ(1.01d, Branch.Side.TWO);
        assertEquals(1.01d, branchObservability.getStandardDeviationQ(Branch.Side.TWO), 0d);

        assertTrue(branchObservability.isRedundantQ(Branch.Side.ONE));
        branchObservability.setRedundantQ(false, Branch.Side.ONE);
        assertFalse(branchObservability.isRedundantQ(Branch.Side.ONE));
        assertFalse(branchObservability.isRedundantQ(Branch.Side.TWO));
        branchObservability.setRedundantQ(true, Branch.Side.TWO);
        assertTrue(branchObservability.isRedundantQ(Branch.Side.TWO));

        // V
        assertEquals(0.00d, branchObservability.getStandardDeviationV(Branch.Side.ONE), 0d);
        branchObservability.setStandardDeviationV(0.01d, Branch.Side.ONE);
        assertEquals(0.01d, branchObservability.getStandardDeviationV(Branch.Side.ONE), 0d);
        assertEquals(0.2d, branchObservability.getStandardDeviationV(Branch.Side.TWO), 0d);
        branchObservability.setStandardDeviationV(0.3d, Branch.Side.TWO);
        assertEquals(0.3d, branchObservability.getStandardDeviationV(Branch.Side.TWO), 0d);

        assertTrue(branchObservability.isRedundantV(Branch.Side.ONE));
        branchObservability.setRedundantV(false, Branch.Side.ONE);
        assertFalse(branchObservability.isRedundantV(Branch.Side.ONE));
        assertFalse(branchObservability.isRedundantV(Branch.Side.TWO));
        branchObservability.setRedundantV(true, Branch.Side.TWO);
        assertTrue(branchObservability.isRedundantV(Branch.Side.TWO));
    }
}

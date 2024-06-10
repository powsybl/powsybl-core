/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class ThreeWindingsTransformerModificationTest {

    private Network network;
    private ThreeWindingsTransformer t3wt;

    @BeforeEach
    public void setUp() {
        network = ThreeWindingsTransformerNetworkFactory.create();
        assertTrue(network.getThreeWindingsTransformerCount() > 0);
        t3wt = network.getThreeWindingsTransformers().iterator().next();
    }

    @Test
    void testThreeWindingsTransformerModification() {
        double ratedU0 = t3wt.getRatedU0();
        double structuralRatioLeg1 = getStructuralRatio(t3wt.getLeg1(), ratedU0);
        double structuralRatioLeg2 = getStructuralRatio(t3wt.getLeg2(), ratedU0);
        double structuralRatioLeg3 = getStructuralRatio(t3wt.getLeg3(), ratedU0);

        double newRatedU0 = 135.0;
        assertNotEquals(ratedU0, newRatedU0);

        ThreeWindingsTransformerModification t3wtModification = new ThreeWindingsTransformerModification(t3wt.getId(), newRatedU0);
        assertEquals(ratedU0, t3wt.getRatedU0());
        t3wtModification.apply(network);
        assertEquals(newRatedU0, t3wt.getRatedU0());
        assertEquals(structuralRatioLeg1, getStructuralRatio(t3wt.getLeg1(), t3wt.getRatedU0()));
        assertEquals(structuralRatioLeg2, getStructuralRatio(t3wt.getLeg2(), t3wt.getRatedU0()));
        assertEquals(structuralRatioLeg3, getStructuralRatio(t3wt.getLeg3(), t3wt.getRatedU0()));
    }

    private static double getStructuralRatio(ThreeWindingsTransformer.Leg leg, double ratedU0) {
        return leg.getRatedU() / ratedU0;
    }

    @Test
    void testApplyChecks() {
        ThreeWindingsTransformerModification t3wtModification = new ThreeWindingsTransformerModification("UNKNOWN_ID", Double.NaN);
        assertThrows(PowsyblException.class, () -> t3wtModification.apply(network, true, ReportNode.NO_OP),
                "An invalid ID should fail to apply.");
        assertDoesNotThrow(() -> t3wtModification.apply(network, false, ReportNode.NO_OP),
                "An invalid ID should not throw if throwException is false.");
    }

    @Test
    void testGetters() {
        ThreeWindingsTransformerModification t3wtModification = new ThreeWindingsTransformerModification(t3wt.getId(), 135.0);
        assertEquals(t3wt.getId(), t3wtModification.getTransformerId());
        assertEquals(135, t3wtModification.getRatedU0());
    }
}

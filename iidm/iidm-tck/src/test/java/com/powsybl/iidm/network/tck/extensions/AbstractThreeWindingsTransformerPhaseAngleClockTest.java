/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public abstract class AbstractThreeWindingsTransformerPhaseAngleClockTest {

    private ThreeWindingsTransformer transformer;

    @BeforeEach
    public void setUp() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        transformer = network.getThreeWindingsTransformer("3WT");
    }

    @Test
    public void testEnd() {
        transformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(6).withPhaseAngleClockLeg3(1).add();
        ThreeWindingsTransformerPhaseAngleClock pacOut = transformer.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        assertEquals(6, pacOut.getPhaseAngleClockLeg2());
        assertEquals(1, pacOut.getPhaseAngleClockLeg3());
        assertEquals("3WT", pacOut.getExtendable().getId());

        ThreeWindingsTransformerPhaseAngleClock pacIn = transformer.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        pacIn.setPhaseAngleClockLeg2(1);
        pacIn.setPhaseAngleClockLeg3(6);
        pacOut = transformer.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        assertEquals(1, pacOut.getPhaseAngleClockLeg2());
        assertEquals(6, pacOut.getPhaseAngleClockLeg3());
    }

    @Test
    public void testError1Leg2() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> transformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(12).withPhaseAngleClockLeg3(1).add());
        assertEquals("Unexpected value for phaseAngleClock: 12", e.getMessage());
    }

    @Test
    public void testError1Leg3() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> transformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(1).withPhaseAngleClockLeg3(12).add());
        assertEquals("Unexpected value for phaseAngleClock: 12", e.getMessage());
    }

    @Test
    public void testError2Leg2() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> transformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(-1).withPhaseAngleClockLeg3(0).add());
        assertEquals("Unexpected value for phaseAngleClock: -1", e.getMessage());
    }

    @Test
    public void testError2Leg3() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> transformer.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClockLeg2(0).withPhaseAngleClockLeg3(-1).add());
        assertEquals("Unexpected value for phaseAngleClock: -1", e.getMessage());
    }
}

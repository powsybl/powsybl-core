/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class ThreeWindingsTransformerPhaseAngleClockTest {

    private ThreeWindingsTransformer transformer;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Network network = ThreeWindingsTransformerNetworkFactory.create();
        transformer = network.getThreeWindingsTransformer("3WT");
    }

    @Test
    public void testEnd() {
        ThreeWindingsTransformerPhaseAngleClock pac = new ThreeWindingsTransformerPhaseAngleClock(transformer, 6, 1);
        transformer.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, pac);
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
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: 12");
        ThreeWindingsTransformerPhaseAngleClock pac = new ThreeWindingsTransformerPhaseAngleClock(transformer, 12, 1);
        transformer.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, pac);
    }

    @Test
    public void testError1Leg3() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: 12");
        ThreeWindingsTransformerPhaseAngleClock pac = new ThreeWindingsTransformerPhaseAngleClock(transformer, 1, 12);
        transformer.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, pac);
    }

    @Test
    public void testError2Leg2() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: -1");
        ThreeWindingsTransformerPhaseAngleClock pac = new ThreeWindingsTransformerPhaseAngleClock(transformer, -1, 0);
        transformer.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, pac);
    }

    @Test
    public void testError2Leg3() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: -1");
        ThreeWindingsTransformerPhaseAngleClock pac = new ThreeWindingsTransformerPhaseAngleClock(transformer, 0, -1);
        transformer.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, pac);
    }
}

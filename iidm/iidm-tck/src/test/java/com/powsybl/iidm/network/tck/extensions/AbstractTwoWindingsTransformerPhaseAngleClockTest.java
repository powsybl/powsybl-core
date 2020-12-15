/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractTwoWindingsTransformerPhaseAngleClockTest {

    private TwoWindingsTransformer transformer;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Network network = EurostagTutorialExample1Factory.create();
        transformer = network.getTwoWindingsTransformer("NHV2_NLOAD");
    }

    @Test
    public void testEnd() {
        transformer.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClock(11).add();
        TwoWindingsTransformerPhaseAngleClock pacOut = transformer.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        assertEquals(11, pacOut.getPhaseAngleClock());
        pacOut.setPhaseAngleClock(10);
        assertEquals(10, pacOut.getPhaseAngleClock());
        assertEquals("NHV2_NLOAD", pacOut.getExtendable().getId());

        TwoWindingsTransformerPhaseAngleClock pacIn = transformer.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        pacIn.setPhaseAngleClock(6);
        pacOut = transformer.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        assertEquals(6, pacOut.getPhaseAngleClock());
    }

    @Test
    public void testError1() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: 12");
        transformer.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClock(12).add();
    }

    @Test
    public void testError2() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: -1");
        transformer.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).add();
    }
}

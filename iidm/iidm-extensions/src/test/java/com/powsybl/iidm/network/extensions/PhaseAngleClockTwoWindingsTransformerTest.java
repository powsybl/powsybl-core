/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PhaseAngleClockTwoWindingsTransformerTest {

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
        PhaseAngleClockTwoWindingsTransformer pac = new PhaseAngleClockTwoWindingsTransformer(transformer, 11);
        assertEquals(11, pac.getPhaseAngleClock());
        assertEquals("NHV2_NLOAD", pac.getExtendable().getId());
    }

    @Test
    public void testError1() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: 25");
        new PhaseAngleClockTwoWindingsTransformer(transformer, 25);
    }

    @Test
    public void testError2() {
        exception.expect(PowsyblException.class);
        exception.expectMessage("Unexpected value for phaseAngleClock: -1");
        new PhaseAngleClockTwoWindingsTransformer(transformer, -1);
    }

}

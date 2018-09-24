/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseShifterXmlTest extends AbstractConverterTest {

    @Test
    public void roundTripTest() throws IOException {
        roundTripXmlTest(PhaseShifterTestCaseFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                "/phaseShifterRoundTripRef.xml");
    }

    @Test
    public void testReadV10() {
        Network network = NetworkXml.read(getClass().getResourceAsStream("/phaseShifterRoundTripRef1_0.xml"));
        PhaseTapChanger phaseTapChanger = network.getTwoWindingsTransformer("PS1").getPhaseTapChanger();
        assertEquals(0.0, phaseTapChanger.getStep(0).getRdr(), 0.0);
        assertEquals(0.0, phaseTapChanger.getStep(0).getRdx(), 0.0);
        assertEquals(0.0, phaseTapChanger.getStep(0).getRdg(), 0.0);
        assertEquals(0.0, phaseTapChanger.getStep(0).getRdb(), 0.0);
    }
}

/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseShifterXmlTest extends AbstractXmlConverterTest {
    @Test
    public void roundTripTest() throws IOException {
        roundTripVersionnedXmlTest("phaseShifterRoundTripRef.xml", IIDM_VERSION_1_0_DIR_NAME);

        roundTripXmlTest(PhaseShifterTestCaseFactory.createWithTargetDeadband(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                IIDM_CURRENT_VERSION_DIR_NAME + "phaseShifterRoundTripRef.xml");
    }
}

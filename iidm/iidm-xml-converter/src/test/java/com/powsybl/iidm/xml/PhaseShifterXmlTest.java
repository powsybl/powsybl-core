/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.IidmXmlVersion;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.commons.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PhaseShifterXmlTest extends AbstractXmlConverterTest {
    @Test
    public void roundTripTest() throws IOException {
        roundTripVersionnedXmlTest("phaseShifterRoundTripRef.xml", IidmXmlVersion.V_1_0);

        roundTripXmlTest(PhaseShifterTestCaseFactory.createWithTargetDeadband(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "phaseShifterRoundTripRef.xml");
    }
}

/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.IidmXmlVersion;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.commons.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class EurostagXmlTest extends AbstractXmlConverterTest {

    @Test
    public void loadFlowResultsTest() throws IOException {
        roundTripXmlTest(EurostagTutorialExample1Factory.createWithLFResults(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionDir(CURRENT_IIDM_XML_VERSION) + "eurostag-tutorial1-lf.xml");

        //backward compatibility 1.0
        roundTripVersionnedXmlTest("eurostag-tutorial1-lf.xml", IidmXmlVersion.V_1_0);
    }
}

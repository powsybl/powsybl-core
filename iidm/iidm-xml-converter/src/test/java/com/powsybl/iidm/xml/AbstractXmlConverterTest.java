/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlTestConstants.IIDM_CURRENT_VERSION_DIR_NAME;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractXmlConverterTest extends AbstractConverterTest {

    protected void roundTripVersionnedXmlTest(String file, String... versionDirs) throws IOException {
        for (String versionDir : versionDirs) {
            roundTripXmlTest(NetworkXml.read(getClass().getResourceAsStream(versionDir + file)),
                    NetworkXml::writeAndValidate,
                    NetworkXml::read,
                    IIDM_CURRENT_VERSION_DIR_NAME + file);
        }
    }
}

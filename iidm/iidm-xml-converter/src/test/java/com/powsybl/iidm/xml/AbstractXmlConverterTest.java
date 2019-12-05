/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.xml.IidmXmlVersion;

import java.io.IOException;

import static com.powsybl.commons.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractXmlConverterTest extends AbstractConverterTest {

    static String getVersionDir(IidmXmlVersion version) {
        return "/V" + version.toString("_") + "/";
    }

    protected void roundTripVersionnedXmlTest(String file, IidmXmlVersion... versions) throws IOException {
        for (IidmXmlVersion version : versions) {
            roundTripXmlTest(NetworkXml.read(getClass().getResourceAsStream(getVersionDir(version) + file)),
                    NetworkXml::writeAndValidate,
                    NetworkXml::validateAndRead,
                    getVersionDir(CURRENT_IIDM_XML_VERSION) + file);
        }
    }

    protected void roundTripAllVersionnedXmlTest(String file) throws IOException {
        roundTripVersionnedXmlTest(file, IidmXmlVersion.values());
    }
}

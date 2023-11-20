/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serializer.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class FictitiousSwitchXmlTest extends AbstractXmlConverterTest {

    @Test
    void roundTripTest() throws IOException {
        roundTripXmlTest(FictitiousSwitchFactory.create(),
                NetworkXml::writeAndValidate,
                NetworkXml::read,
                getVersionedNetworkPath("fictitiousSwitchRef.xml", CURRENT_IIDM_XML_VERSION));

        //backward compatibility
        roundTripAllPreviousVersionedXmlTest("fictitiousSwitchRef.xml");
    }

}

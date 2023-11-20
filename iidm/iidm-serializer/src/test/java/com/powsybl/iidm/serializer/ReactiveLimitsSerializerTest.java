/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.test.ReactiveLimitsTestNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serializer.IidmSerializerConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ReactiveLimitsSerializerTest extends AbstractIidmSerializerTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        roundTripAllPreviousVersionedXmlTest("reactiveLimitsRoundTripRef.xml");

        roundTripXmlTest(ReactiveLimitsTestNetworkFactory.create(),
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("reactiveLimitsRoundTripRef.xml", CURRENT_IIDM_XML_VERSION));
    }
}

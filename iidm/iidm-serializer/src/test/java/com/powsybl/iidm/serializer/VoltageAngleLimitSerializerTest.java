/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serializer;

import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serializer.IidmSerializerConstants.CURRENT_IIDM_XML_VERSION;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VoltageAngleLimitSerializerTest extends AbstractIidmSerializerTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("voltageAngleLimit.xiidm", IidmVersion.V_1_11);

        roundTripXmlTest(EurostagTutorialExample1Factory.createWithVoltageAngleLimit(),
                NetworkSerializer::writeAndValidate,
                NetworkSerializer::read,
                getVersionedNetworkPath("voltageAngleLimit.xiidm", CURRENT_IIDM_XML_VERSION));
    }
}

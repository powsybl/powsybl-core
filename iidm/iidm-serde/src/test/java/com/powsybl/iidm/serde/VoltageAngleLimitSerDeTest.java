/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class VoltageAngleLimitSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        // backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("voltageAngleLimit.xiidm", IidmVersion.V_1_11);
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("voltageAngleLimit.xiidm", IidmVersion.V_1_12);

        allFormatsRoundTripTest(EurostagTutorialExample1Factory.createWithVoltageAngleLimit(), "voltageAngleLimit.xiidm", CURRENT_IIDM_VERSION);
    }
}

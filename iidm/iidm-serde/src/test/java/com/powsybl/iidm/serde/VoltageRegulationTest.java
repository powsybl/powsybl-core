/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class VoltageRegulationTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("remoteVoltageRegulationWithoutLocalTarget.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // Tests for backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("remoteVoltageRegulationWithoutLocalTarget.xml", IidmVersion.V_1_17);
    }
}

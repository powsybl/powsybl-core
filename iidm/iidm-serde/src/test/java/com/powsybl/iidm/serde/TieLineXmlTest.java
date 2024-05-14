/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class TieLineXmlTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        allFormatsRoundTripAllVersionedXmlTest("tieline.xml");
    }

    @Test
    void testV10() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("tieline.xml", IidmVersion.V_1_10);
    }

    @Test
    void testV9() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("tieline.xml", IidmVersion.V_1_9);
    }
}

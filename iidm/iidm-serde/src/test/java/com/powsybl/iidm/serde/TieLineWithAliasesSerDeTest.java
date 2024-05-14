/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey@rte-france.com>}
 */
class TieLineWithAliasesSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        allFormatsRoundTripFromVersionedXmlTest("tielineWithAliases.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        // Tests for backward compatibility
        allFormatsRoundTripFromVersionedXmlFromMinToCurrentVersionTest("tielineWithAliases.xml", IidmVersion.V_1_3);
    }
}

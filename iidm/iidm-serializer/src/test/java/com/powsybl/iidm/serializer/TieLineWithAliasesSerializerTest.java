/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.serializer;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey@rte-france.com>}
 */
class TieLineWithAliasesSerializerTest extends AbstractIidmSerializerTest {

    @Test
    void test() throws IOException {
        roundTripVersionedXmlTest("tielineWithAliases.xml", IidmSerializerConstants.CURRENT_IIDM_XML_VERSION);

        // Tests for backward compatibility
        roundTripVersionedXmlFromMinToCurrentVersionTest("tielineWithAliases.xml", IidmVersion.V_1_3);
    }
}

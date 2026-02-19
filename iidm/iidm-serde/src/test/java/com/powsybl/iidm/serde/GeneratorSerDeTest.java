/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class GeneratorSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void isCondenserTest() throws IOException {
        allFormatsRoundTripFromVersionedXmlFromMinVersionTest("generator.xml", IidmVersion.V_1_12);
    }
}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class TieLineTerminalRefBugTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        assertDoesNotThrow(() -> NetworkSerDe.read(getClass().getResourceAsStream(getVersionedNetworkPath("tieLineTerminalRefBug.xml", CURRENT_IIDM_VERSION))));
    }
}

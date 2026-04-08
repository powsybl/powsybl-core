/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.network.UcteNodeStatus.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteNodeStatusTest {

    @Test
    void test() {
        assertEquals(2, UcteNodeStatus.values().length);
        assertArrayEquals(new UcteNodeStatus[] {REAL, EQUIVALENT}, UcteNodeStatus.values());
    }
}

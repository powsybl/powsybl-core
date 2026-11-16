/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteElementStatusTest {

    @Test
    void test() {
        int[] status = {0, 8, 1, 9, 2, 7};

        assertEquals(6, UcteElementStatus.values().length);
        for (int i = 0; i < UcteElementStatus.values().length; ++i) {
            UcteElementStatus elementStatus = UcteElementStatus.values()[i];
            assertEquals(status[i], elementStatus.getCode());
            assertEquals(elementStatus, UcteElementStatus.fromCode(status[i]));
        }
    }

    @Test
    void unknownElementStatus() {
        assertThrows(IllegalArgumentException.class, () -> UcteElementStatus.fromCode(10));
    }
}

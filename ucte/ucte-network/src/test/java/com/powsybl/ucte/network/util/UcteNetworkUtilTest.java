/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network.util;

import com.powsybl.ucte.network.UcteException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Clément LECLERC {@literal <clement.leclerc at rte-france.com>}
 */
class UcteNetworkUtilTest {

    @Test
    void getOrderCodeTest() {
        assertEquals('0', UcteNetworkUtil.getOrderCode(0));
        assertEquals('A', UcteNetworkUtil.getOrderCode(10));
        assertThrows(UcteException.class, () -> UcteNetworkUtil.getOrderCode(-1));
        assertThrows(UcteException.class, () -> UcteNetworkUtil.getOrderCode(50));
    }
}

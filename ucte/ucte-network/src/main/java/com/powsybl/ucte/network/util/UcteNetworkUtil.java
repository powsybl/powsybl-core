/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.network.util;

import com.powsybl.ucte.network.UcteException;

import java.util.List;

/**
 * @author Clément LECLERC {@literal <clement.leclerc at rte-france.com>}
 */
public final class UcteNetworkUtil {

    private UcteNetworkUtil() {
        throw new IllegalStateException("Should not be constructed");
    }

    public static final List<Character> ORDER_CODES = List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '-', '.', ' ');

    public static char getOrderCode(int index) {
        if (index > ORDER_CODES.size() || index < 0) {
            throw new UcteException("Order code index out of bounds");
        }
        return ORDER_CODES.get(index);
    }
}

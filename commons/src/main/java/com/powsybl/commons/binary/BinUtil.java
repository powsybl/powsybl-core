/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.binary;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class BinUtil {

    private BinUtil() {
    }

    static final int END_NODE = 0;
    static final int END_ATTRS = 0;
    static final int MAX_ATTR_IDX = 0xFFFF;

    static final byte TYPE_DOUBLE = 0;
    static final byte TYPE_FLOAT = 1;
    static final byte TYPE_INT = 2;
    static final byte TYPE_BOOLEAN = 3;
    static final byte TYPE_STRING = 4;
    static final byte TYPE_ENUM = 5;
    static final byte TYPE_INT_ARRAY = 6;
    static final byte TYPE_STRING_ARRAY = 7;
    static final byte TYPE_STRING_CONTENT = 8;

}

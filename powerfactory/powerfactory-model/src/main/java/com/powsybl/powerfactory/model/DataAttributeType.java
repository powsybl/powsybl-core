/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum DataAttributeType {
    INTEGER,
    INTEGER_VECTOR,
    DOUBLE,
    DOUBLE_VECTOR,
    DOUBLE_MATRIX,
    OBJECT,
    OBJECT_VECTOR,
    STRING,
    STRING_VECTOR,
    INTEGER64,
    INTEGER64_VECTOR,
    FLOAT,
    FLOAT_VECTOR
}

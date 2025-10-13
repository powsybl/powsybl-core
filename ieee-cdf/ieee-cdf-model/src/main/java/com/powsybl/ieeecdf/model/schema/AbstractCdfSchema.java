/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.schema;

import org.jsapar.schema.FixedWidthSchemaCell;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractCdfSchema {

    protected AbstractCdfSchema() {
        // private constructor to prevent instantiation
    }

    protected static FixedWidthSchemaCell filler(int size, int fillerId) {
        return FixedWidthSchemaCell.builder("filler_" + fillerId, size)
            .withPadCharacter(' ')
            .build();
    }
}

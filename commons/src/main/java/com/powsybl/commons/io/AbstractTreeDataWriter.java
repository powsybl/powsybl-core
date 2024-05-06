/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractTreeDataWriter implements TreeDataWriter {

    @Override
    public void writeOptionalBooleanAttribute(String name, Boolean value) {
        if (value != null) {
            writeBooleanAttribute(name, value);
        }
    }

    @Override
    public void writeOptionalDoubleAttribute(String name, Double value) {
        if (value != null) {
            writeDoubleAttribute(name, value);
        }
    }

    @Override
    public void writeOptionalIntAttribute(String name, Integer value) {
        if (value != null) {
            writeIntAttribute(name, value);
        }
    }
}

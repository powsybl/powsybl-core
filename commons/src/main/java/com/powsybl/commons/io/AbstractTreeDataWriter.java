/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.io;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractTreeDataWriter implements TreeDataWriter {

    @Override
    public void writeOptionalBooleanAttribute(String name, BooleanSupplier valueSupplier, BooleanSupplier write) {
        if (write.getAsBoolean()) {
            writeBooleanAttribute(name, valueSupplier.getAsBoolean());
        }
    }

    @Override
    public void writeOptionalBooleanAttribute(String name, Boolean value) {
        if (value != null) {
            writeBooleanAttribute(name, value);
        }
    }

    @Override
    public void writeOptionalDoubleAttribute(String name, DoubleSupplier valueSupplier, BooleanSupplier write) {
        if (write.getAsBoolean()) {
            writeDoubleAttribute(name, valueSupplier.getAsDouble());
        }
    }

    @Override
    public void writeOptionalDoubleAttribute(String name, Double value) {
        if (value != null) {
            writeDoubleAttribute(name, value);
        }
    }

    @Override
    public void writeOptionalIntAttribute(String name, IntSupplier valueSupplier, BooleanSupplier write) {
        if (write.getAsBoolean()) {
            writeIntAttribute(name, valueSupplier.getAsInt());
        }
    }
}

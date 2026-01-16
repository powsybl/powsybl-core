/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public record SplitString(String[] left, String[] right, boolean isIncluded) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SplitString(String[] otherLeft, String[] otherRight, boolean otherIncluded))) {
            return false;
        }
        return isIncluded == otherIncluded
            && Arrays.equals(left, otherLeft)
            && Arrays.equals(right, otherRight);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(isIncluded);
        result = 31 * result + Arrays.hashCode(left) + Arrays.hashCode(right);
        return result;
    }

    @Override
    @NonNull
    public String toString() {
        return "SplitString{" +
            "left=" + Arrays.toString(left) +
            ", right=" + Arrays.toString(right) +
            ", isIncluded=" + isIncluded +
            '}';
    }
}

/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum MatpowerFormatVersion {
    V1(10),
    V2(21);

    private final int generatorColumns;

    MatpowerFormatVersion(int generatorColumns) {
        this.generatorColumns = generatorColumns;
    }

    public int getGeneratorColumns() {
        return generatorColumns;
    }

    @JsonCreator
    public static MatpowerFormatVersion fromString(String version) {
        return switch (version) {
            case "1" -> V1;
            case "2" -> V2;
            default -> throw new IllegalArgumentException("Unsupported Matpower format version: " + version);
        };
    }

    @JsonValue
    @Override
    public String toString() {
        return switch (this) {
            case V1 -> "1";
            case V2 -> "2";
        };
    }
}

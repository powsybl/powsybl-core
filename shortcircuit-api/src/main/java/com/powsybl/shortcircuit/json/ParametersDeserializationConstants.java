/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class ParametersDeserializationConstants {
    private ParametersDeserializationConstants() {
    }

    public static final String SOURCE_VERSION_ATTRIBUTE = "sourceVersionAttribute";

    public static final String SOURCE_PARAMETER_TYPE_ATTRIBUTE = "sourceParameterTypeAttribute";

    public enum ParametersType {
        FAULT,
        SHORT_CIRCUIT
    }
}

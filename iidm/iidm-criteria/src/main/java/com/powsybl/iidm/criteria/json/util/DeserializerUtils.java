/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.util;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class DeserializerUtils {

    private DeserializerUtils() {
    }

    public static boolean checkBoundData(Number value, Boolean closed, String valueAttribute, String booleanAttribute, String errorMessageTemplate) {
        if (value != null && closed == null) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, booleanAttribute, valueAttribute));
        }
        if (value == null && closed != null) {
            throw new IllegalArgumentException(String.format(errorMessageTemplate, valueAttribute, booleanAttribute));
        }
        return value != null;
    }
}

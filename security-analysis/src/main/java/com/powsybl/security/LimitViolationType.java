/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum LimitViolationType {
    CURRENT,
    LOW_VOLTAGE,
    HIGH_VOLTAGE,
    LOW_SHORT_CIRCUIT_CURRENT,
    HIGH_SHORT_CIRCUIT_CURRENT,
    OTHER
}

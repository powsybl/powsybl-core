/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * Used by {@link LoadingLimits} to indicate if the duration of the violation that is acceptable at a given value should be taken from
 * the limit above the value (HIGH) or below the value (LOW).
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public enum DetectionKind {
    HIGH,
    LOW
}

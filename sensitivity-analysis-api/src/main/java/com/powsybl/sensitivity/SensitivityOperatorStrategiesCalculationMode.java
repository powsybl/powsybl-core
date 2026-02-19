/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum SensitivityOperatorStrategiesCalculationMode {
    NONE, // deactivate calculation of operator strategies sensitivities and only calculate N and N-K sensitivities
    ALL_CONTINGENCIES, // calculate operator strategies for all contingencies in addition to N, N-K sensitivities
    ONLY_OPERATOR_STRATEGIES // only calculate operator strategies sensitivities and skip N and N-K sensitivities
}

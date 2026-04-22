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
public record SensitivityState(String contingencyId, String operatorStrategyId) {

    public static final SensitivityState PRE_CONTINGENCY = new SensitivityState(null, null);

    public static SensitivityState postContingency(String contingencyId) {
        return new SensitivityState(contingencyId, null);
    }
}

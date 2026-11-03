/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import java.util.Objects;

/*
 * Key type for sensitivity value when stored by function type, contingency, function and variable id
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public record SensitivityValueKey(SensitivityState state,
                                  String variableId,
                                  String functionId,
                                  SensitivityFunctionType functionType,
                                  SensitivityVariableType variableType) {
    public SensitivityValueKey {
        Objects.requireNonNull(state);
        Objects.requireNonNull(variableId);
        Objects.requireNonNull(functionId);
        Objects.requireNonNull(functionType);
        Objects.requireNonNull(variableType);
    }
}

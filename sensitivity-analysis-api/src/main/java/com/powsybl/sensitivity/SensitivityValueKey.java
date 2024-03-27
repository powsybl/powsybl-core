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
public class SensitivityValueKey {

    private final SensitivityFunctionType functionType;
    private final String contingencyId;
    private final String functionId;
    private final String variableId;

    private final SensitivityVariableType variableType;

    public SensitivityValueKey(final String contingencyId, final String variableId, final String functionId, final SensitivityFunctionType functionType, final SensitivityVariableType variableType) {
        this.contingencyId = contingencyId;
        this.variableId = variableId;
        this.functionId = functionId;
        this.functionType = functionType;
        this.variableType = variableType;
    }

    public SensitivityFunctionType getFunctionType() {
        return functionType;
    }

    public String getContingencyId() {
        return contingencyId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public String getVariableId() {
        return variableId;
    }

    public SensitivityVariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof SensitivityValueKey)) {
            return false;
        }

        SensitivityValueKey c = (SensitivityValueKey) o;

        return c.getFunctionType().equals(functionType) && c.getFunctionId().equals(functionId)
               && (c.getContingencyId() != null ? c.getContingencyId().equals(contingencyId) : contingencyId == null)
               && c.getVariableId().equals(variableId) && c.getVariableType().equals(variableType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionType, functionId, contingencyId, variableId, variableType);
    }
}

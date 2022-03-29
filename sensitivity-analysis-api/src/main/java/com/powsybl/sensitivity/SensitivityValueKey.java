package com.powsybl.sensitivity;

import java.util.Objects;


/*
 * Key type for sensitivity value when stored by function type, contingency, function and variable id
 */
public class SensitivityValueKey {

    private final SensitivityFunctionType functionType;
    private final String contingencyId;
    private final String functionId;
    private final String variableId;

    public SensitivityValueKey(final String contingencyId, final String variableId, final String functionId, final SensitivityFunctionType functionType) {
        this.contingencyId = contingencyId;
        this.variableId = variableId;
        this.functionId = functionId;
        this.functionType = functionType;
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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof  SensitivityValueKey)) {
            return false;
        }

        SensitivityValueKey c = (SensitivityValueKey) o;

        return c.getFunctionType().equals(functionType) && c.getFunctionId().equals(functionId)
               && (c.getContingencyId() != null ? c.getContingencyId().equals(contingencyId) : contingencyId == null)
               && c.getVariableId().equals(variableId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionType, functionId, contingencyId, variableId);
    }
}

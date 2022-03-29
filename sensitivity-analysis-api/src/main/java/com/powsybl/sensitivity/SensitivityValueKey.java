package com.powsybl.sensitivity;

/*
 * Key type for sensitivity value when stored by function type, contingency, function and variable id
 */
public class SensitivityValueKey {

    private SensitivityFunctionType functionType;
    private String contingencyId;
    private String functionId;
    private String variableId;

    public SensitivityValueKey(final SensitivityFunctionType functionType, final String contingencyId, final String functionId, final String variableId) {
        this.functionType = functionType;
        this.contingencyId = contingencyId;
        this.functionId = functionId;
        this.variableId = variableId;
    }

    SensitivityFunctionType getFunctionType() {
        return functionType;
    }

    String getContingencyId() {
        return contingencyId;
    }

    String getFunctionId() {
        return functionId;
    }

    String getVariableId() {
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
        final int prime = 31;
        int result = prime + functionType.hashCode();
        result = prime * result + functionId.hashCode();
        result = contingencyId != null ? prime * result + contingencyId.hashCode() : prime * result;
        result = prime * result + variableId.hashCode();
        return result;
    }
}

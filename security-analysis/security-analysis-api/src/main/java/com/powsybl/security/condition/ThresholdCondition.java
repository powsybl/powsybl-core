package com.powsybl.security.condition;

import com.powsybl.iidm.network.ThreeSides;

/**
 * Condition triggered by a threshold
 */
public class ThresholdCondition implements Condition {

    public static final String NAME = "THRESHOLD_CONDITION";

    public enum ComparisonType {
        EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUALS,
        NOT_EQUAL
    }

    public enum Variable {
        ACTIVE_POWER,
        REACTIVE_POWER,
        CURRENT,
        TARGET_P
    }

    // Threshold to be checked for this condition
    private final double threshold;
    // Equipment on which to check the threshold
    private final String equipmentId;
    // Side of the equipment on which to check the threshold
    private final ThreeSides side;
    // Comparison type for the threshold
    private final ComparisonType type;
    // Variable to be measured against the threshold
    private final Variable variable;

    public ThresholdCondition(double threshold, ComparisonType type, String equipmentId, ThreeSides side, Variable variable) {
        this.threshold = threshold;
        this.equipmentId = equipmentId;
        this.type = type;
        this.side = side;
        this.variable = variable;
    }

    public ThresholdCondition(double threshold, ComparisonType type, String equipmentId, Variable variable) {
        this(threshold, type, equipmentId, null, variable);
    }

    public double getThreshold() {
        return threshold;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public ThreeSides getSide() {
        return side;
    }

    public ComparisonType getComparisonType() {
        return type;
    }

    public Variable getVariable() {
        return variable;
    }

    @Override
    public String getType() {
        return NAME;
    }
}

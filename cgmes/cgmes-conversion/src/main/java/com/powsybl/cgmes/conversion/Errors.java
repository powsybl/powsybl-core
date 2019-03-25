package com.powsybl.cgmes.conversion;

public final class Errors {

    private Errors() {
        // private constructor
    }

    public enum Missing {
        NAME,
        SUBSTATION,
        BASE_VOLTAGE,
        VOLTAGE_LEVEL,
        EQUIPMENT,
        TERMINAL,
        NODE_ID,
        EQUIPMENT_AT_BOUNDARY,
        CONVERTER,
        MAX_P,
        ACTIVE_POWER_SETPOINT,
        POWER_TRANSFORMER,
        PHASE_TAP_CHANGER_TABLE,
        PHASE_TAP_CHANGER_TABLE_POINTS,
        WINDING_CONNECTION_ANGLE,
        RATIO_TAP_CHANGER_TABLE,
        RATIO_TAP_CHANGER_TABLE_POINTS,
    }

    public enum Fixes {
        REACTIVE_LIMITS,
        PHASE_TAP_CHANGER_TABLE_POINT,
        RATIO_TAP_CHANGER_TABLE_POINT,
        SWITCH_AS_LOW_IMPEDANCE_LINE
    }
}

/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import java.io.Serializable;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum SecurityIndexType implements Serializable {

    OVERLOAD("Overload", false),
    OVERUNDERVOLTAGE("OverUnderVoltage", false),
    SMALLSIGNAL("SmallSignal", true),
    TRANSIENT("Transient", true),
    TSO_OVERLOAD("TSO_overload", false),
    TSO_OVERVOLTAGE("TSO_overvoltage", false),
    TSO_UNDERVOLTAGE("TSO_undervoltage", false),
    TSO_SYNCHROLOSS("TSO_synchro_loss", true),
    TSO_FREQUENCY("TSO_frequency", true),
    TSO_GENERATOR_VOLTAGE_AUTOMATON("TSO_generator_voltage_automaton", true),
    TSO_GENERATOR_SPEED_AUTOMATON("TSO_generator_speed_automaton", true),
    TSO_DISCONNECTED_GENERATOR("TSO_disconnected_generator", true),
    MULTI_CRITERIA_VOLTAGE_STABILITY("Multi criteria voltage stability", true),
    MULTI_CRITERIA_VOLTAGE_STABILITY2("Multi criteria voltage stability 2", true);

    private final String label;

    private final boolean dynamic;

    public static SecurityIndexType fromLabel(String label) {
        for (SecurityIndexType securityIndexType : SecurityIndexType.values()) {
            if (securityIndexType.getLabel().equals(label)) {
                return securityIndexType;
            }
        }
        throw new IllegalArgumentException("No security index found with label " + label);
    }

    private SecurityIndexType(String label, boolean dynamic) {
        this.label = label;
        this.dynamic = dynamic;
    }

    public String getLabel() {
        return label;
    }

    public boolean isDynamic() {
        return dynamic;
    }

}

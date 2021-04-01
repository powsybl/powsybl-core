/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class TypedValue {

    public static final String UNTYPED = "UNTYPED";
    public static final String TRACE_LOGLEVEL = "TRACE_LOGLEVEL";
    public static final String DEBUG_LOGLEVEL = "DEBUG_LOGLEVEL";
    public static final String INFO_LOGLEVEL = "INFO_LOGLEVEL";
    public static final String WARN_LOGLEVEL = "WARN_LOGLEVEL";
    public static final String ERROR_LOGLEVEL = "ERROR_LOGLEVEL";
    public static final String RESISTANCE = "RESISTANCE";
    public static final String REACTANCE = "REACTANCE";
    public static final String IMPEDANCE = "IMPEDANCE";
    public static final String SUSCEPTANCE = "SUSCEPTANCE";
    public static final String SUBSTATION = "SUBSTATION";
    public static final String VOLTAGE_LEVEL = "VOLTAGE_LEVEL";
    public static final String FILENAME = "FILENAME";

    private final Object value;
    private final String type;

    public TypedValue(Object value, String type) {
        this.value = Objects.requireNonNull(value);
        this.type = Objects.requireNonNull(type);
        if (!(value instanceof Float || value instanceof Double || value instanceof Integer || value instanceof Long || value instanceof Boolean || value instanceof String)) {
            throw new PowsyblException("TypedValue expects only Float, Double, Integer, Long and String values (value is an instance of " + value.getClass() + ")");
        }
    }

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}

/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.PowsyblException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A class associating a value with a type.
 * The value should be an instance of any of the following classes: Integer, Long, Float, Double, Boolean or String.
 * The type is given by a string. Some generic types are provided by public constants of current class.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TypedValue {

    public static final String UNTYPED = "UNTYPED";

    /** Type used for the severity level of the corresponding {@link ReportNode} */
    public static final String SEVERITY = "SEVERITY";

    public static final String ACTIVE_POWER = "ACTIVE_POWER";
    public static final String REACTIVE_POWER = "REACTIVE_POWER";
    public static final String RESISTANCE = "RESISTANCE";
    public static final String REACTANCE = "REACTANCE";
    public static final String IMPEDANCE = "IMPEDANCE";
    public static final String SUSCEPTANCE = "SUSCEPTANCE";
    public static final String VOLTAGE = "VOLTAGE";
    public static final String ANGLE = "ANGLE";
    public static final String SUBSTATION = "SUBSTATION";
    public static final String VOLTAGE_LEVEL = "VOLTAGE_LEVEL";
    public static final String FILENAME = "FILENAME";
    public static final String TIMESTAMP = "TIMESTAMP";
    public static final String URN_UUID = "URN_UUID";
    public static final String CGMES_SUBSET = "CGMES_SUBSET";
    public static final String ID = "ID";

    /** Used for a severity level of TRACE level */
    public static final TypedValue TRACE_SEVERITY = new TypedValue("TRACE", TypedValue.SEVERITY);

    /** Used for a severity level of DEBUG level */
    public static final TypedValue DEBUG_SEVERITY = new TypedValue("DEBUG", TypedValue.SEVERITY);

    /** Used for a severity level of INFO level, that is a functional state which may be of interest for the end user */
    public static final TypedValue INFO_SEVERITY = new TypedValue("INFO", TypedValue.SEVERITY);

    /** Used for a severity level of WARN level, that is, an unwanted state that can be recovered from */
    public static final TypedValue WARN_SEVERITY = new TypedValue("WARN", TypedValue.SEVERITY);

    /** Used for a severity level of ERROR level, that is, a requested operation has not been completed */
    public static final TypedValue ERROR_SEVERITY = new TypedValue("ERROR", TypedValue.SEVERITY);

    /**
     * Used for a severity level of DETAIL level, that is, the severity for children of a {@link ReportNode} of
     * severity WARN or ERROR. Indeed, end users may want several fine-grained messages when a WARN or ERROR occurs, but
     * they want as few WARN / ERROR messages as possible.
     */
    public static final TypedValue DETAIL_SEVERITY = new TypedValue("DETAIL", TypedValue.SEVERITY);

    private final Object value;
    private final String type;

    /**
     * Constructor
     * @param value should be an instance of any of the following classes: Integer, Long, Float, Double, Boolean or String
     * @param type a string representing the value type (see the public constants for some generic types)
     */
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

    public static TypedValue getTimestamp(DateTimeFormatter timestampFormatter) {
        return new TypedValue(timestampFormatter.format(ZonedDateTime.now()), TypedValue.TIMESTAMP);
    }
}

/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A class associating a value with a type.
 * The value should be an instance of any of the following classes: Integer, Long, Float, Double, Boolean or String.
 * The type is given by a string. Some generic types are provided by public constants of current class.
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public final class TypedValue {

    public static final String UNTYPED_TYPE = "UNTYPED";

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
    public static final TypedValue TRACE_SEVERITY = TypedValue.of("TRACE", TypedValue.SEVERITY);

    /** Used for a severity level of DEBUG level */
    public static final TypedValue DEBUG_SEVERITY = TypedValue.of("DEBUG", TypedValue.SEVERITY);

    /** Used for a severity level of INFO level, that is a functional state which may be of interest for the end user */
    public static final TypedValue INFO_SEVERITY = TypedValue.of("INFO", TypedValue.SEVERITY);

    /** Used for a severity level of WARN level, that is, an unwanted state that can be recovered from */
    public static final TypedValue WARN_SEVERITY = TypedValue.of("WARN", TypedValue.SEVERITY);

    /** Used for a severity level of ERROR level, that is, a requested operation has not been completed */
    public static final TypedValue ERROR_SEVERITY = TypedValue.of("ERROR", TypedValue.SEVERITY);

    /**
     * Used for a severity level of DETAIL level, that is, the severity for children of a {@link ReportNode} of
     * severity WARN or ERROR. Indeed, end users may want several fine-grained messages when a WARN or ERROR occurs, but
     * they want as few WARN / ERROR messages as possible.
     */
    public static final TypedValue DETAIL_SEVERITY = TypedValue.of("DETAIL", TypedValue.SEVERITY);

    private final Object value;
    private final String type;

    /**
     * Creating a typed value from the given value and type
     * @param value the value
     * @param type a string representing the value type (see the public constants for some generic types)
     */
    public static TypedValue of(int value, String type) {
        return new TypedValue(value, type);
    }

    /**
     * Creating a typed value from the given value and type
     * @param value the value
     * @param type a string representing the value type (see the public constants for some generic types)
     */
    public static TypedValue of(long value, String type) {
        return new TypedValue(value, type);
    }

    /**
     * Creating a typed value from the given value and type
     * @param value the value
     * @param type a string representing the value type (see the public constants for some generic types)
     */
    public static TypedValue of(float value, String type) {
        return new TypedValue(value, type);
    }

    /**
     * Creating a typed value from the given value and type
     * @param value the value
     * @param type a string representing the value type (see the public constants for some generic types)
     */
    public static TypedValue of(double value, String type) {
        return new TypedValue(value, type);
    }

    /**
     * Creating a typed value from the given value and type
     * @param value the value
     * @param type a string representing the value type (see the public constants for some generic types)
     */
    public static TypedValue of(boolean value, String type) {
        return new TypedValue(value, type);
    }

    /**
     * Creating a typed value from the given value and type
     * @param value the value
     * @param type a string representing the value type (see the public constants for some generic types)
     */
    public static TypedValue of(String value, String type) {
        return new TypedValue(value, type);
    }

    /**
     * Creating an untyped value from the given value
     */
    public static TypedValue untyped(int value) {
        return new TypedValue(value, TypedValue.UNTYPED_TYPE);
    }

    /**
     * Creating an untyped value from the given value
     */
    public static TypedValue untyped(long value) {
        return new TypedValue(value, TypedValue.UNTYPED_TYPE);
    }

    /**
     * Creating an untyped value from the given value
     */
    public static TypedValue untyped(float value) {
        return new TypedValue(value, TypedValue.UNTYPED_TYPE);
    }

    /**
     * Creating an untyped value from the given value
     */
    public static TypedValue untyped(double value) {
        return new TypedValue(value, TypedValue.UNTYPED_TYPE);
    }

    /**
     * Creating an untyped value from the given value
     */
    public static TypedValue untyped(boolean value) {
        return new TypedValue(value, TypedValue.UNTYPED_TYPE);
    }

    /**
     * Creating an untyped value from the given value
     */
    public static TypedValue untyped(String value) {
        return new TypedValue(value, TypedValue.UNTYPED_TYPE);
    }

    TypedValue(Object value, String type) {
        this.value = Objects.requireNonNull(value);
        this.type = Objects.requireNonNull(type);
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
        return TypedValue.of(timestampFormatter.format(ZonedDateTime.now()), TypedValue.TIMESTAMP);
    }

    static void checkSeverityType(TypedValue severity) {
        if (!severity.getType().equals(TypedValue.SEVERITY)) {
            throw new IllegalArgumentException("Expected a " + TypedValue.SEVERITY + " but received " + severity.getType());
        }
    }
}

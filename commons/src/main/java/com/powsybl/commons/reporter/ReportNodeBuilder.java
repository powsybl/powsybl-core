/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * A builder to create {@link ReportNode} objects.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeBuilder {

    private final Map<String, TypedValue> values = new HashMap<>();
    private String key;
    private String defaultMessage;

    /**
     * Build the corresponding {@link ReportNode}.
     * @return the new {@link ReportNode} corresponding to current <code>ReportBuilder</code>
     */
    public ReportNode build() {
        return new ReportNodeImpl(key, defaultMessage, values, new ArrayDeque<>());
    }

    /**
     * Provide the key to build the {@link ReportNode} with.
     * @param key the key identifying the message to build
     * @return a reference to this object
     */
    public ReportNodeBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * Provide the default message to build the {@link ReportNode} with.
     * @param defaultMessage the default report message of the report to build, which may contain references to its
     *                       values or to the values of corresponding {@link ReportNode} or to the values of one of its
     *                       {@link ReportNode} ancestors.
     * @return a reference to this object
     */
    public ReportNodeBuilder withDefaultMessage(String defaultMessage) {
        this.defaultMessage = defaultMessage;
        return this;
    }

    /**
     * Provide one typed string value to build the {@link ReportNode} with.
     * @param key the key for the typed string value
     * @param value the string value
     * @param type the string representing the type of the string value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    public ReportNodeBuilder withTypedValue(String key, String value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    /**
     * Provide one string value to build the {@link ReportNode} with.
     * @param key the key for the string value
     * @param value the string value
     * @return a reference to this object
     */
    public ReportNodeBuilder withValue(String key, String value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    /**
     * Provide one typed double value to build the {@link ReportNode} with.
     * @param key the key for the typed double value
     * @param value the double value
     * @param type the string representing the type of the double value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    public ReportNodeBuilder withTypedValue(String key, double value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    /**
     * Provide one double value to build the {@link ReportNode} with.
     * @param key the key for the double value
     * @param value the double value
     * @return a reference to this object
     */
    public ReportNodeBuilder withValue(String key, double value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    /**
     * Provide one typed float value to build the {@link ReportNode} with.
     * @param key the key for the typed float value
     * @param value the float value
     * @param type the string representing the type of the float value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    public ReportNodeBuilder withTypedValue(String key, float value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    /**
     * Provide one float value to build the {@link ReportNode} with.
     * @param key the key for the float value
     * @param value the float value
     * @return a reference to this object
     */
    public ReportNodeBuilder withValue(String key, float value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    /**
     * Provide one typed int value to build the {@link ReportNode} with.
     * @param key the key for the typed int value
     * @param value the int value
     * @param type the string representing the type of the int value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    public ReportNodeBuilder withTypedValue(String key, int value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    /**
     * Provide one int value to build the {@link ReportNode} with.
     * @param key the key for the int value
     * @param value the int value
     * @return a reference to this object
     */
    public ReportNodeBuilder withValue(String key, int value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    /**
     * Provide one typed long value to build the {@link ReportNode} with.
     * @param key the key for the typed long value
     * @param value the long value
     * @param type the string representing the type of the long value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    public ReportNodeBuilder withTypedValue(String key, long value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    /**
     * Provide one long value to build the {@link ReportNode} with.
     * @param key the key for the long value
     * @param value the long value
     * @return a reference to this object
     */
    public ReportNodeBuilder withValue(String key, long value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    /**
     * Provide one typed boolean value to build the {@link ReportNode} with.
     * @param key the key for the typed boolean value
     * @param value the boolean value
     * @param type the string representing the type of the boolean value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    public ReportNodeBuilder withTypedValue(String key, boolean value, String type) {
        values.put(key, new TypedValue(value, type));
        return this;
    }

    /**
     * Provide one boolean value to build the {@link ReportNode} with.
     * @param key the key for the boolean value
     * @param value the boolean value
     * @return a reference to this object
     */
    public ReportNodeBuilder withValue(String key, boolean value) {
        return withTypedValue(key, value, TypedValue.UNTYPED);
    }

    /**
     * Provide the typed value for the default severity key to build the {@link ReportNode} with.
     * @param severity the typed value
     * @return a reference to this object
     */
    public ReportNodeBuilder withSeverity(TypedValue severity) {
        if (!severity.getType().equals(TypedValue.SEVERITY)) {
            throw new IllegalArgumentException("Expected a " + TypedValue.SEVERITY + " but received " + severity.getType());
        }
        values.put(ReportConstants.REPORT_SEVERITY_KEY, severity);
        return this;
    }

}

/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

public interface ReportNodeAdderOrBuilder<T extends ReportNodeAdderOrBuilder<T>> {

    /**
     * Provide the message template to build the {@link ReportNode} with.
     * @param key             the key identifying uniquely the message template
     * @param messageTemplate functional log, which may contain references to values using the <code>${key}</code> syntax,
     *                        the values mentioned being the values of corresponding {@link ReportNode} and the values of any
     *                        {@link ReportNode} ancestor of the created {@link ReportNode}
     * @return a reference to this object
     */
    T withMessageTemplate(String key, String messageTemplate);

    /**
     * Provide one typed string value to build the {@link ReportNode} with.
     * @param key the key for the typed string value
     * @param value the string value
     * @param type the string representing the type of the string value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    T withTypedValue(String key, String value, String type);

    /**
     * Provide one untyped string value to build the {@link ReportNode} with.
     * @param key the key for the string value
     * @param value the string value
     * @return a reference to this object
     */
    T withUntypedValue(String key, String value);

    /**
     * Provide one typed double value to build the {@link ReportNode} with.
     * @param key the key for the typed double value
     * @param value the double value
     * @param type the string representing the type of the double value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    T withTypedValue(String key, double value, String type);

    /**
     * Provide one untyped double value to build the {@link ReportNode} with.
     * @param key the key for the double value
     * @param value the double value
     * @return a reference to this object
     */
    T withUntypedValue(String key, double value);

    /**
     * Provide one typed float value to build the {@link ReportNode} with.
     * @param key the key for the typed float value
     * @param value the float value
     * @param type the string representing the type of the float value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    T withTypedValue(String key, float value, String type);

    /**
     * Provide one untyped float value to build the {@link ReportNode} with.
     * @param key the key for the float value
     * @param value the float value
     * @return a reference to this object
     */
    T withUntypedValue(String key, float value);

    /**
     * Provide one typed int value to build the {@link ReportNode} with.
     * @param key the key for the typed int value
     * @param value the int value
     * @param type the string representing the type of the int value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    T withTypedValue(String key, int value, String type);

    /**
     * Provide one untyped int value to build the {@link ReportNode} with.
     * @param key the key for the int value
     * @param value the int value
     * @return a reference to this object
     */
    T withUntypedValue(String key, int value);

    /**
     * Provide one typed long value to build the {@link ReportNode} with.
     * @param key the key for the typed long value
     * @param value the long value
     * @param type the string representing the type of the long value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    T withTypedValue(String key, long value, String type);

    /**
     * Provide one untyped long value to build the {@link ReportNode} with.
     * @param key the key for the long value
     * @param value the long value
     * @return a reference to this object
     */
    T withUntypedValue(String key, long value);

    /**
     * Provide one typed boolean value to build the {@link ReportNode} with.
     * @param key the key for the typed boolean value
     * @param value the boolean value
     * @param type the string representing the type of the boolean value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    T withTypedValue(String key, boolean value, String type);

    /**
     * Provide one untyped boolean value to build the {@link ReportNode} with.
     * @param key the key for the boolean value
     * @param value the boolean value
     * @return a reference to this object
     */
    T withUntypedValue(String key, boolean value);

    /**
     * Provide the {@link TypedValue#SEVERITY} typed value associated to {@link ReportConstants#SEVERITY_KEY} key to build the {@link ReportNode} with.
     * @param severity the {@link TypedValue#SEVERITY} typed value associated to {@link ReportConstants#SEVERITY_KEY} key
     * @return a reference to this object
     */
    T withSeverity(TypedValue severity);

    /**
     * Provide the {@link String} value for the {@link TypedValue#SEVERITY} type associated to {@link ReportConstants#SEVERITY_KEY} key to build the {@link ReportNode} with.
     * @param severity the {@link String} value
     * @return a reference to this object
     */
    T withSeverity(String severity);
}

/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.reporter;

public interface ReportNodeChildAdder {

    /**
     * Build the corresponding {@link ReportNode}.
     * @return the new {@link ReportNode} corresponding to current <code>ReportNodeAdder</code>
     */
    ReportNode add();

    /**
     * Provide the key to build the {@link ReportNode} with.
     * @param key the key identifying the message to build
     * @return a reference to this object
     */
    ReportNodeChildAdder withKey(String key);

    /**
     * Provide the message template to build the {@link ReportNode} with.
     * @param messageTemplate functional log, which may contain references to values using the <code>${key}</code> syntax,
     *                        the values mentioned being the values of corresponding {@link ReportNode} and the values of any
     *                        {@link ReportNode} ancestor of the created <code>ReporterNode</code>
     * @return a reference to this object
     */
    ReportNodeChildAdder withMessageTemplate(String messageTemplate);

    /**
     * Provide one typed string value to build the {@link ReportNode} with.
     * @param key the key for the typed string value
     * @param value the string value
     * @param type the string representing the type of the string value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    ReportNodeChildAdder withTypedValue(String key, String value, String type);

    /**
     * Provide one string value to build the {@link ReportNode} with.
     * @param key the key for the string value
     * @param value the string value
     * @return a reference to this object
     */
    ReportNodeChildAdder withValue(String key, String value);

    /**
     * Provide one typed double value to build the {@link ReportNode} with.
     * @param key the key for the typed double value
     * @param value the double value
     * @param type the string representing the type of the double value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    ReportNodeChildAdder withTypedValue(String key, double value, String type);

    /**
     * Provide one double value to build the {@link ReportNode} with.
     * @param key the key for the double value
     * @param value the double value
     * @return a reference to this object
     */
    ReportNodeChildAdder withValue(String key, double value);

    /**
     * Provide one typed float value to build the {@link ReportNode} with.
     * @param key the key for the typed float value
     * @param value the float value
     * @param type the string representing the type of the float value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    ReportNodeChildAdder withTypedValue(String key, float value, String type);

    /**
     * Provide one float value to build the {@link ReportNode} with.
     * @param key the key for the float value
     * @param value the float value
     * @return a reference to this object
     */
    ReportNodeChildAdder withValue(String key, float value);

    /**
     * Provide one typed int value to build the {@link ReportNode} with.
     * @param key the key for the typed int value
     * @param value the int value
     * @param type the string representing the type of the int value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    ReportNodeChildAdder withTypedValue(String key, int value, String type);

    /**
     * Provide one int value to build the {@link ReportNode} with.
     * @param key the key for the int value
     * @param value the int value
     * @return a reference to this object
     */
    ReportNodeChildAdder withValue(String key, int value);

    /**
     * Provide one typed long value to build the {@link ReportNode} with.
     * @param key the key for the typed long value
     * @param value the long value
     * @param type the string representing the type of the long value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    ReportNodeChildAdder withTypedValue(String key, long value, String type);

    /**
     * Provide one long value to build the {@link ReportNode} with.
     * @param key the key for the long value
     * @param value the long value
     * @return a reference to this object
     */
    ReportNodeChildAdder withValue(String key, long value);

    /**
     * Provide one typed boolean value to build the {@link ReportNode} with.
     * @param key the key for the typed boolean value
     * @param value the boolean value
     * @param type the string representing the type of the boolean value provided (see {@link TypedValue} constants for some generic types)
     * @return a reference to this object
     */
    ReportNodeChildAdder withTypedValue(String key, boolean value, String type);

    /**
     * Provide one boolean value to build the {@link ReportNode} with.
     * @param key the key for the boolean value
     * @param value the boolean value
     * @return a reference to this object
     */
    ReportNodeChildAdder withValue(String key, boolean value);

    /**
     * Provide the typed value for the default severity key to build the {@link ReportNode} with.
     * @param severity the typed value
     * @return a reference to this object
     */
    ReportNodeChildAdder withSeverity(TypedValue severity);
}
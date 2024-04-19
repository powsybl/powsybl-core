/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import java.util.Set;

/**
 * A measurement with a discrete value (string, boolean or int) such as: tap position, switch position and so on.
 * Can have properties in addition of explicit fields to add precisions about the measurement if necessary.
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface DiscreteMeasurement {

    /**
     * Specify what is measured.
     */
    enum Type {
        TAP_POSITION,
        SWITCH_POSITION,
        SHUNT_COMPENSATOR_SECTION,
        OTHER
    }

    /**
     * If it is the modelization of a tap position (or any discrete measurement on a tap changer),
     * can explicit which tap changer of the transformer it is applied on.
     */
    enum TapChanger {
        RATIO_TAP_CHANGER,
        PHASE_TAP_CHANGER,
        RATIO_TAP_CHANGER_1,
        PHASE_TAP_CHANGER_1,
        RATIO_TAP_CHANGER_2,
        PHASE_TAP_CHANGER_2,
        RATIO_TAP_CHANGER_3,
        PHASE_TAP_CHANGER_3
    }

    /**
     * Explicit what type of discrete value is used for this measurement (boolean, int or string).
     */
    enum ValueType {
        BOOLEAN,
        INT,
        STRING
    }

    /**
     * Get ID of the measurement if it exists. It is optional (can be null).
     */
    String getId();

    /**
     * Get the type of the measurement (specify what it is measured, see {@link Type}).
     */
    Type getType();

    /**
     * Get the tap changer the measurement is applied on if necessary (see {@link TapChanger}).
     * Must be null if the measurement is not applied to a tap changer. Throw an exception if the extended equipment is not a transformer.
     * Must be not null if the measurement is not applied to a tap changer, else throw an exception.
     */
    TapChanger getTapChanger();

    /**
     * Get all the property names with values applied to this measurement.
     */
    Set<String> getPropertyNames();

    /**
     * Return property value associated to the given property name if it exists, else return null.
     */
    String getProperty(String name);

    /**
     * Put a given property value associated with a given property name.
     */
    DiscreteMeasurement putProperty(String name, String property);

    /**
     * Remove the property value associated with the given property name if it exists. Else, do nothing.
     */
    DiscreteMeasurement removeProperty(String name);

    /**
     * Get the type of measured discrete value (int, boolean or string, see {@link ValueType}).
     */
    ValueType getValueType();

    /**
     * Get value as String if the type of measured discrete value is STRING (see {@link ValueType}).
     * Else throw an exception.
     */
    String getValueAsString();

    /**
     * Get value as int if the type of measured discrete value is INT (see {@link ValueType}).
     * Else throw an exception.
     */
    int getValueAsInt();

    /**
     * Get value as boolean if the type of measured discrete value is BOOLEAN (see {@link ValueType}).
     * Else throw an exception.
     */
    boolean getValueAsBoolean();

    /**
     * Set the discrete measured value and set the value type as STRING (see {@link ValueType}).
     * Can not be null if the measurement is valid.
     */
    DiscreteMeasurement setValue(String value);

    /**
     * Set the discrete measured value and set the value type as INT (see {@link ValueType}).
     */
    DiscreteMeasurement setValue(int value);

    /**
     * Set the discrete measured value and set the value type as BOOLEAN (see {@link ValueType}).
     */
    DiscreteMeasurement setValue(boolean value);

    /**
     * Get validity status of the measurement.
     * If it is true (i.e. the measurement is valid), the discrete measured value can not be null.
     */
    boolean isValid();

    /**
     * Set validity status of the measurement.
     * If it is true (i.e. the measurement is valid), the discrete measured value can not be null.
     */
    DiscreteMeasurement setValid(boolean valid);

    /**
     * Remove the measurement from the equipment.
     */
    void remove();
}

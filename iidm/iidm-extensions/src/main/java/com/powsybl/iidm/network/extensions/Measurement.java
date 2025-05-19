/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Set;

/**
 * A measurement with a continuous numeric value (double) such as: angle, voltage, active power and so on.
 * Can have properties in addition of explicit fields to add precisions about the measurement if necessary.
 *
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface Measurement {

    /**
     * Specify what is measured.
     */
    enum Type {
        ANGLE,
        ACTIVE_POWER,
        APPARENT_POWER,
        REACTIVE_POWER,
        CURRENT,
        VOLTAGE,
        FREQUENCY,
        OTHER
    }

    /**
     * Get ID of the measurement if it exists. It is optional (can be null).
     */
    String getId();

    /**
     * Get the type of measurement (specify what is measured, see {@link Type}).
     */
    Type getType();

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
    Measurement putProperty(String name, String property);

    /**
     * Remove the property value associated with the given property name if it exists. Else, do nothing.
     */
    Measurement removeProperty(String name);

    /**
     * Set measurement value.
     * Can not be NaN if the measurement is valid.
     */
    Measurement setValue(double value);

    /**
     * Get measurement value.
     */
    double getValue();

    /**
     * Set the standard deviation.
     */
    Measurement setStandardDeviation(double standardDeviation);

    /**
     * Get the standard deviation. Return NaN if unspecified.
     */
    double getStandardDeviation();

    /**
     * Get validity status of the measurement.
     * If it is true (i.e. the measurement is valid), the measured value can not be NaN.
     */
    boolean isValid();

    /**
     * Set validity status of the measurement.
     * If it is true (i.e. the measurement is valid), the measured value can not be NaN.
     */
    Measurement setValid(boolean valid);

    /**
     * Set measurement value and validity status at once.
     * This default implementation is based on the two single mutators,
     * called in an order preventing spurious exception throwing.
     * A real implementation will call the check routine just once,
     * before the two effective mutations.
     */
    default Measurement setValueAndValidity(double v, boolean valid) {
        if (valid) {
            setValue(v);
            setValid(true);
        } else {
            setValid(false);
            setValue(v);
        }
        return this;
    }

    /**
     * Get which side the measurement is applied on (see {@link ThreeSides}).
     */
    ThreeSides getSide();

    /**
     * Remove the measurement from the equipment.
     */
    void remove();
}

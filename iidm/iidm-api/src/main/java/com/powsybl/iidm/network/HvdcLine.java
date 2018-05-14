/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A HVDC line connected to two HVDC converters on DC side.
 * It has to be connected to the same <code>{@link HvdcConverterStation}</code> subclass.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface HvdcLine extends Identifiable<HvdcLine> {

    /**
     * Converters mode used to known the sign of the active power of the HVDC line.
     */
    enum ConvertersMode {
        SIDE_1_RECTIFIER_SIDE_2_INVERTER,
        SIDE_1_INVERTER_SIDE_2_RECTIFIER
    }

    /**
     * Get the network this HVDC line belongs.
     * @return the network this HVDC line belongs
     */
    Network getNetwork();

    /**
     * Get converters mode.
     * @return converters mode
     */
    ConvertersMode getConvertersMode();

    /**
     * Change converters mode.
     * @param mode converters mode
     * @return the station itself to allow method chaining.
     */
    HvdcLine setConvertersMode(ConvertersMode mode);

    /**
     * Get resistance (in &#937;) of the line.
     * @return the resistance of the line
     */
    double getR();

    /**
     * Set the resistance (in &#937;) of the line.
     * @param r the resistance of the line
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setR(double r);

    /**
     * Get the nominal voltage (in Kv).
     * @return the nominal voltage.
     */
    double getNominalV();

    /**
     * Set the nominal voltage.
     * @param nominalV the nominal voltage.
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setNominalV(double nominalV);

    /**
     * Get the active power setpoint (in MW).
     * @return the active power setpoint
     */
    double getActivePowerSetpoint();

    /**
     * Set the active power setpoint (in MW).
     * @param activePowerSetpoint the active power setpoint
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setActivePowerSetpoint(double activePowerSetpoint);

    /**
     * Get the maximum active power (in MW).
     * @return the maximum active power
     */
    double getMaxP();

    /**
     * Set the maximum active power (in MW).
     * @param maxP the maximum active power
     * @return the HVDC line itself to allow method chaining
     */
    HvdcLine setMaxP(double maxP);

    /**
     * Get the HVDC converter station connected on side 1.
     * @return the HVDC converter station connected on side 1
     */
    HvdcConverterStation<?> getConverterStation1();

    /**
     * Get the HVDC converter station connected on side 2.
     * @return the HVDC converter station connected on side 2
     */
    HvdcConverterStation<?> getConverterStation2();

    /**
     * Remove the HVDC line
     */
    void remove();
}

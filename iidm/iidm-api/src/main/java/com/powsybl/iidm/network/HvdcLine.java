/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.function.Predicate;

/**
 * A HVDC line connected to two HVDC converters on DC side.
 * It has to be connected to the same <code>{@link HvdcConverterStation}</code> subclass.
 *
 * <p>
 *  Characteristics
 * </p>
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Default value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the HVDC line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the HVDC line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">R</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The resistance of the HVDC line</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ConvertersMode</td>
 *             <td style="border: 1px solid black">ConvertersMode</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The converter's mode</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">NominalV</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The nominal voltage</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ActivePowerSetpoint</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The active power setpoint</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">MaxP</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The maximum active power</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ConverterStationId1</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The ID of the HVDC converter station connected on side 1</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ConverterStationId2</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The ID of the HVDC converter station connected on side 2</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
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
     * Get the HVDC converter station connected to a side
     * @return the HVDC converter station connected to the side
     */
    default HvdcConverterStation<?> getConverterStation(TwoSides side) {
        return (side == TwoSides.ONE) ? getConverterStation1() : getConverterStation2();
    }

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

    boolean connectConverterStations();

    boolean connectConverterStations(Predicate<Switch> isTypeSwitchToOperate);

    boolean connectConverterStations(Predicate<Switch> isTypeSwitchToOperate, TwoSides side);

    boolean disconnectConverterStations();

    boolean disconnectConverterStations(Predicate<Switch> isSwitchOpenable);

    boolean disconnectConverterStations(Predicate<Switch> isSwitchOpenable, TwoSides side);

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.HVDC_LINE;
    }
}

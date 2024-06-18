/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * A power generator.
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
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
 *             <td style="border: 1px solid black">Unique identifier of the generator</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the generator</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">EnergySource</td>
 *             <td style="border: 1px solid black">EnergySource</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> OTHER </td>
 *             <td style="border: 1px solid black">The energy source type</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">MinP</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The minimum active power</td>
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
 *             <td style="border: 1px solid black">RegulatingTerminal</td>
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> The generator's terminal </td>
 *             <td style="border: 1px solid black">The terminal used for regulation</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageRegulatorOn</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> false </td>
 *             <td style="border: 1px solid black">The voltage regulating status</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetP</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MW</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The active power target</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetQ</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MVar</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black">only if `VoltageRegulatorOn` is set to false</td>
 *             <td style="border: 1px solid black">The reactive power target</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetV</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black">only if `VoltageRegulatorOn` is set to true</td>
 *             <td style="border: 1px solid black">The voltage target</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">RatedS</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">MVA</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">The rated nominal power</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">ReactiveLimits</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black">min/max</td>
 *             <td style="border: 1px solid black">Operational limits of the generator (P/Q/U diagram)</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 *<p>
 * To create a generator, see {@link GeneratorAdder}
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see GeneratorAdder
 * @see MinMaxReactiveLimits
 * @see ReactiveCapabilityCurve
 */
public interface Generator extends Injection<Generator>, ReactiveLimitsHolder {

    /**
     * Get the energy source.
     */
    EnergySource getEnergySource();

    Generator setEnergySource(EnergySource energySource);

    /**
     * Get the maximal active power in MW.
     */
    double getMaxP();

    /**
     * Set the maximal active power in MW.
     */
    Generator setMaxP(double maxP);

    /**
     * Get the minimal active power in MW.
     */
    double getMinP();

    /**
     * Set the minimal active power in MW.
     */
    Generator setMinP(double minP);

    /**
     * Get the voltage regulator status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isVoltageRegulatorOn();

    /**
     * Set the voltage regulator status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Get the terminal used for regulation.
     * @return the terminal used for regulation
     */
    Terminal getRegulatingTerminal();

    Generator setRegulatingTerminal(Terminal regulatingTerminal);

    /**
     * Get the voltage target in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * Set the voltage target in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setTargetV(double targetV);

    /**
     * Get the active power target in MW.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetP();

    /**
     * Set the active power target in MW.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setTargetP(double targetP);

    /**
     * Get the reactive power target in MVAR.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetQ();

    /**
     * Set the reactive power target in MVAR.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setTargetQ(double targetQ);

    /**
     * Get the rated nominal power (apparent power rating) in MVA.
     * @return the rated nominal power in MVA or NaN if not defined
     */
    double getRatedS();

    Generator setRatedS(double ratedS);

    /**
     * Get whether the generator can work as a synchronous condenser, that is, whether it may control voltage even if its targetP is equal to zero.
     */
    boolean isCondenser();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.GENERATOR;
    }
}

/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

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
public interface Generator extends Injection<Generator>, ReactiveLimitsHolder, VoltageRegulationHolder {

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
    @Deprecated(forRemoval = true, since = "7.2.0")
    boolean isVoltageRegulatorOn();

    /**
     * Set the voltage regulator status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    Generator setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Get the terminal used for regulation.
     * @return the terminal used for regulation
     */
    @Deprecated(forRemoval = true)
    Terminal getRegulatingTerminal();

    @Deprecated(forRemoval = true)
    Generator setRegulatingTerminal(Terminal regulatingTerminal);

    double getRemoteTargetV();

    /**
     * Get the voltage target in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * <p>
     *     Set the voltage target in kV, for the regulated terminal which can be remote or local AND without setting
     *     the equivalentLocalTargetV which takes the value {@link Double#NaN}
     * </p>
     * <p>
     *     To avoid setting the equivalentLocalTargetV to {@link Double#NaN}, please use {@link Generator#setTargetV(double, double)}
     * <p/>
     * <p>Depends on the working variant.</p>
     * @see VariantManager
     */
    @Deprecated(forRemoval = true)
    Generator setTargetV(double targetV);

    /**
     * <p>
     *     If set, returns a local target voltage that is expected to be consistent with the remote target voltage.
     *     When defined, this value can be used by simulators that deactivate the remote voltage algorithms,
     *     or by dynamic simulators that use this voltage as a starting value.
     * </p>
     * <p>
     *     To set the equivalentLocalTargetV use {@link Generator#setTargetV(double, double)}
     * </p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getEquivalentLocalTargetV();

    /**
     * <p>
     *     Set the voltage target in kV and set the local target in kV.
     * </p>
     * <p>Depends on the working variant.</p>
     * @param targetV the voltage target in kV (see {@link Generator#getTargetV()}).
     * @param equivalentLocalTargetV the local target in kV (see {@link Generator#getEquivalentLocalTargetV()}).
     * @see VariantManager
     */
    @Deprecated(forRemoval = true)
    Generator setTargetV(double targetV, double equivalentLocalTargetV);

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
     * Get whether the generator may behave as a condenser, for instance if it may control voltage even if its targetP is equal to zero.
     */
    boolean isCondenser();

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.GENERATOR;
    }

    default void applySolvedValues() {
        setTargetPToP();
        setTargetQToQ();
        setTargetVToV();
    }

    default void setTargetPToP() {
        double p = this.getTerminal().getP();
        if (!Double.isNaN(p)) {
            this.setTargetP(-p);
        }
    }

    default void setTargetQToQ() {
        double q = this.getTerminal().getQ();
        if (!Double.isNaN(q)) {
            this.setTargetQ(-q);
        }
    }

    default void setTargetVToV() {
        Bus bus = this.getTerminal().getBusView().getBus();
        if (bus != null && !Double.isNaN(bus.getV())) {
            this.setTargetV(bus.getV());
        }
    }
}

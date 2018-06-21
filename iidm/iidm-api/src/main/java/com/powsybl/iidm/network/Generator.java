/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A power generator.
 *<p>
 * To create a generator, see {@link GeneratorAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
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
     * Depends on the working state.
     * @see StateManager
     */
    boolean isVoltageRegulatorOn();

    /**
     * Set the voltage regulator status.
     * <p>
     * Depends on the working state.
     * @see StateManager
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
     * Depends on the working state.
     * @see StateManager
     */
    double getTargetV();

    /**
     * Set the voltage target in kV.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Generator setTargetV(double targetV);

    /**
     * Get the active power target in MW.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    double getTargetP();

    /**
     * Set the active power target in MW.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Generator setTargetP(double targetP);

    /**
     * Get the reactive power target in MVAR.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    double getTargetQ();

    /**
     * Set the reactive power target in MVAR.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Generator setTargetQ(double targetQ);

    /**
     * Get the rated nominal power in MVA.
     * @return the rated nominal power in MVA or NaN if not defined
     */
    double getRatedS();

    Generator setRatedS(double ratedS);
}

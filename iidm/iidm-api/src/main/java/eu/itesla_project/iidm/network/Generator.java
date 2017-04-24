/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

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
public interface Generator extends SingleTerminalConnectable<Generator>, ReactiveLimitsHolder {

    /**
     * Get the energy source.
     */
    EnergySource getEnergySource();

    Generator setEnergySource(EnergySource energySource);

    /**
     * Get the maximal active power in MW.
     */
    float getMaxP();

    /**
     * Set the maximal active power in MW.
     */
    Generator setMaxP(float maxP);

    /**
     * Get the minimal active power in MW.
     */
    float getMinP();

    /**
     * Set the minimal active power in MW.
     */
    Generator setMinP(float minP);

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
    float getTargetV();

    /**
     * Set the voltage target in kV.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Generator setTargetV(float targetV);

    /**
     * Get the active power target in MW.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    float getTargetP();

    /**
     * Set the active power target in MW.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Generator setTargetP(float targetP);

    /**
     * Get the reactive power target in MVAR.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    float getTargetQ();

    /**
     * Set the reactive power target in MVAR.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    Generator setTargetQ(float targetQ);

    /**
     * Get the rated nominal power in MVA.
     * @return the rated nominal power in MVA or NaN if not defined
     */
    float getRatedS();

    Generator setRatedS(float ratedS);
}

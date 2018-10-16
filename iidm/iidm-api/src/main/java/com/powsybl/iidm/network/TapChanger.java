/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TapChanger<C extends TapChanger<C, S>, S extends TapChanger.Tap<S>> {

    /**
     * Get the lowest tap position corresponding to the first tap of the tap changer.
     */
    int getLowTapPosition();

    /**
     * Get the highest tap position corresponding to the last tap of the tap changer.
     */
    int getHighTapPosition();

    /**
     * Get the current tap position.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    int getTapPosition();

    /**
     * Set the current tap position.
     * <p>
     * It is expected to be contained between the lowest and the highest tap position.
     * <p>
     * Depends on the working state.
     * @see StateManager
     *
     * @param tapPosition the current tap position
     */
    C setTapPosition(int tapPosition);

    /**
     * Get the number of taps.
     */
    int getTapCount();

    /**
     * Get a tap.
     *
     * @param tapPosition position of the tap
     * @return the tap
     */
    S getTap(int tapPosition);

    /**
     * Get the current tap.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    S getCurrentTap();

    /**
     * Get the regulating status.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    boolean isRegulating();

    /**
     * Set the regulating status.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    C setRegulating(boolean regulating);

    /**
     * Get the terminal used for regulation.
     */
    Terminal getRegulationTerminal();

    /**
     * Set the terminal used for regulation.
     */
    C setRegulationTerminal(Terminal regulationTerminal);

    /**
     * Remove the tap changer.
     */
    void remove();

    public interface Tap<S extends Tap> {

        /**
         * Get the voltage ratio in per unit.
         */
        double getRatio();

        /**
         * Set the voltage ratio in per unit.
         */
        S setRatio(double ratio);

        /**
         * Get the resistance deviation in percent of nominal value.
         */
        double getRdr();

        /**
         * Set the resistance deviation in percent of nominal value.
         */
        S setRdr(double rdr);

        /**
         * Get the reactance deviation in percent of nominal value.
         */
        double getRdx();

        /**
         * Set the reactance deviation in percent of nominal value.
         */
        S setRdx(double rdx);

        /**
         * Get the susceptance deviation in percent of nominal value.
         */
        double getRdb();

        /**
         * Set the susceptance deviation in percent of nominal value.
         */
        S setRdb(double rdb);

        /**
         * Get the conductance deviation in percent of nominal value.
         */
        double getRdg();

        /**
         * Set the conductance deviation in percent of nominal value.
         */
        S setRdg(double rdg);

    }
}

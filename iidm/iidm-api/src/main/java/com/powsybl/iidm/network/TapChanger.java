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
public interface TapChanger<TC extends TapChanger<TC, TCS>, TCS extends TapChangerStep<TCS>> {

    /**
     * Get the lowest tap position corresponding to the first step of the tap changer.
     */
    int getLowTapPosition();

    /**
     * Get the highest tap position corresponding to the last step of the tap changer.
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
    TC setTapPosition(int tapPosition);

    /**
     * Get the number of steps.
     */
    int getStepCount();

    /**
     * Get a step.
     *
     * @param tapPosition position of the tap
     * @return the step
     */
    TCS getStep(int tapPosition);

    /**
     * Get the current step.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    TCS getCurrentStep();

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
    TC setRegulating(boolean regulating);

    /**
     * Get the terminal used for regulation.
     */
    Terminal getRegulationTerminal();

    /**
     * Set the terminal used for regulation.
     */
    TC setRegulationTerminal(Terminal regulationTerminal);

    /**
     * Remove the tap changer.
     */
    void remove();

}

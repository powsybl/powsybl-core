/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface TapChanger<TC extends TapChanger<TC, TCS>, TCS extends TapChangerStep<TCS>> {

    /**
     * Get the number of steps.
     */
    int getStepCount();

    /**
     * Get the lowest step position.
     */
    int getLowStepPosition();

    /**
     * Get the highest step position.
     */
    int getHighStepPosition();

    /**
     * Get the current step position.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    int getCurrentStepPosition();

    /**
     * Set the current step position.
     * <p>
     * It is expected to be contained between the lowest and the highest step position.
     * <p>
     * Depends on the working state.
     * @see StateManager
     *
     * @param currentStepPosition the current step position
     */
    TC setCurrentStepPosition(int currentStepPosition);

    /**
     * Get a step.
     *
     * @param position position of the step
     * @return the step
     */
    TCS getStep(int position);

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
    Terminal getTerminal();

    /**
     * Set the terminal used for regulation.
     */
    void setTerminal(Terminal t);

    /**
     * Remove the tap changer.
     */
    void remove();

}

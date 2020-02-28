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
public interface TapChanger<C extends TapChanger<C, S>, S extends TapChangerStep<S>> {

    /**
     * Get the lowest tap position corresponding to the first step of the tap changer.
     */
    int getLowTapPosition();

    /**
     * Set the lowest tap position corresponding to the first step of the tap changer.
     */
    C setLowTapPosition(int lowTapPosition);

    /**
     * Get the highest tap position corresponding to the last step of the tap changer.
     */
    int getHighTapPosition();

    /**
     * Get the current tap position.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    int getTapPosition();

    /**
     * Set the current tap position.
     * <p>
     * It is expected to be contained between the lowest and the highest tap position.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     *
     * @param tapPosition the current tap position
     */
    C setTapPosition(int tapPosition);

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
    S getStep(int tapPosition);

    /**
     * Get the current step.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    S getCurrentStep();

    /**
     * Get the regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isRegulating();

    /**
     * Set the regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
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
     * Get the tap changer's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the tap changer is regulating.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getTargetDeadband() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the tap changer's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the tap changer is regulating. It must be positive.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default C setTargetDeadband(double targetDeadband) {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove the tap changer.
     */
    void remove();

}

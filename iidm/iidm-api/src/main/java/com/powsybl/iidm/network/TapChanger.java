/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TapChanger<
    C extends TapChanger<C, S, R, A>,
    S extends TapChangerStep<S>,
    R extends TapChangerStepsReplacer<R, A>,
    A extends TapChangerStepAdder<A, R>> {

    /**
     * Get the load tap changing capabilities status.
     */
    boolean hasLoadTapChangingCapabilities();

    /**
     * Set the load tap changing capabilities status.
     * @return itself for method chaining.
     */
    C setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities);

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
     * Get an optional containing the current tap position if it is defined.
     * Otherwise, get an empty optional.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default OptionalInt findTapPosition() {
        return OptionalInt.of(getTapPosition());
    }

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
     * Unset the current tap position: tap position is now undefined.
     * Note: this can be done <b>only</b> in SCADA validation level.
     */
    default C unsetTapPosition() {
        throw ValidationUtil.createUnsetMethodException();
    }

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
     * Return a replacer that allow to replace the whole step list.
     */
    R stepsReplacer();

    /**
     * Get the current step.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    S getCurrentStep();

    /**
     * Get the position of the neutral step (rho = 1, alpha = 0) if it exists.
     * Otherwise return an empty optional.
     */
    default OptionalInt getNeutralPosition() {
        return OptionalInt.empty();
    }

    /**
     * Get the neutral step (rho = 1, alpha = 0) if it exists.
     * Otherwise return an empty optional.
     */
    default Optional<S> getNeutralStep() {
        return Optional.empty();
    }

    /**
     * Get the regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isRegulating();

    /**
     * Set the regulating status.
     * Tap changer must support onload tap changing capabilities to enable regulation.
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

    /**
     * Get all Tap changer steps
     */
    default Map<Integer, S> getAllSteps() {
        Map<Integer, S> steps = new HashMap<>();
        for (int i = getLowTapPosition(); i <= getHighTapPosition(); i++) {
            steps.put(i, getStep(i));
        }
        return steps;
    }
}

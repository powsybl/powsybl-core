/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A shunt compensator.
 *
 * To create a shunt compensator, see {@link ShuntCompensatorAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see ShuntCompensatorAdder
 */
public interface ShuntCompensator extends Injection<ShuntCompensator> {

    /**
     * Get the maximum section count.
     */
    int getMaximumSectionCount();

    /**
     * Set the maximum number of section.
     *
     * @param maximumSectionCount the maximum number of section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensator setMaximumSectionCount(int maximumSectionCount);

    /**
     * Get the current section count.
     * <p>
     * It is expected to be greater than one and lesser than or equal to the
     * maximum section count.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    int getCurrentSectionCount();

    /**
     * Change the number of section.
     *
     * <p>
     * Depends on the working state.
     *
     * @see StateManager
     * @param currentSectionCount the number of section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensator setCurrentSectionCount(int currentSectionCount);

    /**
     * Get the susceptance per section in S.
     */
    double getbPerSection();

    /**
     * Set the susceptance per section in S.
     *
     * @param bPerSection the susceptance per section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensator setbPerSection(double bPerSection);

    /**
     * Get the susceptance for the maximum section count.
     */
    double getMaximumB();

    /**
     * Get the susceptance for the current section counts.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    double getCurrentB();

}

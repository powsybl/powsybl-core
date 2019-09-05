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

    ShuntCompensatorModelType getModelType();

    <M extends ShuntCompensatorModel> M getModel(Class<M> type);

    /**
     * Get the current section count.
     * <p>
     * It is expected to be greater than one and lesser than or equal to the
     * maximum section count.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    int getCurrentSectionCount();

    /**
     * Change the number of section.
     *
     * <p>
     * Depends on the working variant.
     *
     * @see VariantManager
     * @param currentSectionCount the number of section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensator setCurrentSectionCount(int currentSectionCount);

    /**
     * Get the susceptance for the current section counts.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getCurrentB();

    /**
     * Get the maximum susceptance.
     */
    double getMaximumB();

    /**
     * Get the terminal used for regulation if it exists. Else return null.
     */
    Terminal getRegulatingTerminal();

    /**
     * Set the terminal used for regulation.
     */
    ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal);

    /**
     * Get the shunt compensator's regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isRegulating();

    /**
     * Set the shunt compensator's regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    ShuntCompensator setRegulating(boolean regulating);

    /**
     * Get the voltage target in kV if it exists. Else return NaN.
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
    ShuntCompensator setTargetV(double targetV);
}

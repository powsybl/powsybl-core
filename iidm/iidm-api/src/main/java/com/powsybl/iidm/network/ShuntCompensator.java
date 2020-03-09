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
     * Get the maximum number of sections.
     */
    int getMaximumSectionCount();

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
     * Get the susceptance for the current section count.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getCurrentB();

    /**
     * Get the conductance for the current section count.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getCurrentG();

    /**
     * Get the model type of the shunt compensator (linear or non-linear)
     */
    ShuntCompensatorModelType getModelType();

    /**
     * Get the shunt model.
     */
    ShuntCompensatorModel getModel();

    <M extends ShuntCompensatorModel> M getModel(Class<M> modelType);

    /**
     * Get the terminal used for regulation.
     */
    default Terminal getRegulatingTerminal() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the terminal used for regulation.
     * If null is passed as regulating terminal, the regulation is considered local.
     */
    default ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the shunt compensator's regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default boolean isVoltageRegulatorOn() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the shunt compensator's regulating status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default ShuntCompensator setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the shunt compensator's voltage target in kV if it exists. Else return NaN.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getTargetV() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the shunt compensator's voltage target in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default ShuntCompensator setTargetV(double targetV) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the shunt compensator's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the shunt compensator is regulating.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default double getTargetDeadband() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the shunt compensator's deadband (in kV) used to avoid excessive update of discrete control while regulating.
     * This attribute is necessary only if the shunt compensator is regulating. It must be positive.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default ShuntCompensator setTargetDeadband(double targetDeadband) {
        throw new UnsupportedOperationException();
    }
}

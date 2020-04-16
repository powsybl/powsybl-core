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
     * Get the current count of sections in service.
     * Please note sections can only be sequentially in service i.e. all sections from section 1 to section currentSectionCount are in service.
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
     * Change the current count of sections in service.
     * Please note sections can only be sequentially in service i.e. all section from section 1 to section currentSectionCount are in service.
     * <p>
     * Depends on the working variant.
     *
     * @see VariantManager
     * @param currentSectionCount the number of section
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensator setCurrentSectionCount(int currentSectionCount);

    /**
     * Get the susceptance (in S) for the current section count i.e. the sum of the sections' susceptances for all sections in service.
     * Return 0 if no section is in service (disconnected state).
     * @see #getCurrentSectionCount()
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getCurrentB();

    /**
     * Get the conductance (in S) for the current section count i.e. the sum of the sections' conductances for all sections in service.
     * If the conductance of a section in service is undefined, it is considered equal to 0.
     * Return 0 if no section is in service (disconnected state).
     * @see #getCurrentSectionCount()
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getCurrentG();

    /**
     * Get the susceptance (in S) for a given section count i.e. the sum of the sections' susceptances from 1 to sectionCount.
     * Return O if sectionCount is equal to 0 (disconnected state).
     */
    double getB(int sectionCount);

    /**
     * Get the conductance (in S) for a given section count i.e. the sum of the sections' conductances from 1 to sectionCount.
     * If the conductance of a section is undefined, it is considered equal to 0.
     * Return 0 if sectionCount is equal to 0 (disconnected state).
     */
    double getG(int sectionCount);

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

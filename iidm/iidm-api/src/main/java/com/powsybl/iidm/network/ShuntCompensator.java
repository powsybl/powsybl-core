/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.OptionalInt;

/**
 * A shunt compensator.
 *
 * <p>
 *  Characteristics
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse: collapse">
 *     <thead>
 *         <tr>
 *             <th style="border: 1px solid black">Attribute</th>
 *             <th style="border: 1px solid black">Type</th>
 *             <th style="border: 1px solid black">Unit</th>
 *             <th style="border: 1px solid black">Required</th>
 *             <th style="border: 1px solid black">Defaut value</th>
 *             <th style="border: 1px solid black">Description</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td style="border: 1px solid black">Id</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Unique identifier of the shunt compensator</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the shunt compensator</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">bPerSection</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The Positive sequence shunt (charging) susceptance per section </td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">MaximumSectionCount</td>
 *             <td style="border: 1px solid black">integer</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The maximum number of sections that may be switched on</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">CurrentSectionCount</td>
 *             <td style="border: 1px solid black">integer</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The current number of section that may be switched on</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">RegulatingTerminal</td>
 *             <td style="border: 1px solid black">Terminal</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> The shunt compensator's terminal </td>
 *             <td style="border: 1px solid black">The terminal used for regulation</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetV</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">only if VoltageRegulatorOn is set to true</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The voltage target</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">TargetDeadband</td></td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">kV</td>
 *             <td style="border: 1px solid black">only if VoltageRegulatorOn is set to true</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The deadband used to avoid excessive update of controls</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">VoltageRegulatorOn</td>
 *             <td style="border: 1px solid black">boolean</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">no</td>
 *             <td style="border: 1px solid black"> false </td>
 *             <td style="border: 1px solid black">The voltage regulating status</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * To create a shunt compensator, see {@link ShuntCompensatorAdder}
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @see ShuntCompensatorAdder
 */
public interface ShuntCompensator extends Injection<ShuntCompensator> {

    /**
     * Get the count of sections in service.
     * Please note sections can only be sequentially in service i.e. the first sectionCount sections are in service.
     * <p>
     * It is expected to be greater than one and lesser than or equal to the
     * maximum section count.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    int getSectionCount();

    /**
     * Get the count of sections in service if it is defined.
     * Otherwise, get an empty optional.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    default OptionalInt findSectionCount() {
        return OptionalInt.of(getSectionCount());
    }

    /**
     * Get the maximum number of sections that can be in service
     */
    int getMaximumSectionCount();

    /**
     * Change the count of sections in service.
     * Please note sections can only be sequentially in service i.e. the first sectionCount sections are in service.
     * <p>
     * Depends on the working variant.
     *
     * @see VariantManager
     * @param sectionCount the number of sections wanted to be put in service
     * @return the shunt compensator to chain method calls.
     */
    ShuntCompensator setSectionCount(int sectionCount);

    /**
     * Unset the count of sections in service.
     * Note: this can be done <b>only</b> in SCADA validation level.
     */
    default ShuntCompensator unsetSectionCount() {
        throw ValidationUtil.createUnsetMethodException();
    }

    /**
     * Get the susceptance (in S) of the shunt in its current state i.e. the sum of the sections' susceptances for all sections in service.
     * Return 0 if no section is in service (disconnected state).
     * @see #getSectionCount()
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getB();

    /**
     * Get the conductance (in S) of the shunt in its current state i.e. the sum of the sections' conductances for all sections in service.
     * If the conductance of a section in service is undefined, it is considered equal to 0.
     * Return 0 if no section is in service (disconnected state).
     * @see #getSectionCount()
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getG();

    /**
     * Get the susceptance (in S) for a given activated sections count i.e. the sum of the sections' susceptances from 1 to sectionCount.
     * Return O if sectionCount is equal to 0 (disconnected state).
     *
     * @param sectionCount count of wanted activated sections. Must be in [0; maximumSectionCount]. Else, throws a {@link ValidationException}.
     */
    double getB(int sectionCount);

    /**
     * Get the conductance (in S) for a given activated sections count i.e. the sum of the sections' conductances from 1 to sectionCount.
     * If the conductance of a section is undefined, it is considered equal to 0.
     * Return 0 if sectionCount is equal to 0 (disconnected state).
     *
     * @param sectionCount count of wanted activated sections. Must be in [0; maximumSectionCount]. Else, throws a {@link ValidationException}.
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
    boolean isVoltageRegulatorOn();

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

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.SHUNT_COMPENSATOR;
    }
}

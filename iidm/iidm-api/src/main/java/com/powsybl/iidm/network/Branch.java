/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Optional;

import static com.powsybl.iidm.network.util.LoadingLimitsUtil.initializeFromLoadingLimits;

/**
 * An equipment with two terminals.
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
 *             <td style="border: 1px solid black">Unique identifier of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">Name</td>
 *             <td style="border: 1px solid black">String</td>
 *             <td style="border: 1px solid black">-</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">Human-readable name of the branch</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">R</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series resistance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">X</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">&Omega;</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The series reactance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B1</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The first side shunt susceptance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">G2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt conductance</td>
 *         </tr>
 *         <tr>
 *             <td style="border: 1px solid black">B2</td>
 *             <td style="border: 1px solid black">double</td>
 *             <td style="border: 1px solid black">S</td>
 *             <td style="border: 1px solid black">yes</td>
 *             <td style="border: 1px solid black"> - </td>
 *             <td style="border: 1px solid black">The second side shunt susceptance</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Branch<I extends Branch<I>> extends Identifiable<I> {

    /**
     * Get the first terminal.
     */
    Terminal getTerminal1();

    /**
     * Get the second terminal.
     */
    Terminal getTerminal2();

    Terminal getTerminal(TwoSides side);

    Terminal getTerminal(String voltageLevelId);

    TwoSides getSide(Terminal terminal);

    FlowsLimitsHolder getOperationalLimitsHolder1();

    FlowsLimitsHolder getOperationalLimitsHolder2();

    /**
     * Get the collection of the defined {@link OperationalLimitsGroup} on side 1.
     * @return the {@link OperationalLimitsGroup} s on side 1.
     */
    Collection<OperationalLimitsGroup> getOperationalLimitsGroups1();

    /**
     * Get the {@link OperationalLimitsGroup} corresponding to an ID on side 1.
     * @return the {@link OperationalLimitsGroup} of the given ID on side 1 if any, an empty {@link Optional} otherwise.
     */
    Optional<String> getSelectedOperationalLimitsGroupId1();

    /**
     * Get the {@link OperationalLimitsGroup} corresponding to an ID on side 1.
     * @return the {@link OperationalLimitsGroup} of the given ID on side 1 if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getOperationalLimitsGroup1(String id);

    /**
     * Get the selected {@link OperationalLimitsGroup} on side 1.
     * @return the selected {@link OperationalLimitsGroup} on side 1 if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup1();

    /**
     * <p>Create a new {@link OperationalLimitsGroup} on side 1 with the given ID.</p>
     * <p>If a group of the given ID already exists, it is replaced silently.</p>
     * @return the newly created group {@link OperationalLimitsGroup}.
     */
    OperationalLimitsGroup newOperationalLimitsGroup1(String id);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given ID as as the selected one on side 1.</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * To reset the selected group, use {@link #cancelSelectedOperationalLimitsGroup1}.</p>
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void setSelectedOperationalLimitsGroup1(String id);

    /**
     * <p>Remove the {@link OperationalLimitsGroup} corresponding to the given ID on side 1.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void removeOperationalLimitsGroup1(String id);

    /**
     * <p>Cancel the selected {@link OperationalLimitsGroup} on side 1.</p>
     * <p>After calling this method, no {@link OperationalLimitsGroup} is selected on side 1.</p>
     */
    void cancelSelectedOperationalLimitsGroup1();

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 1.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 1 if any, <code>null</code> otherwise.
     */
    default Optional<CurrentLimits> getCurrentLimits1() {
        return getSelectedOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 1.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 1 if any, <code>null</code> otherwise.
     */
    default CurrentLimits getNullableCurrentLimits1() {
        return getCurrentLimits1().orElse(null);
    }

    /**
     * Get the {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 1.
     * @return {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 1 if any, an empty {@link Optional} otherwise.
     */
    default Optional<ActivePowerLimits> getActivePowerLimits1() {
        return getSelectedOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    /**
     * Get the {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 1.
     * @return {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 1 if any, <code>null</code> otherwise.
     */
    default ActivePowerLimits getNullableActivePowerLimits1() {
        return getActivePowerLimits1().orElse(null);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 1.
     * @return {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 1 if any, an empty {@link Optional} otherwise.
     */
    default Optional<ApparentPowerLimits> getApparentPowerLimits1() {
        return getSelectedOperationalLimitsGroup1().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 1.
     * @return {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 1 if any, <code>null</code> otherwise.
     */
    default ApparentPowerLimits getNullableApparentPowerLimits1() {
        return getApparentPowerLimits1().orElse(null);
    }

    /**
     * <p>Create an adder to add a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup} on side 1.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup} on side 1.
     */
    CurrentLimitsAdder newCurrentLimits1();

    /**
     * <p>Create an adder to add a copy of the {@link CurrentLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 1. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param currentLimits a set of existing current limits.
     * @return an adder allowing to create a new {@link CurrentLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 1.
     */
    default CurrentLimitsAdder newCurrentLimits1(CurrentLimits currentLimits) {
        CurrentLimitsAdder currentLimitsAdder = newCurrentLimits1();
        return initializeFromLoadingLimits(currentLimitsAdder, currentLimits);
    }

    /**
     * <p>Create an adder to add a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup} on side 1.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup} on side 1.
     */
    ActivePowerLimitsAdder newActivePowerLimits1();

    /**
     * <p>Create an adder to add a copy of the {@link ActivePowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 1. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param activePowerLimits a set of existing active power limits.
     * @return an adder allowing to create a new {@link ActivePowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 1.
     */
    default ActivePowerLimitsAdder newActivePowerLimits1(ActivePowerLimits activePowerLimits) {
        ActivePowerLimitsAdder activePowerLimitsAdder = newActivePowerLimits1();
        return initializeFromLoadingLimits(activePowerLimitsAdder, activePowerLimits);
    }

    /**
     * <p>Create an adder to add a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup} on side 1.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup} on side 1.
     */
    ApparentPowerLimitsAdder newApparentPowerLimits1();

    /**
     * <p>Create an adder to add a copy of the {@link ApparentPowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 1. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param apparentPowerLimits a set of existing apparent power limits.
     * @return an adder allowing to create a new {@link ApparentPowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 1.
     */
    default ApparentPowerLimitsAdder newApparentPowerLimits1(ApparentPowerLimits apparentPowerLimits) {
        ApparentPowerLimitsAdder apparentPowerLimitsAdder = newApparentPowerLimits1();
        return initializeFromLoadingLimits(apparentPowerLimitsAdder, apparentPowerLimits);
    }

    /**
     * Get the collection of the defined {@link OperationalLimitsGroup} on side 2.
     * @return the {@link OperationalLimitsGroup} s on side 2.
     */
    Collection<OperationalLimitsGroup> getOperationalLimitsGroups2();

    /**
     * Get the {@link OperationalLimitsGroup} corresponding to an ID on side 2.
     * @return the {@link OperationalLimitsGroup} of the given ID on side 2 if any, an empty {@link Optional} otherwise.
     */
    Optional<String> getSelectedOperationalLimitsGroupId2();

    /**
     * Get the {@link OperationalLimitsGroup} corresponding to an ID on side 2.
     * @return the {@link OperationalLimitsGroup} of the given ID on side 2 if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getOperationalLimitsGroup2(String id);

    /**
     * Get the selected {@link OperationalLimitsGroup} on side 2.
     * @return the selected {@link OperationalLimitsGroup} on side 2 if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup2();

    /**
     * <p>Create a new {@link OperationalLimitsGroup} on side 2 with the given ID.</p>
     * <p>If a group of the given ID already exists, it is replaced silently.</p>
     * @return the newly created group {@link OperationalLimitsGroup}.
     */
    OperationalLimitsGroup newOperationalLimitsGroup2(String id);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given ID as as the selected one on side 2.</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * To reset the selected group, use {@link #cancelSelectedOperationalLimitsGroup2}.</p>
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void setSelectedOperationalLimitsGroup2(String id);

    /**
     * <p>Remove the {@link OperationalLimitsGroup} corresponding to the given ID on side 2.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void removeOperationalLimitsGroup2(String id);

    /**
     * <p>Cancel the selected {@link OperationalLimitsGroup} on side 2.</p>
     * <p>After calling this method, no {@link OperationalLimitsGroup} is selected on side 2.</p>
     */
    void cancelSelectedOperationalLimitsGroup2();

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 2.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 2 if any, <code>null</code> otherwise.
     */
    default Optional<CurrentLimits> getCurrentLimits2() {
        return getSelectedOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 2.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} on side 2 if any, <code>null</code> otherwise.
     */
    default CurrentLimits getNullableCurrentLimits2() {
        return getCurrentLimits2().orElse(null);
    }

    /**
     * Get the {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 2.
     * @return {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 2 if any, an empty {@link Optional} otherwise.
     */
    default Optional<ActivePowerLimits> getActivePowerLimits2() {
        return getSelectedOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    /**
     * Get the {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 2.
     * @return {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} on side 2 if any, <code>null</code> otherwise.
     */
    default ActivePowerLimits getNullableActivePowerLimits2() {
        return getActivePowerLimits2().orElse(null);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 2.
     * @return {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 2 if any, an empty {@link Optional} otherwise.
     */
    default Optional<ApparentPowerLimits> getApparentPowerLimits2() {
        return getSelectedOperationalLimitsGroup2().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 2.
     * @return {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} on side 2 if any, <code>null</code> otherwise.
     */
    default ApparentPowerLimits getNullableApparentPowerLimits2() {
        return getApparentPowerLimits2().orElse(null);
    }

    /**
     * <p>Create an adder to add a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup} on side 2.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup} on side 2.
     */
    CurrentLimitsAdder newCurrentLimits2();

    /**
     * <p>Create an adder to add a copy of the {@link CurrentLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 2. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param currentLimits a set of existing current limits.
     * @return an adder allowing to create a new {@link CurrentLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 2.
     */
    default CurrentLimitsAdder newCurrentLimits2(CurrentLimits currentLimits) {
        CurrentLimitsAdder currentLimitsAdder = newCurrentLimits2();
        return initializeFromLoadingLimits(currentLimitsAdder, currentLimits);
    }

    /**
     * <p>Create an adder to add a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup} on side 2.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup} on side 2.
     */
    ActivePowerLimitsAdder newActivePowerLimits2();

    /**
     * <p>Create an adder to add a copy of the {@link ActivePowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 2. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param activePowerLimits a set of existing active power limits.
     * @return an adder allowing to create a new {@link ActivePowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 2.
     */
    default ActivePowerLimitsAdder newActivePowerLimits2(ActivePowerLimits activePowerLimits) {
        ActivePowerLimitsAdder activePowerLimitsAdder = newActivePowerLimits2();
        return initializeFromLoadingLimits(activePowerLimitsAdder, activePowerLimits);
    }

    /**
     * <p>Create an adder to add a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup} on side 2.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup} on side 2.
     */
    ApparentPowerLimitsAdder newApparentPowerLimits2();

    /**
     * <p>Create an adder to add a copy of the {@link ApparentPowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 2. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param apparentPowerLimits a set of existing apparent power limits.
     * @return an adder allowing to create a new {@link ApparentPowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 2.
     */
    default ApparentPowerLimitsAdder newApparentPowerLimits2(ApparentPowerLimits apparentPowerLimits) {
        ApparentPowerLimitsAdder apparentPowerLimitsAdder = newApparentPowerLimits2();
        return initializeFromLoadingLimits(apparentPowerLimitsAdder, apparentPowerLimits);
    }

    default Optional<CurrentLimits> getCurrentLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getCurrentLimits1();
            case TWO -> getCurrentLimits2();
        };
    }

    default Optional<ActivePowerLimits> getActivePowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getActivePowerLimits1();
            case TWO -> getActivePowerLimits2();
        };
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getApparentPowerLimits1();
            case TWO -> getApparentPowerLimits2();
        };
    }

    default Optional<? extends LoadingLimits> getLimits(LimitType type, TwoSides side) {
        return switch (type) {
            case CURRENT -> getCurrentLimits(side);
            case ACTIVE_POWER -> getActivePowerLimits(side);
            case APPARENT_POWER -> getApparentPowerLimits(side);
            default ->
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        };
    }

    default CurrentLimits getNullableCurrentLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getNullableCurrentLimits1();
            case TWO -> getNullableCurrentLimits2();
        };
    }

    default ActivePowerLimits getNullableActivePowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getNullableActivePowerLimits1();
            case TWO -> getNullableActivePowerLimits2();
        };
    }

    default ApparentPowerLimits getNullableApparentPowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getNullableApparentPowerLimits1();
            case TWO -> getNullableApparentPowerLimits2();
        };
    }

    default LoadingLimits getNullableLimits(LimitType type, TwoSides side) {
        return switch (type) {
            case CURRENT -> getNullableCurrentLimits(side);
            case ACTIVE_POWER -> getNullableActivePowerLimits(side);
            case APPARENT_POWER -> getNullableApparentPowerLimits(side);
            default ->
                throw new UnsupportedOperationException(String.format("Getting %s limits is not supported.", type.name()));
        };
    }

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded();

    /**
     * Only checks overloading for LimitType.Current and permanent limits
     */
    boolean isOverloaded(double limitReductionValue);

    int getOverloadDuration();

    boolean checkPermanentLimit(TwoSides side, double limitReductionValue, LimitType type);

    boolean checkPermanentLimit(TwoSides side, LimitType type);

    boolean checkPermanentLimit1(double limitReductionValue, LimitType type);

    boolean checkPermanentLimit1(LimitType type);

    boolean checkPermanentLimit2(double limitReductionValue, LimitType type);

    boolean checkPermanentLimit2(LimitType type);

    Overload checkTemporaryLimits(TwoSides side, double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits(TwoSides side, LimitType type);

    Overload checkTemporaryLimits1(double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits1(LimitType type);

    Overload checkTemporaryLimits2(double limitReductionValue, LimitType type);

    Overload checkTemporaryLimits2(LimitType type);
}

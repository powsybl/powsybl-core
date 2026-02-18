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
import java.util.function.Function;

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
 *             <th style="border: 1px solid black">Default value</th>
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
     * Get all the selected {@link OperationalLimitsGroup} on the given <code>side</code>.
     * @param side the side to get the group limits on
     * @return all the selected {@link OperationalLimitsGroup} on the <code>side</code>, might be empty if none is selected.
     */
    Collection<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups(TwoSides side);

    /**
     * Get the id of all the selected {@link OperationalLimitsGroup} on the given <code>side</code>
     * @param side the side to get the id of the group limits on
     * @return all the selected {@link OperationalLimitsGroup} on the <code>side</code>, might be empty if none is selected.
     */
    Collection<String> getAllSelectedOperationalLimitsGroupIds(TwoSides side);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given IDs as selected on the given <code>side</code>. If other groups were also selected, they are still selected</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if any ID is <code>null</code>.</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(TwoSides, String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup1()}
     * To have a single group selected and deselect all other groups, use {@link #setSelectedOperationalLimitsGroup1(String)}
     * @param side the side on which to add selected groups
     * @param ids the IDs of one or more {@link OperationalLimitsGroup}
     */
    void addSelectedOperationalLimitsGroups(TwoSides side, String... ids);

    /**
     * <p>Deselect the {@link OperationalLimitsGroup} corresponding to all the <code>ids</code> on the given <code>side</code>.</p>
     * <p>If the {@link OperationalLimitsGroup} exists but is not selected, this method will do nothing</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group</p>
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * To deselect all {@link OperationalLimitsGroup}, use {@link #cancelSelectedOperationalLimitsGroup1()}
     * @param side the side on which to deselect some selected groups
     * @param ids the ID of the groups to remove from the selected
     */
    void deselectOperationalLimitsGroups(TwoSides side, String... ids);

    /**
     * <p>Create a new {@link OperationalLimitsGroup} on side 1 with the given ID.</p>
     * <p>If a group of the given ID already exists, it is replaced silently.</p>
     * @return the newly created group {@link OperationalLimitsGroup}.
     */
    OperationalLimitsGroup newOperationalLimitsGroup1(String id);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given ID as the selected one on side 1.</p>
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
     * Helper function to return all the limits of this branch's <code>side</code>, of a given type using the provided function to get it from each {@link OperationalLimitsGroup}
     * @param operationalLimitToLoadingLimitFunction the function that will return an optional {@link LoadingLimits} from an {@link OperationalLimitsGroup}
     * @return a collection of loadingLimits, all the same type
     * @param <T> the type of loadingLimit
     */
    private <T extends LoadingLimits> Collection<T> getAllSelectedLoadingLimits(Function<OperationalLimitsGroup, Optional<T>> operationalLimitToLoadingLimitFunction, TwoSides side) {
        return getAllSelectedOperationalLimitsGroups(side)
                .stream()
                .map(operationalLimitToLoadingLimitFunction)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * <p>Create an adder to add a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup} on side 1.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup} on side 1.
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    CurrentLimitsAdder newCurrentLimits1();

    /**
     * <p>Create an adder to add a copy of the {@link CurrentLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 1. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param currentLimits a set of existing current limits.
     * @return an adder allowing to create a new {@link CurrentLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 1.
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits(CurrentLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
    default CurrentLimitsAdder newCurrentLimits1(CurrentLimits currentLimits) {
        CurrentLimitsAdder currentLimitsAdder = newCurrentLimits1();
        return initializeFromLoadingLimits(currentLimitsAdder, currentLimits);
    }

    /**
     * <p>Get the {@link OperationalLimitsGroup} corresponding to the default ID or create a new one if it does not exist.
     * Set the {@link OperationalLimitsGroup} as the selected one on side 1.</p>
     * @return the selected {@link OperationalLimitsGroup} on side 1.
     */
    OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup1();

    /**
     * <p>Get the {@link OperationalLimitsGroup} corresponding to the default ID or create a new one if it does not exist.
     * Set the {@link OperationalLimitsGroup} as the selected one on side 2.</p>
     * @return the selected {@link OperationalLimitsGroup} on side 2.
     */
    OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup2();

    /**
     * <p>Get the {@link OperationalLimitsGroup} corresponding to the given ID or create a new one if it does not exist.
     * Set the {@link OperationalLimitsGroup} as the selected one on side 1.</p>
     * @param limitsGroupId an ID of {@link OperationalLimitsGroup}
     * @return the selected {@link OperationalLimitsGroup} on side 1.
     */
    default OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup1(String limitsGroupId) {
        OperationalLimitsGroup operationalLimitsGroup = getOperationalLimitsGroup1(limitsGroupId).orElseGet(() -> newOperationalLimitsGroup1(limitsGroupId));
        setSelectedOperationalLimitsGroup1(limitsGroupId);
        return operationalLimitsGroup;
    }

    /**
     * <p>Get the {@link OperationalLimitsGroup} corresponding to the given ID or create a new one if it does not exist.
     * Set the {@link OperationalLimitsGroup} as the selected one on side 2.</p>
     * @param limitsGroupId an ID of {@link OperationalLimitsGroup}
     * @return the selected {@link OperationalLimitsGroup} on side 2.
     */
    default OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup2(String limitsGroupId) {
        OperationalLimitsGroup operationalLimitsGroup = getOperationalLimitsGroup2(limitsGroupId).orElseGet(() -> newOperationalLimitsGroup2(limitsGroupId));
        setSelectedOperationalLimitsGroup2(limitsGroupId);
        return operationalLimitsGroup;
    }

    /**
     * <p>Create an adder to add a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup} on side 1.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup} on side 1.
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    ActivePowerLimitsAdder newActivePowerLimits1();

    /**
     * <p>Create an adder to add a copy of the {@link ActivePowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 1. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param activePowerLimits a set of existing active power limits.
     * @return an adder allowing to create a new {@link ActivePowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 1.
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits(ActivePowerLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
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
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    ApparentPowerLimitsAdder newApparentPowerLimits1();

    /**
     * <p>Create an adder to add a copy of the {@link ApparentPowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 1. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param apparentPowerLimits a set of existing apparent power limits.
     * @return an adder allowing to create a new {@link ApparentPowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 1.
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits(ApparentPowerLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
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
     CurrentLimits currentLimits
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    CurrentLimitsAdder newCurrentLimits2();

    /**
     * <p>Create an adder to add a copy of the {@link CurrentLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 2. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param currentLimits a set of existing current limits.
     * @return an adder allowing to create a new {@link CurrentLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 2.
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits(CurrentLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
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
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    ActivePowerLimitsAdder newActivePowerLimits2();

    /**
     * <p>Create an adder to add a copy of the {@link ActivePowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 2. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param activePowerLimits a set of existing active power limits.
     * @return an adder allowing to create a new {@link ActivePowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 2.
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits(ActivePowerLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
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
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    ApparentPowerLimitsAdder newApparentPowerLimits2();

    /**
     * <p>Create an adder to add a copy of the {@link ApparentPowerLimits} in parameters in the selected {@link OperationalLimitsGroup} on side 2. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param apparentPowerLimits a set of existing apparent power limits.
     * @return an adder allowing to create a new {@link ApparentPowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup} on side 2.
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits(ApparentPowerLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
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

    /**
     * Get all the limits of type {@link LimitType#CURRENT} on the <code>side</code> of this branch,
     * for the {@link OperationalLimitsGroup} that are selected
     * @param side the side on which to get the limits
     * @return a collection of all the current limits of this branch on the given <code>side</code>,
     * one for each {@link OperationalLimitsGroup} that is selected
     */
    default Collection<CurrentLimits> getAllSelectedCurrentLimits(TwoSides side) {
        return getAllSelectedLoadingLimits(OperationalLimitsGroup::getCurrentLimits, side);
    }

    default Optional<ActivePowerLimits> getActivePowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getActivePowerLimits1();
            case TWO -> getActivePowerLimits2();
        };
    }

    /**
     * Get all the limits of type {@link LimitType#ACTIVE_POWER} on the <code>side</code> of this branch,
     * for the {@link OperationalLimitsGroup} that are selected
     * @param side the side on which to get the limits
     * @return a collection of all the active power limits of this branch on the given <code>side</code>,
     * one for each {@link OperationalLimitsGroup} that is selected
     */
    default Collection<ActivePowerLimits> getAllSelectedActivePowerLimits(TwoSides side) {
        return getAllSelectedLoadingLimits(OperationalLimitsGroup::getActivePowerLimits, side);
    }

    default Optional<ApparentPowerLimits> getApparentPowerLimits(TwoSides side) {
        return switch (side) {
            case ONE -> getApparentPowerLimits1();
            case TWO -> getApparentPowerLimits2();
        };
    }

    /**
     * Get all the limits of type {@link LimitType#APPARENT_POWER} on the <code>side</code> of this branch,
     * for the {@link OperationalLimitsGroup} that are selected
     * @param side the side on which to get the limits
     * @return a collection of all the apparent power limits of this branch on the given <code>side</code>,
     * one for each {@link OperationalLimitsGroup} that is selected
     */
    default Collection<ApparentPowerLimits> getAllSelectedApparentPowerLimits(TwoSides side) {
        return getAllSelectedLoadingLimits(OperationalLimitsGroup::getApparentPowerLimits, side);
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

    /**
     * Get all the limits of the given <code>type</code>, on the <code>side</code> of this branch,
     * for the {@link OperationalLimitsGroup} that are selected
     * @param type the type of the limit, refer to {@link LimitType}
     * @param side the side on which to get the limits
     * @return a collection of all the <code>type</code> limits of this branch on the <code>side</code>,
     * one for each {@link OperationalLimitsGroup} that is selected. Might be empty if none is selected.
     */
    default Collection<? extends LoadingLimits> getAllSelectedLimits(LimitType type, TwoSides side) {
        return switch (type) {
            case CURRENT -> getAllSelectedCurrentLimits(side);
            case ACTIVE_POWER -> getAllSelectedActivePowerLimits(side);
            case APPARENT_POWER -> getAllSelectedApparentPowerLimits(side);
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

    /**
     * For the last selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()},
     * return an overload for the <code>side</code> of the branch, of the <code>type</code>, taking into account a reduction of the limits
     * by a factor of <code>limitReductionValue</code>.
     * @param side the side of the branch to look at
     * @param limitReductionValue a reduction coefficient of the limit (between 0 and 1)
     * @param type the type of the limit
     * @return an {@link Overload} if there is an overload on temporary limits, otherwise a null
     */
    Overload checkTemporaryLimits(TwoSides side, double limitReductionValue, LimitType type);

    /**
     * For the last selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()},
     * return an overload for the <code>side</code> of the branch, of the <code>type</code>. This does not reduce the limits.
     * @param side the side of the branch to look at
     * @param type the type of the limit
     * @return an {@link Overload} if there is an overload on temporary limits, otherwise a null
     */
    Overload checkTemporaryLimits(TwoSides side, LimitType type);

    /**
     * For the last selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()},
     * return an overload for the side 1 of the branch, of the <code>type</code>, taking into account a reduction of the limits
     * by a factor of <code>limitReductionValue</code>.
     * @param limitReductionValue a reduction coefficient of the limit (between 0 and 1)
     * @param type the type of the limit
     * @return an {@link Overload} if there is an overload on temporary limits, otherwise a null
     */
    Overload checkTemporaryLimits1(double limitReductionValue, LimitType type);

    /**
     * For the last selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()},
     * return an overload for the side 1 of the branch, of the <code>type</code>. This does not reduce the limits.
     * @param type the type of the limit
     * @return an {@link Overload} if there is an overload on temporary limits, otherwise a null
     */
    Overload checkTemporaryLimits1(LimitType type);

    /**
     * For the last selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()},
     * return an overload for the side 2 of the branch, of the <code>type</code>, taking into account a reduction of the limits
     * by a factor of <code>limitReductionValue</code>.
     * @param limitReductionValue a reduction coefficient of the limit (between 0 and 1)
     * @param type the type of the limit
     * @return an {@link Overload} if there is an overload on temporary limits, otherwise a null
     */
    Overload checkTemporaryLimits2(double limitReductionValue, LimitType type);

    /**
     * For the last selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getSelectedOperationalLimitsGroup()},
     * return an overload for the side 2 of the branch, of the <code>type</code>. This does not reduce the limits.
     * @param type the type of the limit
     * @return an {@link Overload} if there is an overload on temporary limits, otherwise a null
     */
    Overload checkTemporaryLimits2(LimitType type);

    /**
     * For all the selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getAllSelectedOperationalLimitsGroups()},
     * return an overload for the <code>side</code> of the branch, of the <code>type</code>, taking into account a reduction of the limits
     * by a factor of <code>limitReductionValue</code>.
     * @param side the side of the branch to look at
     * @param limitReductionValue a reduction coefficient of the limit (between 0 and 1)
     * @param type the type of the limit
     */
    Collection<Overload> checkAllTemporaryLimits(TwoSides side, double limitReductionValue, LimitType type);

    /**
     * For all the selected {@link OperationalLimitsGroup} as defined by {@link FlowsLimitsHolder#getAllSelectedOperationalLimitsGroups()},
     * return an overload for the <code>side</code> of the branch, of the <code>type</code>. This does not reduce the limits.
     * @param side the side of the branch to look at
     * @param type the type of the limit
     */
    Collection<Overload> checkAllTemporaryLimits(TwoSides side, LimitType type);
}

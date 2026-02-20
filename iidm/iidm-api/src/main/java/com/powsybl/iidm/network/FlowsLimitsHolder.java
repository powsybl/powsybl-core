/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.powsybl.iidm.network.util.LoadingLimitsUtil.initializeFromLoadingLimits;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface FlowsLimitsHolder {

    /**
     * Get the collection of the defined {@link OperationalLimitsGroup}.
     * @return the {@link OperationalLimitsGroup} s.
     */
    Collection<OperationalLimitsGroup> getOperationalLimitsGroups();

    /**
     * <p>Get the ID of the last {@link OperationalLimitsGroup} set as selected (either by {@link #setSelectedOperationalLimitsGroup(String)} or any other mean).</p>
     * <p>If the last selected was deselected (using {@link #deselectOperationalLimitsGroups(String...)} (String)}),
     * then this will return the ID of the OperationalLimitsGroup selected before that if any (this logic can be repeated, if the two previously selected are not selected anymore, gets the 3rd, the 4th, etc...),
     * otherwise an empty {@link Optional}</p>
     * @return the ID of the last selected {@link OperationalLimitsGroup} from all the selected groups if any,
     * the one selected before that if the last selected is not selected anymore (repeatable),
     * an empty {@link Optional} otherwise.
     */
    Optional<String> getSelectedOperationalLimitsGroupId();

    /**
     * Get the IDs of all the selected {@link OperationalLimitsGroup}
     * @return a collection containing one ID per selected {@link OperationalLimitsGroup} (might be empty if there is none selected)
     */
    Collection<String> getAllSelectedOperationalLimitsGroupIds();

    /**
     * Get the {@link OperationalLimitsGroup} corresponding to an ID.
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * @return the {@link OperationalLimitsGroup} of the given ID if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id);

    /**
     * Get the {@link OperationalLimitsGroup} that was last selected (either by {@link #setSelectedOperationalLimitsGroup(String)} or any other mean)
     * If the last selected is not selected anymore, it will return the one selected before that (or the 3rd, 4th... if those are not selected anymore either)
     * @return the first selected {@link OperationalLimitsGroup} from all the selected if any,
     * the one selected before that if it is not selected anymore (repeatable),
     * an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup();

    /**
     * Get all the selected {@link OperationalLimitsGroup}
     * @return a collection containing all selected {@link OperationalLimitsGroup} (might be empty if there is none selected)
     */
    Collection<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups();

    /**
     * <p>Create a new {@link OperationalLimitsGroup} with the given ID.</p>
     * <p>If a group of the given ID already exists, it is replaced silently.</p>
     * @return the newly created group {@link OperationalLimitsGroup}.
     */
    OperationalLimitsGroup newOperationalLimitsGroup(String id);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given ID as the only selected one. If other groups were also selected, they are all deselected</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup()}
     * To have multiple groups selected instead of a single one, use {@link #addSelectedOperationalLimitsGroups(String...)}
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void setSelectedOperationalLimitsGroup(String id);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given IDs as selected. If other groups were also selected, they are still selected</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if any ID is <code>null</code>.</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup()}
     * To have a single group selected and deselect all other groups, use {@link #setSelectedOperationalLimitsGroup(String)}
     * @param ids the IDs of one or more {@link OperationalLimitsGroup}
     */
    void addSelectedOperationalLimitsGroups(String... ids);

    /**
     * <p>Set all the existing {@link OperationalLimitsGroup} whose id match the <code>predicate</code> as selected</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup()}
     * To have a single group selected and deselect all other groups, use {@link #setSelectedOperationalLimitsGroup(String)}
     * @param operationalLimitsGroupIdPredicate a predicate dictating which groups must be activated.
     *                                          All groups whose ID would return true given the predicate will be activated
     *                                          All groups whose ID would return false will stay in the same state as before,
     *                                          groups that do not match will not be unselected.
     */
    default void addSelectedOperationalLimitsGroupByPredicate(Predicate<String> operationalLimitsGroupIdPredicate) {
        addSelectedOperationalLimitsGroups(
            getOperationalLimitsGroups()
                .stream()
                .map(OperationalLimitsGroup::getId)
                .filter(operationalLimitsGroupIdPredicate)
                .toArray(String[]::new)
        );
    }

    /**
     * <p>Remove the {@link OperationalLimitsGroup} corresponding to the given ID.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void removeOperationalLimitsGroup(String id);

    /**
     * <p>Deselect all the selected {@link OperationalLimitsGroup}.</p>
     * <p>After calling this method, no {@link OperationalLimitsGroup} is selected.</p>
     * To deselect a specific {@link OperationalLimitsGroup}, use {@link #deselectOperationalLimitsGroups(String...)}
     */
    void cancelSelectedOperationalLimitsGroup();

    /**
     * <p>Deselect the {@link OperationalLimitsGroup} corresponding to all the <code>ids</code>.</p>
     * <p>If the {@link OperationalLimitsGroup} exists but is not selected, this method will do nothing</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group</p>
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * To deselect all {@link OperationalLimitsGroup}, use {@link #cancelSelectedOperationalLimitsGroup()}
     * @param ids the ID of the groups to remove from the selected
     */
    void deselectOperationalLimitsGroups(String... ids);

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default Optional<CurrentLimits> getCurrentLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the {@link OperationalLimitsGroup} corresponding to <code>id</code>
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * @return a {@link Optional} containing the {@link CurrentLimits} of the {@link OperationalLimitsGroup} corresponding to the id if it exists,
     * an empty {@link Optional} otherwise,
     */
    default Optional<CurrentLimits> getCurrentLimitsFromId(String id) {
        return getOperationalLimitsGroup(id).flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    /**
     * Get all the {@link CurrentLimits} of all the selected {@link OperationalLimitsGroup}
     * @return a collection of {@link CurrentLimits}, one per {@link OperationalLimitsGroup} that is selected, might be empty if none is selected
     */
    default Collection<CurrentLimits> getAllSelectedCurrentLimits() {
        return getAllSelectedLoadingLimits(OperationalLimitsGroup::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default CurrentLimits getNullableCurrentLimits() {
        return getCurrentLimits().orElse(null);
    }

    /**
     * <p>Get the {@link OperationalLimitsGroup} corresponding to the default ID or create a new one if it does not exist.
     * Set the {@link OperationalLimitsGroup} as a selected one.</p>
     * @return the selected {@link OperationalLimitsGroup}.
     */
    OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup();

    /**
     * <p>Get the {@link OperationalLimitsGroup} corresponding to the given ID or create a new one if it does not exist.
     * Set the {@link OperationalLimitsGroup} as a selected one .</p>
     * @param limitsGroupId an ID of {@link OperationalLimitsGroup}
     * @return the selected {@link OperationalLimitsGroup}.
     */
    default OperationalLimitsGroup getOrCreateSelectedOperationalLimitsGroup(String limitsGroupId) {
        OperationalLimitsGroup operationalLimitsGroup = getOperationalLimitsGroup(limitsGroupId).orElseGet(() -> newOperationalLimitsGroup(limitsGroupId));
        setSelectedOperationalLimitsGroup(limitsGroupId);
        return operationalLimitsGroup;
    }

    /**
     * Get the {@link ActivePowerLimits} of the last selected {@link OperationalLimitsGroup}.
     * @return {@link ActivePowerLimits} of the last selected {@link OperationalLimitsGroup} if any, an empty {@link Optional} otherwise.
     */
    default Optional<ActivePowerLimits> getActivePowerLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    /**
     * Get the {@link ActivePowerLimits} of the {@link OperationalLimitsGroup} corresponding to <code>id</code>
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * @return a {@link Optional} containing the {@link ActivePowerLimits} of the {@link OperationalLimitsGroup} corresponding to the id if it exists,
     * an empty {@link Optional} otherwise,
     */
    default Optional<ActivePowerLimits> getActivePowerLimitsFromId(String id) {
        return getOperationalLimitsGroup(id).flatMap(OperationalLimitsGroup::getActivePowerLimits);
    }

    /**
     * Get all the {@link ActivePowerLimits} of all the selected {@link OperationalLimitsGroup}
     * @return a collection of {@link ActivePowerLimits}, one per {@link OperationalLimitsGroup} that is selected, might be empty if none is selected
     */
    default Collection<ActivePowerLimits> getAllSelectedActivePowerLimits() {
        return getAllSelectedLoadingLimits(OperationalLimitsGroup::getActivePowerLimits);
    }

    /**
     * Get the {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default ActivePowerLimits getNullableActivePowerLimits() {
        return getActivePowerLimits().orElse(null);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} if any, an empty {@link Optional} otherwise.
     */
    default Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the {@link OperationalLimitsGroup} corresponding to <code>id</code>
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * @return a {@link Optional} containing the {@link ApparentPowerLimits} of the {@link OperationalLimitsGroup} corresponding to the id if it exists,
     * an empty {@link Optional} otherwise,
     */
    default Optional<ApparentPowerLimits> getApparentPowerLimitsFromId(String id) {
        return getOperationalLimitsGroup(id).flatMap(OperationalLimitsGroup::getApparentPowerLimits);
    }

    /**
     * Get all the {@link ApparentPowerLimits} of all the selected {@link OperationalLimitsGroup}
     * @return a collection of {@link ApparentPowerLimits}, one per {@link OperationalLimitsGroup} that is selected, might be empty if none is selected
     */
    default Collection<ApparentPowerLimits> getAllSelectedApparentPowerLimits() {
        return getAllSelectedLoadingLimits(OperationalLimitsGroup::getApparentPowerLimits);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link ApparentPowerLimits} of the selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default ApparentPowerLimits getNullableApparentPowerLimits() {
        return getApparentPowerLimits().orElse(null);
    }

    /**
     * <p>Create an adder to add a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup}.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link CurrentLimits} in the selected {@link OperationalLimitsGroup}.
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits(CurrentLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
    CurrentLimitsAdder newCurrentLimits();

    /**
     * <p>Create an adder to add a copy of the {@link CurrentLimits} in parameters in the selected {@link OperationalLimitsGroup}. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param limits a set of existing current limits.
     * @return an adder allowing to create a new {@link CurrentLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup}.
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits(CurrentLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
    default CurrentLimitsAdder newCurrentLimits(CurrentLimits limits) {
        CurrentLimitsAdder adder = newCurrentLimits();
        return initializeFromLoadingLimits(adder, limits);
    }

    /**
     * <p>Create an adder to add a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup}.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup}.
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    ApparentPowerLimitsAdder newApparentPowerLimits();

    /**
     * <p>Create an adder to add a copy of the {@link ApparentPowerLimits} in parameters in the selected {@link OperationalLimitsGroup}. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ApparentPowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param limits a set of existing apparent power limits.
     * @return an adder allowing to create a new {@link ApparentPowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup}.
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits(ApparentPowerLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
    default ApparentPowerLimitsAdder newApparentPowerLimits(ApparentPowerLimits limits) {
        ApparentPowerLimitsAdder adder = newApparentPowerLimits();
        return initializeFromLoadingLimits(adder, limits);
    }

    /**
     * <p>Create an adder to add a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup}.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup}.
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    ActivePowerLimitsAdder newActivePowerLimits();

    /**
     * <p>Create an adder to add a copy of the {@link ActivePowerLimits} in parameters in the selected {@link OperationalLimitsGroup}. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link ActivePowerLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param limits a set of existing active power limits.
     * @return an adder allowing to create a new {@link ActivePowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup}.
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits(ActivePowerLimits)} instead.
     */
    @Deprecated(since = "6.8.0")
    default ActivePowerLimitsAdder newActivePowerLimits(ActivePowerLimits limits) {
        ActivePowerLimitsAdder adder = newActivePowerLimits();
        return initializeFromLoadingLimits(adder, limits);
    }

    /**
     * Helper function to return an operational limit of a given type using the provided function
     * @param operationalLimitToLoadingLimitFunction the function that will return an optional {@link LoadingLimits} from an {@link OperationalLimitsGroup}
     * @return a collection of loadingLimits, all the same type
     * @param <T> the type of loadingLimit
     */
    private <T extends LoadingLimits> Collection<T> getAllSelectedLoadingLimits(Function<OperationalLimitsGroup, Optional<T>> operationalLimitToLoadingLimitFunction) {
        return getAllSelectedOperationalLimitsGroups()
                .stream()
                .map(operationalLimitToLoadingLimitFunction)
                .flatMap(Optional::stream)
                .toList();
    }

}

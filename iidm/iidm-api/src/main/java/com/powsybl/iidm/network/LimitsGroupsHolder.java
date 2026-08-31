/**
 * Copyright (c) 2026, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Dissoubray Nathan {@literal <nathan.dissoubray at rte-france.com>}
 */
public interface LimitsGroupsHolder<G extends CurrentOperationalLimitsGroup> {

    /**
     * Get the collection of the defined {@link OperationalLimitsGroup}.
     * @return the {@link OperationalLimitsGroup} s.
     */
    Collection<G> getOperationalLimitsGroups();

    /**
     * <p>Get the ID of the last {@link CurrentOperationalLimitsGroup} set as selected (either by {@link #setSelectedOperationalLimitsGroup(String)} or any other mean).</p>
     * <p>If the last selected was deselected (using {@link #deselectOperationalLimitsGroups(String...)} (String)}),
     * then this will return the ID of the OperationalLimitsGroup selected before that if any (this logic can be repeated,
     * if the two previously selected are not selected anymore, gets the 3rd, the 4th, etc...),
     * otherwise an empty {@link Optional}</p>
     * @return the ID of the last selected {@link CurrentOperationalLimitsGroup} from all the selected groups if any,
     * the one selected before that if the last selected is not selected anymore (repeatable),
     * an empty {@link Optional} otherwise.
     */
    Optional<String> getSelectedOperationalLimitsGroupId();

    /**
     * Get the IDs of all the selected {@link CurrentOperationalLimitsGroup}
     * @return a collection containing one ID per selected {@link CurrentOperationalLimitsGroup} (might be empty if there is none selected)
     */
    Collection<String> getAllSelectedOperationalLimitsGroupIds();

    /**
     * Get the IDs of all the selected {@link CurrentOperationalLimitsGroup}, in the order in which they were selected.<br>
     * If an element that was previously selected is selected again, it will be considered as if it was just selected with the last selection.
     * Meaning if two groups A and B are selected as such: select A, select B, select A, the order will be B, A.
     *
     * @return an ordered collection of the IDs of all the selected {@link CurrentOperationalLimitsGroup}, the ordering relation being the order of the selection, from
     * the oldest selected group to the most recently selected group.
     */
    List<String> getAllSelectedOperationalLimitsGroupIdsOrdered();

    /**
     * Get the {@link CurrentOperationalLimitsGroup} corresponding to an ID.
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * @return the {@link CurrentOperationalLimitsGroup} of the given ID if any, an empty {@link Optional} otherwise.
     */
    Optional<G> getOperationalLimitsGroup(String id);

    /**
     * Get the {@link CurrentOperationalLimitsGroup} that was last selected (either by {@link #setSelectedOperationalLimitsGroup(String)} or any other mean)
     * If the last selected is not selected anymore, it will return the one selected before that (or the 3rd, 4th... if those are not selected anymore either)
     * @return the first selected {@link CurrentOperationalLimitsGroup} from all the selected if any,
     * the one selected before that if it is not selected anymore (repeatable),
     * an empty {@link Optional} otherwise.
     */
    Optional<G> getSelectedOperationalLimitsGroup();

    /**
     * Get all the selected {@link CurrentOperationalLimitsGroup}. The list's order must be stable when elements are added or deleted.
     * @return a list containing all selected {@link CurrentOperationalLimitsGroup} (might be empty if there is none selected)
     */
    List<G> getAllSelectedOperationalLimitsGroups();

    /**
     * <p>Create a new {@link CurrentOperationalLimitsGroup} with the given ID.</p>
     * <p>If a group of the given ID already exists, it is replaced silently.</p>
     * @return the newly created group {@link CurrentOperationalLimitsGroup}.
     */
    G newOperationalLimitsGroup(String id);

    /**
     * <p>Set the {@link CurrentOperationalLimitsGroup} corresponding to the given ID as the only selected one. If other groups were also selected, they are all deselected</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup()}
     * To have multiple groups selected instead of a single one, use {@link #addSelectedOperationalLimitsGroups(String...)}
     * @param id an ID of {@link CurrentOperationalLimitsGroup}
     */
    void setSelectedOperationalLimitsGroup(String id);

    /**
     * <p>Set the {@link CurrentOperationalLimitsGroup} corresponding to the given IDs as selected. If other groups were also selected, they are still selected</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if any of the IDs don't correspond to an existing group.</p>
     * <p>Throw an {@link NullPointerException} if any ID is <code>null</code>.</p>
     * <p>Note that in the case of an error, this function will not stop at the first error but try on all groups</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup()}
     * To have a single group selected and deselect all other groups, use {@link #setSelectedOperationalLimitsGroup(String)}
     * @param ids the IDs of one or more {@link CurrentOperationalLimitsGroup}
     */
    void addSelectedOperationalLimitsGroups(String... ids);

    /**
     * <p>Set all the existing {@link CurrentOperationalLimitsGroup} whose id match the <code>predicate</code> as selected</p>
     * To deselect a selected group, use {@link #deselectOperationalLimitsGroups(String...)}.
     * To deselect all the selected groups, use {@link #cancelSelectedOperationalLimitsGroup()}
     * To have a single group selected and deselect all other groups, use {@link #setSelectedOperationalLimitsGroup(String)}
     * @param operationalLimitsGroupIdPredicate a predicate dictating which groups must be activated.<br>
     *                                          All groups whose ID would return true given the predicate will be activated<br>
     *                                          All groups whose ID would return false will stay in the same state as before.
     */
    default void addSelectedOperationalLimitsGroupByPredicate(Predicate<String> operationalLimitsGroupIdPredicate) {
        addSelectedOperationalLimitsGroups(
            getOperationalLimitsGroups()
                .stream()
                .map(G::getId)
                .filter(operationalLimitsGroupIdPredicate)
                .toArray(String[]::new)
        );
    }

    /**
     * <p>Remove the {@link CurrentOperationalLimitsGroup} corresponding to the given ID.
     * This does not fail if the group corresponding to this ID doesn't exist</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * @param id an ID of {@link CurrentOperationalLimitsGroup}
     */
    void removeOperationalLimitsGroup(String id);

    /**
     * <p>Deselect all the selected {@link CurrentOperationalLimitsGroup}.</p>
     * <p>After calling this method, no {@link CurrentOperationalLimitsGroup} is selected.</p>
     * To deselect a specific {@link CurrentOperationalLimitsGroup}, use {@link #deselectOperationalLimitsGroups(String...)}
     */
    void cancelSelectedOperationalLimitsGroup();

    /**
     * <p>Deselect the {@link CurrentOperationalLimitsGroup} corresponding to all the <code>ids</code>.</p>
     * <p>For any of the ID, this method will do nothing in the following cases:
     * <ul>
     *     <li>The {@link CurrentOperationalLimitsGroup} corresponding to the ID exists but is not selected</li>
     *     <li>The ID does not correspond to any existing group</li>
     *     <li>The ID is null</li>
     * </ul>
     * </p>
     * To deselect all {@link CurrentOperationalLimitsGroup}, use {@link #cancelSelectedOperationalLimitsGroup()}
     * @param ids the IDs of the groups to remove from the selected
     */
    void deselectOperationalLimitsGroups(String... ids);

    /**
     * Get the {@link CurrentLimits} of the last selected {@link CurrentOperationalLimitsGroup}.
     * @return {@link CurrentLimits} of the last selected {@link CurrentOperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default Optional<CurrentLimits> getCurrentLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(G::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the {@link CurrentOperationalLimitsGroup} corresponding to <code>id</code>
     * <p>Throw a {@link NullPointerException} if the ID is <code>null</code>.</p>
     * @return a {@link Optional} containing the {@link CurrentLimits} of the {@link CurrentOperationalLimitsGroup} corresponding to the id if it exists,
     * an empty {@link Optional} otherwise,
     */
    default Optional<CurrentLimits> getCurrentLimitsFromId(String id) {
        return getOperationalLimitsGroup(id).flatMap(G::getCurrentLimits);
    }

    /**
     * Get all the {@link CurrentLimits} of all the selected {@link CurrentOperationalLimitsGroup}
     * @return a collection of {@link CurrentLimits}, one per {@link CurrentOperationalLimitsGroup} that is selected, might be empty if none is selected
     */
    default Collection<CurrentLimits> getAllSelectedCurrentLimits() {
        return getAllSelectedLoadingLimits(G::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the last selected {@link CurrentOperationalLimitsGroup}.
     * @return {@link CurrentLimits} of the last selected {@link CurrentOperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default CurrentLimits getNullableCurrentLimits() {
        return getCurrentLimits().orElse(null);
    }

    /**
     * <p>Get the {@link CurrentOperationalLimitsGroup} corresponding to the default ID or create a new one if it does not exist.
     * Set the {@link CurrentOperationalLimitsGroup} as the only selected one.</p>
     * @return the selected {@link CurrentOperationalLimitsGroup}.
     */
    G getOrCreateSelectedOperationalLimitsGroup();

    /**
     * <p>Get the {@link CurrentOperationalLimitsGroup} corresponding to the given ID or create a new one if it does not exist.
     * Set the {@link CurrentOperationalLimitsGroup} as the only selected one .</p>
     * @param limitsGroupId an ID of {@link CurrentOperationalLimitsGroup}
     * @return the selected {@link CurrentOperationalLimitsGroup}.
     */
    default G getOrCreateSelectedOperationalLimitsGroup(String limitsGroupId) {
        G operationalLimitsGroup = getOperationalLimitsGroup(limitsGroupId).orElseGet(() -> newOperationalLimitsGroup(limitsGroupId));
        setSelectedOperationalLimitsGroup(limitsGroupId);
        return operationalLimitsGroup;
    }

    /**
     * Helper function to return an operational limit of a given type using the provided function
     * @param operationalLimitToLoadingLimitFunction the function that will return an optional {@link LoadingLimits} from an {@link CurrentOperationalLimitsGroup}
     * @return a collection of loadingLimits, all the same type
     * @param <T> the type of loadingLimit
     */
    default <T extends LoadingLimits> Collection<T> getAllSelectedLoadingLimits(Function<G, Optional<T>> operationalLimitToLoadingLimitFunction) {
        return getAllSelectedOperationalLimitsGroups()
            .stream()
            .map(operationalLimitToLoadingLimitFunction)
            .flatMap(Optional::stream)
            .toList();
    }
}

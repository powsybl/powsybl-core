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

import static com.powsybl.iidm.network.util.LoadingLimitsUtil.getAllSelectedLoadingLimits;
import static com.powsybl.iidm.network.util.LoadingLimitsUtil.initializeFromLoadingLimits;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public interface FlowsLimitsHolder extends LimitsGroupsHolder<OperationalLimitsGroup> {

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
        return getAllSelectedLoadingLimits(this, OperationalLimitsGroup::getActivePowerLimits);
    }

    /**
     * Get the {@link ActivePowerLimits} of the last selected {@link OperationalLimitsGroup}.
     * @return {@link ActivePowerLimits} of the last selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default ActivePowerLimits getNullableActivePowerLimits() {
        return getActivePowerLimits().orElse(null);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the last selected {@link OperationalLimitsGroup}.
     * @return {@link ApparentPowerLimits} of the last selected {@link OperationalLimitsGroup} if any, an empty {@link Optional} otherwise.
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
        return getAllSelectedLoadingLimits(this, OperationalLimitsGroup::getApparentPowerLimits);
    }

    /**
     * Get the {@link ApparentPowerLimits} of the last selected {@link OperationalLimitsGroup}.
     * @return {@link ApparentPowerLimits} of the last selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
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

}

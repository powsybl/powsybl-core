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
     * Get the ID of the selected {@link OperationalLimitsGroup}.
     * @return the ID of the selected {@link OperationalLimitsGroup} if any, an empty {@link Optional} otherwise.
     */
    Optional<String> getSelectedOperationalLimitsGroupId();

    /**
     * Get the {@link OperationalLimitsGroup} corresponding to an ID.
     * @return the {@link OperationalLimitsGroup} of the given ID if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id);

    /**
     * Get the selected {@link OperationalLimitsGroup}.
     * @return the selected {@link OperationalLimitsGroup} if any, an empty {@link Optional} otherwise.
     */
    Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup();

    /**
     * <p>Create a new {@link OperationalLimitsGroup} with the given ID.</p>
     * <p>If a group of the given ID already exists, it is replaced silently.</p>
     * @return the newly created group {@link OperationalLimitsGroup}.
     */
    OperationalLimitsGroup newOperationalLimitsGroup(String id);

    /**
     * <p>Set the {@link OperationalLimitsGroup} corresponding to the given ID as as the selected one.</p>
     * <p>Throw a {@link com.powsybl.commons.PowsyblException} if the ID doesn't correspond to any existing group.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * To reset the selected group, use {@link #cancelSelectedOperationalLimitsGroup}.</p>
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void setSelectedOperationalLimitsGroup(String id);

    /**
     * <p>Remove the {@link OperationalLimitsGroup} corresponding to the given ID.</p>
     * <p>Throw an {@link NullPointerException} if the ID is <code>null</code>.
     * @param id an ID of {@link OperationalLimitsGroup}
     */
    void removeOperationalLimitsGroup(String id);

    /**
     * <p>Cancel the selected {@link OperationalLimitsGroup}.</p>
     * <p>After calling this method, no {@link OperationalLimitsGroup} is selected.</p>
     */
    void cancelSelectedOperationalLimitsGroup();

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default Optional<CurrentLimits> getCurrentLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getCurrentLimits);
    }

    /**
     * Get the {@link CurrentLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link CurrentLimits} of the selected {@link OperationalLimitsGroup} if any, <code>null</code> otherwise.
     */
    default CurrentLimits getNullableCurrentLimits() {
        return getCurrentLimits().orElse(null);
    }

    /**
     * Get the {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup}.
     * @return {@link ActivePowerLimits} of the selected {@link OperationalLimitsGroup} if any, an empty {@link Optional} otherwise.
     */
    default Optional<ActivePowerLimits> getActivePowerLimits() {
        return getSelectedOperationalLimitsGroup().flatMap(OperationalLimitsGroup::getActivePowerLimits);
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
     */
    CurrentLimitsAdder newCurrentLimits();

    /**
     * <p>Create an adder to add a copy of the {@link CurrentLimits} in parameters in the selected {@link OperationalLimitsGroup}. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param limits a set of existing current limits.
     * @return an adder allowing to create a new {@link CurrentLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup}.
     */
    default CurrentLimitsAdder newCurrentLimits(CurrentLimits limits) {
        CurrentLimitsAdder adder = newCurrentLimits();
        return initializeFromLoadingLimits(adder, limits);
    }

    /**
     * <p>Create an adder to add a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup}.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ApparentPowerLimits} in the selected {@link OperationalLimitsGroup}.
     */
    ApparentPowerLimitsAdder newApparentPowerLimits();

    /**
     * <p>Create an adder to add a copy of the {@link ApparentPowerLimits} in parameters in the selected {@link OperationalLimitsGroup}. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param limits a set of existing apparent power limits.
     * @return an adder allowing to create a new {@link ApparentPowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup}.
     */
    default ApparentPowerLimitsAdder newApparentPowerLimits(ApparentPowerLimits limits) {
        ApparentPowerLimitsAdder adder = newApparentPowerLimits();
        return initializeFromLoadingLimits(adder, limits);
    }

    /**
     * <p>Create an adder to add a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup}.</p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @return an adder allowing to create a new {@link ActivePowerLimits} in the selected {@link OperationalLimitsGroup}.
     */
    ActivePowerLimitsAdder newActivePowerLimits();

    /**
     * <p>Create an adder to add a copy of the {@link ActivePowerLimits} in parameters in the selected {@link OperationalLimitsGroup}. </p>
     * <p>If there's no selected group, the adder will also create a new group with the default name and set it as selected.
     * This operation is performed when the limits are created via {@link CurrentLimitsAdder#add()}, only if the limits to add
     * are valid.</p>
     * @param limits a set of existing active power limits.
     * @return an adder allowing to create a new {@link ActivePowerLimits} initialized from the limits in parameters in the selected {@link OperationalLimitsGroup}.
     */
    default ActivePowerLimitsAdder newActivePowerLimits(ActivePowerLimits limits) {
        ActivePowerLimitsAdder adder = newActivePowerLimits();
        return initializeFromLoadingLimits(adder, limits);
    }
}

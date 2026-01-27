/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class OperationalLimitsGroupsImpl implements FlowsLimitsHolder {

    private static final String DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID = "DEFAULT";

    private final String attributeName;
    private final LinkedHashSet<String> selectedLimitsIds = new LinkedHashSet<>();

    private final Map<String, OperationalLimitsGroupImpl> operationalLimitsGroupById = new LinkedHashMap<>();
    private final AbstractIdentifiable<?> identifiable;

    OperationalLimitsGroupsImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = attributeName;
    }

    @Override
    public OperationalLimitsGroupImpl newOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroupImpl newLimits = new OperationalLimitsGroupImpl(id, identifiable, attributeName, selectedLimitsIds);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.put(id, newLimits);
        if (selectedLimitsIds.contains(id)) {
            notifyUpdate(oldLimits, newLimits);
        }
        return newLimits;
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
        return getOperationalLimitsGroupImpl(id).map(Function.identity());
    }

    private Optional<OperationalLimitsGroupImpl> getOperationalLimitsGroupImpl(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(operationalLimitsGroupById.get(id));
    }

    @Override
    public void removeOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        if (selectedLimitsIds.contains(id)) {
            deselectOperationalLimitsGroup(id);
        }
        operationalLimitsGroupById.remove(id);

    }

    @Override
    public void setSelectedOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroup newSelectedLimits = getOperationalLimitsGroupOrThrow(id);
        boolean wasAlreadySelected = selectedLimitsIds.contains(id);
        cancelSelectedOperationalLimitsGroup();
        selectedLimitsIds.add(id);
        if (!wasAlreadySelected) {
            notifyUpdate(null, newSelectedLimits);
        }
    }

    @Override
    public void addSelectedOperationalLimitsGroups(String... ids) {
        for (String id : ids) {
            Objects.requireNonNull(id);
            OperationalLimitsGroup newSelectedLimits = getOperationalLimitsGroupOrThrow(id);
            boolean wasAlreadySelected = selectedLimitsIds.contains(id);
            // re-insert the element with remove -> add, so that getSelectedOperationalLimitsGroupId returns things in the correct order (since add alone won't re-insert if already present)
            selectedLimitsIds.remove(id);
            selectedLimitsIds.add(id);
            if (!wasAlreadySelected) {
                notifyUpdate(null, newSelectedLimits);
            }
        }
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup() {
        Stream<String> selectedLimitsIdsStream = selectedLimitsIds.stream();
        selectedLimitsIds.clear();
        //notify update that nothing is selected anymore
        selectedLimitsIdsStream.forEach(this::notifyDeselect);
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup() {
        return getSelectedOperationalLimitsGroupId().map(operationalLimitsGroupById::get);
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups() {
        return Collections.unmodifiableCollection(operationalLimitsGroupById.values());
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId() {
        if (selectedLimitsIds.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(selectedLimitsIds.getLast());
        }
    }

    @Override
    public OperationalLimitsGroupImpl getOrCreateSelectedOperationalLimitsGroup() {
        String groupId = DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID;
        if (operationalLimitsGroupById.containsKey(groupId)) {
            return operationalLimitsGroupById.get(groupId);
        } else {
            OperationalLimitsGroupImpl newDefault = newOperationalLimitsGroup(groupId);
            setSelectedOperationalLimitsGroup(groupId);
            return newDefault;
        }
    }

    @Override
    public Collection<String> getAllSelectedOperationalLimitsGroupIds() {
        return Collections.unmodifiableSet(selectedLimitsIds);
    }

    @Override
    public Collection<OperationalLimitsGroup> getAllSelectedOperationalLimitsGroups() {
        return operationalLimitsGroupById.entrySet()
                .stream()
                .filter(e -> selectedLimitsIds.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void deselectOperationalLimitsGroup(String id) {
        selectedLimitsIds.remove(id);
        notifyDeselect(id);
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newCurrentLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this::getOrCreateSelectedOperationalLimitsGroup,
                identifiable, identifiable.getId(), identifiable.getNetwork());
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newActivePowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(this::getOrCreateSelectedOperationalLimitsGroup,
                identifiable, identifiable.getId(), identifiable.getNetwork());
    }

    /**
     * @deprecated Use {@link OperationalLimitsGroup#newApparentPowerLimits()} instead.
     */
    @Deprecated(since = "6.8.0")
    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this::getOrCreateSelectedOperationalLimitsGroup,
                identifiable, identifiable.getId(), identifiable.getNetwork());
    }

    private void notifyUpdate(OperationalLimitsGroup oldValue, OperationalLimitsGroup newValue) {
        CurrentLimits oldCurrentValue = Optional.ofNullable(oldValue).flatMap(OperationalLimitsGroup::getCurrentLimits).orElse(null);
        CurrentLimits newCurrentValue = Optional.ofNullable(newValue).flatMap(OperationalLimitsGroup::getCurrentLimits).orElse(null);
        identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + LimitType.CURRENT, oldCurrentValue, newCurrentValue);

        ActivePowerLimits oldActivePowerLimits = Optional.ofNullable(oldValue).flatMap(OperationalLimitsGroup::getActivePowerLimits).orElse(null);
        ActivePowerLimits newActivePowerLimits = Optional.ofNullable(newValue).flatMap(OperationalLimitsGroup::getActivePowerLimits).orElse(null);
        identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + LimitType.ACTIVE_POWER, oldActivePowerLimits, newActivePowerLimits);

        ApparentPowerLimits oldApparentPowerLimits = Optional.ofNullable(oldValue).flatMap(OperationalLimitsGroup::getApparentPowerLimits).orElse(null);
        ApparentPowerLimits newApparentPowerLimits = Optional.ofNullable(newValue).flatMap(OperationalLimitsGroup::getApparentPowerLimits).orElse(null);
        identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + LimitType.APPARENT_POWER, oldApparentPowerLimits, newApparentPowerLimits);
    }

    private void notifyDeselect(String id) {
        notifyUpdate(operationalLimitsGroupById.get(id), null);
    }

    private OperationalLimitsGroup getOperationalLimitsGroupOrThrow(String id) {
        return getOperationalLimitsGroup(id).orElseThrow(() -> new PowsyblException("No operational limits group is associated to id " + id + " so this id can't be part of the selected groups"));
    }
}

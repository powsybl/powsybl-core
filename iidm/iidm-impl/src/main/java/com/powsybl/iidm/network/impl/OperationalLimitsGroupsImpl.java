/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.RefObj;

import java.util.*;
import java.util.function.Function;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class OperationalLimitsGroupsImpl implements FlowsLimitsHolder {

    private static final String DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID = "DEFAULT";

    private final String attributeName;
    private final RefObj<String> selectedLimitsId = new RefObj<>(null);

    private final Map<String, OperationalLimitsGroupImpl> operationalLimitsGroupById = new LinkedHashMap<>();
    private final AbstractIdentifiable<?> identifiable;

    OperationalLimitsGroupsImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = attributeName;
    }

    @Override
    public OperationalLimitsGroupImpl newOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroupImpl newLimits = new OperationalLimitsGroupImpl(id, identifiable, attributeName, selectedLimitsId);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.put(id, newLimits);
        if (id.equals(selectedLimitsId.get())) {
            // Notify done only for default limits change
            notifyUpdate(oldLimits, newLimits);
        }
        return newLimits;
    }

    @Override
    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(operationalLimitsGroupById.get(id));
    }

    @Override
    public void removeOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.remove(id);
        if (id.equals(selectedLimitsId.get())) {
            selectedLimitsId.set(null);
            notifyUpdate(oldLimits, null);
        }
    }

    @Override
    public void setSelectedOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        if (id.equals(selectedLimitsId.get())) {
            return;
        }
        OperationalLimitsGroup newDefaultLimits = operationalLimitsGroupById.get(id);
        if (newDefaultLimits == null) {
            throw new PowsyblException("No operational limits group is associated to id " + id + " so this id can't be the default one");
        }
        Optional<OperationalLimitsGroup> oldDefaultLimits = getSelectedOperationalLimitsGroup();
        selectedLimitsId.set(id);
        oldDefaultLimits.ifPresent(olg -> notifyUpdate(olg, newDefaultLimits));
    }

    @Override
    public void cancelSelectedOperationalLimitsGroup() {
        getSelectedOperationalLimitsGroup().ifPresent(oldDefaultLimits -> {
            selectedLimitsId.set(null);
            notifyUpdate(oldDefaultLimits, null);
        });
    }

    @Override
    public Optional<OperationalLimitsGroup> getSelectedOperationalLimitsGroup() {
        return getSelectedOperationalLimitsGroupImpl().map(Function.identity());
    }

    private Optional<OperationalLimitsGroupImpl> getSelectedOperationalLimitsGroupImpl() {
        return Optional.ofNullable(selectedLimitsId.get()).map(operationalLimitsGroupById::get);
    }

    @Override
    public Collection<OperationalLimitsGroup> getOperationalLimitsGroups() {
        return Collections.unmodifiableCollection(operationalLimitsGroupById.values());
    }

    @Override
    public Optional<String> getSelectedOperationalLimitsGroupId() {
        return Optional.ofNullable(selectedLimitsId.get());
    }

    private OperationalLimitsGroupImpl getOrCreateSelectedOperationalLimitsGroup() {
        Optional<OperationalLimitsGroupImpl> opt = getSelectedOperationalLimitsGroupImpl();
        if (opt.isPresent()) {
            return opt.get();
        }
        String groupId = DEFAULT_SELECTED_OPERATIONAL_LIMITS_GROUP_ID;
        OperationalLimitsGroupImpl group = Optional.ofNullable(operationalLimitsGroupById.get(groupId))
            .orElseGet(() -> newOperationalLimitsGroup(groupId));
        setSelectedOperationalLimitsGroup(groupId);
        return group;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this::getOrCreateSelectedOperationalLimitsGroup,
                identifiable, identifiable.getId());
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(this::getOrCreateSelectedOperationalLimitsGroup,
                identifiable, identifiable.getId());
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this::getOrCreateSelectedOperationalLimitsGroup,
                identifiable, identifiable.getId());
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
}

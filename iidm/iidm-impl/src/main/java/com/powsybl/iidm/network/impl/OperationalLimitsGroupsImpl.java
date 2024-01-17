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
class OperationalLimitsGroupsImpl implements OperationalLimitsGroups {

    private static final String DEFAULT_OPERATIONAL_LIMITS_GROUP_DEFAULT_ID = "DEFAULT";

    private final String attributeName;
    private final RefObj<String> defaultLimitsId = new RefObj<>(null);

    private final Map<String, OperationalLimitsGroupImpl> operationalLimitsGroupById = new LinkedHashMap<>();
    private final AbstractIdentifiable<?> identifiable;

    OperationalLimitsGroupsImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = attributeName;
    }

    @Override
    public Collection<OperationalLimitsGroup> getAllOperationalLimitsGroup() {
        return Collections.unmodifiableCollection(operationalLimitsGroupById.values());
    }

    @Override
    public OperationalLimitsGroupImpl newOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroupImpl newLimits = new OperationalLimitsGroupImpl(id, identifiable, attributeName, defaultLimitsId);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.put(id, newLimits);
        if (id.equals(defaultLimitsId.get())) {
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
        if (id.equals(defaultLimitsId.get())) {
            defaultLimitsId.set(null);
            notifyUpdate(oldLimits, null);
        }
    }

    @Override
    public void setDefaultOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        if (id.equals(defaultLimitsId.get())) {
            return;
        }
        OperationalLimitsGroup newDefaultLimits = operationalLimitsGroupById.get(id);
        if (newDefaultLimits == null) {
            throw new PowsyblException("No operational limits group is associated to id " + id + " so this id can't be the default one");
        }
        Optional<OperationalLimitsGroup> oldDefaultLimits = getDefaultOperationalLimitsGroup();
        defaultLimitsId.set(id);
        oldDefaultLimits.ifPresent(olg -> notifyUpdate(olg, newDefaultLimits));
    }

    @Override
    public void cancelDefaultOperationalLimitsGroup() {
        getDefaultOperationalLimitsGroup().ifPresent(oldDefaultLimits -> {
            defaultLimitsId.set(null);
            notifyUpdate(oldDefaultLimits, null);
        });
    }

    @Override
    public Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup() {
        return getDefaultOperationalLimitsGroupImpl().map(Function.identity());
    }

    private Optional<OperationalLimitsGroupImpl> getDefaultOperationalLimitsGroupImpl() {
        return Optional.ofNullable(defaultLimitsId.get()).map(operationalLimitsGroupById::get);
    }

    @Override
    public Optional<String> getDefaultOperationalLimitsGroupId() {
        return Optional.ofNullable(defaultLimitsId.get());
    }

    private OperationalLimitsGroupImpl getOrCreateDefaultOperationalLimitsGroup() {
        return getDefaultOperationalLimitsGroupImpl()
                .or(() -> Optional.ofNullable(operationalLimitsGroupById.get(DEFAULT_OPERATIONAL_LIMITS_GROUP_DEFAULT_ID)))
                .orElseGet(() -> newOperationalLimitsGroup(DEFAULT_OPERATIONAL_LIMITS_GROUP_DEFAULT_ID));
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        OperationalLimitsGroupImpl group = getOrCreateDefaultOperationalLimitsGroup();
        setDefaultOperationalLimitsGroup(group.getId());
        return new CurrentLimitsAdderImpl(getOrCreateDefaultOperationalLimitsGroup(), identifiable, identifiable.getId());
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        OperationalLimitsGroupImpl group = getOrCreateDefaultOperationalLimitsGroup();
        setDefaultOperationalLimitsGroup(group.getId());
        return new ActivePowerLimitsAdderImpl(group, identifiable, identifiable.getId());
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        OperationalLimitsGroupImpl group = getOrCreateDefaultOperationalLimitsGroup();
        setDefaultOperationalLimitsGroup(group.getId());
        return new ApparentPowerLimitsAdderImpl(group, identifiable, identifiable.getId());
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

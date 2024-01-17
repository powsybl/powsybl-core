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

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class OperationalLimitsGroupsImpl implements OperationalLimitsGroups {

    private final String attributeName;
    private final RefObj<String> defaultLimitsId = new RefObj<>(null);

    private final Map<String, OperationalLimitsGroup> operationalLimitsGroupById = new HashMap<>();
    private final AbstractIdentifiable<?> identifiable;

    OperationalLimitsGroupsImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = attributeName;
    }

    public List<OperationalLimitsGroup> getAllOperationalLimitsGroup() {
        return new ArrayList<>(operationalLimitsGroupById.values());
    }

    public OperationalLimitsGroup newOperationalLimitsGroup(String id) {
        // NB. Do not check if id exist but replace
        // NB. Notify only for default limits change (can be changed)
        Objects.requireNonNull(id);
        OperationalLimitsGroup newLimits = new OperationalLimitsGroupImpl(id, identifiable, attributeName, defaultLimitsId);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.put(id, newLimits);
        if (id.equals(defaultLimitsId.get())) {
            notifyUpdate(oldLimits, newLimits);
        }
        return newLimits;
    }

    private OperationalLimitsGroup getDefault() {
        Objects.requireNonNull(defaultLimitsId);
        OperationalLimitsGroup defaultLimits = operationalLimitsGroupById.get(defaultLimitsId.get());
        if (defaultLimits == null) {
            throw new PowsyblException("Default id exist and is " + defaultLimitsId + " but associated operational limits group do not exist");
        }
        return defaultLimits;
    }

    public Optional<OperationalLimitsGroup> getOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(operationalLimitsGroupById.get(id));
    }

    public void removeOperationalLimitsGroup(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.remove(id);
        if (id.equals(defaultLimitsId.get())) {
            defaultLimitsId.set(null);
            notifyUpdate(oldLimits, null);
        }
    }

    public void setDefault(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroup newDefaultLimits = operationalLimitsGroupById.get(id);
        if (newDefaultLimits == null) {
            throw new PowsyblException("No operational limits group is associated to id " + id + " so this id can't be the default one");
        }
        OperationalLimitsGroup oldDefaultLimits = getDefault();
        defaultLimitsId.set(id);
        notifyUpdate(oldDefaultLimits, newDefaultLimits);
    }

    public void cancelDefault() {
        if (defaultLimitsId.get() != null) {
            OperationalLimitsGroup oldDefaultLimits = getDefault();
            defaultLimitsId.set(null);
            notifyUpdate(oldDefaultLimits, null);
        }
    }

    public Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup() {
        return defaultLimitsId.get() == null ? Optional.empty() : Optional.of(getDefault());
    }

    public Optional<String> getDefaultId() {
        return Optional.ofNullable(defaultLimitsId.get());
    }

    // For retro-compatibility:
    CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAndGroupAdderImpl(this, identifiable);
    }

    ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAndGroupAdderImpl(this, identifiable);
    }

    ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAndGroupAdderImpl(this, identifiable);
    }

    private void notifyUpdate(@Nullable OperationalLimitsGroup oldValue, @Nullable OperationalLimitsGroup newValue) {
        // NB. Normally only called for default limits
        if (newValue == null) {
            Objects.requireNonNull(oldValue);

        }
        CurrentLimits oldCurrentValue = oldValue == null ? null : oldValue.getCurrentLimits().orElse(null);
        CurrentLimits newCurrentValue = newValue == null ? null : newValue.getCurrentLimits().orElse(null);
        if (!Objects.equals(oldCurrentValue, newCurrentValue)) {
            identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + LimitType.CURRENT, oldCurrentValue, newCurrentValue);
        }
        ActivePowerLimits oldActivePowerLimits = oldValue == null ? null : oldValue.getActivePowerLimits().orElse(null);
        ActivePowerLimits newActivePowerLimits = newValue == null ? null : newValue.getActivePowerLimits().orElse(null);
        if (!Objects.equals(oldActivePowerLimits, newActivePowerLimits)) {
            identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + LimitType.ACTIVE_POWER, oldActivePowerLimits, newActivePowerLimits);
        }
        ApparentPowerLimits oldApparentPowerLimits = oldValue == null ? null : oldValue.getApparentPowerLimits().orElse(null);
        ApparentPowerLimits newApparentPowerLimits = newValue == null ? null : newValue.getApparentPowerLimits().orElse(null);
        if (!Objects.equals(oldApparentPowerLimits, newApparentPowerLimits)) {
            identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + LimitType.APPARENT_POWER, oldApparentPowerLimits, newApparentPowerLimits);
        }
    }
}

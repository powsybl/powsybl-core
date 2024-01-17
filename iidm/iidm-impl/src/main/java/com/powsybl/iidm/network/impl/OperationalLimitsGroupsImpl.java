/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class OperationalLimitsGroupsImpl implements OperationalLimitsGroups {

    private String defaultLimitsId = null;

    class ValidableImpl implements GroupsValidable {

        private final AbstractIdentifiable<?> identifiable;
        private final String attributeName;

        ValidableImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
            this.identifiable = Objects.requireNonNull(identifiable);
            this.attributeName = Objects.requireNonNull(attributeName);
        }

        @Override
        public String getMessageHeader() {
            return identifiable.getMessageHeader();
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

        @Override
        public void notifyUpdateIfDefaultLimits(String id, LimitType limitType, String attribute, double oldValue, double newValue) {
            if (defaultLimitsId != null && defaultLimitsId.equals(id)) {
                identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType + "." + attribute, oldValue, newValue);
            }
        }

        @Override
        public void notifyUpdateIfDefaultLimits(String id, LimitType limitType, @Nullable OperationalLimits oldValue, @Nullable OperationalLimits newValue) {
            if (defaultLimitsId != null && defaultLimitsId.equals(id)) {
                if (newValue == null) {
                    Objects.requireNonNull(oldValue);

                }
                identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType, oldValue, newValue);
            }
        }
    }

    private final Map<String, OperationalLimitsGroup> operationalLimitsGroupById = new HashMap<>();
    private final ValidableImpl validable;

    OperationalLimitsGroupsImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.validable = new ValidableImpl(identifiable, attributeName);
    }

    public List<OperationalLimitsGroup> getAllOperationalLimitsGroup() {
        return new ArrayList<>(operationalLimitsGroupById.values());
    }

    public OperationalLimitsGroup newOperationalLimitsGroup(String id) {
        // NB. Do not check if id exist but replace
        // NB. Notify only for default limits change (can be changed)
        Objects.requireNonNull(id);
        OperationalLimitsGroup newLimits = new OperationalLimitsGroupImpl(id, validable);
        OperationalLimitsGroup oldLimits = operationalLimitsGroupById.put(id, newLimits);
        if (defaultLimitsId != null && defaultLimitsId.equals(id)) {
            validable.notifyUpdate(oldLimits, newLimits);
        }
        return newLimits;
    }

    private OperationalLimitsGroup getDefault() {
        Objects.requireNonNull(defaultLimitsId);
        OperationalLimitsGroup defaultLimits = operationalLimitsGroupById.get(defaultLimitsId);
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
        if (defaultLimitsId != null && Objects.equals(defaultLimitsId, id)) {
            defaultLimitsId = null;
            validable.notifyUpdate(oldLimits, null);
        }
    }

    public void setDefault(String id) {
        Objects.requireNonNull(id);
        OperationalLimitsGroup newDefaultLimits = operationalLimitsGroupById.get(id);
        if (newDefaultLimits == null) {
            throw new PowsyblException("No operational limits group is associated to id " + id + " so this id can't be the default one");
        }
        OperationalLimitsGroup oldDefaultLimits = defaultLimitsId != null ? getDefault() : null;
        defaultLimitsId = id;
        validable.notifyUpdate(oldDefaultLimits, newDefaultLimits);
    }

    public void cancelDefault() {
        if (defaultLimitsId != null) {
            OperationalLimitsGroup oldDefaultLimits = getDefault();
            defaultLimitsId = null;
            validable.notifyUpdate(oldDefaultLimits, null);
        }
    }

    public Optional<OperationalLimitsGroup> getDefaultOperationalLimitsGroup() {
        return defaultLimitsId == null ? Optional.empty() : Optional.of(getDefault());
    }

    public Optional<String> getDefaultId() {
        return Optional.ofNullable(defaultLimitsId);
    }

    // For retro-compatibility:
    CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAndGroupAdderImpl(this, validable);
    }

    ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAndGroupAdderImpl(this, validable);
    }

    ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAndGroupAdderImpl(this, validable);
    }
}

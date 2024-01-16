/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
class OperationalLimitsGroupImpl implements OperationalLimitsGroup {

    private final OperationalLimitsGroups.GroupsValidable validable;
    private final String id;
    private CurrentLimits currentLimits;
    private ActivePowerLimits activePowerLimits;
    private ApparentPowerLimits apparentPowerLimits;

    OperationalLimitsGroupImpl(String id, OperationalLimitsGroups.GroupsValidable validable) {
        this.validable = Objects.requireNonNull(validable);
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<CurrentLimits> getCurrentLimits() {
        return Optional.ofNullable(currentLimits);
    }

    @Override
    public Optional<ActivePowerLimits> getActivePowerLimits() {
        return Optional.ofNullable(activePowerLimits);
    }

    @Override
    public Optional<ApparentPowerLimits> getApparentPowerLimits() {
        return Optional.ofNullable(apparentPowerLimits);
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this, validable);
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(this, validable);
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this, validable);
    }

    @Override
    public void removeCurrentLimits() {
        setCurrentLimits(null);
    }

    @Override
    public void removeActivePowerLimits() {
        setActivePowerLimits(null);
    }

    @Override
    public void removeApparentPowerLimits() {
        setApparentPowerLimits(null);
    }

    public void setCurrentLimits(CurrentLimits limits) {
        OperationalLimits oldValue = this.currentLimits;
        this.currentLimits = limits;
        validable.notifyUpdateIfDefaultLimits(getId(), LimitType.CURRENT, oldValue, limits);
    }

    public void setActivePowerLimits(ActivePowerLimits limits) {
        OperationalLimits oldValue = this.activePowerLimits;
        this.activePowerLimits = limits;
        validable.notifyUpdateIfDefaultLimits(getId(), LimitType.ACTIVE_POWER, oldValue, limits);
    }

    public void setApparentPowerLimits(ApparentPowerLimits limits) {
        OperationalLimits oldValue = this.apparentPowerLimits;
        this.apparentPowerLimits = limits;
        validable.notifyUpdateIfDefaultLimits(getId(), LimitType.APPARENT_POWER, oldValue, limits);
    }

    public Validable getValidable() {
        return validable;
    }

    public void notifyUpdateIfDefaultLimits(LimitType limitType, String attribute, double oldValue, double newValue) {
        validable.notifyUpdateIfDefaultLimits(getId(), limitType, attribute, oldValue, newValue);
    }

    @Override
    public boolean isEmpty() {
        return currentLimits == null && apparentPowerLimits == null && activePowerLimits == null;
    }

}

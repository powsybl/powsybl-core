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
public class OperationalLimitsGroupImpl implements OperationalLimitsGroup, Validable {

    private final String id;
    private CurrentLimits currentLimits;
    private ActivePowerLimits activePowerLimits;
    private ApparentPowerLimits apparentPowerLimits;
    private final Identifiable<?> identifiable;
    private final NetworkListenerList listeners;
    private final Validable validable;
    private final String attributeName;
    private String selectedGroupId;

    OperationalLimitsGroupImpl(String id, AbstractIdentifiable<?> identifiable, String attributeName, String selectedGroupId) {
        this(id, Objects.requireNonNull(identifiable), identifiable.getNetwork().getListeners(),
                identifiable, attributeName, selectedGroupId);
    }

    public OperationalLimitsGroupImpl(String id, Identifiable<?> identifiable, NetworkListenerList listeners,
                                      Validable validable, String attributeName, String selectedGroupId) {
        this.id = Objects.requireNonNull(id);
        this.identifiable = Objects.requireNonNull(identifiable);
        this.listeners = listeners;
        this.validable = Objects.requireNonNull(validable);
        this.attributeName = Objects.requireNonNull(attributeName);
        this.selectedGroupId = selectedGroupId;
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
        return new CurrentLimitsAdderImpl(() -> this, validable, identifiable.getId(), getNetwork());
    }

    @Override
    public ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(() -> this, validable, identifiable.getId(), getNetwork());
    }

    @Override
    public ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(() -> this, validable, identifiable.getId(), getNetwork());
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

    public NetworkImpl getNetwork() {
        return (NetworkImpl) identifiable.getNetwork();
    }

    public void setCurrentLimits(CurrentLimits limits) {
        OperationalLimits oldValue = this.currentLimits;
        this.currentLimits = limits;
        notifyUpdate(LimitType.CURRENT, oldValue, limits);
    }

    public void setActivePowerLimits(ActivePowerLimits limits) {
        OperationalLimits oldValue = this.activePowerLimits;
        this.activePowerLimits = limits;
        notifyUpdate(LimitType.ACTIVE_POWER, oldValue, limits);
    }

    public void setApparentPowerLimits(ApparentPowerLimits limits) {
        OperationalLimits oldValue = this.apparentPowerLimits;
        this.apparentPowerLimits = limits;
        notifyUpdate(LimitType.APPARENT_POWER, oldValue, limits);
    }

    public Validable getValidable() {
        return validable;
    }

    public void notifyPermanentLimitUpdate(LimitType limitType, double oldValue, double newValue) {
        PermanentLimitInfo oldPermanentLimitInfo = new PermanentLimitInfo(oldValue, id, id.equals(selectedGroupId));
        PermanentLimitInfo newPermanentLimitInfo = new PermanentLimitInfo(newValue, id, id.equals(selectedGroupId));
        doNotify(attributeName + "_" + limitType + ".permanentLimit", oldPermanentLimitInfo, newPermanentLimitInfo);
    }

    private void notifyUpdate(LimitType limitType, OperationalLimits oldValue, OperationalLimits newValue) {
        OperationalLimitsInfo oldOperationalLimitsInfo = new OperationalLimitsInfo(oldValue, id, id.equals(selectedGroupId));
        OperationalLimitsInfo newOperationalLimitsInfo = new OperationalLimitsInfo(newValue, id, id.equals(selectedGroupId));
        doNotify(attributeName + "_" + limitType, oldOperationalLimitsInfo, newOperationalLimitsInfo);
    }

    public void notifyTemporaryLimitValueUpdate(LimitType limitType, double oldValue, double newValue, int acceptableDuration) {
        TemporaryLimitInfo oldTemporaryLimitInfo = new TemporaryLimitInfo(oldValue, id, id.equals(selectedGroupId), acceptableDuration);
        TemporaryLimitInfo newTemporaryLimitInfo = new TemporaryLimitInfo(newValue, id, id.equals(selectedGroupId), acceptableDuration);
        doNotify(attributeName + "_" + limitType + ".temporaryLimit.value", oldTemporaryLimitInfo, newTemporaryLimitInfo);
    }

    private void doNotify(String attribute, Object oldValue, Object newValue) {
        if (listeners != null) {
            listeners.notifyUpdate(identifiable, attribute, oldValue, newValue);
        }
    }

    @Override
    public String getMessageHeader() {
        return validable.getMessageHeader();
    }

    @Override
    public boolean isEmpty() {
        return currentLimits == null && apparentPowerLimits == null && activePowerLimits == null;
    }

    public void setSelectedGroupId(String selectedGroupId) {
        this.selectedGroupId = selectedGroupId;
    }

    public record PermanentLimitInfo(double value, String groupId, boolean inSelectedGroup) {
    }

    public record OperationalLimitsInfo(OperationalLimits value, String groupId, boolean inSelectedGroup) {
    }

    public record TemporaryLimitInfo(double value, String groupId, boolean inSelectedGroup, int acceptableDuration) {
    }
}

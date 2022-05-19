/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class OperationalLimitsHolderImpl implements OperationalLimitsOwner {

    private final EnumMap<LimitType, String> activeLimitsIds = new EnumMap<>(LimitType.class);
    private final EnumMap<LimitType, AbstractOperationalLimitsSet<?>> limitsSet = new EnumMap<>(LimitType.class);
    private final AbstractIdentifiable<?> identifiable;
    private final String attributeName;

    OperationalLimitsHolderImpl(AbstractIdentifiable<?> identifiable, String attributeName) {
        this.identifiable = Objects.requireNonNull(identifiable);
        this.attributeName = Objects.requireNonNull(attributeName);
    }

    @Override
    public void remove(LimitType type) {
        limitsSet.remove(type);
    }

    @Override
    public Optional<String> getActiveLimitId(LimitType limitType) {
        return Optional.ofNullable(activeLimitsIds.get(limitType));
    }

    @Override
    public void setActiveLimitId(LimitType limitType, String id) {
        if (limitsSet.get(limitType) == null || limitsSet.get(limitType).getLimits(id) == null) {
            throw new PowsyblException();
        }
        activeLimitsIds.put(limitType, id);
    }

    @Override
    public <L extends AbstractOperationalLimits<L>> void setOperationalLimits(LimitType limitType, L operationalLimits) {
        OperationalLimits oldValue;
        AbstractOperationalLimitsSet<L> set = (AbstractOperationalLimitsSet<L>) limitsSet.computeIfAbsent(limitType, k -> AbstractOperationalLimitsSet.create(limitType, this));
        oldValue = set.addLimit(operationalLimits);
        identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType, oldValue, operationalLimits);
    }

    @Override
    public void notifyUpdate(LimitType limitType, String attribute, Object oldValue, Object newValue) {
        identifiable.getNetwork().getListeners().notifyUpdate(identifiable, attributeName + "_" + limitType + "." + attribute, oldValue, newValue);
    }

    <L extends OperationalLimits, S extends OperationalLimitsSet<L>> S getOperationalLimitsSet(LimitType type, Class<S> limitClazz) {
        if (type == null) {
            throw new IllegalArgumentException("limit type is null");
        }
        OperationalLimitsSet<?> ol = this.limitsSet.get(type);
        if (ol == null) {
            return (S) getEmptySet(type);
        }
        if (limitClazz.isInstance(ol)) {
            return (S) ol;
        }
        throw new AssertionError("Unexpected class for operational limits of type " + type + ". Expected: " + ol.getClass().getName() + ", actual: " + limitClazz.getName() + ".");
    }

    static OperationalLimitsSet<?> getEmptySet(LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return ActivePowerLimitsSet.EMPTY;
            case APPARENT_POWER:
                return ApparentPowerLimitsSet.EMPTY;
            case CURRENT:
                return CurrentLimitsSet.EMPTY;
            default:
                throw new PowsyblException();
        }
    }

    <L extends OperationalLimits, S extends OperationalLimitsSet<L>> Optional<L> getActiveLimits(LimitType type, Class<S> limitClazz) {
        OperationalLimitsSet<L> set = getOperationalLimitsSet(type, limitClazz);
        if (set == null) {
            return Optional.empty();
        }
        return set.getActiveLimits();
    }

    CurrentLimitsAdder newCurrentLimits() {
        return new CurrentLimitsAdderImpl(this);
    }

    ApparentPowerLimitsAdder newApparentPowerLimits() {
        return new ApparentPowerLimitsAdderImpl(this);
    }

    ActivePowerLimitsAdder newActivePowerLimits() {
        return new ActivePowerLimitsAdderImpl(this);
    }

    @Override
    public String getMessageHeader() {
        return identifiable.getMessageHeader();
    }
}

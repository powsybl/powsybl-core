/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractOperationalLimitsSet<L extends OperationalLimits> implements OperationalLimitsSet<L> {

    private final OperationalLimitsOwner owner;
    private L limits;
    private final Map<String, L> limitsMap = new HashMap<>();

    AbstractOperationalLimitsSet(OperationalLimitsOwner owner) {
        this.owner = owner;
    }

    abstract LimitType getType();

    @Override
    public L getLimits(String id) {
        if (limits != null) {
            throw new PowsyblException();
        }
        return Optional.ofNullable(limitsMap.get(id)).orElseThrow(PowsyblException::new);
    }

    @Override
    public Collection<L> getLimits() {
        if (limitsMap.isEmpty()) {
            return Collections.singleton(limits);
        }
        return Collections.unmodifiableCollection(limitsMap.values());
    }

    @Override
    public Optional<L> getActiveLimits() {
        if (limitsMap.isEmpty()) {
            return Optional.ofNullable(limits);
        }
        return owner.getActiveLimitId(getType()).map(limitsMap::get).or(Optional::empty);
    }

    @Override
    public void remove() {
        owner.remove(getType());
    }

    OperationalLimits addLimit(L operationalLimits) {
        Optional<String> id = operationalLimits.getId();
        OperationalLimits oldValue;
        if (id.isEmpty()) {
            if (!limitsMap.isEmpty()) {
                throw new PowsyblException();
            }
            oldValue = limits;
            limits = operationalLimits;
        } else {
            if (limits != null) {
                throw new PowsyblException();
            }
            oldValue = limitsMap.put(id.get(), operationalLimits);
        }
        ((AbstractOperationalLimits<L>) operationalLimits).setLimitSet(this);
        owner.notifyUpdate(getType(), "limit" + (oldValue != null && oldValue.getId().isPresent() ? "_" + oldValue.getId().get() : ""), oldValue, operationalLimits);
        return oldValue;
    }

    void removeLimit(L operationalLimits) {
        L oldValue;
        if (limits == operationalLimits) {
            oldValue = limits;
            limits = null;
        } else if (operationalLimits.getId().isPresent()) {
            Optional<String> id = operationalLimits.getId();
            Optional<String> activeLimitId = owner.getActiveLimitId(getType());
            if (activeLimitId.isPresent() && id.isPresent() && activeLimitId.get().equals(id.get())) {
                throw new PowsyblException();
            }
            oldValue = operationalLimits.getId().map(limitsMap::remove).orElseThrow(PowsyblException::new);
        } else {
            throw new PowsyblException();
        }
        owner.notifyUpdate(getType(), "limit" + (oldValue.getId().isPresent() ? "_" + oldValue.getId().get() : ""), oldValue, null);
    }

    static AbstractOperationalLimitsSet<?> create(LimitType limitType, OperationalLimitsOwner owner) {
        switch (limitType) {
            case ACTIVE_POWER:
                return new ActivePowerLimitsSetImpl(owner);
            case APPARENT_POWER:
                return new ApparentPowerLimitsSetImpl(owner);
            case CURRENT:
                return new CurrentLimitsSetImpl(owner);
            default:
                throw new PowsyblException();
        }
    }
}

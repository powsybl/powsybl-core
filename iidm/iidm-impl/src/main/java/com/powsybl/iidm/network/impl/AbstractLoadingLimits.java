/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
abstract class AbstractLoadingLimits<L extends AbstractLoadingLimits<L>> extends AbstractOperationalLimits implements LoadingLimits {

    private double permanentLimit;

    private final TreeMap<Integer, TemporaryLimit> temporaryLimits;

    static class TemporaryLimitImpl implements TemporaryLimit {

        private final String name;

        private final double value;

        private final int acceptableDuration;

        private final boolean fictitious;

        TemporaryLimitImpl(String name, double value, int acceptableDuration, boolean hasOverloadingProtection) {
            this.name = Objects.requireNonNull(name);
            this.value = value;
            this.acceptableDuration = acceptableDuration;
            this.fictitious = hasOverloadingProtection;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public double getValue() {
            return value;
        }

        @Override
        public int getAcceptableDuration() {
            return acceptableDuration;
        }

        @Override
        public boolean isFictitious() {
            return fictitious;
        }
    }

    AbstractLoadingLimits(OperationalLimitsOwner owner, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        super(owner);
        this.permanentLimit = permanentLimit;
        this.temporaryLimits = Objects.requireNonNull(temporaryLimits);
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public L setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        double oldValue = this.permanentLimit;
        this.permanentLimit = permanentLimit;
        owner.notifyUpdate(getLimitType(), "permanentLimit", oldValue, this.permanentLimit);
        return (L) this;
    }

    @Override
    public Collection<TemporaryLimit> getTemporaryLimits() {
        return temporaryLimits.values();
    }

    @Override
    public TemporaryLimit getTemporaryLimit(int acceptableDuration) {
        return temporaryLimits.get(acceptableDuration);
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        TemporaryLimit tl = getTemporaryLimit(acceptableDuration);
        return tl != null ? tl.getValue() : Double.NaN;
    }
}

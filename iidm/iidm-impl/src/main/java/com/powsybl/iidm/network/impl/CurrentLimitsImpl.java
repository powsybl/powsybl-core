/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;
import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsImpl implements CurrentLimits {

    private double permanentLimit;

    private final TreeMap<Integer, TemporaryLimit> temporaryLimits;

    private final CurrentLimitsOwner<?> owner;

    static class TemporaryLimitImpl implements TemporaryLimit {

        private final String name;

        private final double value;

        private final int acceptableDuration;

        private boolean fictitious;

        TemporaryLimitImpl(String name, double value, int acceptableDuration, boolean fictitious) {
            this.name = Objects.requireNonNull(name);
            this.value = value;
            this.acceptableDuration = acceptableDuration;
            this.fictitious = fictitious;
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

    CurrentLimitsImpl(double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits, CurrentLimitsOwner<?> owner) {
        this.permanentLimit = permanentLimit;
        this.temporaryLimits = Objects.requireNonNull(temporaryLimits);
        this.owner = Objects.requireNonNull(owner);
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public CurrentLimitsImpl setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        this.permanentLimit = permanentLimit;
        return this;
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

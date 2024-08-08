/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractLoadingLimits<L extends AbstractLoadingLimits<L>> implements LoadingLimits {

    protected Validable validable;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadingLimits.class);
    protected final OperationalLimitsGroupImpl group;
    private double permanentLimit;
    private final TreeMap<Integer, TemporaryLimit> temporaryLimits;

    static class TemporaryLimitImpl implements TemporaryLimit {

        private final String name;

        private double value;

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

        @Override
        public void setValue(double temporaryLimitValue) {
            this.value = temporaryLimitValue;
        }
    }

    AbstractLoadingLimits(Validable validable, OperationalLimitsGroupImpl owner, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        this.validable = Objects.requireNonNull(validable);
        this.group = Objects.requireNonNull(owner);
        this.permanentLimit = permanentLimit;
        this.temporaryLimits = Objects.requireNonNull(temporaryLimits);
        // The limits validation must be performed before calling this constructor (in the adders).
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public L setPermanentLimit(double permanentLimit) {
        NetworkImpl network = group.getNetwork();
        ValidationUtil.checkPermanentLimit(group.getValidable(), permanentLimit, getTemporaryLimits(),
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        double oldValue = this.permanentLimit;
        this.permanentLimit = permanentLimit;
        network.invalidateValidationLevel();
        group.notifyPermanentLimitUpdate(getLimitType(), oldValue, this.permanentLimit);
        return (L) this;
    }

    @Override
    public L setTemporaryLimitValue(int acceptableDuration, double temporaryLimitValue) {
        if (temporaryLimitValue < 0 || Double.isNaN(temporaryLimitValue)) {
            throw new ValidationException(validable, "Temporary limit value must be a non-negative double");
        }

        // Identify the limit that needs to be modified
        TemporaryLimit identifiedLimit = getTemporaryLimit(acceptableDuration);
        if (identifiedLimit == null) {
            throw new ValidationException(validable, "No temporary limit found for the given acceptable duration");
        }

        TreeMap<Integer, TemporaryLimit> temporaryLimitTreeMap = new TreeMap<>(this.temporaryLimits);
        // Creation of index markers
        Map.Entry<Integer, TemporaryLimit> biggerDurationEntry = temporaryLimitTreeMap.lowerEntry(acceptableDuration);
        Map.Entry<Integer, TemporaryLimit> smallerDurationEntry = temporaryLimitTreeMap.higherEntry(acceptableDuration);

        double oldValue = identifiedLimit.getValue();

        if (isTemporaryLimitValueValid(biggerDurationEntry, smallerDurationEntry, acceptableDuration, temporaryLimitValue)) {
            LOGGER.info("Temporary limit value changed from {} to {}", oldValue, temporaryLimitValue);
        } else {
            LOGGER.warn("Temporary limit value changed from {} to {}, but it is not valid", oldValue, temporaryLimitValue);
        }

        identifiedLimit.setValue(temporaryLimitValue);
        group.notifyTemporaryLimitValueUpdate(getLimitType(), oldValue, temporaryLimitValue, acceptableDuration);

        return (L) this;
    }

    protected boolean isTemporaryLimitValueValid(Map.Entry<Integer, TemporaryLimit> biggerDurationEntry,
                                               Map.Entry<Integer, TemporaryLimit> smallerDurationEntry,
                                               int acceptableDuration,
                                               double temporaryLimitValue) {

        if (biggerDurationEntry != null && smallerDurationEntry != null) {
            return biggerDurationEntry.getValue().getAcceptableDuration() > acceptableDuration
                    && smallerDurationEntry.getValue().getAcceptableDuration() < acceptableDuration
                    && biggerDurationEntry.getValue().getValue() < temporaryLimitValue
                    && smallerDurationEntry.getValue().getValue() > temporaryLimitValue;
        } else if (biggerDurationEntry == null && smallerDurationEntry != null) {
            return temporaryLimitValue > this.permanentLimit &&
                    smallerDurationEntry.getValue().getValue() > temporaryLimitValue
                    && smallerDurationEntry.getValue().getAcceptableDuration() < acceptableDuration;
        } else if (biggerDurationEntry != null) {
            return biggerDurationEntry.getValue().getAcceptableDuration() > acceptableDuration
                    && biggerDurationEntry.getValue().getValue() < temporaryLimitValue;
        }
        return temporaryLimitValue > this.permanentLimit;
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

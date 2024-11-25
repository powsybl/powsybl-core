/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.LoadingLimits;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractLoadingLimits<L extends AbstractLoadingLimits<L>> implements LoadingLimits {

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
    }

    AbstractLoadingLimits(OperationalLimitsGroupImpl owner, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
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
            throw new ValidationException(group.getValidable(), "Temporary limit value must be a positive double");
        }

        // Identify the limit that needs to be modified
        TemporaryLimit identifiedLimit = getTemporaryLimit(acceptableDuration);
        if (identifiedLimit == null) {
            throw new ValidationException(group.getValidable(), "No temporary limit found for the given acceptable duration");
        }

        TreeMap<Integer, TemporaryLimit> temporaryLimitTreeMap = new TreeMap<>(this.temporaryLimits);
        // Creation of index markers
        Map.Entry<Integer, TemporaryLimit> biggerDurationEntry = temporaryLimitTreeMap.lowerEntry(acceptableDuration);
        Map.Entry<Integer, TemporaryLimit> smallerDurationEntry = temporaryLimitTreeMap.higherEntry(acceptableDuration);

        double oldValue = identifiedLimit.getValue();

        if (isTemporaryLimitValueValid(biggerDurationEntry, smallerDurationEntry, acceptableDuration, temporaryLimitValue)) {
            LOGGER.info("{}Temporary limit value changed from {} to {}", group.getValidable().getMessageHeader(), oldValue, temporaryLimitValue);
        } else {
            LOGGER.warn("{}Temporary limit value changed from {} to {}, but it is not valid", group.getValidable().getMessageHeader(), oldValue, temporaryLimitValue);
        }

        this.temporaryLimits.put(acceptableDuration, new TemporaryLimitImpl(identifiedLimit.getName(), temporaryLimitValue,
                identifiedLimit.getAcceptableDuration(), identifiedLimit.isFictitious()));

        group.notifyTemporaryLimitValueUpdate(getLimitType(), oldValue, temporaryLimitValue, acceptableDuration);

        return (L) this;
    }

    protected boolean isTemporaryLimitValueValid(Map.Entry<Integer, TemporaryLimit> biggerDurationEntry,
                                               Map.Entry<Integer, TemporaryLimit> smallerDurationEntry,
                                               int acceptableDuration,
                                               double temporaryLimitValue) {

        boolean checkAgainstBigger = true;
        boolean checkAgainstSmaller = true;
        if (biggerDurationEntry != null) {
            checkAgainstBigger = biggerDurationEntry.getValue().getAcceptableDuration() > acceptableDuration
                    && biggerDurationEntry.getValue().getValue() < temporaryLimitValue;
        }
        if (smallerDurationEntry != null) {
            checkAgainstSmaller = smallerDurationEntry.getValue().getAcceptableDuration() < acceptableDuration
                    && smallerDurationEntry.getValue().getValue() > temporaryLimitValue;
        }
        return temporaryLimitValue > this.permanentLimit && checkAgainstBigger && checkAgainstSmaller;
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

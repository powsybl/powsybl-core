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

        @Override
        public void setValue(double temporaryLimitValue) {
            this.value = temporaryLimitValue;
        }
    }

    AbstractLoadingLimits(Validable validable, OperationalLimitsGroupImpl owner, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
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

        // verif
        NetworkImpl network = group.getNetwork();
        ValidationUtil.checkLoadingLimits(
                group.getValidable(),
                getPermanentLimit(),
                getTemporaryLimits(),
                network.getMinValidationLevel(),
                network.getReportNodeContext().getReportNode()
        );

        TreeMap<Integer, TemporaryLimit> temporaryLimitTreeMap = new TreeMap<>(this.temporaryLimits);
        // création des repérages par index
        Map.Entry<Integer, TemporaryLimit> lowerEntry = temporaryLimitTreeMap.lowerEntry(acceptableDuration);
        Map.Entry<Integer, TemporaryLimit> higherEntry = temporaryLimitTreeMap.higherEntry(acceptableDuration);

        // trouver la limite qu'on veut modifier et sa valeur
        TemporaryLimit identifiedLimit = getTemporaryLimit(acceptableDuration);
        if (identifiedLimit == null) {
            LOGGER.info("No temporary limit found for the given acceptable duration");
            return (L) this;
        }

        double oldValue = identifiedLimit.getValue();

        if (isTemporaryLimitVaLueValid(lowerEntry, higherEntry, acceptableDuration, temporaryLimitValue)) {
            identifiedLimit.setValue(temporaryLimitValue);
            network.invalidateValidationLevel();
            group.notifyTemporaryLimitValueUpdate(getLimitType(), oldValue, temporaryLimitValue);
            LOGGER.info("Temporary limit value changed from {} to {}", oldValue, temporaryLimitValue);
        } else {
            LOGGER.info("Temporary limit value couldn't be changed because it is not valid");
            return (L) this;
        }

        return (L) this;
    }

    protected boolean isTemporaryLimitVaLueValid(Map.Entry<Integer, TemporaryLimit> lowerEntry,
                                               Map.Entry<Integer, TemporaryLimit> higherEntry,
                                               int acceptableDuration,
                                               double temporaryLimitValue) {

        if (lowerEntry != null && higherEntry != null) {
            return lowerEntry.getValue().getAcceptableDuration() > acceptableDuration
                    && higherEntry.getValue().getAcceptableDuration() < acceptableDuration
                    && lowerEntry.getValue().getValue() < temporaryLimitValue
                    && higherEntry.getValue().getValue() > temporaryLimitValue;
        } else if (lowerEntry == null && higherEntry != null) {
            return temporaryLimitValue > this.permanentLimit &&
                    higherEntry.getValue().getValue() > temporaryLimitValue;
        } else if (lowerEntry != null && higherEntry == null) {
            return lowerEntry.getValue().getAcceptableDuration() > acceptableDuration
                    && lowerEntry.getValue().getValue() < temporaryLimitValue;
        }
        return false;
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

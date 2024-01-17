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

import java.util.Collection;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    AbstractLoadingLimits(OperationalLimitsGroupImpl owner, double permanentLimit, TreeMap<Integer, TemporaryLimit> temporaryLimits) {
        this.group = Objects.requireNonNull(owner);
        this.permanentLimit = permanentLimit;
        this.temporaryLimits = Objects.requireNonNull(temporaryLimits);
        checkLoadingLimits();
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public L setPermanentLimit(double permanentLimit) {
        ValidationUtil.checkPermanentLimit(group.getValidable(), permanentLimit, getTemporaryLimits());
        double oldValue = this.permanentLimit;
        this.permanentLimit = permanentLimit;
        group.notifyUpdateIfDefaultLimits(getLimitType(), "permanentLimit", oldValue, this.permanentLimit);
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

    private void checkTemporaryLimits() {
        // check temporary limits are consistent with permanent
        if (LOGGER.isDebugEnabled()) {
            double previousLimit = Double.NaN;
            boolean wrongOrderMessageAlreadyLogged = false;
            for (LoadingLimits.TemporaryLimit tl : temporaryLimits.values()) { // iterate in ascending order
                if (tl.getValue() <= permanentLimit) {
                    LOGGER.debug("{}, temporary limit should be greater than permanent limit", group.getValidable().getMessageHeader());
                }
                if (!wrongOrderMessageAlreadyLogged && !Double.isNaN(previousLimit) && tl.getValue() <= previousLimit) {
                    LOGGER.debug("{} : temporary limits should be in ascending value order", group.getValidable().getMessageHeader());
                    wrongOrderMessageAlreadyLogged = true;
                }
                previousLimit = tl.getValue();
            }
        }
        // check name unicity
        temporaryLimits.values().stream()
                .collect(Collectors.groupingBy(LoadingLimits.TemporaryLimit::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        throw new ValidationException(group.getValidable(), temporaryLimits1.size() + "temporary limits have the same name " + name);
                    }
                });
    }

    protected void checkLoadingLimits() {
        ValidationUtil.checkPermanentLimit(group.getValidable(), permanentLimit, temporaryLimits.values());
        checkTemporaryLimits();
    }
}

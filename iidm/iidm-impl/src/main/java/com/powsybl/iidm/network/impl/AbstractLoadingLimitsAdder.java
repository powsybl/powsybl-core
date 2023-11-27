/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
abstract class AbstractLoadingLimitsAdder<L extends LoadingLimits, A extends LoadingLimitsAdder<L, A>> implements LoadingLimitsAdder<L, A> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadingLimitsAdder.class);

    private static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuration1, acceptableDuration2) -> acceptableDuration2 - acceptableDuration1;

    protected final Validable validable;

    protected double permanentLimit = Double.NaN;

    protected final TreeMap<Integer, LoadingLimits.TemporaryLimit> temporaryLimits = new TreeMap<>(ACCEPTABLE_DURATION_COMPARATOR);

    public class TemporaryLimitAdderImpl<B extends LoadingLimitsAdder<L, B>> implements TemporaryLimitAdder<B> {

        private String name;

        private double value = Double.NaN;

        private Integer acceptableDuration;

        private boolean fictitious = false;

        private boolean ensureNameUnicity = false;

        @Override
        public TemporaryLimitAdder<B> setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> setValue(double value) {
            this.value = value;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> setAcceptableDuration(int acceptableDuration) {
            this.acceptableDuration = acceptableDuration;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> setFictitious(boolean fictitious) {
            this.fictitious = fictitious;
            return this;
        }

        @Override
        public TemporaryLimitAdder<B> ensureNameUnicity() {
            this.ensureNameUnicity = true;
            return this;
        }

        @Override
        public B endTemporaryLimit() {
            if (Double.isNaN(value)) {
                throw new ValidationException(validable, "temporary limit value is not set");
            }
            if (value <= 0) {
                throw new ValidationException(validable, "temporary limit value must be > 0");
            }
            if (acceptableDuration == null) {
                throw new ValidationException(validable, "acceptable duration is not set");
            }
            if (acceptableDuration < 0) {
                throw new ValidationException(validable, "acceptable duration must be >= 0");
            }
            checkAndGetUniqueName();
            temporaryLimits.put(acceptableDuration, new AbstractLoadingLimits.TemporaryLimitImpl(name, value, acceptableDuration, fictitious));
            return (B) AbstractLoadingLimitsAdder.this;
        }

        private void checkAndGetUniqueName() {
            if (name == null) {
                throw new ValidationException(validable, "name is not set");
            }
            if (ensureNameUnicity) {
                int i = 0;
                String uniqueName = name;
                while (i < Integer.MAX_VALUE && nameExists(uniqueName)) {
                    uniqueName = name + "#" + i;
                    i++;
                }
                name = uniqueName;
            }
        }

        private boolean nameExists(String name) {
            return temporaryLimits.values().stream().anyMatch(t -> t.getName().equals(name));
        }
    }

    AbstractLoadingLimitsAdder(Validable validable) {
        this.validable = Objects.requireNonNull(validable);
    }

    @Override
    public A setPermanentLimit(double permanentLimit) {
        this.permanentLimit = permanentLimit;
        return (A) this;
    }

    @Override
    public TemporaryLimitAdder<A> beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl<>();
    }

    @Override
    public double getPermanentLimit() {
        return permanentLimit;
    }

    @Override
    public double getTemporaryLimitValue(int acceptableDuration) {
        return Optional.ofNullable(temporaryLimits.get(acceptableDuration)).map(LoadingLimits.TemporaryLimit::getValue).orElse(Double.NaN);
    }

    @Override
    public boolean hasTemporaryLimits() {
        return !temporaryLimits.isEmpty();
    }

    private void checkTemporaryLimits() {
        // check temporary limits are consistents with permanent
        double previousLimit = Double.NaN;
        for (LoadingLimits.TemporaryLimit tl : temporaryLimits.values()) { // iterate in ascending order
            if (tl.getValue() <= permanentLimit) {
                LOGGER.debug("{}, temporary limit should be greater than permanent limit", validable.getMessageHeader());
            }
            if (Double.isNaN(previousLimit)) {
                previousLimit = tl.getValue();
            } else if (tl.getValue() <= previousLimit) {
                LOGGER.debug("{} : temporary limits should be in ascending value order", validable.getMessageHeader());
            }
        }
        // check name unicity
        temporaryLimits.values().stream()
                .collect(Collectors.groupingBy(LoadingLimits.TemporaryLimit::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        throw new ValidationException(validable, temporaryLimits1.size() + "temporary limits have the same name " + name);
                    }
                });
    }

    protected void checkLoadingLimits() {
        ValidationUtil.checkPermanentLimit(validable, permanentLimit);
        checkTemporaryLimits();
    }
}

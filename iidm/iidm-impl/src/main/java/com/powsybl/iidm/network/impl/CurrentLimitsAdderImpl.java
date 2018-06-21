/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimits.TemporaryLimit;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.impl.CurrentLimitsImpl.TemporaryLimitImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsAdderImpl<S, O extends CurrentLimitsOwner<S>> implements CurrentLimitsAdder {

    private static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuraction1, acceptableDuraction2) -> acceptableDuraction2 - acceptableDuraction1;

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentLimitsAdderImpl.class);

    private final S side;

    private final O owner;

    private double permanentLimit = Double.NaN;

    private final TreeMap<Integer, TemporaryLimit> temporaryLimits = new TreeMap<>(ACCEPTABLE_DURATION_COMPARATOR);

    public class TemporaryLimitAdderImpl implements TemporaryLimitAdder {

        private String name;

        private double value = Double.NaN;

        private Integer acceptableDuration;

        private boolean fictitious = false;

        @Override
        public TemporaryLimitAdder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public TemporaryLimitAdder setValue(double value) {
            this.value = value;
            return this;
        }

        @Override
        public TemporaryLimitAdder setAcceptableDuration(int acceptableDuration) {
            this.acceptableDuration = acceptableDuration;
            return this;
        }

        @Override
        public TemporaryLimitAdder setFictitious(boolean fictitious) {
            this.fictitious = fictitious;
            return this;
        }

        @Override
        public CurrentLimitsAdder endTemporaryLimit() {
            if (Double.isNaN(value)) {
                throw new ValidationException(owner, "temporary limit value is not set");
            }
            if (value <= 0) {
                throw new ValidationException(owner, "temporary limit value must be > 0");
            }
            if (acceptableDuration == null) {
                throw new ValidationException(owner, "acceptable duration is not set");
            }
            if (acceptableDuration < 0) {
                throw new ValidationException(owner, "acceptable duration must be >= 0");
            }
            if (name == null) {
                throw new ValidationException(owner, "name is not set");
            }
            temporaryLimits.put(acceptableDuration, new TemporaryLimitImpl(name, value, acceptableDuration, fictitious));
            return CurrentLimitsAdderImpl.this;
        }

    }

    public CurrentLimitsAdderImpl(S side, O owner) {
        this.side = side;
        this.owner = owner;
    }

    @Override
    public CurrentLimitsAdder setPermanentLimit(double limit) {
        this.permanentLimit = limit;
        return this;
    }

    @Override
    public TemporaryLimitAdder beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl();
    }

    private void checkTemporaryLimits() {
        // check temporary limits are consistents with permanent
        double previousLimit = Double.NaN;
        for (TemporaryLimit tl : temporaryLimits.values()) { // iterate in ascending order
            if (tl.getValue() <= permanentLimit) {
                LOGGER.debug("{}, temporary limit should be greather than permanent limit", owner.getMessageHeader());
            }
            if (Double.isNaN(previousLimit)) {
                previousLimit = tl.getValue();
            } else {
                if (tl.getValue() <= previousLimit) {
                    LOGGER.debug("{} : temporary limits should be in ascending value order", owner.getMessageHeader());
                }
            }
        }
        // check name unicity
        temporaryLimits.values().stream()
                .collect(Collectors.groupingBy(TemporaryLimit::getName))
                .forEach((name, temporaryLimits1) -> {
                    if (temporaryLimits1.size() > 1) {
                        throw new ValidationException(owner, temporaryLimits1.size() + "temporary limits have the same name " + name);
                    }
                });
    }

    @Override
    public CurrentLimits add() {
        ValidationUtil.checkPermanentLimit(owner, permanentLimit);
        checkTemporaryLimits();
        CurrentLimitsImpl limits = new CurrentLimitsImpl(permanentLimit, temporaryLimits, owner);
        owner.setCurrentLimits(side, limits);
        return limits;
    }


}

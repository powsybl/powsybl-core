/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.CurrentLimits;
import eu.itesla_project.iidm.network.CurrentLimits.TemporaryLimit;
import eu.itesla_project.iidm.network.CurrentLimitsAdder;
import eu.itesla_project.iidm.network.impl.CurrentLimitsImpl.TemporaryLimitImpl;
import java.util.Comparator;
import java.util.TreeMap;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CurrentLimitsAdderImpl<SIDE, OWNER extends CurrentLimitsOwner<SIDE> & Validable> implements CurrentLimitsAdder {

    private static final Comparator<Integer> ACCEPTABLE_DURATION_COMPARATOR = (acceptableDuraction1, acceptableDuraction2) -> acceptableDuraction2 - acceptableDuraction1;

    private final SIDE side;

    private final OWNER owner;

    private float permanentLimit = Float.NaN;

    private final TreeMap<Integer, TemporaryLimit> temporaryLimits = new TreeMap<>(ACCEPTABLE_DURATION_COMPARATOR);

    public class TemporaryLimitAdderImpl implements TemporaryLimitAdder {

        private float limit = Float.NaN;

        private Integer acceptableDuration;

        @Override
        public TemporaryLimitAdder setLimit(float limit) {
            this.limit = limit;
            return this;
        }

        @Override
        public TemporaryLimitAdder setAcceptableDuration(int acceptableDuration) {
            this.acceptableDuration = acceptableDuration;
            return this;
        }

        @Override
        public CurrentLimitsAdder endTemporaryLimit() {
            if (Float.isNaN(limit)) {
                throw new ValidationException(owner, "temporary limit is not set");
            }
            if (limit <= 0) {
                throw new ValidationException(owner, "temporary limit must be > 0");
            }
            if (acceptableDuration == null) {
                throw new ValidationException(owner, "acceptable duration is not set");
            }
            if (acceptableDuration < 0) {
                throw new ValidationException(owner, "acceptable duration must be >= 0");
            }
            temporaryLimits.put(acceptableDuration, new TemporaryLimitImpl(limit, acceptableDuration));
            return CurrentLimitsAdderImpl.this;
        }

    }

    public CurrentLimitsAdderImpl(SIDE side, OWNER owner) {
        this.side = side;
        this.owner = owner;
    }

    @Override
    public CurrentLimitsAdder setPermanentLimit(float limit) {
        this.permanentLimit = limit;
        return this;
    }

    @Override
    public TemporaryLimitAdder beginTemporaryLimit() {
        return new TemporaryLimitAdderImpl();
    }

    private void check() {
        // check temporary limits are consistents with permanent
        float previousLimit = Float.NaN;
        for (TemporaryLimit tl : temporaryLimits.values()) { // iterate in ascending order
            if (tl.getLimit() <= permanentLimit) {
                throw new ValidationException(owner, "Temporary limit should be greather than permanent limit");
            }
            if (Float.isNaN(previousLimit)) {
                previousLimit = tl.getLimit();
            } else {
                if (tl.getLimit() <= previousLimit) {
                    throw new ValidationException(owner, "Temporary limit inconsistency");
                }
            }
        }
    }

    @Override
    public CurrentLimits add() {
        if (permanentLimit <= 0) {
            throw new ValidationException(owner, "permanent limit must be > 0");
        }
        CurrentLimitsImpl limits = new CurrentLimitsImpl(permanentLimit, temporaryLimits);
        owner.setCurrentLimits(side, limits);
        return limits;
    }


}

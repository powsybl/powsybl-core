/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.dsl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.security.DefaultLimitViolationDetector;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link com.powsybl.security.LimitViolationDetector LimitViolationDetector} which detects violations based on
 * an underlying definition of {@link LimitFactors} to be applied to current limits.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LimitViolationDetectorWithFactors extends DefaultLimitViolationDetector {

    private final LimitFactors factors;

    public LimitViolationDetectorWithFactors(LimitFactors factors) {
        this.factors = Objects.requireNonNull(factors);
    }

    @Override
    public void checkCurrent(Contingency contingency, Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {

        CurrentLimits limits = branch.getCurrentLimits(side);
        if (limits == null || Double.isNaN(currentValue)) {
            return;
        }

        //Iterate current limits with ascending duration
        for (CurrentLimits.TemporaryLimit tl : Lists.reverse(ImmutableList.copyOf(limits.getTemporaryLimits()))) {
            LimitViolation violation = checkTemporaryLimit(contingency, branch, side, tl, currentValue);
            if (violation != null) {
                consumer.accept(violation);
                return;
            }
        }

        if (Double.isNaN(limits.getPermanentLimit())) {
            return;
        }

        LimitViolation violation = checkPermanentLimit(contingency, branch, side, limits, currentValue);
        if (violation != null) {
            consumer.accept(violation);
        }
    }

    private LimitViolation checkTemporaryLimit(Contingency contingency, Branch branch,
                                               Branch.Side side, CurrentLimits.TemporaryLimit tl, double currentValue) {

        float factor = factors.getFactor(contingency, branch, side, tl).orElse(1f);

        if (currentValue < factor * tl.getValue()) {
            return null;
        }

        return new LimitViolation(branch.getId(),
                LimitViolationType.CURRENT,
                tl.getName(),
                tl.getAcceptableDuration(),
                tl.getValue(),
                factor,
                currentValue,
                side);
    }

    private LimitViolation checkPermanentLimit(Contingency contingency, Branch branch,
                                               Branch.Side side, CurrentLimits limits, double currentValue) {

        float factor = factors.getFactor(contingency, branch, side, null).orElse(1f);
        if (currentValue < limits.getPermanentLimit() * factor) {
            return null;
        }
        return new LimitViolation(branch.getId(),
                LimitViolationType.CURRENT,
                null,
                Integer.MAX_VALUE,
                limits.getPermanentLimit(),
                factor,
                currentValue,
                side);
    }

}

/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.contingency.violations.LimitViolation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
abstract class AbstractFaultResult extends AbstractExtendable<FaultResult> implements FaultResult {

    private final Status status;

    private final Fault fault;

    private final double shortCircuitPower;

    private final Duration timeConstant;

    private final List<FeederResult> feederResults;

    private final List<LimitViolation> limitViolations;

    private final List<ShortCircuitBusResults> shortCircuitBusResults;

    private final double equivalentR;

    private final double equivalentX;

    protected AbstractFaultResult(Fault fault, Status status, double shortCircuitPower, Duration timeConstant, List<FeederResult> feederResults,
                               List<LimitViolation> limitViolations, List<ShortCircuitBusResults> shortCircuitBusResults, double equivalentR, double equivalentX) {
        this.fault = Objects.requireNonNull(fault);
        this.shortCircuitPower = shortCircuitPower;
        this.limitViolations = new ArrayList<>();
        if (limitViolations != null) {
            this.limitViolations.addAll(limitViolations);
        }
        this.timeConstant = timeConstant;
        this.shortCircuitBusResults = new ArrayList<>();
        if (shortCircuitBusResults != null) {
            this.shortCircuitBusResults.addAll(shortCircuitBusResults);
        }
        this.feederResults = new ArrayList<>();
        if (feederResults != null) {
            this.feederResults.addAll(feederResults);
        }
        this.status = Objects.requireNonNull(status);
        this.equivalentR = equivalentR;
        this.equivalentX = equivalentX;
    }

    @Override
    public Fault getFault() {
        return fault;
    }

    @Override
    public double getShortCircuitPower() {
        return shortCircuitPower;
    }

    @Override
    public List<FeederResult> getFeederResults() {
        return feederResults;
    }

    @Override
    public List<LimitViolation> getLimitViolations() {
        return limitViolations;
    }

    @Override
    public Duration getTimeConstant() {
        return timeConstant;
    }

    @Override
    public List<ShortCircuitBusResults> getShortCircuitBusResults() {
        return shortCircuitBusResults;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public double getFeederCurrent(String feederId) {
        for (FeederResult feederResult : feederResults) {
            if (feederResult.getConnectableId().equals(feederId)) {
                if (feederResult instanceof FortescueFeederResult fortescueFeederResult) {
                    return fortescueFeederResult.getCurrent().getPositiveMagnitude();
                } else {
                    return ((MagnitudeFaultResult) feederResult).getCurrent();
                }
            }
        }
        return Double.NaN;
    }

    @Override
    public double getEquivalentR() {
        return equivalentR;
    }

    @Override
    public double getEquivalentX() {
        return equivalentX;
    }
}

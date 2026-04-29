/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.contingency.violations.LoadingLimitType;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.security.*;
import com.powsybl.security.limitreduction.SimpleLimitsComputer;

import java.util.*;
import java.util.function.Consumer;

import static com.powsybl.security.LimitViolationDetection.createViolationLocation;

/**
 * Implements the default behaviour for limit violation detection.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class DefaultLimitViolationDetector extends AbstractContingencyBlindDetector {

    private final double limitReductionValue;
    private final Set<LoadingLimitType> currentLimitTypes;
    private final LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer;

    public DefaultLimitViolationDetector(double limitReductionValue, Collection<LoadingLimitType> currentLimitTypes) {
        if (limitReductionValue <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReductionValue);
        }
        this.limitReductionValue = limitReductionValue;
        limitsComputer = new SimpleLimitsComputer(limitReductionValue);
        this.currentLimitTypes = EnumSet.copyOf(Objects.requireNonNull(currentLimitTypes));
    }

    public DefaultLimitViolationDetector(Collection<LoadingLimitType> currentLimitTypes) {
        this(1.0f, currentLimitTypes);
    }

    public DefaultLimitViolationDetector() {
        this(EnumSet.allOf(LoadingLimitType.class));
    }

    @Override
    public void checkCurrent(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(branch, side, value, consumer, LimitType.CURRENT);
    }

    @Override
    public void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(transformer, side, value, consumer, LimitType.CURRENT);
    }

    @Override
    public void checkActivePower(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(branch, side, value, consumer, LimitType.ACTIVE_POWER);
    }

    @Override
    public void checkActivePower(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        checkLimitViolation(transformer, side, value, consumer, LimitType.ACTIVE_POWER);
    }

    @Override
    public void checkApparentPower(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer) {

        checkLimitViolation(branch, side, value, consumer, LimitType.APPARENT_POWER);
    }

    @Override
    public void checkApparentPower(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer) {
        checkLimitViolation(transformer, side, value, consumer, LimitType.APPARENT_POWER);
    }

    @Override
    public void checkVoltage(Bus bus, double value, Consumer<LimitViolation> consumer) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(
                LimitViolation.builder()
                    .subject(vl.getId())
                    .subjectName(vl.getOptionalName().orElse(null))
                    .type(LimitViolationType.LOW_VOLTAGE)
                    .limit(vl.getLowVoltageLimit())
                    .reduction(limitReductionValue)
                    .value(value)
                    .violationLocation(createViolationLocation(bus))
                    .build()
            );
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(
                LimitViolation.builder()
                    .subject(vl.getId())
                    .subjectName(vl.getOptionalName().orElse(null))
                    .type(LimitViolationType.HIGH_VOLTAGE)
                    .limit(vl.getHighVoltageLimit())
                    .reduction(limitReductionValue)
                    .value(value)
                    .violationLocation(createViolationLocation(bus))
                    .build()
            );
        }
    }

    @Override
    public void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double value, Consumer<LimitViolation> consumer) {
        if (Double.isNaN(value)) {
            return;
        }
        voltageAngleLimit.getLowLimit().ifPresent(
            lowLimit -> {
                if (value <= lowLimit) {
                    consumer.accept(
                        LimitViolation.builder()
                            .subject(voltageAngleLimit.getId())
                            .type(LimitViolationType.LOW_VOLTAGE_ANGLE)
                            .limit(lowLimit)
                            .reduction(limitReductionValue)
                            .value(value)
                            .build()
                    );
                }
            });
        voltageAngleLimit.getHighLimit().ifPresent(
            highLimit -> {
                if (value >= highLimit) {
                    consumer.accept(
                        LimitViolation.builder()
                            .subject(voltageAngleLimit.getId())
                            .type(LimitViolationType.HIGH_VOLTAGE_ANGLE)
                            .limit(highLimit)
                            .reduction(limitReductionValue)
                            .value(value)
                            .build()
                    );
                }
            });
    }

    public void checkLimitViolation(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        LimitViolationDetection.checkLimitViolation(branch, side, value, type, currentLimitTypes, limitsComputer, consumer);
    }

    public void checkLimitViolation(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        LimitViolationDetection.checkLimitViolation(transformer, side, value, type, currentLimitTypes, limitsComputer, consumer);
    }
}

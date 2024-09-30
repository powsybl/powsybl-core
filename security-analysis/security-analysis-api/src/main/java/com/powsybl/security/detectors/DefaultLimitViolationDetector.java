/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.security.*;

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

    public DefaultLimitViolationDetector(double limitReductionValue, Collection<LoadingLimitType> currentLimitTypes) {
        if (limitReductionValue <= 0) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReductionValue);
        }
        this.limitReductionValue = limitReductionValue;
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
        ViolationLocation voltageViolationLocation = createViolationLocation(bus);
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.LOW_VOLTAGE,
                    vl.getLowVoltageLimit(), limitReductionValue, value, voltageViolationLocation));
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.HIGH_VOLTAGE,
                    vl.getHighVoltageLimit(), limitReductionValue, value, voltageViolationLocation));
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
                    consumer.accept(new LimitViolation(voltageAngleLimit.getId(), LimitViolationType.LOW_VOLTAGE_ANGLE, lowLimit,
                            limitReductionValue, value));
                }
            });
        voltageAngleLimit.getHighLimit().ifPresent(
            highLimit -> {
                if (value >= highLimit) {
                    consumer.accept(new LimitViolation(voltageAngleLimit.getId(), LimitViolationType.HIGH_VOLTAGE_ANGLE, highLimit,
                            limitReductionValue, value));
                }
            });
    }

    public void checkLimitViolation(Branch branch, TwoSides side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        boolean noOverloadOnTemporary = true;
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            noOverloadOnTemporary = checkTemporary(branch, side, limitReductionValue, value, consumer, type);
        }
        if (noOverloadOnTemporary && currentLimitTypes.contains(LoadingLimitType.PATL)) {
            checkPermanentLimit(branch, side, limitReductionValue, value, consumer, type);
        }
    }

    public void checkLimitViolation(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        boolean noOverloadOnTemporary = true;
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            noOverloadOnTemporary = checkTemporary(transformer, side, limitReductionValue, value, consumer, type);
        }
        if (noOverloadOnTemporary && currentLimitTypes.contains(LoadingLimitType.PATL)) {
            checkPermanentLimit(transformer, side, limitReductionValue, value, consumer, type);
        }
    }
}

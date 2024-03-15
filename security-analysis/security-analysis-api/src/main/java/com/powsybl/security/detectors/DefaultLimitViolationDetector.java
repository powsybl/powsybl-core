/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Implements the default behaviour for limit violation detection.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class DefaultLimitViolationDetector extends AbstractContingencyBlindDetector {

    private final float limitReductionValue;
    private final Set<LoadingLimitType> currentLimitTypes;

    public DefaultLimitViolationDetector(float limitReductionValue, Collection<LoadingLimitType> currentLimitTypes) {
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
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.LOW_VOLTAGE,
                    vl.getLowVoltageLimit(), limitReductionValue, value));
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.HIGH_VOLTAGE,
                    vl.getHighVoltageLimit(), limitReductionValue, value));
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
        Overload overload = LimitViolationUtils.checkTemporaryLimits(branch, side, limitReductionValue, value, type);
        if (currentLimitTypes.contains(LoadingLimitType.TATL) && overload != null) {
            checkTemporary(branch, side, limitReductionValue, value, consumer, type);
        } else if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            checkPermanentLimit(branch, side, limitReductionValue, value, consumer, type);
        }
    }

    public void checkLimitViolation(ThreeWindingsTransformer transformer, ThreeSides side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        Overload overload = LimitViolationUtils.checkTemporaryLimits(transformer, side, limitReductionValue, value, type);
        if (currentLimitTypes.contains(LoadingLimitType.TATL) && overload != null) {
            checkTemporary(transformer, side, limitReductionValue, value, consumer, type);
        } else if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            checkPermanentLimit(transformer, side, limitReductionValue, value, consumer, type);
        }
    }
}

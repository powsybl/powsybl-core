/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.security.detectors.LoadingLimitType;

import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LimitViolationDetection {
    //TODO rename this class?

    private LimitViolationDetection() {
    }

    public static void checkAllViolations(Network network, Consumer<LimitViolation> consumer,
                                          Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        network.getBranchStream().forEach(b -> checkCurrent(b, consumer, currentLimitTypes, limitReductionValue));
        network.getThreeWindingsTransformerStream().forEach(t -> checkCurrent(t, consumer, currentLimitTypes, limitReductionValue));
        network.getVoltageLevelStream()
                .flatMap(vl -> vl.getBusView().getBusStream())
                .forEach(b -> checkVoltage(b, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(valOk, consumer));
    }

    public static void checkCurrent(Branch<?> branch, Consumer<LimitViolation> consumer,
                                    Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        checkCurrent(branch, TwoSides.ONE, consumer, currentLimitTypes, limitReductionValue);
        checkCurrent(branch, TwoSides.TWO, consumer, currentLimitTypes, limitReductionValue);
    }

    public static void checkCurrent(Branch<?> branch, TwoSides side, Consumer<LimitViolation> consumer,
                                    Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        checkLimitViolation(branch, side, branch.getTerminal(side).getI(), consumer, LimitType.CURRENT,
                currentLimitTypes, limitReductionValue);
    }

    public static void checkLimitViolation(Branch<?> branch, TwoSides side, double value, Consumer<LimitViolation> consumer,
                                           LimitType type, Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        boolean overloadOnTemporary = false;
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            Overload overload = LimitViolationUtils.checkTemporaryLimits(branch, side, limitReductionValue, value, type);
            if (overload != null) {
                consumer.accept(new LimitViolation(branch.getId(),
                        branch.getOptionalName().orElse("null"),
                        toLimitViolationType(type),
                        overload.getPreviousLimitName(),
                        overload.getTemporaryLimit().getAcceptableDuration(),
                        overload.getPreviousLimit(),
                        limitReductionValue,
                        value,
                        side));
                overloadOnTemporary = true;
            }
        }
        if (!overloadOnTemporary && currentLimitTypes.contains(LoadingLimitType.PATL)
                && LimitViolationUtils.checkPermanentLimit(branch, side, limitReductionValue, value, type)) {
            double limit = branch.getLimits(type, side).map(LoadingLimits::getPermanentLimit).orElseThrow(PowsyblException::new);
            consumer.accept(new LimitViolation(branch.getId(),
                    branch.getOptionalName().orElse(null),
                    toLimitViolationType(type),
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    Integer.MAX_VALUE,
                    limit,
                    limitReductionValue,
                    value,
                    side));
        }
    }

    public static void checkCurrent(ThreeWindingsTransformer transformer, Consumer<LimitViolation> consumer,
                                    Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        checkCurrent(transformer, ThreeSides.ONE, consumer, currentLimitTypes, limitReductionValue);
        checkCurrent(transformer, ThreeSides.TWO, consumer, currentLimitTypes, limitReductionValue);
        checkCurrent(transformer, ThreeSides.THREE, consumer, currentLimitTypes, limitReductionValue);
    }

    public static void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side, Consumer<LimitViolation> consumer,
                                    Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        checkLimitViolation(transformer, side, transformer.getTerminal(side).getI(), consumer, LimitType.CURRENT,
                currentLimitTypes, limitReductionValue);
    }

    public static void checkLimitViolation(ThreeWindingsTransformer transformer, ThreeSides side, double value,
                                           Consumer<LimitViolation> consumer, LimitType type,
                                           Set<LoadingLimitType> currentLimitTypes, double limitReductionValue) {
        boolean overloadOnTemporary = false;
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            Overload overload = LimitViolationUtils.checkTemporaryLimits(transformer, side, limitReductionValue, value, type);
            if (overload != null) {
                consumer.accept(new LimitViolation(transformer.getId(),
                        transformer.getOptionalName().orElse(null),
                        toLimitViolationType(type),
                        overload.getPreviousLimitName(),
                        overload.getTemporaryLimit().getAcceptableDuration(),
                        overload.getPreviousLimit(),
                        limitReductionValue,
                        value,
                        side));
                overloadOnTemporary = true;
            }
        }
        if (!overloadOnTemporary && currentLimitTypes.contains(LoadingLimitType.PATL)
                && LimitViolationUtils.checkPermanentLimit(transformer, side, limitReductionValue, value, type)) {
            double limit = transformer.getLeg(side).getLimits(type).map(LoadingLimits::getPermanentLimit).orElseThrow(PowsyblException::new);
            consumer.accept(new LimitViolation(transformer.getId(),
                    transformer.getOptionalName().orElse(null),
                    toLimitViolationType(type),
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    Integer.MAX_VALUE,
                    limit,
                    limitReductionValue,
                    value,
                    side));
        }
    }

    public static void checkVoltage(Bus bus, Consumer<LimitViolation> consumer) {
        double value = bus.getV();
        checkVoltage(bus, value, consumer);
    }

    public static void checkVoltage(Bus bus, double value, Consumer<LimitViolation> consumer) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.LOW_VOLTAGE,
                    vl.getLowVoltageLimit(), 1., value));
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.HIGH_VOLTAGE,
                    vl.getHighVoltageLimit(), 1., value));
        }
    }

    public static LimitViolationType toLimitViolationType(LimitType type) {
        return switch (type) {
            case ACTIVE_POWER -> LimitViolationType.ACTIVE_POWER;
            case APPARENT_POWER -> LimitViolationType.APPARENT_POWER;
            case CURRENT -> LimitViolationType.CURRENT;
            default ->
                    throw new UnsupportedOperationException(String.format("Unsupported conversion for %s from limit type to limit violation type.", type.name()));
        };
    }

    public static void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, Consumer<LimitViolation> consumer) {
        Bus referenceBus = voltageAngleLimit.getTerminalFrom().getBusView().getBus();
        Bus otherBus = voltageAngleLimit.getTerminalTo().getBusView().getBus();
        if (referenceBus != null && otherBus != null
                && referenceBus.getConnectedComponent().getNum() == otherBus.getConnectedComponent().getNum()
                && referenceBus.getSynchronousComponent().getNum() == otherBus.getSynchronousComponent().getNum()) {
            double voltageAngleDifference = otherBus.getAngle() - referenceBus.getAngle();
            checkVoltageAngle(voltageAngleLimit, voltageAngleDifference, consumer);
        }
    }

    public static void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double value, Consumer<LimitViolation> consumer) {
        if (Double.isNaN(value)) {
            return;
        }
        voltageAngleLimit.getLowLimit().ifPresent(
                lowLimit -> {
                    if (value <= lowLimit) {
                        consumer.accept(new LimitViolation(voltageAngleLimit.getId(), LimitViolationType.LOW_VOLTAGE_ANGLE, lowLimit,
                                1., value));
                    }
                });
        voltageAngleLimit.getHighLimit().ifPresent(
                highLimit -> {
                    if (value >= highLimit) {
                        consumer.accept(new LimitViolation(voltageAngleLimit.getId(), LimitViolationType.HIGH_VOLTAGE_ANGLE, highLimit,
                                1., value));
                    }
                });
    }
}

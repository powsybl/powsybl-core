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
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.network.util.PermanentLimitCheckResult;
import com.powsybl.security.detectors.LoadingLimitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class LimitViolationDetection {

    private static final Logger LOGGER = LoggerFactory.getLogger(LimitViolationDetection.class);

    private LimitViolationDetection() {
    }

    /**
     * Checks whether the current and voltage values on all equipments
     * of the specified {@link Network} should be considered as {@link LimitViolation}s.
     * In case it should, feeds the consumer with it.
     *
     * @param network           The network on which physical values must be checked.
     * @param currentLimitTypes The current limit type to consider.
     * @param limitsComputer    The computer of the limit reductions to apply.
     * @param consumer          Will be fed with possibly created limit violations.
     */
    public static void checkAll(Network network, Set<LoadingLimitType> currentLimitTypes,
                                LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrent(b, currentLimitTypes, limitsComputer, consumer));
        network.getThreeWindingsTransformerStream().forEach(t -> checkCurrent(t, currentLimitTypes, limitsComputer, consumer));
        network.getVoltageLevelStream()
                .flatMap(vl -> vl.getBusView().getBusStream())
                .forEach(b -> checkVoltage(b, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(valOk, consumer));
    }

    /**
     * Checks whether the current and voltage values on all equipments
     * of the specified {@link Network} should be considered as {@link LimitViolation}s.
     * In case it should, feeds the consumer with it.
     * In this DC power flow mode, the current is computed using the DC power factor if necessary.
     *
     * @param network             The network on which physical values must be checked.
     * @param dcPowerFactor       The DC power factor used to convert the active power into current.
     * @param currentLimitTypes   The current limit type to consider.
     * @param limitsComputer      The computer of the limit reductions to apply.
     * @param consumer            Will be fed with possibly created limit violations.
     */
    public static void checkAllDc(Network network, double dcPowerFactor, Set<LoadingLimitType> currentLimitTypes,
                                  LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, currentLimitTypes, limitsComputer, consumer));
        network.getThreeWindingsTransformerStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, currentLimitTypes, limitsComputer, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(valOk, consumer));
    }

    private static void checkCurrent(Branch<?> branch, Set<LoadingLimitType> currentLimitTypes,
                                     LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, TwoSides.ONE, currentLimitTypes, limitsComputer, consumer);
        checkCurrent(branch, TwoSides.TWO, currentLimitTypes, limitsComputer, consumer);
    }

    private static void checkCurrent(Branch<?> branch, TwoSides side, Set<LoadingLimitType> currentLimitTypes,
                                     LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        checkLimitViolation(branch, side, branch.getTerminal(side).getI(), LimitType.CURRENT, currentLimitTypes, limitsComputer, consumer);
    }

    private static void checkCurrentDc(Branch<?> branch, double dcPowerFactor, Set<LoadingLimitType> currentLimitTypes,
                                       LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        checkCurrentDc(branch, TwoSides.ONE, dcPowerFactor, currentLimitTypes, limitsComputer, consumer);
        checkCurrentDc(branch, TwoSides.TWO, dcPowerFactor, currentLimitTypes, limitsComputer, consumer);
    }

    private static void checkCurrentDc(Branch<?> branch, TwoSides side, double dcPowerFactor,
                                       Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                       Consumer<LimitViolation> consumer) {
        double i = getTerminalIOrAnApproximation(branch.getTerminal(side), dcPowerFactor);
        checkLimitViolation(branch, side, i, LimitType.CURRENT, currentLimitTypes, limitsComputer, consumer);
    }

    public static double getTerminalIOrAnApproximation(Terminal terminal, double dcPowerFactor) {
        // After a DC load flow, the current at terminal can be undefined (NaN). In that case, we use the DC power factor,
        // the nominal voltage and the active power at terminal in order to approximate the current following formula
        // P = sqrt(3) x Vnom x I x dcPowerFactor
        return Double.isNaN(terminal.getI()) ?
                (1000. * terminal.getP()) / (terminal.getVoltageLevel().getNominalV() * Math.sqrt(3) * dcPowerFactor)
                : terminal.getI();
    }

    static void checkLimitViolation(Branch<?> branch, TwoSides side, double value, LimitType type,
                                    Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                    Consumer<LimitViolation> consumer) {
        boolean overloadOnTemporary = false;
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            Overload overload = LimitViolationUtils.checkTemporaryLimits(branch, side, limitsComputer, value, type);
            if (overload != null) {
                consumer.accept(new LimitViolation(branch.getId(),
                        branch.getOptionalName().orElse("null"),
                        toLimitViolationType(type),
                        overload.getPreviousLimitName(),
                        overload.getTemporaryLimit().getAcceptableDuration(),
                        overload.getPreviousLimit(),
                        overload.getLimitReductionCoefficient(),
                        value,
                        side));
                overloadOnTemporary = true;
            }
        }
        if (!overloadOnTemporary && currentLimitTypes.contains(LoadingLimitType.PATL)) {
            PermanentLimitCheckResult overload = LimitViolationUtils.checkPermanentLimit(branch, side, value, type, limitsComputer);
            if (overload.isOverload()) {
                double limit = branch.getLimits(type, side).map(LoadingLimits::getPermanentLimit).orElseThrow(PowsyblException::new);
                consumer.accept(new LimitViolation(branch.getId(),
                    branch.getOptionalName().orElse(null),
                    toLimitViolationType(type),
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    Integer.MAX_VALUE,
                    limit,
                    overload.limitReductionValue(),
                    value,
                    side));
            }
        }
    }

    private static void checkCurrent(ThreeWindingsTransformer transformer, Set<LoadingLimitType> currentLimitTypes,
                                     LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        checkCurrent(transformer, ThreeSides.ONE, currentLimitTypes, limitsComputer, consumer);
        checkCurrent(transformer, ThreeSides.TWO, currentLimitTypes, limitsComputer, consumer);
        checkCurrent(transformer, ThreeSides.THREE, currentLimitTypes, limitsComputer, consumer);
    }

    private static void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side,
                                     Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                     Consumer<LimitViolation> consumer) {
        checkLimitViolation(transformer, side, transformer.getTerminal(side).getI(), LimitType.CURRENT, currentLimitTypes, limitsComputer, consumer
        );
    }

    private static void checkCurrentDc(ThreeWindingsTransformer transformer, double dcPowerFactor,
                                       Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                       Consumer<LimitViolation> consumer) {
        checkCurrentDc(transformer, ThreeSides.ONE, dcPowerFactor, currentLimitTypes, limitsComputer, consumer);
        checkCurrentDc(transformer, ThreeSides.TWO, dcPowerFactor, currentLimitTypes, limitsComputer, consumer);
        checkCurrentDc(transformer, ThreeSides.THREE, dcPowerFactor, currentLimitTypes, limitsComputer, consumer);
    }

    private static void checkCurrentDc(ThreeWindingsTransformer transformer, ThreeSides side, double dcPowerFactor,
                                       Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                       Consumer<LimitViolation> consumer) {
        double i = getTerminalIOrAnApproximation(transformer.getTerminal(side), dcPowerFactor);
        checkLimitViolation(transformer, side, i, LimitType.CURRENT, currentLimitTypes, limitsComputer, consumer);
    }

    static void checkLimitViolation(ThreeWindingsTransformer transformer, ThreeSides side, double value,
                                    LimitType type, Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                    Consumer<LimitViolation> consumer) {
        boolean overloadOnTemporary = false;
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            Overload overload = LimitViolationUtils.checkTemporaryLimits(transformer, side, limitsComputer, value, type);
            if (overload != null) {
                consumer.accept(new LimitViolation(transformer.getId(),
                        transformer.getOptionalName().orElse(null),
                        toLimitViolationType(type),
                        overload.getPreviousLimitName(),
                        overload.getTemporaryLimit().getAcceptableDuration(),
                        overload.getPreviousLimit(),
                        overload.getLimitReductionCoefficient(),
                        value,
                        side));
                overloadOnTemporary = true;
            }
        }
        if (!overloadOnTemporary && currentLimitTypes.contains(LoadingLimitType.PATL)) {
            PermanentLimitCheckResult overload = LimitViolationUtils.checkPermanentLimit(transformer, side, limitsComputer, value, type);
            if (overload.isOverload()) {
                double limit = transformer.getLeg(side).getLimits(type).map(LoadingLimits::getPermanentLimit).orElseThrow(PowsyblException::new);
                consumer.accept(new LimitViolation(transformer.getId(),
                    transformer.getOptionalName().orElse(null),
                    toLimitViolationType(type),
                    LimitViolationUtils.PERMANENT_LIMIT_NAME,
                    Integer.MAX_VALUE,
                    limit,
                    overload.limitReductionValue(),
                    value,
                    side));
            }
        }
    }

    private static void checkVoltage(Bus bus, Consumer<LimitViolation> consumer) {
        double value = bus.getV();
        checkVoltage(bus, value, consumer);
    }

    static void checkVoltage(Bus bus, double value, Consumer<LimitViolation> consumer) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.LOW_VOLTAGE,
                    vl.getLowVoltageLimit(), 1., value, createViolationLocation(bus)));
        }

        if (!Double.isNaN(vl.getHighVoltageLimit()) && value >= vl.getHighVoltageLimit()) {
            consumer.accept(new LimitViolation(vl.getId(), vl.getOptionalName().orElse(null), LimitViolationType.HIGH_VOLTAGE,
                    vl.getHighVoltageLimit(), 1., value, createViolationLocation(bus)));
        }
    }

    /**
     * Helper function to convert a limit type to a limit violation type
     *
     * @param type The limit type to convert.
     * @return The matching LimitViolationTYpe
     */
    public static LimitViolationType toLimitViolationType(LimitType type) {
        return switch (type) {
            case ACTIVE_POWER -> LimitViolationType.ACTIVE_POWER;
            case APPARENT_POWER -> LimitViolationType.APPARENT_POWER;
            case CURRENT -> LimitViolationType.CURRENT;
            default ->
                    throw new UnsupportedOperationException(String.format("Unsupported conversion for %s from limit type to limit violation type.", type.name()));
        };
    }

    private static void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, Consumer<LimitViolation> consumer) {
        Bus referenceBus = voltageAngleLimit.getTerminalFrom().getBusView().getBus();
        Bus otherBus = voltageAngleLimit.getTerminalTo().getBusView().getBus();
        if (referenceBus != null && otherBus != null
                && referenceBus.getConnectedComponent().getNum() == otherBus.getConnectedComponent().getNum()
                && referenceBus.getSynchronousComponent().getNum() == otherBus.getSynchronousComponent().getNum()) {
            double voltageAngleDifference = otherBus.getAngle() - referenceBus.getAngle();
            checkVoltageAngle(voltageAngleLimit, voltageAngleDifference, consumer);
        }
    }

    static void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double value, Consumer<LimitViolation> consumer) {
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

    public static ViolationLocation createViolationLocation(Bus bus) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (vl.getTopologyKind() == TopologyKind.NODE_BREAKER) {
            List<Integer> nodes = Networks.getNodesByBus(vl).get(bus.getId()).stream().toList();
            return new NodeBreakerViolationLocation(vl.getId(), nodes);
        } else {
            try {
                List<String> configuredBusIds = vl.getBusBreakerView().getBusStreamFromBusViewBusId(bus.getId())
                        .map(Identifiable::getId)
                        .sorted().toList();
                return new BusBreakerViolationLocation(configuredBusIds);
            } catch (Exception e) {
                LOGGER.error("Error generating ViolationLocation", e);
                return null;
            }
        }
    }
}

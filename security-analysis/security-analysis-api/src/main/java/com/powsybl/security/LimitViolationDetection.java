/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.contingency.violations.*;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.limitmodification.LimitsComputer;
import com.powsybl.iidm.network.limitmodification.result.LimitsContainer;
import com.powsybl.iidm.network.util.LimitViolationUtils;
import com.powsybl.iidm.network.util.Networks;
import com.powsybl.iidm.network.util.PermanentLimitCheckResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
     * of the specified {@link Network} should be considered as {@link com.powsybl.contingency.violations.LimitViolation}s.
     * In case it should, feeds the consumer with it.
     *
     * @param network           The network on which physical values must be checked.
     * @param currentLimitTypes The current limit type to consider.
     * @param limitsComputer    The computer of the limit scalings to apply.
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
        checkAllDetailedDcEquipment(network, currentLimitTypes, consumer);
    }

    /**
     * Checks whether the current and voltage values on all equipments
     * of the specified {@link Network} should be considered as {@link com.powsybl.contingency.violations.LimitViolation}s.
     * In case it should, feeds the consumer with it.
     * In this DC power flow mode, the current is computed using the DC power factor if necessary.
     *
     * @param network             The network on which physical values must be checked.
     * @param dcPowerFactor       The DC power factor used to convert the active power into current.
     * @param currentLimitTypes   The current limit type to consider.
     * @param limitsComputer      The computer of the limit scalings to apply.
     * @param consumer            Will be fed with possibly created limit violations.
     */
    public static void checkAllDc(Network network, double dcPowerFactor, Set<LoadingLimitType> currentLimitTypes,
                                  LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, currentLimitTypes, limitsComputer, consumer));
        network.getThreeWindingsTransformerStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, currentLimitTypes, limitsComputer, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(valOk, consumer));
        // The DC power factor is not used for detailed DC equipment, so the detection is the same after an AC LF and a DC (linearized) LF.
        checkAllDetailedDcEquipment(network, currentLimitTypes, consumer);
    }

    /**
     * Checks whether the values on the detailed DC equipments (DC nodes, DC lines and AC/DC converters)
     * of the specified {@link Network} should be considered as {@link com.powsybl.contingency.violations.LimitViolation}s.
     * In case it should, feeds the consumer with it.
     *
     * @param network           The network on which physical values must be checked.
     * @param currentLimitTypes The current limit type to consider.
     * @param consumer          Will be fed with possibly created limit violations.
     */
    public static void checkAllDetailedDcEquipment(Network network, Set<LoadingLimitType> currentLimitTypes, Consumer<LimitViolation> consumer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(currentLimitTypes);
        Objects.requireNonNull(consumer);
        network.getDcNodeStream().forEach(n -> checkVoltage(n, consumer));
        if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            network.getDcLineStream().forEach(l -> checkCurrent(l, consumer));
        }
        network.getVoltageSourceConverterStream().forEach(c -> checkActivePower(c, consumer));
        network.getLineCommutatedConverterStream().forEach(c -> checkActivePower(c, consumer));
    }

    /**
     * Checks the voltage magnitude of a DC node against its low and high voltage limits.
     * Limits are never scaled, and the signed voltage is reported.
     */
    private static void checkVoltage(DcNode dcNode, Consumer<LimitViolation> consumer) {
        double v = dcNode.getV();
        if (Double.isNaN(v)) {
            return;
        }
        double magnitude = Math.abs(v);
        double lowLimit = dcNode.getLowVoltageLimit();
        if (!Double.isNaN(lowLimit) && magnitude <= lowLimit) {
            consumer.accept(createDetailedDcEquipmentViolation(dcNode, LimitViolationType.LOW_VOLTAGE, lowLimit, v, null));
        }
        double highLimit = dcNode.getHighVoltageLimit();
        if (!Double.isNaN(highLimit) && magnitude >= highLimit) {
            consumer.accept(createDetailedDcEquipmentViolation(dcNode, LimitViolationType.HIGH_VOLTAGE, highLimit, v, null));
        }
    }

    /**
     * Checks the current magnitude at terminal 1 of a DC line against its permanent current limit.
     * A DC line has no shunt admittance, so checking terminal 1 is enough. The limit is never scaled,
     * and the signed current is reported.
     */
    private static void checkCurrent(DcLine dcLine, Consumer<LimitViolation> consumer) {
        double i = dcLine.getDcTerminal1().getI();
        double limit = dcLine.getPermanentCurrentLimit();
        if (!Double.isNaN(i) && !Double.isNaN(limit) && Math.abs(i) >= limit) {
            consumer.accept(createDetailedDcEquipmentViolation(dcLine, LimitViolationType.CURRENT, limit, i, ThreeSides.ONE));
        }
    }

    /**
     * Checks the active power of an AC/DC converter against its minP and maxP bounds.
     * Bounds are never scaled, and bounds left at their default values never produce a violation.
     */
    private static void checkActivePower(AcDcConverter<?> converter, Consumer<LimitViolation> consumer) {
        double p = getConverterActivePower(converter);
        if (Double.isNaN(p)) {
            return;
        }
        double maxP = converter.getMaxP();
        if (maxP != Double.MAX_VALUE && p >= maxP) {
            consumer.accept(createDetailedDcEquipmentViolation(converter, LimitViolationType.ACTIVE_POWER, maxP, p, ThreeSides.ONE));
        }
        double minP = converter.getMinP();
        if (minP != -Double.MAX_VALUE && p <= minP) {
            consumer.accept(createDetailedDcEquipmentViolation(converter, LimitViolationType.ACTIVE_POWER, minP, p, ThreeSides.ONE));
        }
    }

    /**
     * @return the active power at the PCC terminal of the converter if any, otherwise the sum of the active powers
     * at its AC terminals.
     */
    private static double getConverterActivePower(AcDcConverter<?> converter) {
        Terminal pccTerminal = converter.getPccTerminal();
        if (pccTerminal != null) {
            return pccTerminal.getP();
        }
        return converter.getTerminal1().getP() + converter.getTerminal2().map(Terminal::getP).orElse(0.);
    }

    private static LimitViolation createDetailedDcEquipmentViolation(Identifiable<?> subject, LimitViolationType type, double limit,
                                                            double value, ThreeSides side) {
        return new LimitViolation(subject.getId(), subject.getOptionalName().orElse(null), null, type, null,
                Integer.MAX_VALUE, limit, 1.0, value, side, null);
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

    /**
     * Checks all the selected {@link OperationalLimitsGroup} of the <code>side</code> of the <code>branch</code>.
     * It will check for the limits of the given <code>type</code>, using the <code>value</code> as the value to check against the limits.
     * @param branch the branch on which to check the limits
     * @param side the side of the branch to check
     * @param value what to check against the limits (value in A for intensity, in MW for active power, in MVar for reactive power)
     * @param type the type of the limit to check
     * @param currentLimitTypes check for permanent, temporary or both
     * @param limitsComputer how to calculate the limits
     * @param consumer what to do with the violations that are detected (log, store, etc...)
     */
    public static void checkLimitViolation(Branch<?> branch, TwoSides side, double value, LimitType type,
                                    Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                    Consumer<LimitViolation> consumer) {
        Objects.requireNonNull(side);
        Collection<LimitsContainer<LoadingLimits>> allLoadingLimits = limitsComputer.computeLimits(branch, type, side.toThreeSides(), false);
        final Set<String> temporaryOverloadIds = new HashSet<>();
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            //get all the temporary overloads ids, and also use the consumer on them as we go
            allLoadingLimits.stream()
                .map(limits -> LimitViolationUtils.getOverload(limits, value))
                .flatMap(Optional::stream)
                .forEach(
                    overload -> {
                        consumer.accept(
                            new LimitViolation(branch.getId(),
                                branch.getOptionalName().orElse(null),
                                overload.getOperationalLimitsGroupId(),
                                toLimitViolationType(type),
                                overload.getPreviousLimitName(),
                                overload.getTemporaryLimit().getAcceptableDuration(),
                                overload.getPreviousLimit(),
                                overload.getLimitScalingCoefficient(),
                                value,
                                side.toThreeSides(),
                                null
                            )
                        );
                        temporaryOverloadIds.add(overload.getOperationalLimitsGroupId());
                    }
                );
        }

        if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            //do the same on the permanent, only for the groups on which we don't have an overload on a temporary
            allLoadingLimits.stream()
                    .filter(limits ->
                        !temporaryOverloadIds.contains(limits.getOperationalLimitsGroupId())
                            && limits.getOriginalLimits().getDetectionKind() == DetectionKind.HIGH
                    )
                    .map(limits -> LimitViolationUtils.checkPermanentLimitIfAny(limits, value))
                    .filter(PermanentLimitCheckResult::isOverload)
                    .forEach(permanentLimitCheckResult -> consumer.accept(new LimitViolation(branch.getId(),
                            branch.getOptionalName().orElse(null),
                            permanentLimitCheckResult.operationalLimitsGroupId(),
                            toLimitViolationType(type),
                            permanentLimitCheckResult.permanentLimitName(),
                            Integer.MAX_VALUE,
                            permanentLimitCheckResult.permanentLimitValue(),
                            permanentLimitCheckResult.limitScalingValue(),
                            value,
                            side.toThreeSides(),
                            null
                    )));
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

    /**
     * Checks all the selected {@link OperationalLimitsGroup} of the <code>side</code> of the <code>transformer</code>.
     * It will check for the limits of the given <code>type</code>, using the <code>value</code> as the value to check against the limits.
     * @param transformer the three-winding transformer on which to check the limits
     * @param side the side of the transformer to check
     * @param value what to check against the limits (value in A for intensity, in MW for active power, in MVar for reactive power)
     * @param type the type of the limit to check
     * @param currentLimitTypes check for permanent, temporary or both
     * @param limitsComputer how to calculate the limits
     * @param consumer what to do with the violations that are detected (log, store, etc...)
     */
    public static void checkLimitViolation(ThreeWindingsTransformer transformer, ThreeSides side, double value,
                                    LimitType type, Set<LoadingLimitType> currentLimitTypes, LimitsComputer<Identifiable<?>, LoadingLimits> limitsComputer,
                                    Consumer<LimitViolation> consumer) {
        Collection<LimitsContainer<LoadingLimits>> allLoadingLimits = limitsComputer.computeLimits(transformer, type, side, false);
        final Set<String> temporaryOverloadIds = new HashSet<>();
        if (currentLimitTypes.contains(LoadingLimitType.TATL)) {
            //get all the temporary overloads and send them through the consumer
            allLoadingLimits.stream()
                .map(limits -> LimitViolationUtils.getOverload(limits, value))
                .flatMap(Optional::stream)
                .forEach(overload -> {
                    consumer.accept(
                        new LimitViolation(transformer.getId(),
                            transformer.getOptionalName().orElse(null),
                            overload.getOperationalLimitsGroupId(),
                            toLimitViolationType(type),
                            overload.getPreviousLimitName(),
                            overload.getTemporaryLimit().getAcceptableDuration(),
                            overload.getPreviousLimit(),
                            overload.getLimitScalingCoefficient(),
                            value,
                            side,
                            null
                        )
                    );
                    temporaryOverloadIds.add(overload.getOperationalLimitsGroupId());
                });
        }

        if (currentLimitTypes.contains(LoadingLimitType.PATL)) {
            //do the same on the permanent, only for the groups on which we don't have an overload on a temporary
            allLoadingLimits.stream()
                    .filter(limits ->
                        !temporaryOverloadIds.contains(limits.getOperationalLimitsGroupId())
                            && limits.getOriginalLimits().getDetectionKind() == DetectionKind.HIGH
                    )
                    .map(limits -> LimitViolationUtils.checkPermanentLimitIfAny(limits, value))
                    .filter(PermanentLimitCheckResult::isOverload)
                    .forEach(permanentLimitCheckResult ->
                        consumer.accept(new LimitViolation(transformer.getId(),
                                transformer.getOptionalName().orElse(null),
                                permanentLimitCheckResult.operationalLimitsGroupId(),
                                toLimitViolationType(type),
                                permanentLimitCheckResult.permanentLimitName(),
                                Integer.MAX_VALUE,
                                permanentLimitCheckResult.permanentLimitValue(),
                                permanentLimitCheckResult.limitScalingValue(),
                                value,
                                side,
                                null
                        )
                    )
                );
        }
    }

    private static void checkVoltage(Bus bus, Consumer<LimitViolation> consumer) {
        double value = bus.getV();
        checkVoltage(bus, value, consumer);
    }

    static void checkVoltage(Bus bus, double value, Consumer<LimitViolation> consumer) {
        VoltageLevel vl = bus.getVoltageLevel();
        if (!Double.isNaN(vl.getLowVoltageLimit()) && value <= vl.getLowVoltageLimit()) {
            consumer.accept(
                LimitViolation.builder()
                .subject(vl.getId())
                .subjectName(vl.getOptionalName().orElse(null))
                .type(LimitViolationType.LOW_VOLTAGE)
                .limit(vl.getLowVoltageLimit())
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
                    .value(value)
                    .violationLocation(createViolationLocation(bus))
                    .build()
            );
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
                        consumer.accept(
                            LimitViolation.builder()
                                .subject(voltageAngleLimit.getId())
                                .type(LimitViolationType.LOW_VOLTAGE_ANGLE)
                                .limit(lowLimit)
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
                                .value(value)
                                .build()
                        );
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

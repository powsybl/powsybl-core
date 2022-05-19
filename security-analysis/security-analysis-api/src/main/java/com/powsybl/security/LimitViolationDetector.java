/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.LimitViolationUtils;

import java.util.function.Consumer;

/**
 * Being given some physical values (currents, voltages, ...) for network elements,
 * is in charge of deciding whether there are limit violations or not.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface LimitViolationDetector {

    /**
     * Checks whether the specified current value on the specified side
     * of the specified {@link Branch} should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param branch        The branch on which the current must be checked.
     * @param side          The side of the branch on which the current must be checked.
     * @param currentValue  The current value to be checked, in A.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkCurrent(Contingency contingency, Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, side, currentValue, consumer);
    }

    /**
     * Checks whether the current value on the specified side
     * of the specified {@link Branch} should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param branch        The branch on which the current must be checked.
     * @param side          The side of the branch on which the current must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkCurrent(Contingency contingency, Branch branch, Branch.Side side, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, side, consumer);
    }

    /**
     * Checks whether the specified voltage value on the specified {@link Bus}
     * should be considered as a {@link LimitViolation} or not.
     * In case it should, returns the corresponding limit violation.
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param bus           The bus on which the voltage must be checked.
     * @param voltageValue  The voltage value to be checked, in V.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkVoltage(Contingency contingency, Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
        checkVoltage(bus, voltageValue, consumer);
    }

    /**
     * Checks whether the voltage value on the specified {@link Bus}
     * should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param bus           The bus on which the voltage must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkVoltage(Contingency contingency, Bus bus, Consumer<LimitViolation> consumer) {
        checkVoltage(bus, consumer);
    }

    /**
     * Checks whether the voltage value on the specified {@link VoltageLevel}
     * should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param voltageLevel  The voltage level on which the voltage must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkVoltage(Contingency contingency, VoltageLevel voltageLevel, Consumer<LimitViolation> consumer) {
        checkVoltage(voltageLevel, consumer);
    }

    /**
     * Checks whether the current value on both sides of the specified {@link Branch}
     * should be considered as {@link LimitViolation}(s).
     * In case it should, feeds the consumer with it.
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param branch        The branch on which the current must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkCurrent(Contingency contingency, Branch branch, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, consumer);
    }

    /**
     * Checks whether the current and voltage values on all equipments
     * of the specified {@link Network} should be considered as {@link LimitViolation}s.
     * In case it should, feeds the consumer with it.
     *
     * @param contingency   The contingency for which current must be checked, {@code null} for N situation.
     * @param network       The network on which physical values must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    default void checkAll(Contingency contingency, Network network, Consumer<LimitViolation> consumer) {
        checkAll(network, consumer);
    }

    /**
     * Checks whether the specified current value on the specified side
     * of the specified {@link Branch} should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     *
     * @param branch        The branch on which the current must be checked.
     * @param side          The side of the branch on which the current must be checked.
     * @param currentValue  The current value to be checked, in A.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkCurrent(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the specified active power value on the specified side
     * of the specified {@link Branch} should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     *
     * @param branch        The branch on which the current must be checked.
     * @param side          The side of the branch on which the current must be checked.
     * @param value         The active power value to be checked, in A.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkActivePower(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the specified apparent power value on the specified side
     * of the specified {@link Branch} should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     *
     * @param branch        The branch on which the current must be checked.
     * @param side          The side of the branch on which the current must be checked.
     * @param value         The apparent power value to be checked, in A.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkApparentPower(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the current value on the specified side
     * of the specified {@link Branch} should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     * @param branch        The branch on which the current must be checked.
     * @param side          The side of the branch on which the current must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkCurrent(Branch branch, Branch.Side side, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the specified voltage value on the specified {@link Bus}
     * should be considered as a {@link LimitViolation} or not.
     * In case it should, returns the corresponding limit violation.
     *
     * @param bus           The bus on which the voltage must be checked.
     * @param voltageValue  The voltage value to be checked, in V.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkVoltage(Bus bus, double voltageValue, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the voltage value on the specified {@link Bus}
     * should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     * @param bus           The bus on which the voltage must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkVoltage(Bus bus, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the voltage value on the specified {@link VoltageLevel}
     * should be considered as a {@link LimitViolation} or not.
     * In case it should, feeds the consumer with it.
     *
     * @param voltageLevel  The voltage level on which the voltage must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkVoltage(VoltageLevel voltageLevel, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the current value on both sides of the specified {@link Branch}
     * should be considered as {@link LimitViolation}(s).
     * In case it should, feeds the consumer with it.
     *
     * @param branch        The branch on which the current must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkCurrent(Branch branch, Consumer<LimitViolation> consumer);

    /**
     * Checks whether the current and voltage values on all equipments
     * of the specified {@link Network} should be considered as {@link LimitViolation}s.
     * In case it should, feeds the consumer with it.
     *
     * @param network       The network on which physical values must be checked.
     * @param consumer      Will be fed with possibly created limit violations.
     */
    void checkAll(Network network, Consumer<LimitViolation> consumer);

    /**
     * Helper function to convert a limit type to a limit violation type
     *
     * @param type The limit type to convert.
     * @return The matching LimitViolationTYpe
     */
    default LimitViolationType toLimitViolationType(LimitType type) {
        switch (type) {
            case ACTIVE_POWER:
                return LimitViolationType.ACTIVE_POWER;
            case APPARENT_POWER:
                return LimitViolationType.APPARENT_POWER;
            case CURRENT:
                return LimitViolationType.CURRENT;
            case VOLTAGE:
            default:
                throw new UnsupportedOperationException(String.format("Unsupported conversion for %s from limit type to limit violation type.", type.name()));
        }
    }

    /**
     * Generic implementation for permanent limit checks
     * @param branch
     * @param side
     * @param value
     * @param consumer
     * @param type
     */
    default void checkPermanentLimit(Branch<?> branch, Branch.Side side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        if (LimitViolationUtils.checkPermanentLimit(branch, side, 1.0f, value, type)) {
            consumer.accept(new LimitViolation(branch.getId(),
                    ((Branch<?>) branch).getOptionalName().orElse(null),
                    toLimitViolationType(type),
                    null,
                    Integer.MAX_VALUE,
                    branch.getActiveLimits(type, side).map(LoadingLimits::getPermanentLimit).orElseThrow(PowsyblException::new),
                    1.0f,
                    value,
                    side));
        }
    }

    /**
     * Generic implementation for temporary limit checks
     * @param branch
     * @param side
     * @param value
     * @param consumer
     * @param type
     */
    default void checkTemporary(Branch branch, Branch.Side side, double value, Consumer<LimitViolation> consumer, LimitType type) {
        Branch.Overload overload = LimitViolationUtils.checkTemporaryLimits(branch, side, 1.0f, value, type);
        if (overload != null) {
            consumer.accept(new LimitViolation(branch.getId(),
                    ((Branch<?>) branch).getOptionalName().orElse(null),
                    toLimitViolationType(type),
                    overload.getPreviousLimitName(),
                    overload.getTemporaryLimit().getAcceptableDuration(),
                    overload.getPreviousLimit(),
                    1.0f,
                    value,
                    side));
        }
    }
}

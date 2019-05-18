/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

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
}

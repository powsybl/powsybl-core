/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.security.LimitViolation;

import java.util.function.Consumer;

/**
 * Provides implementations for aggregation methods of {@link LimitViolationDetector}.
 * Actual implementations will only have to focus on detecting violations element-wise.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public abstract class AbstractLimitViolationDetector extends AbstractContingencyBlindDetector {

    @Override
    public void checkCurrent(Branch branch, TwoSides side, double currentValue, Consumer<LimitViolation> consumer) {
        checkCurrent(null, branch, side, currentValue, consumer);
    }

    @Override
    public void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side, double currentValue, Consumer<LimitViolation> consumer) {
        checkCurrent(null, transformer, side, currentValue, consumer);
    }

    @Override
    public void checkVoltage(Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
        checkVoltage(null, bus, voltageValue, consumer);
    }

    @Override
    public void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, double voltageAngleDifference, Consumer<LimitViolation> consumer) {
        checkVoltageAngle(null, voltageAngleLimit, voltageAngleDifference, consumer);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation takes the current value to be checked from the Network.</p>
     */
    @Override
    public void checkCurrent(Contingency contingency, Branch branch, TwoSides side, Consumer<LimitViolation> consumer) {
        checkCurrent(contingency, branch, side, branch.getTerminal(side).getI(), consumer);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation takes the current value to be checked from the Network.</p>
     */
    @Override
    public void checkCurrent(Contingency contingency, ThreeWindingsTransformer transformer, ThreeSides side, Consumer<LimitViolation> consumer) {
        checkCurrent(contingency, transformer, side, transformer.getTerminal(side).getI(), consumer);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation takes the voltage value to be checked from the Network.</p>
     */
    @Override
    public void checkVoltage(Contingency contingency, Bus bus, Consumer<LimitViolation> consumer) {
        checkVoltage(contingency, bus, bus.getV(), consumer);
    }

    @Override
    public void checkVoltageAngle(Contingency contingency, VoltageAngleLimit voltageAngleLimit, Consumer<LimitViolation> consumer) {
        Bus referenceBus = voltageAngleLimit.getTerminalFrom().getBusView().getBus();
        Bus otherBus = voltageAngleLimit.getTerminalTo().getBusView().getBus();
        if (referenceBus != null && otherBus != null
            && referenceBus.getConnectedComponent().getNum() == otherBus.getConnectedComponent().getNum()
            && referenceBus.getSynchronousComponent().getNum() == otherBus.getSynchronousComponent().getNum()) {
            double voltageAngleDifference = otherBus.getAngle() - referenceBus.getAngle();
            checkVoltageAngle(contingency, voltageAngleLimit, voltageAngleDifference, consumer);
        }
    }

    @Override
    public void checkVoltage(Contingency contingency, VoltageLevel voltageLevel, Consumer<LimitViolation> consumer) {
        voltageLevel.getBusView().getBusStream().forEach(b -> checkVoltage(contingency, b, consumer));
    }

    @Override
    public void checkCurrent(Contingency contingency, Branch branch, Consumer<LimitViolation> consumer) {
        checkCurrent(contingency, branch, TwoSides.ONE, consumer);
        checkCurrent(contingency, branch, TwoSides.TWO, consumer);
    }

    @Override
    public void checkCurrent(Contingency contingency, ThreeWindingsTransformer transformer, Consumer<LimitViolation> consumer) {
        checkCurrent(contingency, transformer, ThreeSides.ONE, consumer);
        checkCurrent(contingency, transformer, ThreeSides.TWO, consumer);
        checkCurrent(contingency, transformer, ThreeSides.THREE, consumer);
    }

    @Override
    public void checkAll(Contingency contingency, Network network, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrent(contingency, b, consumer));
        network.getThreeWindingsTransformerStream().forEach(t -> checkCurrent(contingency, t, consumer));
        network.getVoltageLevelStream()
                .flatMap(v -> v.getBusView().getBusStream())
                .forEach(b -> checkVoltage(contingency, b, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(contingency, valOk, consumer));
    }
}

/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.security.LimitViolation;

import java.util.function.Consumer;

/**
 * Provides implementations for aggregation methods of {@link LimitViolationDetector}.
 * Contingency based methods are not implemented, default implementation are left untouched.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public abstract class AbstractContingencyBlindDetector implements LimitViolationDetector {

    /**
     * {@inheritDoc}
     * <p>This implementation takes the current value to be checked from the Network.</p>
     */
    @Override
    public void checkCurrent(Branch branch, TwoSides side, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, side, branch.getTerminal(side).getI(), consumer);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation takes the current value to be checked from the Network.</p>
     */
    @Override
    public void checkCurrent(ThreeWindingsTransformer transformer, ThreeSides side, Consumer<LimitViolation> consumer) {
        checkCurrent(transformer, side, transformer.getTerminal(side).getI(), consumer);
    }

    private double getTerminalIOrAnApproximation(Terminal terminal, double dcPowerFactor) {
        // After a DC load flow, the current at terminal can be undefined (NaN). In that case, we use the DC power factor,
        // the nominal voltage and the active power at terminal in order to approximate the current following formula
        // P = sqrt(3) x Vnom x I x dcPowerFactor
        return Double.isNaN(terminal.getI()) ?
                (1000. * terminal.getP()) / (terminal.getVoltageLevel().getNominalV() * Math.sqrt(3) * dcPowerFactor)
                : terminal.getI();
    }

    /**
     * {@inheritDoc}
     * <p>This implementation computes the current value from the power value, if current is not provided (NaN).</p>
     */
    @Override
    public void checkCurrentDc(Branch branch, TwoSides side, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        double i = getTerminalIOrAnApproximation(branch.getTerminal(side), dcPowerFactor);
        checkCurrent(branch, side, i, consumer);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation computes the current value from the power value, if current is not provided (NaN).</p>
     */
    @Override
    public void checkCurrentDc(ThreeWindingsTransformer transformer, ThreeSides side, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        double i = getTerminalIOrAnApproximation(transformer.getTerminal(side), dcPowerFactor);
        checkCurrent(transformer, side, i, consumer);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation takes the voltage value to be checked from the Network.</p>
     */
    @Override
    public void checkVoltage(Bus bus, Consumer<LimitViolation> consumer) {
        checkVoltage(bus, bus.getV(), consumer);
    }

    @Override
    public void checkVoltage(VoltageLevel voltageLevel, Consumer<LimitViolation> consumer) {
        voltageLevel.getBusView().getBusStream().forEach(b -> checkVoltage(b, consumer));
    }

    /**
     * {@inheritDoc}
     * <p>This implementation takes the voltageAngle difference to be checked from the Network.</p>
     */
    @Override
    public void checkVoltageAngle(VoltageAngleLimit voltageAngleLimit, Consumer<LimitViolation> consumer) {
        Bus referenceBus = voltageAngleLimit.getTerminalFrom().getBusView().getBus();
        Bus otherBus = voltageAngleLimit.getTerminalTo().getBusView().getBus();
        if (referenceBus != null && otherBus != null
            && referenceBus.getConnectedComponent().getNum() == otherBus.getConnectedComponent().getNum()
            && referenceBus.getSynchronousComponent().getNum() == otherBus.getSynchronousComponent().getNum()) {
            double voltageAngleDifference = otherBus.getAngle() - referenceBus.getAngle();
            checkVoltageAngle(voltageAngleLimit, voltageAngleDifference, consumer);
        }
    }

    @Override
    public void checkCurrent(Branch branch, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, TwoSides.ONE, consumer);
        checkCurrent(branch, TwoSides.TWO, consumer);
    }

    @Override
    public void checkCurrent(ThreeWindingsTransformer transformer, Consumer<LimitViolation> consumer) {
        checkCurrent(transformer, ThreeSides.ONE, consumer);
        checkCurrent(transformer, ThreeSides.TWO, consumer);
        checkCurrent(transformer, ThreeSides.THREE, consumer);
    }

    @Override
    public void checkCurrentDc(Branch branch, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        checkCurrentDc(branch, TwoSides.ONE, dcPowerFactor, consumer);
        checkCurrentDc(branch, TwoSides.TWO, dcPowerFactor, consumer);
    }

    @Override
    public void checkCurrentDc(ThreeWindingsTransformer transformer, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        checkCurrentDc(transformer, ThreeSides.ONE, dcPowerFactor, consumer);
        checkCurrentDc(transformer, ThreeSides.TWO, dcPowerFactor, consumer);
        checkCurrentDc(transformer, ThreeSides.THREE, dcPowerFactor, consumer);
    }

    @Override
    public void checkAll(Network network, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrent(b, consumer));
        network.getThreeWindingsTransformerStream().forEach(t -> checkCurrent(t, consumer));
        network.getVoltageLevelStream()
                .flatMap(vl -> vl.getBusView().getBusStream())
                .forEach(b -> checkVoltage(b, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(valOk, consumer));
    }

    @Override
    public void checkAllDc(Network network, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, consumer));
        network.getThreeWindingsTransformerStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, consumer));
        network.getVoltageAngleLimitsStream().forEach(valOk -> checkVoltageAngle(valOk, consumer));
    }
}

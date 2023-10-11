/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.*;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;

import java.util.function.Consumer;

/**
 * Provides implementations for aggregation methods of {@link LimitViolationDetector}.
 * Contingency based methods are not implemented, default implementation are left untouched.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public abstract class AbstractContingencyBlindDetector implements LimitViolationDetector {

    /**
     * This implementation takes the current value to be checked from the Network.
     */
    @Override
    public void checkCurrent(Branch branch, Branch.Side side, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, side, branch.getTerminal(side).getI(), consumer);
    }

    /**
     * Mirror checkCurrent on {@link Branch} but for {@link ThreeWindingsTransformer} instead.
     */
    @Override
    public void checkCurrent(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side, Consumer<LimitViolation> consumer) {
        checkCurrent(transformer, side, transformer.getTerminal(side).getI(), consumer);
    }

    /**
     * This implementation computes the current value from the power value, if current is not provided (NaN).
     */
    @Override
    public void checkCurrentDc(Branch branch, Branch.Side side, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        // After a DC load flow, the current at terminal can be undefined (NaN). In that case, we use the DC power factor,
        // the nominal voltage and the active power at terminal in order to approximate the current following formula
        // P = sqrt(3) x Vnom x I x dcPowerFactor
        double i = Double.isNaN(branch.getTerminal(side).getI()) ?
                (1000. * branch.getTerminal(side).getP()) / (branch.getTerminal(side).getVoltageLevel().getNominalV() * Math.sqrt(3) * dcPowerFactor)
                : branch.getTerminal(side).getI();
        checkCurrent(branch, side, i, consumer);
    }

    /**
     * Mirror checkCurrentDc on {@link Branch} but it is on {@link ThreeWindingsTransformer} instead.
     * This implementation computes the current value from the power value, if current is not provided (NaN).
     */
    @Override
    public void checkCurrentDc(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        // After a DC load flow, the current at terminal can be undefined (NaN). In that case, we use the DC power factor,
        // the nominal voltage and the active power at terminal in order to approximate the current following formula
        // P = sqrt(3) x Vnom x I x dcPowerFactor
        double i = Double.isNaN(transformer.getTerminal(side).getI()) ?
                (1000. * transformer.getTerminal(side).getP()) / (transformer.getTerminal(side).getVoltageLevel().getNominalV() * Math.sqrt(3) * dcPowerFactor)
                : transformer.getTerminal(side).getI();
        checkCurrent(transformer, side, i, consumer);
    }

    /**
     * This implementation takes the voltage value to be checked from the Network.
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
     * This implementation takes the voltageAngle difference to be checked from the Network.
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
        checkCurrent(branch, Branch.Side.ONE, consumer);
        checkCurrent(branch, Branch.Side.TWO, consumer);
    }

    @Override
    public void checkCurrent(ThreeWindingsTransformer transformer, Consumer<LimitViolation> consumer) {
        checkCurrent(transformer, ThreeWindingsTransformer.Side.ONE, consumer);
        checkCurrent(transformer, ThreeWindingsTransformer.Side.TWO, consumer);
        checkCurrent(transformer, ThreeWindingsTransformer.Side.THREE, consumer);
    }

    @Override
    public void checkCurrentDc(Branch branch, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        checkCurrentDc(branch, Branch.Side.ONE, dcPowerFactor, consumer);
        checkCurrentDc(branch, Branch.Side.TWO, dcPowerFactor, consumer);
    }

    @Override
    public void checkCurrentDc(ThreeWindingsTransformer transformer, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        checkCurrentDc(transformer, ThreeWindingsTransformer.Side.ONE, dcPowerFactor, consumer);
        checkCurrentDc(transformer, ThreeWindingsTransformer.Side.TWO, dcPowerFactor, consumer);
        checkCurrentDc(transformer, ThreeWindingsTransformer.Side.THREE, dcPowerFactor, consumer);
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

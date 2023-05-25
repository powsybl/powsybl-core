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
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
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
     * This implementation takes the current value to be checked from the Network.
     */
    @Override
    public void checkCurrent(TieLine tieLine, Branch.Side side, Consumer<LimitViolation> consumer) {
        checkCurrent(tieLine, side, tieLine.getDanglingLine(side).getTerminal().getI(), consumer);
    }

    /**
     * This implementation computes the current value from the power value, if current is not provided (NaN).
     */
    @Override
    public void checkCurrentDc(Branch branch, Branch.Side side, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        Terminal terminal = branch.getTerminal(side);
        double i = computeCurrentDc(dcPowerFactor, terminal);
        checkCurrent(branch, side, i, consumer);
    }

    /**
     * This implementation computes the current value from the power value, if current is not provided (NaN).
     */
    @Override
    public void checkCurrentDc(TieLine tieLine, Branch.Side side, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        Terminal terminal = tieLine.getDanglingLine(side).getTerminal();
        double i = computeCurrentDc(dcPowerFactor, terminal);
        checkCurrent(tieLine, side, i, consumer);
    }

    private double computeCurrentDc(double dcPowerFactor, Terminal terminal) {
        // After a DC load flow, the current at terminal can be undefined (NaN). In that case, we use the DC power factor,
        // the nominal voltage and the active power at terminal in order to approximate the current following formula
        // P = sqrt(3) x Vnom x I x dcPowerFactor
        return Double.isNaN(terminal.getI()) ?
                (1000. * terminal.getP()) / (terminal.getVoltageLevel().getNominalV() * Math.sqrt(3) * dcPowerFactor)
                : terminal.getI();
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

    @Override
    public void checkCurrent(Branch branch, Consumer<LimitViolation> consumer) {
        checkCurrent(branch, Branch.Side.ONE, consumer);
        checkCurrent(branch, Branch.Side.TWO, consumer);
    }

    @Override
    public void checkCurrent(TieLine tieLine, Consumer<LimitViolation> consumer) {
        checkCurrent(tieLine, Branch.Side.ONE, consumer);
        checkCurrent(tieLine, Branch.Side.TWO, consumer);
    }

    @Override
    public void checkCurrentDc(Branch branch, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        checkCurrentDc(branch, Branch.Side.ONE, dcPowerFactor, consumer);
        checkCurrentDc(branch, Branch.Side.TWO, dcPowerFactor, consumer);
    }

    @Override
    public void checkCurrentDc(TieLine tieLine, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        checkCurrentDc(tieLine, Branch.Side.ONE, dcPowerFactor, consumer);
        checkCurrentDc(tieLine, Branch.Side.TWO, dcPowerFactor, consumer);
    }

    @Override
    public void checkAll(Network network, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrent(b, consumer));
        network.getVoltageLevelStream()
                .flatMap(v -> v.getBusView().getBusStream())
                .forEach(b -> checkVoltage(b, consumer));
        network.getTieLineStream().forEach(t -> checkCurrent(t, consumer));
    }

    @Override
    public void checkAllDc(Network network, double dcPowerFactor, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrentDc(b, dcPowerFactor, consumer));
        network.getTieLineStream().forEach(t -> checkCurrentDc(t, dcPowerFactor, consumer));
    }
}

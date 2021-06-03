/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.*;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationDetector;

import java.util.function.Consumer;

/**
 * Provides implementations for aggregation methods of {@link LimitViolationDetector}.
 * Actual implementations will only have to focus on detecting violations element-wise.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public abstract class AbstractLimitViolationDetector extends AbstractContingencyBlindDetector {

    @Override
    public void checkCurrent(Branch branch, Branch.Side side, double currentValue, Consumer<LimitViolation> consumer) {
        checkCurrent(null, branch, side, currentValue, consumer);
    }

    @Override
    public void checkVoltage(Bus bus, double voltageValue, Consumer<LimitViolation> consumer) {
        checkVoltage(null, bus, voltageValue, consumer);
    }

    /**
     * This implementation takes the current value to be checked from the Network.
     */
    @Override
    public void checkCurrent(Contingency contingency, Branch branch, Branch.Side side, Consumer<LimitViolation> consumer) {
        checkCurrent(contingency, branch, side, branch.getTerminal(side).getI(), consumer);
    }

    /**
     * This implementation takes the voltage value to be checked from the Network.
     */
    @Override
    public void checkVoltage(Contingency contingency, Bus bus, Consumer<LimitViolation> consumer) {
        checkVoltage(contingency, bus, bus.getV(), consumer);
    }

    @Override
    public void checkVoltage(Contingency contingency, VoltageLevel voltageLevel, Consumer<LimitViolation> consumer) {
        voltageLevel.getBusView().getBusStream().forEach(b -> checkVoltage(contingency, b, consumer));
    }

    @Override
    public void checkCurrent(Contingency contingency, Branch branch, Consumer<LimitViolation> consumer) {
        checkCurrent(contingency, branch, Branch.Side.ONE, consumer);
        checkCurrent(contingency, branch, Branch.Side.TWO, consumer);
    }

    @Override
    public void checkAll(Contingency contingency, Network network, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrent(contingency, b, consumer));
        network.getVoltageLevelStream()
                .flatMap(v -> v.getBusView().getBusStream())
                .forEach(b -> checkVoltage(contingency, b, consumer));
    }
}

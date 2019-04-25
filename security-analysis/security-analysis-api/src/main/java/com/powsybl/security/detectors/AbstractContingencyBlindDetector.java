/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.detectors;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
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
    public void checkAll(Network network, Consumer<LimitViolation> consumer) {
        network.getBranchStream().forEach(b -> checkCurrent(b, consumer));
        network.getVoltageLevelStream()
                .flatMap(v -> v.getBusView().getBusStream())
                .forEach(b -> checkVoltage(b, consumer));
    }
}

/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class DanglingLineBoundaryImpl implements Boundary {
    // for SV use: side represents the network side, that is always
    // Side.ONE for a dangling line.

    private final DanglingLine parent;

    public DanglingLineBoundaryImpl(DanglingLine parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public double getV() {
        DanglingLineData danglingLineData = new DanglingLineData(parent, true);
        return danglingLineData.getBoundaryBusU();
    }

    @Override
    public double getAngle() {
        DanglingLineData danglingLineData = new DanglingLineData(parent, true);
        return Math.toDegrees(danglingLineData.getBoundaryBusTheta());
    }

    @Override
    public double getP() {
        DanglingLineData danglingLineData = new DanglingLineData(parent, true);
        return danglingLineData.getBoundaryFlowP();
    }

    @Override
    public double getQ() {
        DanglingLineData danglingLineData = new DanglingLineData(parent, true);
        return danglingLineData.getBoundaryFlowQ();
    }

    @Override
    public Branch.Side getSide() {
        return null;
    }

    @Override
    public DanglingLine getConnectable() {
        return parent;
    }

    @Override
    public VoltageLevel getVoltageLevel() {
        return parent.getTerminal().getVoltageLevel();
    }
}

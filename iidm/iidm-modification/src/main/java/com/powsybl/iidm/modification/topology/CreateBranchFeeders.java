/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;

import java.util.Objects;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateBranchFeeders extends AbstractCreateConnectableFeeders {

    private final BranchAdder<?> branchAdder;
    private final String bbsId1;
    private final String bbsId2;
    private final int positionOrder1;
    private final int positionOrder2;
    private final ConnectablePosition.Direction direction1;
    private final ConnectablePosition.Direction direction2;

    CreateBranchFeeders(BranchAdder<?> branchAdder, String bbsId1, String bbsId2, Integer positionOrder1, Integer positionOrder2,
                        ConnectablePosition.Direction direction1, ConnectablePosition.Direction direction2) {
        super(1, 2);
        this.branchAdder = Objects.requireNonNull(branchAdder);
        this.bbsId1 = Objects.requireNonNull(bbsId1);
        this.bbsId2 = Objects.requireNonNull(bbsId2);
        this.positionOrder1 = Objects.requireNonNull(positionOrder1);
        this.positionOrder2 = Objects.requireNonNull(positionOrder2);
        this.direction1 = Objects.requireNonNull(direction1);
        this.direction2 = Objects.requireNonNull(direction2);
    }

    @Override
    protected String getBbsId(int side) {
        if (side == 1) {
            return bbsId1;
        }
        if (side == 2) {
            return bbsId2;
        }
        throw createSideAssertionError(side);
    }

    @Override
    protected void setNode(int side, int node, String voltageLevelId) {
        if (side == 1) {
            branchAdder.setNode1(node).setVoltageLevel1(voltageLevelId);
        } else if (side == 2) {
            branchAdder.setNode2(node).setVoltageLevel2(voltageLevelId);
        } else {
            throw createSideAssertionError(side);
        }
    }

    @Override
    protected Connectable<?> add() {
        if (branchAdder instanceof LineAdder) {
            return ((LineAdder) branchAdder).add();
        } else if (branchAdder instanceof TwoWindingsTransformerAdder) {
            return ((TwoWindingsTransformerAdder) branchAdder).add();
        } else {
            throw new AssertionError("Given BranchAdder not supported: " + branchAdder.getClass().getName());
        }
    }

    @Override
    protected VoltageLevel getVoltageLevel(int side, Connectable<?> connectable) {
        Branch<?> branch = (Branch<?>) connectable;
        if (side == 1) {
            return branch.getTerminal1().getVoltageLevel();
        }
        if (side == 2) {
            return branch.getTerminal2().getVoltageLevel();
        }
        throw createSideAssertionError(side);
    }

    @Override
    protected int getPositionOrder(int side) {
        if (side == 1) {
            return positionOrder1;
        }
        if (side == 2) {
            return positionOrder2;
        }
        throw createSideAssertionError(side);
    }

    @Override
    protected ConnectablePosition.Direction getDirection(int side) {
        if (side == 1) {
            return direction1;
        }
        if (side == 2) {
            return direction2;
        }
        throw createSideAssertionError(side);
    }

    @Override
    protected int getNode(int side, Connectable<?> connectable) {
        Branch<?> branch = (Branch<?>) connectable;
        if (side == 1) {
            return branch.getTerminal1().getNodeBreakerView().getNode();
        }
        if (side == 2) {
            return branch.getTerminal2().getNodeBreakerView().getNode();
        }
        throw createSideAssertionError(side);
    }

    @Override
    protected ConnectablePositionAdder.FeederAdder<?> getFeederAdder(int side, ConnectablePositionAdder<?> connectablePositionAdder) {
        if (side == 1) {
            return connectablePositionAdder.newFeeder1();
        }
        if (side == 2) {
            return connectablePositionAdder.newFeeder2();
        }
        throw createSideAssertionError(side);
    }

    private static AssertionError createSideAssertionError(int side) {
        return new AssertionError("Unexpected side: " + side);
    }
}

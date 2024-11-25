/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;

import java.util.Objects;
import java.util.Optional;

/**
 * This class allows to add a new branch's feeders on existing busbar sections. The voltage level containing the
 * busbar section should be described in node/breaker topology. The branch is created and connected to
 * the busbar sections with a breaker and a closed disconnector each. The branch is also connected to all
 * the parallel busbar sections, if any, with an open disconnector.
 *
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class CreateBranchFeederBays extends AbstractCreateConnectableFeederBays {

    private final BranchAdder<?, ?> branchAdder;
    private final String busOrBbsId1;
    private final String busOrBbsId2;
    private final Integer positionOrder1;
    private final Integer positionOrder2;
    private final String feederName1;
    private final String feederName2;
    private final ConnectablePosition.Direction direction1;
    private final ConnectablePosition.Direction direction2;

    CreateBranchFeederBays(BranchAdder<?, ?> branchAdder, String busOrBbsId1, String busOrBbsId2, Integer positionOrder1, Integer positionOrder2,
                           String feederName1, String feederName2, ConnectablePosition.Direction direction1, ConnectablePosition.Direction direction2) {
        super(1, 2);
        this.branchAdder = Objects.requireNonNull(branchAdder);
        this.busOrBbsId1 = Objects.requireNonNull(busOrBbsId1);
        this.busOrBbsId2 = Objects.requireNonNull(busOrBbsId2);
        this.positionOrder1 = positionOrder1;
        this.positionOrder2 = positionOrder2;
        this.feederName1 = feederName1;
        this.feederName2 = feederName2;
        this.direction1 = Objects.requireNonNull(direction1);
        this.direction2 = Objects.requireNonNull(direction2);
    }

    @Override
    public String getName() {
        return "CreateBranchFeederBays";
    }

    @Override
    protected String getBusOrBusbarSectionId(int side) {
        if (side == 1) {
            return busOrBbsId1;
        }
        if (side == 2) {
            return busOrBbsId2;
        }
        throw createSideIllegalStateException(side);
    }

    @Override
    protected void setBus(int side, Bus bus, String voltageLevelId) {
        if (side == 1) {
            branchAdder.setConnectableBus1(bus.getId()).setBus1(bus.getId()).setVoltageLevel1(voltageLevelId);
        } else if (side == 2) {
            branchAdder.setConnectableBus2(bus.getId()).setBus2(bus.getId()).setVoltageLevel2(voltageLevelId);
        } else {
            throw createSideIllegalStateException(side);
        }
    }

    @Override
    protected void setNode(int side, int node, String voltageLevelId) {
        if (side == 1) {
            branchAdder.setNode1(node).setVoltageLevel1(voltageLevelId);
        } else if (side == 2) {
            branchAdder.setNode2(node).setVoltageLevel2(voltageLevelId);
        } else {
            throw createSideIllegalStateException(side);
        }
    }

    @Override
    protected Connectable<?> add() {
        return branchAdder.add();
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
        throw createSideIllegalStateException(side);
    }

    @Override
    protected Integer getPositionOrder(int side) {
        if (side == 1) {
            return positionOrder1;
        }
        if (side == 2) {
            return positionOrder2;
        }
        throw createSideIllegalStateException(side);
    }

    @Override
    protected Optional<String> getFeederName(int side) {
        if (side == 1) {
            return Optional.ofNullable(feederName1);
        }
        if (side == 2) {
            return Optional.ofNullable(feederName2);
        }
        throw createSideIllegalStateException(side);
    }

    @Override
    protected ConnectablePosition.Direction getDirection(int side) {
        if (side == 1) {
            return direction1;
        }
        if (side == 2) {
            return direction2;
        }
        throw createSideIllegalStateException(side);
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
        throw createSideIllegalStateException(side);
    }

    @Override
    protected ConnectablePositionAdder.FeederAdder<?> getFeederAdder(int side, ConnectablePositionAdder<?> connectablePositionAdder) {
        if (side == 1) {
            return connectablePositionAdder.newFeeder1();
        }
        if (side == 2) {
            return connectablePositionAdder.newFeeder2();
        }
        throw createSideIllegalStateException(side);
    }

    private static IllegalStateException createSideIllegalStateException(int side) {
        return new IllegalStateException("Unexpected side: " + side);
    }
}

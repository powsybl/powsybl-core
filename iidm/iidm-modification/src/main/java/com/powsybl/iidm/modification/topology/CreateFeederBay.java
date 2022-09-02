/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.util.Objects;

/**
 * This method adds a new injection bay on an existing busbar section. The voltage level containing the
 * busbar section should be described in node/breaker topology. The injection is created and connected to
 * the busbar section with a breaker and a closed disconnector. The injection is also connected to all
 * the parallel busbar sections, if any, with an open disconnector.
 *
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class CreateFeederBay extends AbstractCreateConnectableFeeders {

    private final InjectionAdder<?> injectionAdder;
    private final String bbsId;
    private final int injectionPositionOrder;
    private final ConnectablePosition.Direction injectionDirection;

    /**
     * Constructor.
     *
     * @param injectionAdder         The injection adder.
     * @param bbsId                  The ID of the existing busbar section where we want to connect the injection.
     *                               Please note that there will be switches between this busbar section and the connection point of the injection. This switch will be closed.
     * @param injectionPositionOrder The order of the injection to be attached from its extension {@link ConnectablePosition}.
     * @param injectionDirection     The direction of the injection to be attached from its extension {@link ConnectablePosition}.
     */
    CreateFeederBay(InjectionAdder<?> injectionAdder, String bbsId, Integer injectionPositionOrder, ConnectablePosition.Direction injectionDirection) {
        super(0);
        this.injectionAdder = Objects.requireNonNull(injectionAdder);
        this.bbsId = Objects.requireNonNull(bbsId);
        this.injectionPositionOrder = injectionPositionOrder;
        this.injectionDirection = Objects.requireNonNull(injectionDirection);
    }

    @Override
    protected String getBbsId(int side) {
        return bbsId;
    }

    @Override
    protected void setNode(int side, int node) {
        injectionAdder.setNode(node);
    }

    @Override
    protected Connectable<?> add() {
        if (injectionAdder instanceof LoadAdder) {
            return ((LoadAdder) injectionAdder).add();
        } else if (injectionAdder instanceof BatteryAdder) {
            return ((BatteryAdder) injectionAdder).add();
        } else if (injectionAdder instanceof DanglingLineAdder) {
            return ((DanglingLineAdder) injectionAdder).add();
        } else if (injectionAdder instanceof GeneratorAdder) {
            return ((GeneratorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof ShuntCompensatorAdder) {
            return ((ShuntCompensatorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof StaticVarCompensatorAdder) {
            return ((StaticVarCompensatorAdder) injectionAdder).add();
        } else if (injectionAdder instanceof LccConverterStationAdder) {
            return ((LccConverterStationAdder) injectionAdder).add();
        } else if (injectionAdder instanceof VscConverterStationAdder) {
            return ((VscConverterStationAdder) injectionAdder).add();
        } else {
            throw new AssertionError("Given InjectionAdder not supported: " + injectionAdder.getClass().getName());
        }
    }

    @Override
    protected VoltageLevel getVoltageLevel(int side, Connectable<?> connectable) {
        return ((Injection<?>) connectable).getTerminal().getVoltageLevel();
    }

    @Override
    protected int getPositionOrder(int side) {
        return injectionPositionOrder;
    }

    @Override
    protected ConnectablePosition.Direction getDirection(int side) {
        return injectionDirection;
    }

    @Override
    protected int getNode(int side, Connectable<?> connectable) {
        return ((Injection<?>) connectable).getTerminal().getNodeBreakerView().getNode();
    }
}

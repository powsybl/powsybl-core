/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractConnectDisconnectModification extends AbstractNetworkModification {
    final String identifiableId;
    ThreeSides side;
    final boolean isConnecting;

    AbstractConnectDisconnectModification(String identifiableId, ThreeSides side, boolean isConnecting) {
        this.identifiableId = Objects.requireNonNull(identifiableId);
        this.side = side;
        this.isConnecting = isConnecting;
    }

    @Override
    public NetworkModificationImpact hasImpactOnNetwork(Network network) {
        impact = DEFAULT_IMPACT;
        Identifiable<?> identifiable = network.getIdentifiable(identifiableId);
        if (checkIfCannotBeAppliedOnNetwork(identifiable)) {
            impact = NetworkModificationImpact.CANNOT_BE_APPLIED;
        } else if (identifiable instanceof Connectable<?> connectable && checkIfNoImpactOnNetwork(connectable)
            || identifiable instanceof TieLine tieLine && checkIfNoImpactOnNetwork(tieLine)
            || identifiable instanceof HvdcLine hvdcLine && checkIfNoImpactOnNetwork(hvdcLine)) {
            impact = NetworkModificationImpact.NO_IMPACT_ON_NETWORK;
        }
        return impact;
    }

    private boolean checkIfCannotBeAppliedOnNetwork(Identifiable<?> identifiable) {
        return !(identifiable instanceof Connectable<?> || identifiable instanceof TieLine || identifiable instanceof HvdcLine)
            || identifiable instanceof Connectable<?> connectable && (side == ThreeSides.TWO && connectable.getTerminals().size() < 2 || side == ThreeSides.THREE && connectable.getTerminals().size() < 3)
            || (identifiable instanceof TieLine || identifiable instanceof HvdcLine) && side == ThreeSides.THREE;
    }

    private boolean checkIfNoImpactOnNetwork(Connectable<?> connectable) {
        return side == null && connectable.getTerminals().stream().allMatch(terminal -> terminal.isConnected() == isConnecting)
            || side == ThreeSides.ONE && connectable.getTerminals().get(0).isConnected() == isConnecting
            || side == ThreeSides.TWO && connectable.getTerminals().get(1).isConnected() == isConnecting
            || side == ThreeSides.THREE && connectable.getTerminals().get(2).isConnected() == isConnecting;
    }

    private boolean checkIfNoImpactOnNetwork(TieLine tieLine) {
        return side == null && tieLine.getTerminal1().isConnected() == isConnecting && tieLine.getTerminal2().isConnected() == isConnecting
            || side == ThreeSides.ONE && tieLine.getTerminal1().isConnected() == isConnecting
            || side == ThreeSides.TWO && tieLine.getTerminal2().isConnected() == isConnecting;
    }

    private boolean checkIfNoImpactOnNetwork(HvdcLine hvdcLine) {
        return side == null && hvdcLine.getConverterStation1().getTerminal().isConnected() == isConnecting
            && hvdcLine.getConverterStation2().getTerminal().isConnected() == isConnecting
            || side == ThreeSides.ONE && hvdcLine.getConverterStation1().getTerminal().isConnected() == isConnecting
            || side == ThreeSides.TWO && hvdcLine.getConverterStation2().getTerminal().isConnected() == isConnecting;
    }

}

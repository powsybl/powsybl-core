/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Objects;
import java.util.Optional;

/**
 * Simple {@link NetworkModification} for connection or disconnection of a connectable terminals (or specific terminal).
 *
 * @author Pauline JEAN-MARIE {@literal <pauline.jean-marie at artelys.com>}
 */
public class TerminalsConnectionModification extends AbstractNetworkModification {

    private final String connectableId;
    private ThreeSides side;
    private final boolean open;

    /**
     * @param connectableId the id of the element which terminals are operated.
     *                      The element can be any connectable, including a tie line by referring the terminal of
     *                      an underlying dangling line.
     * @param side          the side of the element to operate in the action.
     * @param open          the status for the terminal to operate. {@code true} means terminal opening.
     */
    public TerminalsConnectionModification(String connectableId, ThreeSides side, boolean open) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.side = Objects.requireNonNull(side);
        this.open = open;
    }

    /**
     * @param connectableId the id of the element which terminals are operated.
     *                      The element can be any connectable, including a tie line by referring the terminal of
     *                      an underlying dangling line.
     * @param open          the status for the terminals to operate. {@code true} means terminals opening.
     */
    public TerminalsConnectionModification(String connectableId, boolean open) {
        this.connectableId = Objects.requireNonNull(connectableId);
        this.open = open;
    }

    public String getConnectableId() {
        return connectableId;
    }

    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }

    public boolean toOpen() {
        return open;
    }

    @Override
    public void apply(Network network, NamingStrategy namingStrategy, boolean throwException, ComputationManager computationManager,
                      ReportNode reportNode) {
        Connectable<?> connectable = network.getConnectable(getConnectableId());
        connectable.getTerminals()
                .stream()
                .filter(t -> getSide().map(s -> t.getSide().getNum() == s.getNum()).orElse(true))
                .forEach(t -> {
                    if (toOpen()) {
                        t.disconnect();
                    } else {
                        t.connect();
                    }
                });
    }
}

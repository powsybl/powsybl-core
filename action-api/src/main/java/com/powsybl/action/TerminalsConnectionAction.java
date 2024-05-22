/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.modification.*;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Objects;
import java.util.Optional;

/**
 * An action of opening or closing an equipment terminal(s).
 *
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class TerminalsConnectionAction extends AbstractAction {

    public static final String NAME = "TERMINALS_CONNECTION";
    private final String elementId;
    private ThreeSides side;
    private final boolean open;

    /**
     * @param id the id of the action.
     * @param elementId the id of the element which terminals are operated.
     *                  The element can be any connectable, including a tie line by referring the terminal of
     *                  an underlying dangling line.
     * @param side the side of the element to operate in the action.
     * @param open the status for the terminal to operate. {@code true} means terminal opening.
     */
    public TerminalsConnectionAction(String id, String elementId, ThreeSides side, boolean open) {
        super(id);
        this.elementId = Objects.requireNonNull(elementId);
        this.side = side;
        this.open = open;
    }

    /**
     * @param id the id of the action.
     * @param elementId the id of the element which terminals are operated.
     *                  The element can be any connectable, including a tie line by referring the terminal of
     *                  an underlying dangling line.
     * @param open the status for all the terminals of the element to operate. {@code true} means all terminals opening.
     */
    public TerminalsConnectionAction(String id, String elementId, boolean open) {
        super(id);
        this.elementId = Objects.requireNonNull(elementId);
        this.open = open;
    }

    @Override
    public String getType() {
        return NAME;
    }

    /**
     * @return the element id.
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * The side is optional. Empty means that all the terminals of the element will be operated
     * in the action with the defined open or close status.
     *
     * @return the optional side of the connection/disconnection action.
     */
    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }

    /**
     * If {@code true}, applying the action will open the terminal reference with side is given,
     * else it will close it. If the side is not given, if {@code true}, applying the action will open all the terminals
     * of the element, else it will close them.
     */
    public boolean isOpen() {
        return open;
    }

    @Override
    public NetworkModification toModification() {
        if (isOpen()) {
            PlannedDisconnectionBuilder builder = new PlannedDisconnectionBuilder()
                .withConnectableId(elementId)
                .withSide(side);
            return builder.build();
        } else {
            ConnectableConnectionBuilder builder = new ConnectableConnectionBuilder()
                    .withConnectableId(elementId)
                    .withOnlyBreakersOperable(true)
                .withSide(side);
            return builder.build();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TerminalsConnectionAction that = (TerminalsConnectionAction) o;
        return open == that.open && Objects.equals(elementId, that.elementId) && side == that.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), elementId, side, open);
    }
}

/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Objects;
import java.util.Optional;

/**
 * An action of opening or closing a terminal.
 *
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class TerminalConnectionAction extends AbstractAction {

    public static final String NAME = "TERMINAL_CONNECTION";
    private final String elementId;
    private ThreeSides side;
    private final boolean open;

    public TerminalConnectionAction(String id, String elementId, ThreeSides side, boolean open) {
        super(id);
        this.elementId = Objects.requireNonNull(elementId);
        this.side = Objects.requireNonNull(side);
        this.open = open;
    }

    public TerminalConnectionAction(String id, String elementId, boolean open) {
        super(id);
        this.elementId = Objects.requireNonNull(elementId);
        this.open = open;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getElementId() {
        return elementId;
    }

    /**
     * Return the optional side of the connection/disconnection action.
     */
    public Optional<ThreeSides> getSide() {
        return Optional.ofNullable(side);
    }

    /**
     * If {@code true}, applying the action will open the terminal 2 of the line,
     * else it will close it.
     */
    public boolean isOpen() {
        return open;
    }
}

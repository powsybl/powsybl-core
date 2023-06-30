/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

public class TerminalWithPriorityImpl implements TerminalWithPriority {

    private final Terminal terminal;
    private final int priority;

    public TerminalWithPriorityImpl(final Terminal terminal, final int priority) {
        this.terminal = Objects.requireNonNull(terminal);
        this.priority = priority;
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public int getPriority() {
        return priority;
    }

}

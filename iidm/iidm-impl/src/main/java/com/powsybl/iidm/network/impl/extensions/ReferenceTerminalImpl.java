/**
 * Copyright (c) 2023, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferenceTerminal;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class ReferenceTerminalImpl implements ReferenceTerminal {

    private final Terminal terminal;
    private final int priority;

    ReferenceTerminalImpl(Terminal terminal, int priority) {
        this.terminal = Objects.requireNonNull(terminal, "Terminal needs to be set for ReferenceTerminal extension");
        if (priority < 0) {
            throw new PowsyblException("Priority should be zero or positive for ReferenceTerminal extension");
        }
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

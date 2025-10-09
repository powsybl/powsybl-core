/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class TerminalMockExt extends AbstractExtension<Load> {

    private Terminal terminal;

    public TerminalMockExt(Load load) {
        super(load);
        terminal = load.getTerminal();
    }

    @Override
    public String getName() {
        return "terminalMock";
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public TerminalMockExt setTerminal(Terminal terminal) {
        this.terminal = Objects.requireNonNull(terminal);
        return this;
    }
}

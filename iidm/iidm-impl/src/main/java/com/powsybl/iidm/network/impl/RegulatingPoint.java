/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import java.util.function.Supplier;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class RegulatingPoint {

    private final Supplier<TerminalExt> localTerminalSupplier;
    private TerminalExt terminal = null;

    RegulatingPoint(Supplier<TerminalExt> localTerminalSupplier) {
        this.localTerminalSupplier = localTerminalSupplier;
    }

    void set(TerminalExt terminal) {
        if (this.terminal != null) {
            this.terminal.removeRegulatingPoint(this);
        }
        this.terminal = terminal != null ? terminal : localTerminalSupplier.get();
        if (this.terminal != null) {
            this.terminal.setAsRegulatingPoint(this);
        }
    }

    TerminalExt get() {
        return terminal;
    }

    void remove() {
        terminal = localTerminalSupplier.get();
    }
}

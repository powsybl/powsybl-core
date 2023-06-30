/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

public interface TerminalWithPriority {

    public Terminal getTerminal();

    public int getPriority();

    default VoltageLevel getVoltageLevel() {
        return getTerminal().getVoltageLevel();
    }
}

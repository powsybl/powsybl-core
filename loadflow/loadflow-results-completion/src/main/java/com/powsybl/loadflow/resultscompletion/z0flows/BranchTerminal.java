/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;

import java.util.Objects;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
final class BranchTerminal {

    private BranchTerminal() {
    }

    static Terminal ofOtherBus(Branch<?> branch, Bus bus) {
        Objects.requireNonNull(branch);
        if (busAtTerminal(branch.getTerminal1()) == bus) {
            return branch.getTerminal2();
        } else if (busAtTerminal(branch.getTerminal2()) == bus) {
            return branch.getTerminal1();
        }
        return null;
    }

    static Terminal ofBus(Branch<?> branch, Bus bus) {
        Objects.requireNonNull(branch);
        if (busAtTerminal(branch.getTerminal1()) == bus) {
            return branch.getTerminal1();
        } else if (busAtTerminal(branch.getTerminal2()) == bus) {
            return branch.getTerminal2();
        }
        return null;
    }

    private static Bus busAtTerminal(Terminal t) {
        if (t.isConnected()) {
            return t.getBusView().getBus();
        }
        return null;
    }
}

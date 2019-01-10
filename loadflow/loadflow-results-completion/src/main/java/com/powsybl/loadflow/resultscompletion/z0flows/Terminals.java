/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.loadflow.resultscompletion.z0flows;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

final class Terminals {

    private Terminals() {
    }

    static Terminal getOther(Branch<?> branch, Bus bus) {
        if (branch.getTerminal1().isConnected()
                && bus.getId().equals(branch.getTerminal1().getBusView().getBus().getId())) {
            return branch.getTerminal2();
        } else {
            return branch.getTerminal1();
        }
    }

    static Terminal get(Branch<?> branch, Bus bus) {
        if (branch.getTerminal1().isConnected()
                && bus.getId().equals(branch.getTerminal1().getBusView().getBus().getId())) {
            return branch.getTerminal1();
        } else {
            return branch.getTerminal2();
        }
    }

    static Terminal get(ThreeWindingsTransformer tlt, Bus bus) {
        if (tlt.getLeg1().getTerminal().isConnected()
                && tlt.getLeg1().getTerminal().getBusView().getBus() != null
                && bus.getId().equals(tlt.getLeg1().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg1().getTerminal();
        } else if (tlt.getLeg2().getTerminal().isConnected()
                && tlt.getLeg2().getTerminal().getBusView().getBus() != null
                && bus.getId().equals(tlt.getLeg2().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg2().getTerminal();
        } else if (tlt.getLeg3().getTerminal().isConnected()
                && tlt.getLeg3().getTerminal().getBusView().getBus() != null
                && bus.getId().equals(tlt.getLeg3().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg3().getTerminal();
        } else {
            return null;
        }
    }
}

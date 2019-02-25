/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple.equations;

import com.powsybl.iidm.network.Branch;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public interface DcFlowEquations {

    double p1();

    double p2();

    double dp1dph1();

    double dp1dph2();

    double dp2dph1();

    double dp2dph2();

    static DcFlowEquations of(Branch branch) {
        if (!branch.getTerminal1().isConnected() || !branch.getTerminal2().isConnected()) {
            return new OpenBranchDcFlowEquations();
        }
        return new DcFlowEquationsImpl(branch);
    }
}

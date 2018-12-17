/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.simple.equations;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class OpenBranchDcFlowEquations extends AbstractDcFlowEquations {
    @Override
    public double p1() {
        return 0;
    }

    @Override
    public double dp1dph1() {
        return 0;
    }
}

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
public class DcFlowEquationsImpl extends AbstractDcFlowEquations {

    private final double deltaPhase;
    private final double power;

    public DcFlowEquationsImpl(Branch branch) {
        BranchCharacteristics bc = new BranchCharacteristics(branch);

        double v1 = branch.getTerminal1().getVoltageLevel().getNominalV();
        double v2 = branch.getTerminal2().getVoltageLevel().getNominalV();

        double ph1 = branch.getTerminal1().getBusView().getBus().getAngle();
        double ph2 = branch.getTerminal2().getBusView().getBus().getAngle();

        power =  1 / bc.x() * v1 * v2;
        deltaPhase =  ph1 + bc.a1() - ph2 - bc.a2();
    }

    @Override
    public double dp1dph1() {
        return power;
    }

    @Override
    public double p1() {
        return power * deltaPhase;
    }
}

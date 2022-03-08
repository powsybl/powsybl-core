/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ValuesOnThreePhases {
    private double phase1;
    private double phase2;
    private double phase3;

    public ValuesOnThreePhases(double phase1, double phase2, double phase3) {
        this.phase1 = phase1;
        this.phase2 = phase2;
        this.phase3 = phase3;
    }

    public double getPhase1() {
        return phase1;
    }

    public void setPhase1(double phase1) {
        this.phase1 = phase1;
    }

    public double getPhase2() {
        return phase2;
    }

    public void setPhase2(double phase2) {
        this.phase2 = phase2;
    }

    public double getPhase3() {
        return phase3;
    }

    public void setPhase3(double phase3) {
        this.phase3 = phase3;
    }
}

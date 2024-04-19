/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseVersioned;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerImpedanceCorrection extends PsseVersioned {

    private final int i;
    private final List<PsseTransformerImpedanceCorrectionPoint> points;

    public PsseTransformerImpedanceCorrection(int i) {
        this.i = i;
        this.points = new ArrayList<>();
    }

    public int getI() {
        return i;
    }

    public List<PsseTransformerImpedanceCorrectionPoint> getPoints() {
        return points;
    }

}

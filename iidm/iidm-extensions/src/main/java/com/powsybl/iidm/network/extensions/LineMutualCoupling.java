/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Line;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface LineMutualCoupling extends Extension<Line> {

    String NAME = "lineMutualCoupling";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * The mutual coupling resistance of the line (in Ohm).
     */
    double getR();

    void setR(double r);

    /**
     * The mutual coupling reactance of the line (in Ohm).
     */
    double getX();

    void setX(double x);
}

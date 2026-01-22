/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineMutualCoupling;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class LineMutualCouplingImpl extends AbstractExtension<Line> implements LineMutualCoupling {

    private double r;
    private double x;

    public LineMutualCouplingImpl(Line line, double r, double x) {
        super(line);
        this.r = r;
        this.x = x;
    }

    @Override
    public double getR() {
        return r;
    }

    @Override
    public void setR(double r) {
        this.r = r;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

}

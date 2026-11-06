/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
public class LineNonReciprocalImpedanceImpl extends AbstractExtension<Line> implements LineNonReciprocalImpedance {

    private double r21;
    private double x21;

    public LineNonReciprocalImpedanceImpl(Line line, double r21, double x21) {
        super(line);
        this.r21 = r21;
        this.x21 = x21;
    }

    @Override
    public double getR21() {
        return r21;
    }

    @Override
    public void setR21(double r21) {
        this.r21 = r21;
    }

    @Override
    public double getX21() {
        return x21;
    }

    @Override
    public void setX21(double x21) {
        this.x21 = x21;
    }
}

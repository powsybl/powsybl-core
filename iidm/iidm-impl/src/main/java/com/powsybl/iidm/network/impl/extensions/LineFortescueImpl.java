/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 */
public class LineFortescueImpl extends AbstractExtension<Line> implements LineFortescue {

    private double r0;
    private double x0;

    public LineFortescueImpl(Line line, double r0, double x0) {
        super(line);
        this.r0 = r0;
        this.x0 = x0;
    }

    @Override
    public double getR0() {
        return r0;
    }

    @Override
    public void setR0(double r0) {
        this.r0 = r0;
    }

    @Override
    public double getX0() {
        return x0;
    }

    @Override
    public void setX0(double x0) {
        this.x0 = x0;
    }
}

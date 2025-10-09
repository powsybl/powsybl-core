/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LineFortescue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 */
public class LineFortescueImpl extends AbstractExtension<Line> implements LineFortescue {

    private double rz;
    private double xz;

    private boolean openPhaseA;
    private boolean openPhaseB;
    private boolean openPhaseC;

    public LineFortescueImpl(Line line, double rz, double xz, boolean openPhaseA, boolean openPhaseB, boolean openPhaseC) {
        super(line);
        this.rz = rz;
        this.xz = xz;
        this.openPhaseA = openPhaseA;
        this.openPhaseB = openPhaseB;
        this.openPhaseC = openPhaseC;
    }

    @Override
    public double getRz() {
        return rz;
    }

    @Override
    public void setRz(double rz) {
        this.rz = rz;
    }

    @Override
    public double getXz() {
        return xz;
    }

    @Override
    public void setXz(double xz) {
        this.xz = xz;
    }

    @Override
    public boolean isOpenPhaseA() {
        return openPhaseA;
    }

    @Override
    public void setOpenPhaseA(boolean openPhaseA) {
        this.openPhaseA = openPhaseA;
    }

    @Override
    public boolean isOpenPhaseB() {
        return openPhaseB;
    }

    @Override
    public void setOpenPhaseB(boolean openPhaseB) {
        this.openPhaseB = openPhaseB;
    }

    @Override
    public boolean isOpenPhaseC() {
        return openPhaseC;
    }

    @Override
    public void setOpenPhaseC(boolean openPhaseC) {
        this.openPhaseC = openPhaseC;
    }
}

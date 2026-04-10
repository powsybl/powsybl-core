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

    private double b1z;
    private double b2z;
    private double g1z;
    private double g2z;

    private boolean openPhaseA;
    private boolean openPhaseB;
    private boolean openPhaseC;

    public LineFortescueImpl(Line line, double rz, double xz, double b1z, double b2z, double g1z, double g2z, boolean openPhaseA, boolean openPhaseB, boolean openPhaseC) {
        super(line);
        this.rz = rz;
        this.xz = xz;
        this.b1z = b1z;
        this.b2z = b2z;
        this.g1z = g1z;
        this.g2z = g2z;
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

    @Override
    public double getB1z() {
        return b1z;
    }

    @Override
    public void setB1z(double b1z) {
        this.b1z = b1z;
    }

    @Override
    public double getB2z() {
        return b2z;
    }

    @Override
    public void setB2z(double b2z) {
        this.b2z = b2z;
    }

    @Override
    public double getG1z() {
        return g1z;
    }

    @Override
    public void setG1z(double g1z) {
        this.g1z = g1z;
    }

    @Override
    public double getG2z() {
        return g2z;
    }

    @Override
    public void setG2z(double g2z) {
        this.g2z = g2z;
    }
}

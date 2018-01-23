/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
/**
 * p = p0 * (a1 * (v / v0)^2 + a2 * v / v0 + a3)
 * q = q0 * (a4 * (v / v0)^2 + a5 * v / v0 + a6)
 */
public class LoadZipModel extends AbstractExtension<Load> {

    float v0;
    float a1;
    float a2;
    float a3;
    float a4;
    float a5;
    float a6;

    public LoadZipModel(Load load, float a1, float a2, float a3, float a4, float a5, float a6, float v0) {
        super(load);
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
        this.a4 = a4;
        this.a5 = a5;
        this.a6 = a6;
        this.v0 = v0;
    }

    public float getV0() {
        return v0;
    }

    public void setV0(float v0) {
        this.v0 = v0;
    }

    public float getA1() {
        return a1;
    }

    public void setA1(float a1) {
        this.a1 = a1;
    }

    public float getA2() {
        return a2;
    }

    public void setA2(float a2) {
        this.a2 = a2;
    }

    public float getA3() {
        return a3;
    }

    public void setA3(float a3) {
        this.a3 = a3;
    }

    public float getA4() {
        return a4;
    }

    public void setA4(float a4) {
        this.a4 = a4;
    }

    public float getA5() {
        return a5;
    }

    public void setA5(float a5) {
        this.a5 = a5;
    }

    public float getA6() {
        return a6;
    }

    public void setA6(float a6) {
        this.a6 = a6;
    }

    @Override
    public String getName() {
        return "loadZipModel";
    }

}


/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorFortescueImpl extends AbstractExtension<Generator> implements GeneratorFortescue {

    private boolean grounded;
    private double r0;
    private double x0;
    private double r2;
    private double x2;
    private double groundingR;
    private double groundingX;

    public GeneratorFortescueImpl(Generator generator, boolean grounded, double r0, double x0, double r2, double x2, double groundingR, double groundingX) {
        super(generator);
        this.grounded = grounded;
        this.r0 = r0;
        this.x0 = x0;
        this.r2 = r2;
        this.x2 = x2;
        this.groundingR = groundingR;
        this.groundingX = groundingX;
    }

    @Override
    public boolean isGrounded() {
        return grounded;
    }

    @Override
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    @Override
    public double getGroundingR() {
        return groundingR;
    }

    @Override
    public void setGroundingR(double groundingR) {
        this.groundingR = groundingR;
    }

    @Override
    public double getGroundingX() {
        return groundingX;
    }

    @Override
    public void setGroundingX(double groundingX) {
        this.groundingX = groundingX;
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

    @Override
    public double getR2() {
        return r2;
    }

    @Override
    public void setR2(double r2) {
        this.r2 = r2;
    }

    @Override
    public double getX2() {
        return x2;
    }

    @Override
    public void setX2(double x2) {
        this.x2 = x2;
    }
}

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
    private double ro;
    private double xo;
    private double ri;
    private double xi;
    private double groundingR;
    private double groundingX;

    public GeneratorFortescueImpl(Generator generator, boolean grounded, double ro, double xo, double ri, double xi, double groundingR, double groundingX) {
        super(generator);
        this.grounded = grounded;
        this.ro = ro;
        this.xo = xo;
        this.ri = ri;
        this.xi = xi;
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
    public double getRo() {
        return ro;
    }

    @Override
    public void setRo(double ro) {
        this.ro = ro;
    }

    @Override
    public double getXo() {
        return xo;
    }

    @Override
    public void setXo(double xo) {
        this.xo = xo;
    }

    @Override
    public double getRi() {
        return ri;
    }

    @Override
    public void setRi(double ri) {
        this.ri = ri;
    }

    @Override
    public double getXi() {
        return xi;
    }

    @Override
    public void setXi(double xi) {
        this.xi = xi;
    }
}

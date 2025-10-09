/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class GeneratorFortescueImpl extends AbstractExtension<Generator> implements GeneratorFortescue {

    private boolean grounded;
    private double rz;
    private double xz;
    private double rn;
    private double xn;
    private double groundingR;
    private double groundingX;

    public GeneratorFortescueImpl(Generator generator, boolean grounded, double rz, double xz, double rn, double xn, double groundingR, double groundingX) {
        super(generator);
        this.grounded = grounded;
        this.rz = rz;
        this.xz = xz;
        this.rn = rn;
        this.xn = xn;
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
    public double getRn() {
        return rn;
    }

    @Override
    public void setRn(double rn) {
        this.rn = rn;
    }

    @Override
    public double getXn() {
        return xn;
    }

    @Override
    public void setXn(double xn) {
        this.xn = xn;
    }
}

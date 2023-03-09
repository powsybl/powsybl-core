/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;

import static com.powsybl.iidm.network.extensions.FortescueConstants.*;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GeneratorFortescueAdderImpl extends AbstractExtensionAdder<Generator, GeneratorFortescue> implements GeneratorFortescueAdder {

    private boolean grounded = DEFAULT_GROUNDED;
    private double r0 = Double.NaN;
    private double x0 = Double.NaN;
    private double r2 = Double.NaN;
    private double x2 = Double.NaN;
    private double groundingR = DEFAULT_GROUNDING_R;
    private double groundingX = DEFAULT_GROUNDING_X;

    public GeneratorFortescueAdderImpl(Generator generator) {
        super(generator);
    }

    @Override
    public Class<? super GeneratorFortescue> getExtensionClass() {
        return GeneratorFortescue.class;
    }

    @Override
    protected GeneratorFortescue createExtension(Generator generator) {
        return new GeneratorFortescueImpl(generator, grounded, r0, x0, r2, x2, groundingR, groundingX);
    }

    @Override
    public GeneratorFortescueAdderImpl withGrounded(boolean grounded) {
        this.grounded = grounded;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withR0(double ro) {
        this.r0 = ro;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withX0(double xo) {
        this.x0 = xo;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withR2(double r2) {
        this.r2 = r2;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withX2(double x2) {
        this.x2 = x2;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withGroundingR(double groundingR) {
        this.groundingR = groundingR;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withGroundingX(double groundingX) {
        this.groundingX = groundingX;
        return this;
    }
}

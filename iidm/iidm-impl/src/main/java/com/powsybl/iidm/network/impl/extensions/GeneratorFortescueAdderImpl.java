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

    private boolean grounded = DEFAULT_TO_GROUND;
    private double ro = Double.NaN;
    private double xo = Double.NaN;
    private double ri = Double.NaN;
    private double xi = Double.NaN;
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
        return new GeneratorFortescueImpl(generator, grounded, ro, xo, ri, xi, groundingR, groundingX);
    }

    @Override
    public GeneratorFortescueAdderImpl withGrounded(boolean grounded) {
        this.grounded = grounded;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withRo(double ro) {
        this.ro = ro;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withXo(double xo) {
        this.xo = xo;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withRi(double ri) {
        this.ri = ri;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withXi(double xi) {
        this.xi = xi;
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

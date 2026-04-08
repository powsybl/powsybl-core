/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorFortescue;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;

import static com.powsybl.iidm.network.extensions.FortescueConstants.*;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class GeneratorFortescueAdderImpl extends AbstractExtensionAdder<Generator, GeneratorFortescue> implements GeneratorFortescueAdder {

    private boolean grounded = DEFAULT_GROUNDED;
    private double rz = Double.NaN;
    private double xz = Double.NaN;
    private double rn = Double.NaN;
    private double xn = Double.NaN;
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
        return new GeneratorFortescueImpl(generator, grounded, rz, xz, rn, xn, groundingR, groundingX);
    }

    @Override
    public GeneratorFortescueAdderImpl withGrounded(boolean grounded) {
        this.grounded = grounded;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withRz(double rz) {
        this.rz = rz;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withXz(double xz) {
        this.xz = xz;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withRn(double rn) {
        this.rn = rn;
        return this;
    }

    @Override
    public GeneratorFortescueAdderImpl withXn(double xn) {
        this.xn = xn;
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

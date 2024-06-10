/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class GeneratorShortCircuitAdderImpl extends AbstractExtensionAdder<Generator, GeneratorShortCircuit>
        implements GeneratorShortCircuitAdder {

    double directTransX = 0;
    double directSubtransX = Double.NaN;
    double stepUpTransformerX = Double.NaN;

    protected GeneratorShortCircuitAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected GeneratorShortCircuit createExtension(Generator extendable) {
        return new GeneratorShortCircuitImpl(extendable, directSubtransX, directTransX, stepUpTransformerX);
    }

    @Override
    public GeneratorShortCircuitAdder withDirectTransX(double directTransX) {
        this.directTransX = directTransX;
        return this;
    }

    @Override
    public GeneratorShortCircuitAdder withDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return this;
    }

    @Override
    public GeneratorShortCircuitAdder withStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return this;
    }

    @Override
    public GeneratorShortCircuit add() {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        return super.add();
    }
}

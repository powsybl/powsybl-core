/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class GeneratorShortCircuitAdderImpl extends AbstractShortCircuitExtensionAdderImpl<Generator, GeneratorShortCircuit, GeneratorShortCircuitAdder>
        implements GeneratorShortCircuitAdder {

    protected GeneratorShortCircuitAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected GeneratorShortCircuitAdder self() {
        return this;
    }

    @Override
    protected GeneratorShortCircuit createExtension(Generator extendable) {
        return new GeneratorShortCircuitImpl(extendable, directSubtransX, directTransX, stepUpTransformerX);
    }
}

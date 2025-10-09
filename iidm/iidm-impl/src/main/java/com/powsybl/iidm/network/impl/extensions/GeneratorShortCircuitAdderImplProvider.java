/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class GeneratorShortCircuitAdderImplProvider
        implements ExtensionAdderProvider<Generator, GeneratorShortCircuit, GeneratorShortCircuitAdder> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return GeneratorShortCircuit.NAME;
    }

    @Override
    public Class<GeneratorShortCircuitAdder> getAdderClass() {
        return GeneratorShortCircuitAdder.class;
    }

    @Override
    public GeneratorShortCircuitAdder newAdder(Generator extendable) {
        return new GeneratorShortCircuitAdderImpl(extendable);
    }
}

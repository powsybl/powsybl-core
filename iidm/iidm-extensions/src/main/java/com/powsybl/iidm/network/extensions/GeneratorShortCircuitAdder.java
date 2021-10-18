/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Generator;

/**
 *
 * @author Coline Piloquet <coline.piloquet@rte-france.fr>
 */
public interface GeneratorShortCircuitAdder extends ExtensionAdder<Generator, GeneratorShortCircuit> {

    @Override
    default Class<GeneratorShortCircuit> getExtensionClass() {
        return GeneratorShortCircuit.class;
    }

    GeneratorShortCircuitAdder withDirectTransX(double directTransX);

    GeneratorShortCircuitAdder withDirectSubtransX(double direcSubtransX);

    GeneratorShortCircuitAdder withStepUpTransformerX(double stepUpTransformerX);
}

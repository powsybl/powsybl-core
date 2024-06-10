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

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class GeneratorShortCircuitImpl extends AbstractShortCircuitImpl<Generator> implements GeneratorShortCircuit {

    public GeneratorShortCircuitImpl(Generator generator, double directSubtransX, double directTransX,
                                     double stepUpTransformerX) {
        super(generator, directSubtransX, directTransX, stepUpTransformerX);
    }
}

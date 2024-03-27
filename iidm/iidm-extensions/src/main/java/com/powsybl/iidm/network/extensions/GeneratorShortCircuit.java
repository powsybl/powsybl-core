/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface GeneratorShortCircuit extends Extension<Generator> {

    String NAME = "generatorShortCircuit";

    @Override
    default String getName() {
        return NAME;
    }

    /**
     * Get the direct-axis sub-transient reactance (also known as X''d)
     */
    double getDirectSubtransX();

    /**
     * Set the direct-axis sub-transient reactance (also known as X''d)
     */
    GeneratorShortCircuit setDirectSubtransX(double directSubtransX);

    /**
     * Get the direct-axis transient reactance (also known as X'd)
     */
    double getDirectTransX();

    /**
     * Set the direct-axis transient reactance (also known as X'd)
     */
    GeneratorShortCircuit setDirectTransX(double directTransX);

    /**
     * Get the step-up transformer reactance if the generator has a non-modeled step-up transformer.
     */
    double getStepUpTransformerX();

    /**
     * Set the step-up transformer reactance
     */
    GeneratorShortCircuit setStepUpTransformerX(double setUpTransformerX);

}

/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public interface AbstractShortCircuit<T extends Extendable<T>> extends Extension<T> {

    /**
     * Get the direct-axis sub-transient reactance (also known as X''d)
     */
    double getDirectSubtransX();

    /**
     * Set the direct-axis sub-transient reactance (also known as X''d)
     */
    AbstractShortCircuit<T> setDirectSubtransX(double directSubtransX);

    /**
     * Get the direct-axis transient reactance (also known as X'd)
     */
    double getDirectTransX();

    /**
     * Set the direct-axis transient reactance (also known as X'd)
     */
    AbstractShortCircuit<T> setDirectTransX(double directTransX);

    /**
     * Get the step-up transformer reactance if the generator or battery has a non-modeled step-up transformer.
     */
    double getStepUpTransformerX();

    /**
     * Set the step-up transformer reactance
     */
    AbstractShortCircuit<T> setStepUpTransformerX(double setUpTransformerX);

}

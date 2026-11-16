/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.extensions.ShortCircuitExtension;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public abstract class AbstractShortCircuitExtensionImpl<T extends Extendable<T>, S extends AbstractShortCircuitExtensionImpl<T, ?>>
        extends AbstractExtension<T>
        implements ShortCircuitExtension<T> {

    private double directSubtransX; // X''d
    private double directTransX; // X'd
    private double stepUpTransformerX; // Reactance of the step-up transformer

    protected AbstractShortCircuitExtensionImpl(T extendable, double directSubtransX, double directTransX,
                                                double stepUpTransformerX) {
        super(extendable);
        this.directSubtransX = directSubtransX;
        this.directTransX = directTransX;
        this.stepUpTransformerX = stepUpTransformerX;
    }

    protected abstract S self();

    @Override
    public double getDirectSubtransX() {
        return directSubtransX;
    }

    @Override
    public S setDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return self();
    }

    @Override
    public double getDirectTransX() {
        return directTransX;
    }

    @Override
    public S setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        this.directTransX = directTransX;
        return self();
    }

    @Override
    public double getStepUpTransformerX() {
        return stepUpTransformerX;
    }

    @Override
    public S setStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return self();
    }
}

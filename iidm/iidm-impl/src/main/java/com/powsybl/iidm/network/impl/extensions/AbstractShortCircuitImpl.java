/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.extensions.AbstractShortCircuit;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public abstract class AbstractShortCircuitImpl<T extends Extendable<T>> extends AbstractExtension<T>
    implements AbstractShortCircuit<T> {

    private double directSubtransX; // X''d
    private double directTransX; // X'd
    private double stepUpTransformerX; // Reactance of the step-up transformer

    protected AbstractShortCircuitImpl(T extendable, double directSubtransX, double directTransX,
                                    double stepUpTransformerX) {
        super(extendable);
        this.directSubtransX = directSubtransX;
        this.directTransX = directTransX;
        this.stepUpTransformerX = stepUpTransformerX;
    }

    @Override
    public double getDirectSubtransX() {
        return directSubtransX;
    }

    @Override
    public AbstractShortCircuitImpl<T> setDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return this;
    }

    @Override
    public double getDirectTransX() {
        return directTransX;
    }

    @Override
    public AbstractShortCircuitImpl<T> setDirectTransX(double directTransX) {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        this.directTransX = directTransX;
        return this;
    }

    @Override
    public double getStepUpTransformerX() {
        return stepUpTransformerX;
    }

    @Override
    public AbstractShortCircuitImpl<T> setStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return this;
    }
}

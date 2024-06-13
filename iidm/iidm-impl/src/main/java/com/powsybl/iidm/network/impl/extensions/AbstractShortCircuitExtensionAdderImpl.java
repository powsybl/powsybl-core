/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.iidm.network.extensions.ShortCircuitExtension;
import com.powsybl.iidm.network.extensions.ShortCircuitExtensionAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public abstract class AbstractShortCircuitExtensionAdderImpl<T extends Extendable<T>,
            C extends ShortCircuitExtension<T>,
            A extends ShortCircuitExtensionAdder<T, C, ?>>
        extends AbstractExtensionAdder<T, C>
        implements ShortCircuitExtensionAdder<T, C, A> {

    double directTransX = 0;
    double directSubtransX = Double.NaN;
    double stepUpTransformerX = Double.NaN;

    protected AbstractShortCircuitExtensionAdderImpl(T extendable) {
        super(extendable);
    }

    protected abstract A self();

    @Override
    protected C createExtension(T extendable) {
        throw new PowsyblException("Not implemented");
    }

    @Override
    public A withDirectTransX(double directTransX) {
        this.directTransX = directTransX;
        return self();
    }

    @Override
    public A withDirectSubtransX(double directSubtransX) {
        this.directSubtransX = directSubtransX;
        return self();
    }

    @Override
    public A withStepUpTransformerX(double stepUpTransformerX) {
        this.stepUpTransformerX = stepUpTransformerX;
        return self();
    }

    @Override
    public C add() {
        if (Double.isNaN(directTransX)) {
            throw new PowsyblException("Undefined directTransX");
        }
        return super.add();
    }
}

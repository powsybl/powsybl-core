
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
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;

/**
 *
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
public class IdentifiableShortCircuitAdderImpl<I extends Identifiable<I>> extends AbstractExtensionAdder<I, IdentifiableShortCircuit<I>>
        implements IdentifiableShortCircuitAdder<I> {

    double ipMin = Double.NaN;
    double ipMax = Double.NaN;

    protected IdentifiableShortCircuitAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected IdentifiableShortCircuitImpl<I> createExtension(I extendable) {
        return new IdentifiableShortCircuitImpl<>(extendable, ipMin, ipMax);
    }

    @Override
    public IdentifiableShortCircuitAdder<I> withIpMin(double ipMin) {
        this.ipMin = ipMin;
        return this;
    }

    @Override
    public IdentifiableShortCircuitAdder<I> withIpMax(double ipMax) {
        this.ipMax = ipMax;
        return this;
    }

    @Override
    public IdentifiableShortCircuit<I> add() {
        if (Double.isNaN(ipMax)) {
            throw new PowsyblException("Undefined ipMax");
        }
        return super.add();
    }
}
